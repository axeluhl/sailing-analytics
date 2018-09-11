package com.sap.sse.common.util;

/**
 * 
 * @author Robin Fleige (D067799)
 *
 * @param <T>
 *            the type of the observable object An Interface for an Observer in push-update-notification style
 */
public interface GenericObserver<T> {
    void getNotified(T data);
}
