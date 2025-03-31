package com.sap.sse.gwt.client.mutationobserver;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;

/**
 * A mutation observer looking at adding at child nodes of a DOM element (add, remove).
 * @author Frank
 *
 */
public class ElementChildsMutationObserver {
    public static interface DomMutationCallback {
        public void onNodesRemoved(JsArray<Node> removedNodes);

        public void onNodesInserted(JsArray<Node> addedNodes, Node nextSibling);

        public void onNodesAppended(JsArray<Node> addedNodes);
    }

    protected static class NativeMutationObserver extends JavaScriptObject {
        static native NativeMutationObserver create(DomMutationCallback callback) /*-{
            return new MutationObserver(
		function(mutations) {
		    var mutationsCount = mutations.length;
                    for (var i = 0; i < mutationsCount; i++) {
			if (mutations[i].removedNodes.length) {
			    callback.@com.sap.sse.gwt.client.mutationobserver.ElementChildsMutationObserver.DomMutationCallback::onNodesRemoved(Lcom/google/gwt/core/client/JsArray;)(mutations[i].removedNodes);
			}
			if (mutations[i].addedNodes.length) {
			    var nodes = mutations[i].addedNodes;
			    if (mutations[i].nextSibling) {
				callback.@com.sap.sse.gwt.client.mutationobserver.ElementChildsMutationObserver.DomMutationCallback::onNodesInserted(Lcom/google/gwt/core/client/JsArray;Lcom/google/gwt/dom/client/Node;)(nodes, mutations[i].nextSibling)
			    } else {
			        callback.@com.sap.sse.gwt.client.mutationobserver.ElementChildsMutationObserver.DomMutationCallback::onNodesAppended(Lcom/google/gwt/core/client/JsArray;)(nodes);
                            }
		        }
		    }
                });
        }-*/;

        static native boolean exist() /*-{
            return !!$wnd.MutationObserver;
        }-*/;

        protected NativeMutationObserver() {
        }

        final native void observe(Element e) /*-{
            // only observe childList modification
            this.observe(e, {
		attributes : false,
		childList : true,
		characterData : false
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

    public ElementChildsMutationObserver(DomMutationCallback callback) {
        mutator = NativeMutationObserver.create(callback);
    }

    public void observe(Element e) {
        mutator.observe(e);
    }

    public void disconnect() {
        mutator.disconnect();
    }
}