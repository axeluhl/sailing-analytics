package com.sap.sse.gwt.client.xdstorage.impl;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.sap.sse.gwt.client.messaging.MessageEvent;
import com.sap.sse.gwt.client.messaging.MessagePort;
import com.sap.sse.gwt.client.messaging.MessagePort.ResponseListenerHandle;
import com.sap.sse.gwt.client.xdstorage.DelegatingCrossDomainStorageFuture;

public class CrossDomainStorageFallingBackToLocalAfterTimeout extends DelegatingCrossDomainStorageFuture {
    /**
     * The path under which the HTML document running the {@link StorageMessagingEntryPoint} can be loaded.
     */
    private static final String STORAGE_MESSAGING_ENTRY_POINT_PATH = "gwt-base/StorageMessaging.html";

    /**
     * The timeout to wait for upon the first request to this cross-domain storage until the iframe containing the
     * storage in the target domain announces its readiness. If this timeout expires, the {@link #fallbackLocalStorage} is
     * used instead.
     */
    private static final int TIMEOUT_FOR_IFRAME_TO_RESPOND_IN_MILLIS = 10000;
    
    public CrossDomainStorageFallingBackToLocalAfterTimeout(Document documentInWhichToInsertMessagingIframe,
            String baseUrlForStorageMessagingEntryPoint) {
        super(TIMEOUT_FOR_IFRAME_TO_RESPOND_IN_MILLIS, ()->new LocalStorage());
        MessagePort.createInDocument(documentInWhichToInsertMessagingIframe,
                baseUrlForStorageMessagingEntryPoint+(baseUrlForStorageMessagingEntryPoint.endsWith("/")?"":"/")+STORAGE_MESSAGING_ENTRY_POINT_PATH,
                result->{
                    final UUID idOfPingEvent = UUID.randomUUID();
                    GWT.log("connection to cross-domain storage at "+baseUrlForStorageMessagingEntryPoint+" established. Sending PING with ID "+idOfPingEvent);
                    final ResponseListenerHandle[] handle = new ResponseListenerHandle[1];
                    handle[0] = result.addResponseListener((MessageEvent<JavaScriptObject> pongEvent)->{
                        final Response response = pongEvent.getData().cast();
                        if (response.getId().equals(idOfPingEvent.toString())) {
                            result.removeResponseListener(handle[0]);
                            // a PONG came back; we're connected and accepted; set the storage as the one to use:
                            GWT.log("Received "+response.getResult()+", so we're accepted. Using cross-domain storage now.");
                            setStorageToUse(new CrossDomainStorageImpl(result, baseUrlForStorageMessagingEntryPoint));
                        }
                    });
                    result.postMessage(LocalStorageDrivenByMessageEvents.createPingRequest(idOfPingEvent).getJavaScriptObject(),
                            baseUrlForStorageMessagingEntryPoint);
                });
    }
}
