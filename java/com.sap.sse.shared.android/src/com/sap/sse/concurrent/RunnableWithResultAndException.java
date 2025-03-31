package com.sap.sse.concurrent;

@FunctionalInterface
public interface RunnableWithResultAndException<T, E extends Throwable> {
    T run() throws E;
}