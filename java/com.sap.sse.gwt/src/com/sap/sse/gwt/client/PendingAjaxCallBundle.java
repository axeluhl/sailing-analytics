package com.sap.sse.gwt.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

/**
 * <p>The client bundle for the resource "ajax-semaphore.js" which is needed for the execution of UI tests.</p>
 * 
 * @author
 *   D049941
 */
public interface PendingAjaxCallBundle extends ClientBundle {
    /**
     * <p>Returns the JavaScript for the counter of pending Ajax requests. The counter is used in UI tests to be able
     *   to tell exactly when an asynchronous request has finished.</p>
     * 
     * @return
     *   The JavaScript for the counter of pending Ajax requests.
     */
    @Source("ajax-semaphore.js")
    TextResource ajaxSemaphoreJS();
}
