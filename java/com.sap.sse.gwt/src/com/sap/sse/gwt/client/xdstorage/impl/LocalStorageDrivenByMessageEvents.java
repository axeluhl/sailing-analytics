package com.sap.sse.gwt.client.xdstorage.impl;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.sap.sse.gwt.client.Storage;
import com.sap.sse.gwt.client.messaging.MessageEvent;
import com.sap.sse.gwt.client.messaging.MessageListener;
import com.sap.sse.gwt.client.messaging.MessagePort;

/**
 * The request message processor and counterpart to {@link CrossDomainStorageImpl}. Receives {@link Request} objects
 * through a {@link MessagePort} 
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class LocalStorageDrivenByMessageEvents implements MessageListener<JavaScriptObject> {
    private final Storage localStorage;
    
    public LocalStorageDrivenByMessageEvents() {
        localStorage = Storage.getLocalStorageIfSupported();
    }
    
    @Override
    public void onMessageReceived(MessageEvent<JavaScriptObject> messageEvent) {
        // TODO filter by source origin; don't allow arbitrary domains to tamper with this domain's local storage...
        GWT.debugger();
        if (messageEvent.getData() instanceof JavaScriptObject) {
            final Request request = messageEvent.getData().cast();
            final String operation = request.getOperation();
            final StorageOperation op = StorageOperation.valueOf(operation);
            final JSONValue result;
            switch (op) {
            case CLEAR:
                localStorage.clear();
                result = JSONNull.getInstance();
                break;
            case GET_ITEM:
                final String itemValue = localStorage.getItem(request.getKey());
                result = itemValue == null ? JSONNull.getInstance() : new JSONString(itemValue);
                break;
            case GET_LENGTH:
                result = new JSONNumber(localStorage.getLength());
                break;
            case KEY:
                final String keyValue = localStorage.key(request.getIndex());
                result = keyValue == null ? JSONNull.getInstance() : new JSONString(keyValue);
                break;
            case REMOVE_ITEM:
                localStorage.removeItem(request.getKey());
                result = JSONNull.getInstance();
                break;
            case SET_ITEM:
                localStorage.setItem(request.getKey(), request.getValue());
                result = JSONNull.getInstance();
                break;
            default:
                throw new RuntimeException("Unknown operation "+op);
            }
            final JSONObject response = new JSONObject();
            response.put(Response.RESULT, result);
            response.put(Request.ID, new JSONString(request.getId()));
            messageEvent.getSource().postMessage(response.getJavaScriptObject(), messageEvent.getOrigin());
        }
    }
    
    private static JSONObject createEmptyRequest(UUID id) {
        final JSONObject result = new JSONObject();
        result.put(Request.ID, new JSONString(id.toString()));
        return result;
    }
    
    public static UUID getId(JavaScriptObjectWithID requestOrResponse) {
        return UUID.fromString(requestOrResponse.getId());
    }
    
    public static JSONObject createGetItemRequest(UUID id, String key) {
        final JSONObject result = createEmptyRequest(id);
        result.put(Request.OPERATION, new JSONString(StorageOperation.GET_ITEM.name()));
        result.put(Request.KEY, new JSONString(key));
        return result;
    }

    public static JSONObject createRemoveItemRequest(UUID id, String key) {
        final JSONObject result = createEmptyRequest(id);
        result.put(Request.OPERATION, new JSONString(StorageOperation.REMOVE_ITEM.name()));
        result.put(Request.KEY, new JSONString(key));
        return result;
    }
    
    public static JSONObject createGetLengthRequest(UUID id) {
        final JSONObject result = createEmptyRequest(id);
        result.put(Request.OPERATION, new JSONString(StorageOperation.GET_LENGTH.name()));
        return result;
    }
    
    public static JSONObject createKeyRequest(UUID id, int i) {
        final JSONObject result = createEmptyRequest(id);
        result.put(Request.OPERATION, new JSONString(StorageOperation.KEY.name()));
        result.put(Request.INDEX, new JSONNumber(i));
        return result;
    }
    
    public static JSONObject createSetItemRequest(UUID id, String key, String value) {
        final JSONObject result = createEmptyRequest(id);
        result.put(Request.OPERATION, new JSONString(StorageOperation.SET_ITEM.name()));
        result.put(Request.KEY, new JSONString(key));
        result.put(Request.VALUE, new JSONString(value));
        return result;
    }
    
    public static JSONObject createClearRequest(UUID id) {
        final JSONObject result = createEmptyRequest(id);
        result.put(Request.OPERATION, new JSONString(StorageOperation.CLEAR.name()));
        return result;
    }
}
