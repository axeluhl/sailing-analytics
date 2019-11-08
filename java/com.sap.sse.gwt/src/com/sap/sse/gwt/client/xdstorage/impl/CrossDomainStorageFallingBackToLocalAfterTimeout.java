package com.sap.sse.gwt.client.xdstorage.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.sap.sse.gwt.client.messaging.MessagePort;
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
                    GWT.log("connection to cross-domain storage at "+baseUrlForStorageMessagingEntryPoint+" established");
                    setStorageToUse(new CrossDomainStorageImpl(result, baseUrlForStorageMessagingEntryPoint));
                });
    }
}
