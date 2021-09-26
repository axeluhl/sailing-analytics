package com.sap.sse.concurrent;

/**
 * Can be replaced with java.util.function.Supplier when we can consistently use Java 8.
 */
@FunctionalInterface
public interface RunnableWithResult<T> {
    T run();
}