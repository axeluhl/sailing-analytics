package com.sap.sailing.gwt.home.client.shared.dispatch;

import java.util.LinkedList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.BatchAction;
import com.sap.sailing.gwt.ui.shared.dispatch.BatchResult;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;

/**
 * Convenience class used to aggregate {@link Call} instances.
 * 
 * The class and all methods are private, its use is internal only.
 * 
 */
public final class DispatchCallStack {

    /**
     * The list containg all queued actions.
     */
    private final LinkedList<DispatchCall<?, ?>> actionQueue = new LinkedList<DispatchCall<?, ?>>();

    /**
     * The batch action holding the all the actions build in the queue. This instance variable is used to lazy
     * initialize and cache the created batch action.
     */
    private BatchAction batchAction;

    /**
     * Add a call to the queue. Can only be called as long as the batch action has not been accessed/ initialized.
     * 
     * @param call
     */
    public <A extends Action<R>, R extends Result> void addCall(A action, AsyncCallback<R> callback) {
        assert (this.batchAction == null);
        this.actionQueue.add(new DispatchCall<A, R>(action, callback));
    }

    /**
     * Return the aggregated calls as {@link BatchAction} instance. The instance is lazy initialized.
     * 
     * @return
     */
    public BatchAction getBatchAction() {
        if (this.batchAction == null) {
            Action<?>[] actions = new Action<?>[this.actionQueue.size()];
            for (int loop = 0; loop < actions.length; loop++) {
                actions[loop] = this.actionQueue.get(loop).getAction();
            }
            this.batchAction = new BatchAction(actions);
        }
        return this.batchAction;
    }

    /**
     * Fail the complete callstack with the given exception.
     * 
     * @param caught
     */
    public void fail(Throwable caught) {
        assert (caught != null);
        for (DispatchCall<?, ?> call : this.actionQueue) {
            call.getCallback().onFailure(caught);
        }
    }

    /**
     * Process results for the callstack. TODO: check generics usage, get rid of supress warnings
     * 
     * @param batchResult
     */
    @SuppressWarnings("unchecked")
    public void processResult(BatchResult batchResult) {
        assert (batchResult != null);

        int loop = 0;
        for (@SuppressWarnings("rawtypes")
        DispatchCall call : this.actionQueue) {
            Result result = batchResult.getResult(loop);

            if (result == null) {
                Throwable exception = batchResult.getException(loop);
                call.getCallback().onFailure(exception);
            } else {
                call.getCallback().onSuccess(result);
            }
            loop++;
        }
    }
}