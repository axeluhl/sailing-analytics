package com.sap.sse.common.observer;

import java.util.ArrayList;
import java.util.List;

public class ObservableBoolean implements GenericObservable<Boolean> {
    private Boolean value;
    private List<GenericObserver<Boolean>> observer = new ArrayList<GenericObserver<Boolean>>();
    public ObservableBoolean(Boolean value) {
        this.value = value;
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
        for(GenericObserver<Boolean> observer : this.observer){
            observer.getNotified(data);
        }
    }
}
