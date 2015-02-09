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
    
    private AsyncCallback<Void> voidAsyncCallbackWithNoTrigger = new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable caught) {
            // do nothing - just ignore
        }
        @Override
        public void onSuccess(Void result) {
        }
    };
    
    @Test
    public void testAsyncExecutionWithTimeout() {
        executor = new AsyncActionsExecutor(/*maxPendingCalls*/4, /*maxPendingCallsPerType*/5,
                /*durationAfterToResetQueue*/Duration.ONE_SECOND);
        /*
         * We want to test that the executor resends jobs after a certain duration.
         * In order to do this we need to make sure that the Executor never gets
         * a call to callCompleted.
         */
        executor.execute(voidAsyncActionNotTriggeringCallCompleted, voidAsyncCallbackWithNoTrigger);
        // now the executor should have one pending action of type Void
        assertNotNull(executor.getNumberOfPendingActionsPerType(voidAsyncActionNotTriggeringCallCompleted.getClass().getName()));
        assertEquals(1, executor.getNumberOfPendingActionsPerType(voidAsyncActionNotTriggeringCallCompleted.getClass().getName()));
    }

    @Override
    public String getModuleName() {
        return "com.sap.sse.gwt.test.TestSSE";
    }

}
