package com.sap.sse.gwt.client;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Represents the browser's localStorage and sessionStorage objects and provides eventing support.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class Storage extends JavaScriptObject {
    private static final Set<StorageEvent.Handler> storageEventHandlers = new HashSet<>();
    
    static {
        registerAsStorageEventHandler();
    }
    
    protected Storage() {
        super();
    }
    
    private static void registerAsStorageEventHandler() {
        if (isSupported()) {
            try {
                registerAsStorageEventHandlerImpl();
            } catch (Exception e) {
                // in classic DevMode with Internet Explorer an exception is thrown here;
                // to continue support of classic DevMode we'll simply catch and log the exception here:
                GWT.log(e.getMessage());
            }
        }
    }

    private static native void registerAsStorageEventHandlerImpl() /*-{
        $wnd.addEventListener("storage", function(e) {
            @com.sap.sse.gwt.client.Storage::onStorageEvent(Lcom/google/gwt/core/client/JavaScriptObject;)(e);
        });
    }-*/;

    public static HandlerRegistration addStorageEventHandler(final StorageEvent.Handler handler) {
        storageEventHandlers.add(handler);
        return new HandlerRegistration() {
            @Override
            public void removeHandler() {
                storageEventHandlers.remove(handler);
            }
        };
    }

    public static void removeStorageEventHandler(StorageEvent.Handler handler) {
        storageEventHandlers.remove(handler);
    }

    private static void onStorageEvent(JavaScriptObject event) {
        StorageEvent storageEvent = event.cast();
        for (StorageEvent.Handler handler : storageEventHandlers) {
            handler.onStorageChange(storageEvent);
        }
    }
    
    private native static JavaScriptObject getLocalStorageIfSupportedImpl() /*-{
        return $wnd.localStorage;
    }-*/;

    private native static JavaScriptObject getSessionStorageIfSupportedImpl() /*-{
        return $wnd.sessionStorage;
    }-*/;
    
    public native static boolean isLocalStorageSupported() /*-{
        try {
            return "localStorage" in $wnd && $wnd["localStorage"] !== null;
        } catch (e) {
            return false;
        }
    }-*/;
    
    public native static boolean isSessionStorageSupported() /*-{
        try {
            return "sessionStorage" in $wnd && $wnd["sessionStorage"] !== null;
        } catch (e) {
            return false;
        }
    }-*/;
    
    public static boolean isSupported() {
        return isLocalStorageSupported() && isSessionStorageSupported();
    }

    public static Storage getLocalStorageIfSupported() {
        final Storage result;
        if (isLocalStorageSupported()) {
            result = getLocalStorageIfSupportedImpl().cast();
        } else {
            result = null;
        }
        return result;
    }

    public static Storage getSessionStorageIfSupported() {
        final Storage result;
        if (isSessionStorageSupported()) {
            result = getSessionStorageIfSupportedImpl().cast();
        } else {
            result = null;
        }
        return result;
    }
    
    public final native void setItem(String key, String value) /*-{
        this.setItem(key, value);
    }-*/;

    public final native String getItem(String key) /*-{
        return this.getItem(key);
    }-*/;

    public final native void removeItem(String key) /*-{
        return this.removeItem(key);
    }-*/;

    public final native void clear() /*-{
        this.clear();
    }-*/;

    public final native String getKey(int index) /*-{
        return this.key(index);
    }-*/;

    public final native int getLength() /*-{
        return this.length;
    }-*/;
}
