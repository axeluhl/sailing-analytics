package com.sap.sse.common.util;

/**
 * An Interface for an Observable Object in push-notification style
 * 
 * @author Robin Fleige (D067799) 
 *
 */
public interface Observable {
    public void registerObserver(Observer observer);

    public void unregisterObserver(Observer observer);

    public void notifyObserver();
}
