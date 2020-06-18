package com.axibase;


import lombok.SneakyThrows;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Goal which creates a JSON with definitions for MVEL library
 */
@Mojo( name = "generate", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class MvelDefinitionGeneratorMojo extends AbstractMojo {
    /**
     * Location of the file.
     */
    @Parameter( property = "outputFile", required = true )
    private File outputFile;

    /**
     * Fully qualified name of the generator class. Generator class must contain method `static void generate(Path path, Class inspectedClass)`
     */
    @Parameter(property = "generatorClass", required = true )
    private String generatorClass;

    /**
     * Fully qualified name of the MVEL context class
     */
    @Parameter(property = "inspectedClass", required = true )
    private String inspectedClass;

    /**
     * Injected project
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @SneakyThrows
    private URL uriToURL(String uri) {
        if (!uri.endsWith(".jar") && !uri.endsWith("/")) {
            uri = uri + "/";
        }
        return Paths.get(uri).toUri().toURL();
    }

    public void execute() throws MojoExecutionException {
        if (generatorClass == null || generatorClass.trim().length() == 0) {
            throw new MojoExecutionException("generatorClass must be specified");
        }
        if (inspectedClass == null || inspectedClass.trim().length() == 0) {
            throw new MojoExecutionException("inspectedClass must be specified");
        }
        final URL[] classpath;
        try {
            classpath = project.getRuntimeClasspathElements().stream()
                    .peek(cp -> getLog().info(cp))
                    .map(this::uriToURL)
                    .toArray(URL[]::new);
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        final URLClassLoader urlClassLoader = new URLClassLoader(classpath);
        Class<?> contextClass = loadClass(inspectedClass, urlClassLoader);
        Class<?> generator = loadClass(generatorClass, urlClassLoader);
        Method generateMethod = resolveGenerateMethod(generator);
        try {
            generateMethod.invoke(null, outputFile.toPath(), contextClass);
        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("Exception while generating JSON to file '" + outputFile + "'", e);
        }
        getLog().info("Written MVEL functions definitions for class " + inspectedClass + " to " + outputFile);
    }

    private Method resolveGenerateMethod(Class<?> generator) throws MojoExecutionException {
        try {
            return generator.getMethod("generate", Path.class, Class.class);
        } catch (NoSuchMethodException e) {
            throw new MojoExecutionException("generatorClass must contain static method generate(Path, Class");
        }
    }

    private Class<?> loadClass(String className, ClassLoader classLoader) throws MojoExecutionException {
        try {
            return classLoader.loadClass(className);
        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("Could not load class '" + className + "'", e);
        }
    }
}
