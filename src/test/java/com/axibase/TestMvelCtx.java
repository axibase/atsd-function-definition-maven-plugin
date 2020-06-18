package com.axibase;

public class TestMvelCtx {
    public String getStringValue() {
        return "42";
    }

    public Number calculate(Number lhs, Number rhs) {
        return lhs.doubleValue() + rhs.doubleValue();
    }
}