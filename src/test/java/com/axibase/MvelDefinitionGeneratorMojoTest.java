package com.axibase;


import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class MvelDefinitionGeneratorMojoTest
{
    @Rule
    public MojoRule rule = new MojoRule();

    /**
     * @throws Exception if any
     */
    @Test
    public void testExecution() throws Exception {
        File pom = new File( "target/test-classes/project-to-test/" );
        assertNotNull( pom );
        assertTrue( pom.exists() );

        MvelDefinitionGeneratorMojo mvelDefinitionGeneratorMojo = (MvelDefinitionGeneratorMojo) rule.lookupConfiguredMojo( pom, "generate" );
        assertNotNull(mvelDefinitionGeneratorMojo);
        mvelDefinitionGeneratorMojo.execute();

        File outputPath = ( File ) rule.getVariableValueFromObject(mvelDefinitionGeneratorMojo, "outputFile" );
        assertNotNull( outputPath );
        assertTrue( outputPath.exists() );
        final byte[] content = Files.readAllBytes(outputPath.toPath());
        final byte[] expected = "[\"calculate\", \"getStringValue\"]".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, content);
    }
}

