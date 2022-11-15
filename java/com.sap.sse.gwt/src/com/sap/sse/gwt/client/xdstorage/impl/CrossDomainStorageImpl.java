package com.sap.sse.gwt.client.xdstorage.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONObject;
import com.sap.sse.gwt.client.messaging.MessageEvent;
import com.sap.sse.gwt.client.messaging.MessagePort;
import com.sap.sse.gwt.client.xdstorage.CrossDomainStorage;
import com.sap.sse.gwt.client.xdstorage.CrossDomainStorageEvent;
import com.sap.sse.gwt.client.xdstorage.CrossDomainStorageEvent.Handler;

public class CrossDomainStorageImpl implements CrossDomainStorage {
    private final MessagePort portToStorageMessagingEntryPoint;
    private final Map<UUID, Consumer<Object>> resultForwardersByRequestUuidAsString;
    private final String targetOrigin;
    private final Set<CrossDomainStorageEvent.Handler> storageEventHandlers;
    private boolean registeredAsStorageEventListener;
    
    public CrossDomainStorageImpl(MessagePort portToStorageMessagingEntryPoint, String targetOrigin) {
        resultForwardersByRequestUuidAsString = new HashMap<>();
        storageEventHandlers = new HashSet<>();
        this.targetOrigin = targetOrigin;
        this.portToStorageMessagingEntryPoint = portToStorageMessagingEntryPoint;
        portToStorageMessagingEntryPoint.addResponseListener((MessageEvent<JavaScriptObject> messageEvent)->dispatchMessageToCallback(messageEvent));
    }
    
    private void postMessageAndRegisterCallback(UUID id, JSONObject request, Consumer<Object> resultConsumer) {
        resultForwardersByRequestUuidAsString.put(id, resultConsumer);
        portToStorageMessagingEntryPoint.postMessage(request.getJavaScriptObject(), getTargetOrigin());
    }

    private void dispatchMessageToCallback(MessageEvent<JavaScriptObject> messageEvent) {
        final Response response = messageEvent.getData().cast();
        final UUID idOfRequestToWhichThisIsTheResponse = LocalStorageDrivenByMessageEvents.getId(response);
        if (idOfRequestToWhichThisIsTheResponse == null) {
            // this means there was no request for this message; we will interpret it as a CrossDomainStorageEvent:
            final CrossDomainStorageEvent storageEvent = new CrossDomainStorageEventImpl(response.getKey(),
                    response.getNewValue(), response.getOldValue(), response.getUrl());
            GWT.log("Received cross-domain storage event "+storageEvent+"; sending to "+storageEventHandlers.size()+" registered handlers");
            for (final Handler storageEventHandler: storageEventHandlers) {
                storageEventHandler.onStorageChange(storageEvent);
            }
        } else {
            final Consumer<Object> resultForwarder = resultForwardersByRequestUuidAsString.remove(idOfRequestToWhichThisIsTheResponse);
            if (resultForwarder != null) {
                resultForwarder.accept(response.getResult());
            }
        }
    }

    private String getTargetOrigin() {
        return targetOrigin;
    }
    
    @Override
    public HandlerRegistration addStorageEventHandler(final Handler handler) {
        storageEventHandlers.add(handler);
        if (!registeredAsStorageEventListener) {
            registeredAsStorageEventListener = true;
            final UUID id = UUID.randomUUID();
            postMessageAndRegisterCallback(id, LocalStorageDrivenByMessageEvents.createRegisterStorageEventListener(id), /* callback */ null);
        }
        return ()->storageEventHandlers.remove(handler);
    }

    @Override
    public void setItem(String key, String value, Consumer<Void> callback) {
        final UUID id = UUID.randomUUID();
        postMessageAndRegisterCallback(id, LocalStorageDrivenByMessageEvents.createSetItemRequest(id, key, value), result->{
            if (callback != null) {
                callback.accept(null);
            }
        });
    }

    @Override
    public void getItem(String key, Consumer<String> callback) {
        final UUID id = UUID.randomUUID();
        postMessageAndRegisterCallback(id, LocalStorageDrivenByMessageEvents.createGetItemRequest(id, key), result->{
            if (callback != null) {
                callback.accept((String) result);
            }
        });
    }

    @Override
    public void removeItem(String key, Consumer<Void> callback) {
        final UUID id = UUID.randomUUID();
        postMessageAndRegisterCallback(id, LocalStorageDrivenByMessageEvents.createRemoveItemRequest(id, key), result->{
            if (callback != null) {
                callback.accept(null);
            }
        });
    }

    @Override
    public void clear(Consumer<Void> callback) {
        final UUID id = UUID.randomUUID();
        postMessageAndRegisterCallback(id, LocalStorageDrivenByMessageEvents.createClearRequest(id), result->{
            if (callback != null) {
                callback.accept(null);
            }
        });
    }

    @Override
    public void key(int index, Consumer<String> callback) {
        final UUID id = UUID.randomUUID();
        postMessageAndRegisterCallback(id, LocalStorageDrivenByMessageEvents.createKeyRequest(id, index), result->{
            if (callback != null) {
                callback.accept((String) result);
            }
        });
    }

    @Override
    public void getLength(Consumer<Integer> callback) {
        final UUID id = UUID.randomUUID();
        postMessageAndRegisterCallback(id, LocalStorageDrivenByMessageEvents.createGetLengthRequest(id), result->{
            if (callback != null) {
                callback.accept(((Number) result).intValue());
            }
        });
    }
}
