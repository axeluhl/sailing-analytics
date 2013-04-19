package com.sap.sailing.gwt.ui.client;

import com.google.gwt.debug.client.DebugInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * <p>Abstract base class for asynchronous remote procedure calls which should be marked as pending until they complete.
 *   Such calls will be marked using a counter, which is necessary in UI tests to be able able to tell exactly when an
 *   asynchronous request has finished. With this counter a test which triggers a request (which can cause additional
 *   request) can wait until the requests have finished. It's guaranteed that the counter is decremented no matter if
 *   the call was successful or not.</p>
 * 
 * <p>Note: Since the counter for pending Ajax requests is incremented as soon as an instance of this class is created
 *   you must not create instances which are not used or assigned to an field!</p>
 * 
 * @param <T>
 *   The type of the return value for the asynchronous remote procedure call.
 * @author
 *   D049941
 */
public abstract class MarkedAsyncCallback<T> implements AsyncCallback<T> {
    /**
     * <p>The key for the category of global requests. The key for the global category is just an empty string.</p>
     */
    public static final String CATEGORY_GLOBAL = ""; //$NON-NLS-1$
    
    private String category;
    
    /**
     * <p>Creates a marked asynchronous remote procedure call for the global category.</p>
     */
    public MarkedAsyncCallback() {
        this(CATEGORY_GLOBAL);
    }
    
    /**
     * <p>Creates a marked asynchronous remote procedure call for the given category. As soon as the asynchronous remote
     *   procedure call is created, the counter for pending Ajax calls for the given category is incremented.</p>
     * 
     * @param category
     *   The category of the asynchronous remote procedure call.
     */
    public MarkedAsyncCallback(String category) {
        this.category = category;
        
        if(DebugInfo.isDebugIdEnabled()) {
            PendingAjaxCallMarker.incrementPendingAjaxCalls(this.category);
        }
    }
    
    /**
     * <p>Called when the asynchronous call fails to complete normally. The concrete handling of the failure has to be
     *   implemented in {@link #handleFailure(Throwable)}. It's guaranteed that the counter for the pending requests is
     *   decremented.</p>
     * 
     * @param cause
     *   The failure encountered while executing the remote procedure call.
     */
    @Override
    public final void onFailure(Throwable cause) {
        try {
            handleFailure(cause);
        } finally {
            if(DebugInfo.isDebugIdEnabled()) {
                PendingAjaxCallMarker.decrementPendingAjaxCalls(this.category);
            }
        }
    }

    /**
     * <p>Called when the asynchronous call completes successfully. The concrete handling of the result has to be
     *   implemented in {@link #handleSuccess(Object)}. It's guaranteed that the counter for the pending requests is
     *   decremented.</p>
     * 
     * @param result
     *   The return value of the remote produced call.
     */
    @Override
    public final void onSuccess(T result) {
        try {
            handleSuccess(result);
        } finally {
            if(DebugInfo.isDebugIdEnabled()) {
                PendingAjaxCallMarker.decrementPendingAjaxCalls(this.category);
            }
        }
    }

    /**
     * <p>Called when the asynchronous call fails to complete normally.</p>
     * 
     * @param cause
     *   The failure encountered while executing the remote procedure call.
     */
    protected abstract void handleFailure(Throwable cause);
    
    /**
     * <p>Called when the asynchronous call completes successfully.</p>
     * 
     * @param result
     *   The return value of the remote produced call.
     */
    protected abstract void handleSuccess(T result);
}
