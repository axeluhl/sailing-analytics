package com.sap.sse.gwt.client.xdstorage;

import com.google.gwt.core.client.JavaScriptObject;
import com.sap.sse.gwt.client.Storage;
import com.sap.sse.gwt.client.messaging.AbstractMessagingEntryPoint;
import com.sap.sse.gwt.client.messaging.MessageListener;
import com.sap.sse.gwt.client.messaging.MessagePort;

/**
 * A {@link AbstractMessagingEntryPoint messaging entry point} that implements a protocol with {@link CrossDomainStorage} such
 * that a {@link CrossDomainStorage} object configured to point to a {@link MessagePort} connected to this entry point's window
 * can use this entry point's {@link Storage#getLocalStorageIfSupported() local storage}. By nature of the asynchronous messaging
 * between the {@link CrossDomainStorage} client and this entry point's window, any response to any request will be sent to
 * callbacks passed to the {@link CrossDomainStorage} methods.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class StorageMessagingEntryPoint extends AbstractMessagingEntryPoint<JavaScriptObject> {
    protected MessageListener<JavaScriptObject> getMessageListener() {
        return new LocalStorageDrivenByMessageEvents();
    }
}
