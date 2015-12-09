package com.sap.sailing.gwt.dispatch.client.batching;

import java.util.LinkedList;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.dispatch.client.Action;
import com.sap.sailing.gwt.dispatch.client.DispatchContext;
import com.sap.sailing.gwt.dispatch.client.Result;

/**
 * Convenience class used to aggregate {@link Call} instances.
 * 
 * The class and all methods are private, its use is internal only.
 * 
 */
public final class DispatchCallStack<CTX extends DispatchContext> {

    /**
     * The list containg all queued actions.
     */
    private final LinkedList<DispatchCall<?, ?, CTX>> actionQueue = new LinkedList<DispatchCall<?, ?, CTX>>();

    /**
     * The batch action holding the all the actions build in the queue. This instance variable is used to lazy
     * initialize and cache the created batch action.
     */
    private BatchAction<CTX> batchAction;

    /**
     * Add a call to the queue. Can only be called as long as the batch action has not been accessed/ initialized.
     * 
     * @param call
     */
    public <A extends Action<R, CTX>, R extends Result> void addCall(A action, AsyncCallback<R> callback) {
        assert (this.batchAction == null);
        this.actionQueue.add(new DispatchCall<A, R, CTX>(action, callback));
    }

    /**
     * Return the aggregated calls as {@link BatchAction} instance. The instance is lazy initialized.
     * 
     * @return
     */
    public BatchAction<CTX> getBatchAction() {
        if (this.batchAction == null) {
            @SuppressWarnings("unchecked")
            Action<?, CTX>[] actions = new Action[this.actionQueue.size()];
            for (int loop = 0; loop < actions.length; loop++) {
                actions[loop] = this.actionQueue.get(loop).getAction();
            }
            this.batchAction = new BatchAction<CTX>(actions);
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
        for (DispatchCall<?, ?, CTX> call : this.actionQueue) {
            call.getCallback().onFailure(caught);
        }
    }

    /**
     * Process results for the callstack. TODO: check generics usage, get rid of supress warnings
     * 
     * @param batchResult
     */
    public void processResult(BatchResult batchResult, boolean processResultsScheduled) {
        assert (batchResult != null);

        int loop = 0;
        for (@SuppressWarnings("rawtypes")
        DispatchCall call : this.actionQueue) {
            Result result = batchResult.getResult(loop);
            Throwable exception = batchResult.getException(loop);

            CallbackCommand callbackCommand = new CallbackCommand(call, result, exception);
            if(processResultsScheduled) {
                SplitScheduler.get().schedule(callbackCommand);
            } else {
                callbackCommand.execute();
            }
            loop++;
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private class CallbackCommand implements ScheduledCommand {
        private final DispatchCall call;
        private final Result result;
        private final Throwable exception;
        public CallbackCommand(DispatchCall call, Result result, Throwable exception) {
            this.call = call;
            this.result = result;
            this.exception = exception;
        }

        @Override
        public void execute() {
            if (result == null) {
                call.getCallback().onFailure(exception);
            } else {
                call.getCallback().onSuccess(result);
            }
        }
        
    }
}