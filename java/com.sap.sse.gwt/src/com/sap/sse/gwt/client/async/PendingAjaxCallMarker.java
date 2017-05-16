package com.sap.sse.gwt.client.async;

/**
 * <p>Utility class to mark pending Ajax requests.</p>
 * 
 * @author
 *   D049941
 */
public class PendingAjaxCallMarker {
    /**
     * <p>Decrements the counter for pending Ajax request of the given category.</p>
     * 
     * @param category
     *   The category of the Ajax request.
     */
    public static native void decrementPendingAjaxCalls(String category) /*-{
        try {
            if (($wnd.PENDING_AJAX_CALLS) && (typeof $wnd.PENDING_AJAX_CALLS.decrementPendingCalls === "function")) {
                $wnd.PENDING_AJAX_CALLS.decrementPendingCalls(category);
            }
        } catch (exception) {
            // Do error handling  
        }
    }-*/;
    
    /**
     * <p>Increments the counter for pending Ajax request of the given category.</p>
     * 
     * @param category
     *   The category of the Ajax request.
     */
    public static native void incrementPendingAjaxCalls(String category) /*-{
        try {
            if (($wnd.PENDING_AJAX_CALLS) && (typeof $wnd.PENDING_AJAX_CALLS.incrementPendingCalls === "function")) {
                $wnd.PENDING_AJAX_CALLS.incrementPendingCalls(category);
         }
        } catch (exception) {
            // Do error handling  
        }
    }-*/;
    
    private PendingAjaxCallMarker() {
        // Utility class
    }
}
