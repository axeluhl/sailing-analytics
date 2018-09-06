package com.sap.sse.common.observer;

public interface GenericObservable<T> {
    public void registerObserver(GenericObserver<T> observer);
    public void unregisterObserver(GenericObserver<T> observer);
    public void notifyObserver(T data);
}
