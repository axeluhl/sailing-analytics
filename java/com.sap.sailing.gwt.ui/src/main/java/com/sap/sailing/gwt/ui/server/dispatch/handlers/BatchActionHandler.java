package com.sap.sailing.gwt.ui.server.dispatch.handlers;

import java.util.ArrayList;

import com.sap.sailing.gwt.ui.server.dispatch.AbstractHandler;
import com.sap.sailing.gwt.ui.server.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.BatchAction;
import com.sap.sailing.gwt.ui.shared.dispatch.BatchResult;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchException;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;

public class BatchActionHandler extends AbstractHandler<BatchResult, BatchAction> {

    @Override
    public BatchResult execute(BatchAction action, DispatchContext context) throws DispatchException {
            final int nrOfActions = action.getActions().length;
            final ArrayList<Result> results = new ArrayList<Result>(nrOfActions);
            final ArrayList<DispatchException> exceptions = new ArrayList<DispatchException>(nrOfActions);
            for (Action<?> a : action.getActions()) {
                    final String actionName = a.getClass().getSimpleName();
//                    TODO LOG.trace("Found action {} in batch", actionName);
                    Result result = null;
                    try {
                            result = context.execute(a);
                            exceptions.add(null);
                    } catch (Throwable e) {
                            DispatchException e2 = this.handleException(e);
                            exceptions.add(e2);
                    }
                    results.add(result);
            }
            return new BatchResult(results, exceptions);
    }

    /**
     * Throw exception to terminate Batch, return Exception to add to list
     * 
     * @param e
     * @return
     */
    protected DispatchException handleException(Throwable e) {
            return new DispatchException(e.getLocalizedMessage());
    }

}
