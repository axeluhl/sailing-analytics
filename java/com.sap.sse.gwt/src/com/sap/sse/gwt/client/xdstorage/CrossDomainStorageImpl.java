package com.sap.sse.gwt.client.xdstorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.json.client.JSONObject;
import com.sap.sse.gwt.client.messaging.MessageEvent;
import com.sap.sse.gwt.client.messaging.MessagePort;

public class CrossDomainStorageImpl implements CrossDomainStorage {
    /**
     * The path under which the HTML document running the {@link StorageMessagingEntryPoint} can be loaded.
     */
    private static final String STORAGE_MESSAGING_ENTRY_POINT_PATH = "gwt-base/StorageMessaging.html";

    private final MessagePort portToStorageMessagingEntryPoint;
    private final Map<UUID, Consumer<Object>> resultForwardersByRequestUuidAsString;

    public CrossDomainStorageImpl(Document documentInWhichToInsertMessagingIframe, String baseUrlForStorageMessagingEntryPoint) {
        super();
        resultForwardersByRequestUuidAsString = new HashMap<>();
        this.portToStorageMessagingEntryPoint = MessagePort.createInDocument(documentInWhichToInsertMessagingIframe,
                baseUrlForStorageMessagingEntryPoint+(baseUrlForStorageMessagingEntryPoint.endsWith("/")?"":"/")+STORAGE_MESSAGING_ENTRY_POINT_PATH);
        MessagePort.getCurrentWindow().addMessageListener((MessageEvent<JavaScriptObject> messageEvent)->dispatchMessageToCallback(messageEvent));
    }
    
    private void postMessageAndRegisterCallback(UUID id, JSONObject request, Consumer<Object> resultConsumer) {
        resultForwardersByRequestUuidAsString.put(id, resultConsumer);
        portToStorageMessagingEntryPoint.postMessage(request.getJavaScriptObject(), getTargetOrigin());
    }

    private void dispatchMessageToCallback(MessageEvent<JavaScriptObject> messageEvent) {
        final Response response = messageEvent.getData().cast();
        final UUID idOfRequestToWhichThisIsTheResponse = LocalStorageDrivenByMessageEvents.getId(response);
        final Consumer<Object> resultForwarder = resultForwardersByRequestUuidAsString.remove(idOfRequestToWhichThisIsTheResponse);
        if (resultForwarder != null) {
            resultForwarder.accept(response.getResult());
        }
    }

    private String getTargetOrigin() {
        // TODO Implement CrossDomainStorageImpl.getTargetOrigin(...)
        return "*";
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

    @Override
    public void getAllKeys(Consumer<String[]> callback) {
        final UUID id = UUID.randomUUID();
        postMessageAndRegisterCallback(id, LocalStorageDrivenByMessageEvents.createGetAllKeysRequest(id), result->callback.accept((String[]) result));
    }
}
