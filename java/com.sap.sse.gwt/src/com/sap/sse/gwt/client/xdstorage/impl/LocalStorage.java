package com.sap.sse.gwt.client.xdstorage.impl;

import java.util.function.Consumer;

import com.google.gwt.event.shared.HandlerRegistration;
import com.sap.sse.gwt.client.Storage;
import com.sap.sse.gwt.client.xdstorage.CrossDomainStorage;
import com.sap.sse.gwt.client.xdstorage.CrossDomainStorageEvent.Handler;

/**
 * A purely local implementation of the {@link CrossDomainStorage} interface, using the {@link Storage} methods directly
 * and feeding back immediately into the callback consumers.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class LocalStorage implements CrossDomainStorage {
    private final Storage storage;
    
    public LocalStorage() {
        storage = Storage.getLocalStorageIfSupported();
    }
    
    @Override
    public HandlerRegistration addStorageEventHandler(Handler handler) {
        return Storage.addStorageEventHandler(event->
                handler.onStorageChange(new CrossDomainStorageEventImpl(event.getKey(), event.getNewValue(), event.getOldValue(), event.getUrl())));
    }

    @Override
    public void setItem(String key, String value, Consumer<Void> callback) {
        storage.setItem(key, value);
        callback.accept(null);
    }

    @Override
    public void getItem(String key, Consumer<String> callback) {
        callback.accept(storage.getItem(key));
    }

    @Override
    public void removeItem(String key, Consumer<Void> callback) {
        storage.removeItem(key);
        callback.accept(null);
    }

    @Override
    public void clear(Consumer<Void> callback) {
        storage.clear();
        callback.accept(null);
    }

    @Override
    public void key(int index, Consumer<String> callback) {
        callback.accept(storage.key(index));
    }

    @Override
    public void getLength(Consumer<Integer> callback) {
        callback.accept(storage.getLength());
    }
}
