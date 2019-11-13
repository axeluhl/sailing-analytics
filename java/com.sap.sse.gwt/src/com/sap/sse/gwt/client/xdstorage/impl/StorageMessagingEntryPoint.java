package com.sap.sse.gwt.client.xdstorage.impl;

import java.util.function.Consumer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sse.gwt.client.CrossDomainStorageConfigurationService;
import com.sap.sse.gwt.client.CrossDomainStorageConfigurationServiceAsync;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.Storage;
import com.sap.sse.gwt.client.messaging.AbstractMessagingEntryPoint;
import com.sap.sse.gwt.client.messaging.MessageListener;
import com.sap.sse.gwt.client.messaging.MessagePort;
import com.sap.sse.gwt.client.xdstorage.CrossDomainStorage;
import com.sap.sse.gwt.server.RemoteServiceMappingConstants;

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
    protected void getMessageListener(Consumer<MessageListener<JavaScriptObject>> resultCallback) {
        final CrossDomainStorageConfigurationServiceAsync crossDomainStorageConfigurationService = GWT.create(CrossDomainStorageConfigurationService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) crossDomainStorageConfigurationService, RemoteServiceMappingConstants.crossDomainStorageConfigurationServiceRemotePath);
        crossDomainStorageConfigurationService.getAcceptableCrossDomainStorageRequestOriginRegexp(new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(caught.getMessage(), NotificationType.ERROR);
            }

            @Override
            public void onSuccess(String acceptableCrossDomainStorageRequestOriginRegexp) {
                resultCallback.accept(new LocalStorageDrivenByMessageEvents(acceptableCrossDomainStorageRequestOriginRegexp));
            }
        });
    }
}
