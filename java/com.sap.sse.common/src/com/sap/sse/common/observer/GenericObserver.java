package com.sap.sse.common.observer;

public interface GenericObserver<T> {
    void getNotified(T data);
}
