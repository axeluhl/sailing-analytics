package com.sap.sse.gwt.client.mutationobserver;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;

/**
 * A mutation observer specialized in observing the position by observing the top, left, bottom and right attributes of
 * a DOM element in order to recognize element position changes.
 */
public class ElementStyleMutationObserver {
    public static interface DomStyleMutationCallback {
        public void onStyleChanged();
    }

    protected static class NativeMutationObserver extends JavaScriptObject {
        static native NativeMutationObserver create(DomStyleMutationCallback callback) /*-{
            return new MutationObserver(
                    function(mutations) {

                        callback.@com.sap.sse.gwt.client.mutationobserver.ElementStyleMutationObserver.DomStyleMutationCallback::onStyleChanged()();
                    });
        }-*/;

        static native boolean exist() /*-{
            return !!$wnd.MutationObserver;
        }-*/;

        protected NativeMutationObserver() {
        }

        final native void observe(Element e) /*-{
            // observe childList and attributes
            this.observe(e, {
                attributes : true,
                attributeFilter : [ 'style' ],
                childList : true,
            });
        }-*/;

        final native void disconnect() /*-{
            this.disconnect();
        }-*/;
    }

    public static boolean isSupported() {
        return NativeMutationObserver.exist();
    }

    private NativeMutationObserver mutator;

    public ElementStyleMutationObserver(DomStyleMutationCallback callback) {
        mutator = NativeMutationObserver.create(callback);
    }

    public void observe(Element e) {
        mutator.observe(e);
    }

    public void disconnect() {
        mutator.disconnect();
    }
}