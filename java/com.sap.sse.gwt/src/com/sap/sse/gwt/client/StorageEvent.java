package com.sap.sse.gwt.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A change event sent to a {@link Handler} object that has been {@link Storage#addStorageEventHandler(Handler) registered}
 * with the browser's {@link Storage}. It will receive events from both, the session storage and the local storage. Which one
 * the event came from can be determined by calling {@link #getStorageArea()} on the event and comparing with either
 * {@link Storage#getLocalStorageIfSupported()} or {@link Storage#getSessionStorageIfSupported()}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class StorageEvent extends JavaScriptObject {
    public static interface Handler {
        void onStorageChange(StorageEvent event);
    }

    public native String getKey() /*-{
        return this.key;
    }-*/;

    public native String getNewValue() /*-{
	return this.newValue;
    }-*/;

    public native String getOldValue() /*-{
	return this.oldValue;
    }-*/;

    public Storage getStorageArea() {
        return getStorageAreaImpl().cast();
    }

    private native JavaScriptObject getStorageAreaImpl() /*-{
        return this.storageArea;
    }-*/;

    public native String getUrl() /*-{
        return this.url;
    }-*/;
}
