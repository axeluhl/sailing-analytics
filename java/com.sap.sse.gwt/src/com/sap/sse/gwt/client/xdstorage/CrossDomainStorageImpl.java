package com.sap.sse.gwt.client.xdstorage;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.client.messaging.MessagePort;

public class CrossDomainStorageImpl implements CrossDomainStorage {
    private final MessagePort portToStorageMessagingEntryPoint;

    public CrossDomainStorageImpl(MessagePort portToStorageMessagingEntryPoint) {
        super();
        this.portToStorageMessagingEntryPoint = portToStorageMessagingEntryPoint;
    }

    private String getTargetOrigin() {
        // TODO Implement CrossDomainStorageImpl.getTargetOrigin(...)
        return "*";
    }

    // TODO in the following methods record the request ID and register for a response for exactly that ID
    
    @Override
    public void setItem(String key, String value, AsyncCallback<Void> callback) {
        portToStorageMessagingEntryPoint.postMessage(LocalStorageDrivenByMessageEvents.createSetItemRequest(key, value), getTargetOrigin());
    }

    @Override
    public void getItem(String key, AsyncCallback<String> callback) {
        portToStorageMessagingEntryPoint.postMessage(LocalStorageDrivenByMessageEvents.createGetItemRequest(key), getTargetOrigin());
    }

    @Override
    public void removeItem(String key, AsyncCallback<Void> callback) {
        portToStorageMessagingEntryPoint.postMessage(LocalStorageDrivenByMessageEvents.createRemoveItemRequest(key), getTargetOrigin());
    }

    @Override
    public void clear(AsyncCallback<Void> callback) {
        portToStorageMessagingEntryPoint.postMessage(LocalStorageDrivenByMessageEvents.createClearRequest(), getTargetOrigin());
    }

    @Override
    public void key(int index, AsyncCallback<String> callback) {
        portToStorageMessagingEntryPoint.postMessage(LocalStorageDrivenByMessageEvents.createKeyRequest(index), getTargetOrigin());
    }

    @Override
    public void getLength(AsyncCallback<Integer> callback) {
        portToStorageMessagingEntryPoint.postMessage(LocalStorageDrivenByMessageEvents.createGetLengthRequest(), getTargetOrigin());
    }

    @Override
    public void getAllKeys(AsyncCallback<String[]> callback) {
        portToStorageMessagingEntryPoint.postMessage(LocalStorageDrivenByMessageEvents.createGetAllKeysRequest(), getTargetOrigin());
    }
}
