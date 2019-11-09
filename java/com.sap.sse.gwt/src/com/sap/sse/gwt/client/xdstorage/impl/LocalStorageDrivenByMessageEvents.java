package com.sap.sse.gwt.client.xdstorage.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.Storage;
import com.sap.sse.gwt.client.StorageEvent;
import com.sap.sse.gwt.client.messaging.MessageEvent;
import com.sap.sse.gwt.client.messaging.MessageListener;
import com.sap.sse.gwt.client.messaging.MessagePort;

/**
 * The request message processor and counterpart to {@link CrossDomainStorageImpl}. Receives {@link Request} objects
 * through a {@link MessagePort} and executed them against a {@link #localStorage}, sending back a {@link Response}
 * {@link Response#getId() keyed} with the {@link Request#getId() ID} of the request.
 * <p>
 * 
 * Once a {@link Request} with operation {@link StorageOperation#REGISTER_FOR_STORAGE_EVENTS} is received, any
 * {@link StorageEvent} received from the {@link #localStorage} is propagated to the {@link MessagePort} from which
 * the request has been received.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class LocalStorageDrivenByMessageEvents implements MessageListener<JavaScriptObject> {
    private final Storage localStorage;
    
    /**
     * This somewhat awkward construct is owed to the fact that a {@link MessagePort} is just a view on the underlying {@code Window}
     * object, and access to a cross-origin window's data, including computing its hash code or calling its equals method, is not allowed.
     * Therefore, == comparisons and a simple list must do.
     */
    private final ArrayList<Pair<Object, String>> windowsToForwardStorageEventsToAndTheirTargetOrigins;

    private final String acceptableCrossDomainStorageRequestOriginRegexp;
    
    public LocalStorageDrivenByMessageEvents(String acceptableCrossDomainStorageRequestOriginRegexp) {
        this.acceptableCrossDomainStorageRequestOriginRegexp = acceptableCrossDomainStorageRequestOriginRegexp;
        localStorage = Storage.getLocalStorageIfSupported();
        windowsToForwardStorageEventsToAndTheirTargetOrigins = new ArrayList<>();
        Storage.addStorageEventHandler(storageEvent->sendStorageEvent(storageEvent));
    }
    
    private void sendStorageEvent(StorageEvent storageEvent) {
        final JSONObject storageEventResponse = new JSONObject(); // no ID; this identifies a storage event "response"
        storageEventResponse.put(Response.KEY, jsonStringOrJsonNull(storageEvent.getKey()));
        storageEventResponse.put(Response.OLD_VALUE, jsonStringOrJsonNull(storageEvent.getOldValue()));
        storageEventResponse.put(Response.NEW_VALUE, jsonStringOrJsonNull(storageEvent.getNewValue()));
        storageEventResponse.put(Response.URL, jsonStringOrJsonNull(storageEvent.getUrl()));
        GWT.log("Forwarding cross-domain storage event to "+windowsToForwardStorageEventsToAndTheirTargetOrigins.size()+" handlers");
        for (final Pair<Object, String> windowToForwardStorageEventToAndItsTargetOrigin : windowsToForwardStorageEventsToAndTheirTargetOrigins) {
            postMessage(windowToForwardStorageEventToAndItsTargetOrigin.getA(),
                    storageEventResponse.getJavaScriptObject(), windowToForwardStorageEventToAndItsTargetOrigin.getB());
        }
    }
    
    /**
     * This method is required because the {@code messagePort} to send to is a window that probably has a different origin,
     * and any access to such a cross-origin window other than a postMessage call is forbidden, including a GWT type cast
     * which checks properties of the object that is cannot check when from a different origin. Therefore, we need to avoid
     * strongly-typed {@link MessagePort} objects from other origins.
     */
    private native <T> void postMessage(Object messagePort, T message, String targetOrigin) /*-{
        messagePort.postMessage(message, targetOrigin);
    }-*/;

    @Override
    public void onMessageReceived(MessageEvent<JavaScriptObject> messageEvent) {
        if (isRequestFromOriginAllowed(messageEvent.getOrigin())) {
            if (messageEvent.getData() instanceof JavaScriptObject) {
                final Request request = messageEvent.getData().cast();
                GWT.log("Received request with operation "+request.getOperation());
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
                    result = jsonStringOrJsonNull(itemValue);
                    break;
                case GET_LENGTH:
                    result = new JSONNumber(localStorage.getLength());
                    break;
                case KEY:
                    final String keyValue = localStorage.key(request.getIndex());
                    result = jsonStringOrJsonNull(keyValue);
                    break;
                case REMOVE_ITEM:
                    localStorage.removeItem(request.getKey());
                    result = JSONNull.getInstance();
                    break;
                case SET_ITEM:
                    localStorage.setItem(request.getKey(), request.getValue());
                    result = JSONNull.getInstance();
                    break;
                case REGISTER_FOR_STORAGE_EVENTS:
                    GWT.log("received storage event handler registration from "+messageEvent.getOrigin());
                    windowsToForwardStorageEventsToAndTheirTargetOrigins.add(new Pair<>(messageEvent.getSource(), messageEvent.getOrigin()));
                    result = JSONNull.getInstance();
                    break;
                case UNREGISTER_FOR_STORAGE_EVENTS:
                    GWT.log("received storage event handler unregistration from "+messageEvent.getOrigin());
                    // make sure not to invoke hashCode() or equals() on the underlying Window because it's a different origin, therefore disallowed
                    for (final Iterator<Pair<Object, String>> i=windowsToForwardStorageEventsToAndTheirTargetOrigins.iterator(); i.hasNext(); ) {
                        final Pair<Object, String> next = i.next();
                        if (next.getA() == messageEvent.getSource()) {
                            i.remove();
                        }
                    }
                    result = JSONNull.getInstance();
                    break;
                case PING:
                    // send a "PONG" response
                    result = new JSONString("PONG");
                    break;
                default:
                    throw new RuntimeException("Unknown operation "+op);
                }
                final JSONObject response = new JSONObject();
                response.put(Response.RESULT, result);
                response.put(Request.ID, new JSONString(request.getId()));
                messageEvent.getSource().postMessage(response.getJavaScriptObject(), messageEvent.getOrigin());
            }
        } else {
            GWT.log("LocalStorageDrivenByMessageEvents dropped message from inacceptable origin "+messageEvent.getOrigin()+
                    " with data "+messageEvent.getData().toString());
        }
    }

    private boolean isRequestFromOriginAllowed(String origin) {
        return acceptableCrossDomainStorageRequestOriginRegexp == null ? origin.endsWith(".sapsailing.com")
                : origin.matches(acceptableCrossDomainStorageRequestOriginRegexp);
    }

    protected JSONValue jsonStringOrJsonNull(final String s) {
        return s == null ? JSONNull.getInstance() : new JSONString(s);
    }
    
    private static JSONObject createEmptyRequest(UUID id) {
        final JSONObject result = new JSONObject();
        result.put(Request.ID, new JSONString(id.toString()));
        return result;
    }
    
    public static UUID getId(JavaScriptObjectWithID requestOrResponse) {
        return requestOrResponse.getId() == null ? null : UUID.fromString(requestOrResponse.getId());
    }

    private static JSONObject createOperationRequest(UUID id, StorageOperation operation) {
        final JSONObject result = createEmptyRequest(id);
        result.put(Request.OPERATION, new JSONString(operation.name()));
        return result;
    }
    
    public static JSONObject createGetItemRequest(UUID id, String key) {
        final JSONObject result = createOperationRequest(id, StorageOperation.GET_ITEM);
        result.put(Request.KEY, new JSONString(key));
        return result;
    }

    public static JSONObject createRemoveItemRequest(UUID id, String key) {
        final JSONObject result = createOperationRequest(id, StorageOperation.REMOVE_ITEM);
        result.put(Request.KEY, new JSONString(key));
        return result;
    }
    
    public static JSONObject createGetLengthRequest(UUID id) {
        final JSONObject result = createOperationRequest(id, StorageOperation.GET_LENGTH);
        return result;
    }
    
    public static JSONObject createKeyRequest(UUID id, int i) {
        final JSONObject result = createOperationRequest(id, StorageOperation.KEY);
        result.put(Request.INDEX, new JSONNumber(i));
        return result;
    }
    
    public static JSONObject createSetItemRequest(UUID id, String key, String value) {
        final JSONObject result = createOperationRequest(id, StorageOperation.SET_ITEM);
        result.put(Request.KEY, new JSONString(key));
        result.put(Request.VALUE, new JSONString(value));
        return result;
    }
    
    public static JSONObject createClearRequest(UUID id) {
        final JSONObject result = createOperationRequest(id, StorageOperation.CLEAR);
        return result;
    }
    
    public static JSONObject createPingRequest(UUID id) {
        final JSONObject result = createOperationRequest(id, StorageOperation.PING);
        return result;
    }
    
    public static JSONObject createRegisterStorageEventListener(UUID id) {
        final JSONObject result = createOperationRequest(id, StorageOperation.REGISTER_FOR_STORAGE_EVENTS);
        return result;
    }

    public static JSONObject createUnregisterStorageEventListener(UUID id) {
        final JSONObject result = createOperationRequest(id, StorageOperation.UNREGISTER_FOR_STORAGE_EVENTS);
        return result;
    }
}
