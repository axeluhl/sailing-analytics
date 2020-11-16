package com.sap.sse.gwt.client.xdstorage.impl;

import java.util.function.Consumer;

import com.google.gwt.core.client.GWT;
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
        if (storage != null) {
            storage.setItem(key, value);
        } else {
            GWT.log("Warning: cannot store "+key+"="+value+" in local storage; local storage not accessible");
        }
        if (callback != null) {
            callback.accept(null);
        }
    }

    @Override
    public void getItem(String key, Consumer<String> callback) {
        final String result;
        if (storage != null) {
            result = storage.getItem(key);
        } else {
            GWT.log("Warning: cannot read "+key+" from local storage; local storage not accessible");
            result = null;
        }
        if (callback != null) {
            callback.accept(result);
        }
    }

    @Override
    public void removeItem(String key, Consumer<Void> callback) {
        if (storage != null) {
            storage.removeItem(key);
        } else {
            GWT.log("Warning: cannot remove "+key+" from local storage; local storage not accessible");
        }
        if (callback != null) {
            callback.accept(null);
        }
    }

    @Override
    public void clear(Consumer<Void> callback) {
        if (storage != null) {
            storage.clear();
        } else {
            GWT.log("Warning: cannot clear local storage; local storage not accessible");
        }
        if (callback != null) {
            callback.accept(null);
        }
    }

    @Override
    public void key(int index, Consumer<String> callback) {
        final String result;
        if (storage != null) {
            result = storage.key(index);
        } else {
            GWT.log("Warning: cannot read key at index "+index+" from local storage; local storage not accessible");
            result = null;
        }
        if (callback != null) {
            callback.accept(result);
        }
    }

    @Override
    public void getLength(Consumer<Integer> callback) {
        if (callback != null) {
            final Integer result;
            if (storage != null) {
                result = storage.getLength();
            } else {
                GWT.log("Warning: cannot get local storage length; local storage not accessible");
                result = null;
            }
            callback.accept(result);
        }
    }
}
