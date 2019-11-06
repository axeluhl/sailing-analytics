package com.sap.sse.gwt.client.xdstorage;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.sap.sse.gwt.client.Storage;
import com.sap.sse.gwt.client.messaging.MessageEvent;
import com.sap.sse.gwt.client.messaging.MessageListener;

public class LocalStorageDrivenByMessageEvents implements MessageListener<JSONObject> {
    private static final String OPERATION = "o";
    private static final String KEY = "k";
    private static final String VALUE = "v";
    private static final String INDEX = "i";
    private static final String ID = "id";
    
    private final Storage localStorage;
    
    public LocalStorageDrivenByMessageEvents() {
        localStorage = Storage.getLocalStorageIfSupported();
    }
    
    @Override
    public void onMessageReceived(MessageEvent<JSONObject> messageEvent) {
        final JSONObject request = messageEvent.getData();
        GWT.log("Received "+request+" from origin "+messageEvent.getOrigin());
        final JSONValue operation = request.get(OPERATION);
        final Object result;
        if (operation.isString() != null) {
            final StorageOperation op = StorageOperation.valueOf(operation.isString().stringValue());
            switch (op) {
            case CLEAR:
                localStorage.clear();
                result = null;
                break;
            case GET_ALL_KEYS:
                result = localStorage.getAllKeys();
                break;
            case GET_ITEM:
                result = localStorage.getItem(request.get(KEY).isString().stringValue());
                break;
            case GET_LENGTH:
                result = localStorage.getLength();
                break;
            case KEY:
                result = localStorage.key((int) request.get(INDEX).isNumber().doubleValue());
                break;
            case REMOVE_ITEM:
                localStorage.removeItem(request.get(KEY).isString().stringValue());
                result = null;
                break;
            case SET_ITEM:
                localStorage.setItem(request.get(KEY).isString().stringValue(), request.get(VALUE).isString().stringValue());
                result = null;
                break;
            default:
                throw new RuntimeException("Unknown operation "+op);
            }
            // TODO marshal the request ID into the response so the caller can associate accordingly
            messageEvent.getSource().postMessage(result, "*"); // TODO clarify origin restriction
        }
    }
    
    private static JSONObject createEmptyRequest() {
        final JSONObject result = new JSONObject();
        result.put(ID, new JSONString(UUID.randomUUID().toString()));
        return result;
    }
    
    public static JSONObject createGetItemRequest(String key) {
        final JSONObject result = createEmptyRequest();
        result.put(OPERATION, new JSONString(StorageOperation.GET_ITEM.name()));
        result.put(KEY, new JSONString(key));
        return result;
    }

    public static JSONObject createRemoveItemRequest(String key) {
        final JSONObject result = createEmptyRequest();
        result.put(OPERATION, new JSONString(StorageOperation.REMOVE_ITEM.name()));
        result.put(KEY, new JSONString(key));
        return result;
    }
    
    public static JSONObject createGetLengthRequest() {
        final JSONObject result = createEmptyRequest();
        result.put(OPERATION, new JSONString(StorageOperation.GET_LENGTH.name()));
        return result;
    }
    
    public static JSONObject createKeyRequest(int i) {
        final JSONObject result = createEmptyRequest();
        result.put(OPERATION, new JSONString(StorageOperation.GET_LENGTH.name()));
        result.put(INDEX, new JSONNumber(i));
        return result;
    }
    
    public static JSONObject createGetAllKeysRequest() {
        final JSONObject result = createEmptyRequest();
        result.put(OPERATION, new JSONString(StorageOperation.GET_ALL_KEYS.name()));
        return result;
    }
    
    public static JSONObject createSetItemRequest(String key, String value) {
        final JSONObject result = createEmptyRequest();
        result.put(OPERATION, new JSONString(StorageOperation.SET_ITEM.name()));
        result.put(KEY, new JSONString(key));
        result.put(VALUE, new JSONString(value));
        return result;
    }
    
    public static JSONObject createClearRequest() {
        final JSONObject result = createEmptyRequest();
        result.put(OPERATION, new JSONString(StorageOperation.CLEAR.name()));
        return result;
    }
    
    
}
