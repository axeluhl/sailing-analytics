package com.sap.sse.gwt.client.xdstorage.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.sap.sse.gwt.client.messaging.MessageEvent;
import com.sap.sse.gwt.client.messaging.MessagePort;
import com.sap.sse.gwt.client.xdstorage.CrossDomainStorage;

public class CrossDomainStorageImpl implements CrossDomainStorage {
    private final MessagePort portToStorageMessagingEntryPoint;
    private final Map<UUID, Consumer<Object>> resultForwardersByRequestUuidAsString;
    private final String targetOrigin;
    
    public CrossDomainStorageImpl(MessagePort portToStorageMessagingEntryPoint, String targetOrigin) {
        resultForwardersByRequestUuidAsString = new HashMap<>();
        this.targetOrigin = targetOrigin;
        this.portToStorageMessagingEntryPoint = portToStorageMessagingEntryPoint;
        portToStorageMessagingEntryPoint.addResponseListener((MessageEvent<JavaScriptObject> messageEvent)->dispatchMessageToCallback(messageEvent));
    }
    
    private void postMessageAndRegisterCallback(UUID id, JSONObject request, Consumer<Object> resultConsumer) {
        resultForwardersByRequestUuidAsString.put(id, resultConsumer);
        portToStorageMessagingEntryPoint.postMessage(request.getJavaScriptObject(), getTargetOrigin());
    }

    void dispatchMessageToCallback(MessageEvent<JavaScriptObject> messageEvent) {
        final Response response = messageEvent.getData().cast();
        final UUID idOfRequestToWhichThisIsTheResponse = LocalStorageDrivenByMessageEvents.getId(response);
        final Consumer<Object> resultForwarder = resultForwardersByRequestUuidAsString.remove(idOfRequestToWhichThisIsTheResponse);
        if (resultForwarder != null) {
            resultForwarder.accept(response.getResult());
        }
    }

    private String getTargetOrigin() {
        return targetOrigin;
    }

    @Override
    public void setItem(String key, String value, Consumer<Void> callback) {
        final UUID id = UUID.randomUUID();
        postMessageAndRegisterCallback(id, LocalStorageDrivenByMessageEvents.createSetItemRequest(id, key, value), result->callback.accept(null));
    }

    @Override
    public void getItem(String key, Consumer<String> callback) {
        final UUID id = UUID.randomUUID();
        postMessageAndRegisterCallback(id, LocalStorageDrivenByMessageEvents.createGetItemRequest(id, key), result->callback.accept((String) result));
    }

    @Override
    public void removeItem(String key, Consumer<Void> callback) {
        final UUID id = UUID.randomUUID();
        postMessageAndRegisterCallback(id, LocalStorageDrivenByMessageEvents.createRemoveItemRequest(id, key), result->callback.accept(null));
    }

    @Override
    public void clear(Consumer<Void> callback) {
        final UUID id = UUID.randomUUID();
        postMessageAndRegisterCallback(id, LocalStorageDrivenByMessageEvents.createClearRequest(id), result->callback.accept(null));
    }

    @Override
    public void key(int index, Consumer<String> callback) {
        final UUID id = UUID.randomUUID();
        postMessageAndRegisterCallback(id, LocalStorageDrivenByMessageEvents.createKeyRequest(id, index), result->callback.accept((String) result));
    }

    @Override
    public void getLength(Consumer<Integer> callback) {
        final UUID id = UUID.randomUUID();
        postMessageAndRegisterCallback(id, LocalStorageDrivenByMessageEvents.createGetLengthRequest(id), result->callback.accept(((Number) result).intValue()));
    }
}
