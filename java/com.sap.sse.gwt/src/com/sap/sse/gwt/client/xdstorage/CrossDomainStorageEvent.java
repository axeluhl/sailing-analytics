package com.sap.sse.gwt.client.xdstorage;

/**
 * A change event sent to a {@link Handler} object that has been {@link Storage#addStorageEventHandler(Handler)
 * registered} with the browser's {@link Storage}. It will receive events from both, the session storage and the local
 * storage. Which one the event came from can be determined by calling {@link #getStorageArea()} on the event and
 * comparing with either {@link Storage#getLocalStorageIfSupported()} or {@link Storage#getSessionStorageIfSupported()}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface CrossDomainStorageEvent {
    public static interface Handler {
        void onStorageChange(CrossDomainStorageEvent event);
    }

    String getKey();

    String getNewValue();

    String getOldValue();

    String getUrl();
}
