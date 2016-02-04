package com.sap.sse.gwt.dispatch.client.system.batching;

import java.util.ArrayList;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.dispatch.client.system.DispatchContext;
import com.sap.sse.gwt.dispatch.shared.commands.Action;
import com.sap.sse.gwt.dispatch.shared.commands.Result;

/**
 * Convenience class used to aggregate {@link DispatchCall} instances.
 * 
 * The class and all methods are private, its use is internal only.
 * 
 */
public final class DispatchCallStack<CTX extends DispatchContext> {

    /**
     * The list containg all queued actions.
     */
    private final ArrayList<DispatchCall<?, ?, CTX>> actionQueue = new ArrayList<DispatchCall<?, ?, CTX>>();

    /**
     * The batch action holding the all the actions build in the queue. This instance variable is used to lazy
     * initialize and cache the created batch action.
     */
    private BatchAction<CTX> batchAction;

    /**
     * Add a call to the queue. Can only be called as long as the batch action has not been accessed/ initialized.
     * 
     * @param action
     * @param callback
     */
    public <A extends Action<R, CTX>, R extends Result> void addCall(A action, AsyncCallback<R> callback) {
        assert (this.batchAction == null);
        this.actionQueue.add(new DispatchCall<A, R, CTX>(action, callback));
    }

    /**
     * Return the aggregated calls as {@link BatchAction} instance. The instance is initialized lazily.
     * 
     * @return
     */
    public BatchAction<CTX> getBatchAction() {
        if (this.batchAction == null) {
            ArrayList<Action<?, CTX>> actions = new ArrayList<Action<?, CTX>>();
            for (DispatchCall<?, ?, CTX> dispatchCall : this.actionQueue) {
                actions.add(dispatchCall.getAction());
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
     * Process results for the callstack.
     * 
     * @param batchResult
     * @param processResultsScheduled
     *            use a scheduler to execute the calls
     */
    public void processResult(BatchResult batchResult, boolean processResultsScheduled) {
        assert (batchResult != null);
        int loop = 0;
        for (DispatchCall<?, ?, CTX> call : this.actionQueue) {
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
    

    /**
     * Scheduled command wrapping the callback method call.
     */
    private class CallbackCommand implements ScheduledCommand {
        @SuppressWarnings("rawtypes")
        private final DispatchCall call;
        private final Result result;
        private final Throwable exception;

        @SuppressWarnings("rawtypes")
        public CallbackCommand(DispatchCall call, Result result, Throwable exception) {
            this.call = call;
            this.result = result;
            this.exception = exception;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void execute() {
            if (hasFailed()) {
                call.getCallback().onFailure(exception);
            } else {
                call.getCallback().onSuccess(result);
            }
        }
        
        private boolean hasFailed() {
            return exception != null;
        }
    }
}