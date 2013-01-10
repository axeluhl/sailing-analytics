package com.sap.sailing.gwt.ui.client;

public class PendingAjaxCallMarker {
    public static native void decrementPendingAjaxCalls() /*-{
        try {
            if (($wnd.PENDING_AJAX_CALLS) && (typeof $wnd.PENDING_AJAX_CALLS.decrementPendingCalls === "function")) {
                $wnd.PENDING_AJAX_CALLS.decrementPendingCalls();
            }
        } catch (exception) {
            // Do error handling  
        }
    }-*/;

    public static native void incrementPendingAjaxCalls() /*-{
        try {
            if (($wnd.PENDING_AJAX_CALLS) && (typeof $wnd.PENDING_AJAX_CALLS.incrementPendingCalls === "function")) {
                $wnd.PENDING_AJAX_CALLS.incrementPendingCalls();
         }
        } catch (exception) {
            // Do error handling  
        }
    }-*/;
    
    private PendingAjaxCallMarker() {
        super();
    }
}
