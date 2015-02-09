package com.sap.sse.gwt.test.client;

import org.junit.Test;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.Duration;
import com.sap.sse.gwt.client.async.AsyncAction;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;

/**
 * 
 * @author Simon Marcel Pamies
 */
public class AsyncActionsExecutorTest extends GWTTestCase {
    
    private AsyncActionsExecutor executor;
    
    private AsyncAction<Void> voidAsyncActionNotTriggeringCallCompleted = new AsyncAction<Void>() {
        @Override
        public void execute(AsyncCallback<Void> callback) {
            // do nothing as this action emulates a network failure where
            // no onSuccess or onFailure is being triggered
        }
    };
    
    private AsyncAction<Void> voidAsyncActionSuccessfullyCompleting = new AsyncAction<Void>() {
        @Override
        public void execute(AsyncCallback<Void> callback) {
            callback.onSuccess(null);
        }
    };

    private AsyncAction<Void> voidAsyncActionFailingToComplete = new AsyncAction<Void>() {
        @Override
        public void execute(AsyncCallback<Void> callback) {
            callback.onFailure(new RuntimeException());
        }
    };
    
    private AsyncCallback<Void> voidAsyncCallbackWithNoCallbackAction = new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable caught) {
        }
        @Override
        public void onSuccess(Void result) {
        }
    };
    
    @Test
    public void testThatANonTriggeringActionLeadsToPendingCall() {
        executor = new AsyncActionsExecutor(/*maxPendingCalls*/6, /*maxPendingCallsPerType*/5,
                /*durationAfterToResetQueue*/Duration.ONE_SECOND);
        // add a job that will never execute successfully
        executor.execute(voidAsyncActionNotTriggeringCallCompleted, voidAsyncCallbackWithNoCallbackAction);
        // now the executor should have one pending action
        assertNotNull(executor.getNumberOfPendingActionsPerType(voidAsyncActionNotTriggeringCallCompleted.getClass().getName()));
        assertEquals(1, executor.getNumberOfPendingActionsPerType(voidAsyncActionNotTriggeringCallCompleted.getClass().getName()));
    }
    
    @Test
    public void testTriggeringActionsLeadingToEmptyPendingQueue() {
        executor = new AsyncActionsExecutor(/*maxPendingCalls*/6, /*maxPendingCallsPerType*/6,
                /*durationAfterToResetQueue*/Duration.ONE_HOUR);
        // test completing jobs leading to empty pending queue
        executor.execute(voidAsyncActionSuccessfullyCompleting, voidAsyncCallbackWithNoCallbackAction);
        assertEquals(0, executor.getNumberOfPendingActionsPerType(voidAsyncActionSuccessfullyCompleting.getClass().getName()));
        executor.execute(voidAsyncActionFailingToComplete, voidAsyncCallbackWithNoCallbackAction);
        assertEquals(0, executor.getNumberOfPendingActionsPerType(voidAsyncActionFailingToComplete.getClass().getName()));
        // test that max pending calls is being used
        for (int i=0;i<=11;i++) {
            executor.execute(voidAsyncActionNotTriggeringCallCompleted, voidAsyncCallbackWithNoCallbackAction);
        }
        assertEquals(6, executor.getNumberOfPendingActions());
        assertEquals(6, executor.getNumberOfPendingActionsPerType(voidAsyncActionNotTriggeringCallCompleted.getClass().getName()));
    }

    @Override
    public String getModuleName() {
        return "com.sap.sse.gwt.test.TestSSE";
    }

}
