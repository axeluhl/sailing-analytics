package com.sap.sse.gwt.client.xdstorage.impl;

import com.sap.sse.gwt.client.xdstorage.CrossDomainStorageEvent;

public class CrossDomainStorageEventImpl implements CrossDomainStorageEvent {
    private final String key;
    private final String newValue;
    private final String oldValue;
    private final String url;

    public CrossDomainStorageEventImpl(String key, String newValue, String oldValue, String url) {
        super();
        this.key = key;
        this.newValue = newValue;
        this.oldValue = oldValue;
        this.url = url;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getNewValue() {
        return newValue;
    }

    @Override
    public String getOldValue() {
        return oldValue;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "CrossDomainStorageEventImpl [key=" + key + ", newValue=" + newValue + ", oldValue=" + oldValue
                + ", url=" + url + "]";
    }
}
