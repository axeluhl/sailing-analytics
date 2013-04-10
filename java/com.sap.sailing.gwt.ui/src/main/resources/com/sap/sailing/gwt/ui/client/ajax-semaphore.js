/**
 * <p>JavaScript counter for pending Ajax requests. Since GWT application are heavily AJAX driven one of the challenges
 *   in performing UI tests is to be able to tell when your application is in a state you expect. In simple cases you
 *   can wait until a loading animation disappears or until a well known element has changed the state (e.g. visibility
 *   or something else). However, since things are usually not that simple, especially in the context of GWT, it is more
 *   stable if we are able to tell exactly when an asynchronous request has finished. For this reason we use the counter
 *   for pending request which is incremented if an asynchronous callback is created and decremented if the callback
 *   completes. With this counter a test which triggers a request (which can cause additional request) can wait until
 *   the counter reaches 0 again.</p>
 * 
 * TODO: Write some additional documentation for categories!
 * 
 * @author
 *   D049941
 */
// TODO: Support categories of requests!
var PENDING_AJAX_CALLS = PENDING_AJAX_CALLS || function() {
    /* Private variable bound through closure which is initialized the counter to be 1. This is important because the
     * GWT bootstrap process in itself can take a bit of time. So, at a minimum we have to wait until the module is
     * fully loaded. Otherwise, we will get the occasional timing/synchronization failure during test runs.
     */ 
    var pendingCalls = 1;
    
    return {
        /**
         * <p></p>
         * 
         * @param {string} category
         *   The category of pending Ajax requests.
         * @returns {int}
         *   The number of pending Ajax requests.
         */
        numberOfPendingCalls : function(category) {
            return pendingCalls;
        },
        
        /**
         * <p>Decrements the counter by 1. This method should be called if an Ajax request is created.</p>
         * 
         * @param {string} category
         *    The category of the Ajax request.
         */
        decrementPendingCalls : function(category) {
            pendingCalls--;
        },
        
        /**
         * <p>Increments the counter by 1. This method should be called if an Ajax request completes regardless if it
         *   was successful or not.</p>
         *   
         * @param {string} category
         *   The category of the Ajax request.
         */
        incrementPendingCalls : function(category) {
            pendingCalls++;
        }
    }
}();