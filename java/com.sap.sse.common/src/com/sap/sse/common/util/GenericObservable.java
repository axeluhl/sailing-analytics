package com.sap.sse.common.util;

/**
 * 
 * @author Robin Fleige (D067799)
 *
 * @param <T> the class of the observable object
 * 
 *            An Interface for an Observable Object in push-update-notification style
 */
public interface GenericObservable<T> {
    public void registerObserver(GenericObserver<T> observer);

    public void unregisterObserver(GenericObserver<T> observer);

    public void notifyObserver(T data);
}
