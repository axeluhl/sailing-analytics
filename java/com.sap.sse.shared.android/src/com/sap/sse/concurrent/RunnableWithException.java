package com.sap.sse.concurrent;

@FunctionalInterface
public interface RunnableWithException<E extends Throwable> {
    void run() throws E;
}