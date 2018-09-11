package com.sap.sse.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Robin Fleige(D067799)
 *
 *         A class for the implementation of an observable boolean
 */
public class ObservableBoolean implements GenericObservable<Boolean> {
    private Boolean value;
    private final List<GenericObserver<Boolean>> observer;

    public ObservableBoolean(Boolean value) {
        this.value = value;
        observer = new ArrayList<GenericObserver<Boolean>>();
    }

    public void setValue(Boolean value) {
        this.value = value;
        notifyObserver(value);
    }

    public Boolean getValue() {
        return value;
    }

    @Override
    public void registerObserver(GenericObserver<Boolean> observer) {
        this.observer.add(observer);
    }

    @Override
    public void unregisterObserver(GenericObserver<Boolean> observer) {
        this.observer.remove(observer);
    }

    @Override
    public void notifyObserver(Boolean data) {
        for (GenericObserver<Boolean> observer : this.observer) {
            observer.getNotified(data);
        }
    }
}
