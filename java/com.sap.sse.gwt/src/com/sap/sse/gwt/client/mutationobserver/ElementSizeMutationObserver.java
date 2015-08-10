package com.sap.sse.gwt.client.mutationobserver;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;

/**
 * A mutation observer specialized in observing the clientWith and clientHeight attribute of a DOM element
 * in order to recognize element size changes. 
 * @author Frank
 *
 */
public class ElementSizeMutationObserver {
    public static interface DomMutationCallback {
        public void onSizeChanged(int newWidth, int newHeight);
    }

    protected static class NativeMutationObserver extends JavaScriptObject {
        static native NativeMutationObserver create(DomMutationCallback callback) /*-{
            return new MutationObserver(
		function(mutations) {
		    var maxNewWidth = -1;
                    var maxNewHeight = -1;
		    mutations.forEach(function(mutation) {
		        if(mutation.target.clientWidth > maxNewWidth) {
		            maxNewWidth = mutation.target.clientWidth;
		        }
                        if(mutation.target.clientHeight > maxNewHeight) {
                            maxNewHeight = mutation.target.clientHeight;
                        }
                    })
                    callback.@com.sap.sse.gwt.client.mutationobserver.ElementSizeMutationObserver.DomMutationCallback::onSizeChanged(II)(maxNewWidth,maxNewHeight);
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
		attributeFilter: ['clientHeight', 'clientWidth'],
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

    public ElementSizeMutationObserver(DomMutationCallback callback) {
        mutator = NativeMutationObserver.create(callback);
    }

    public void observe(Element e) {
        mutator.observe(e);
    }

    public void disconnect() {
        mutator.disconnect();
    }
}