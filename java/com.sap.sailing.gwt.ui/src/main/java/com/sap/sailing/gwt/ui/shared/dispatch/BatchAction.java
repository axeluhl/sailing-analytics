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
            long start = System.currentTimeMillis();
            try {
                result = a.execute(ctx);
                exceptions.add(null);
            } catch (Throwable e) {
                DispatchException e2 = handleException(e);
                exceptions.add(e2);
                logger.log(Level.SEVERE, "Error trying to dispatch action from type " + a.getClass().getName(), e);
            } finally {
                long duration = System.currentTimeMillis() - start;
                final Level logLevel;
                if (duration < 100) {
                    logLevel = Level.FINEST;
                } else if (duration < 500) {
                    logLevel = Level.INFO;
                } else {
                    logLevel = Level.WARNING;
                }
                logger.log(logLevel, "Dispatch took " + duration + "ms for " + a.getClass().getSimpleName());
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
