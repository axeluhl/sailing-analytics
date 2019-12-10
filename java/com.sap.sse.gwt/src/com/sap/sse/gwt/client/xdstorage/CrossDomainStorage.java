package com.sap.sse.gwt.client.xdstorage;

import java.util.function.Consumer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.shared.HandlerRegistration;
import com.sap.sse.gwt.client.Storage;
import com.sap.sse.gwt.client.xdstorage.impl.CrossDomainStorageFallingBackToLocalAfterTimeout;
import com.sap.sse.gwt.client.xdstorage.impl.LocalStorage;
import com.sap.sse.gwt.client.xdstorage.impl.StorageMessagingEntryPoint;

/**
 * Provides local storage capabilities, similar to {@code window.localStorage} and {@link Storage}, but in a way where
 * you can choose the "origin" under which the items shall be stored within certain boundary conditions.<p>
 * 
 * Local and session {@link Storage} are isolated based on the document's <em>origin</em>. This cannot be influenced by
 * the {@code document.domain} property. Therefore, using {@link Storage} will always store items under your application's
 * origin, even if that origin is a sub-domain of a parent domain and you would like all sub-domain applications to share
 * a local storage as long as they have the same parent domain.<p>
 * 
 * With this interface you can specify a base URL that defines the origin for managing a storage through this interface.
 * This assumes that under that base URL there is an application reachable that also exposes the {@code com.sap.sse.gwt} bundle
 * as a web bundle. When you then add an item using {@link #setItem(String, String, Consumer)}, the item will in fact be added to
 * the local storage for the origin of that base URL provided.<p>
 * 
 * The implementation uses a hidden {@code iframe} element that loads the {@link StorageMessagingEntryPoint} which registers
 * for "message" events posted to its {@code iframe}'s content window and interprets these messages as requests for its own
 * {@link Storage#getLocalStorageIfSupported() local storage}. It sends back success info and results as messages to this
 * application's current window where they are processed and forwarded to the {@link Consumer}s provided to the methods of
 * this interface as callbacks. 
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CrossDomainStorage {
    static CrossDomainStorage create(Document documentInWhichToInsertMessagingIframe, String baseUrlForStorageMessagingEntryPoint) {
        return new CrossDomainStorageFallingBackToLocalAfterTimeout(documentInWhichToInsertMessagingIframe, baseUrlForStorageMessagingEntryPoint);
    }
    
    /**
     * If {@code baseUrlForStorageMessagingEntryPoint} is {@code null}, a {@link LocalStorage} will be returned.
     * Otherwise, the method behaves like {@link #create(Document, String)}, inserts the {@code iframe} into the
     * {@link Document#get() current document} and returns a {@link CrossDomainStorageFallingBackToLocalAfterTimeout}
     * which only resorts to a {@link LocalStorage} again in case the iframe doesn't load within an appropriate
     * timeout starting with the first request.
     */
    static CrossDomainStorage create(String baseUrlForStorageMessagingEntryPoint) {
        final CrossDomainStorage result;
        if (baseUrlForStorageMessagingEntryPoint == null) {
            GWT.log("Using local storage");
            result = new LocalStorage();
        } else {
            GWT.log("Using "+baseUrlForStorageMessagingEntryPoint+" as shared storage");
            result = create(Document.get(), baseUrlForStorageMessagingEntryPoint);
        }
        return result;
    }
    
    HandlerRegistration addStorageEventHandler(final CrossDomainStorageEvent.Handler handler);
    
    void setItem(String key, String value, Consumer<Void> callback);

    void getItem(String key, Consumer<String> callback);

    void removeItem(String key, Consumer<Void> callback);

    void clear(Consumer<Void> callback);

    void key(int index, Consumer<String> callback);

    void getLength(Consumer<Integer> callback);
}
