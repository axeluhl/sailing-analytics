package com.sap.sse.gwt.dispatch.servlets;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.gwt.dispatch.client.system.DispatchContext;
import com.sap.sse.gwt.dispatch.client.transport.gwtrpc.DispatchRPC;
import com.sap.sse.gwt.dispatch.client.transport.gwtrpc.RequestWrapper;
import com.sap.sse.gwt.dispatch.client.transport.gwtrpc.ResultWrapper;
import com.sap.sse.gwt.dispatch.shared.commands.Action;
import com.sap.sse.gwt.dispatch.shared.commands.Result;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;
import com.sap.sse.gwt.dispatch.shared.exceptions.ServerDispatchException;
import com.sap.sse.gwt.server.ResultCachingProxiedRemoteServiceServlet;

/**
 * Abstract server-side part of the dispatch functionality. Needs to be subclassed in modules that want to use dispatch
 * along with a custom DispatchContext.
 *
 * @param <CTX> the concrete {@link DispatchContext} used by dispatch.
 */
public abstract class AbstractDispatchServlet<CTX extends DispatchContext> extends ResultCachingProxiedRemoteServiceServlet
        implements DispatchRPC<CTX> {
    private static final long serialVersionUID = -4279307111608131857L;

    private static final Logger logger = Logger.getLogger(AbstractDispatchServlet.class.getName());

    /**
     * Create the server context required by the action execution.
     * 
     * @param request
     * @return
     */
    protected abstract <R extends Result, A extends Action<R, CTX>> CTX createDispatchContextFor(
            RequestWrapper<R, A, CTX> request);

    @Override
    public final <R extends Result, A extends Action<R, CTX>> ResultWrapper<R> execute(RequestWrapper<R, A, CTX> request)
            throws DispatchException {
                A action = request.getAction();
                long start = System.currentTimeMillis();
                try {
            
                    R executionResult = action.execute(createDispatchContextFor(request));
                    return new ResultWrapper<R>(executionResult);
                } catch (DispatchException d) {
                    logger.log(Level.WARNING, "Server exception", d);
                    throw d;
                } catch (Throwable t) {
                    ServerDispatchException dispatchException = new ServerDispatchException(t);
                    logger.log(Level.SEVERE, "Uncaught server exception id: " + dispatchException.getExceptionId(), t);
                    throw dispatchException;
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
                    logger.log(logLevel, "Dispatch took " + duration + "ms for " + action.getClass().getSimpleName());
                }
            }

    @Override
    protected void doUnexpectedFailure(Throwable e) {
        logger.log(Level.WARNING, "GWT RPC Exception: " + e.getMessage(), e);
        super.doUnexpectedFailure(e);
    }
}