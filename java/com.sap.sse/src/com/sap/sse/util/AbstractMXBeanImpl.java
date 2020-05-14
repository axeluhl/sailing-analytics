package com.sap.sse.util;

public abstract class AbstractMXBeanImpl {
    protected String escapeIllegalObjectNameCharacters(String name) {
        return name.replaceAll("[:/,]", "_");
    }
}
