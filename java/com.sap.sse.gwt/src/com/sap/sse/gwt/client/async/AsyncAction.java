package com.sap.sse.gwt.client.async;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * An action that which will be executed a asynchronous remote call to the server or may get dropped
 * by the executor, e.g., if there is an excessive number of responses outstanding. When dropping an
 * action, the executor will invoke the {@link #dropped()} method on this action. The action may
 * decide to then take measures to compensate somehow for getting dropped.
 * 
 * @param <Result>
 *            The type of the returned value of the call
 * @author c5163874
 */
@FunctionalInterface
public interface AsyncAction<Result> {
    void execute(AsyncCallback<Result> callback);
    
    /**
     * Will be called by the {@link AsyncActionsExecutor} when dropping this action. This way, an action may react to
     * the dropping and may, e.g., enqueue some other, maybe simpler, request for later execution, e.g., in order to
     * achieve some eventual consistency.
     */
    default void dropped(AsyncActionsExecutor executor) {
    }
}
