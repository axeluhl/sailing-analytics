var PENDING_AJAX_CALLS = PENDING_AJAX_CALLS || function() {
    /* Private variable bound through closure which is initialized the counter to be 1. This is important because the
     * GWT bootstrap process in itself can take a bit of time. So, at a minimum we have to wait until the module is
     * fully loaded. Otherwise, we will get the occasional timing/synchronization failure during test runs.
     */ 
    var pendingCalls = 1;
    
    return {   
        numberOfPendingCalls : function() {
            return pendingCalls;
        },
        
        decrementPendingCalls : function() {
            pendingCalls--;
        },
        
        incrementPendingCalls : function() {
            pendingCalls++;
        }
    }
}();