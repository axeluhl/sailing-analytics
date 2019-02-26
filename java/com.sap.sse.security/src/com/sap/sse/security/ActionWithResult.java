package com.sap.sse.security;

@FunctionalInterface
public interface ActionWithResult<T> {
    T run() throws Exception;
}
