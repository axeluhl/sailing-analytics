package com.sap.sse.gwt.client;

import java.util.HashSet;
import java.util.Set;

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
    
    private static void registerAsStorageEventHandler() {
        if (isSupported()) {
            registerAsStorageEventHandlerImpl();
        }
    }

    private static native void registerAsStorageEventHandlerImpl() /*-{
        $wnd.addEventListener("storage", function(e) {
            @com.sap.sse.gwt.client.Storage::onStorageEvent(Lcom/google/gwt/core/client/JavaScriptObject;)(e);
        });
    }-*/;

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
    
    public static HandlerRegistration addStorageEventHandler(final StorageEvent.Handler handler) {
        storageEventHandlers.add(handler);
        return new HandlerRegistration() {
            @Override
            public void removeHandler() {
                storageEventHandlers.remove(handler);
            }
        };
    }

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
}
