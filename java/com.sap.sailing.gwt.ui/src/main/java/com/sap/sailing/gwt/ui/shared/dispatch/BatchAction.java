package com.sap.sailing.gwt.ui.shared.dispatch;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.shared.GwtIncompatible;

public class BatchAction implements Action<BatchResult> {
    private static final Logger logger = Logger.getLogger(BatchAction.class.getName());
    private Action<?>[] actions;

    @SuppressWarnings("unused")
    private BatchAction() {
    }

    public BatchAction(Action<?>... actions) {
        this.actions = actions;
    }

    public Action<?>[] getActions() {
        return actions;
    }

    @Override
    @GwtIncompatible
    public BatchResult execute(DispatchContext ctx) throws DispatchException {

        final int nrOfActions = getActions().length;
        final ArrayList<Result> results = new ArrayList<Result>(nrOfActions);
        final ArrayList<DispatchException> exceptions = new ArrayList<DispatchException>(nrOfActions);
        for (Action<?> a : getActions()) {
            Result result = null;
            try {
                result = a.execute(ctx);
                exceptions.add(null);
            } catch (Throwable e) {
                DispatchException e2 = handleException(e);
                exceptions.add(e2);
                logger.log(Level.SEVERE, "Error trying to dispatch action from type " + a.getClass().getName(), e);
            }
            results.add(result);
        }
        return new BatchResult(results, exceptions);
    }

    @GwtIncompatible
    protected DispatchException handleException(Throwable e) {
        return new DispatchException(e.getLocalizedMessage());
    }
}
