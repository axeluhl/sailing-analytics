package com.sap.sailing.gwt.home.server.servlets;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.server.SailingDispatchContextImpl;
import com.sap.sailing.gwt.ui.server.Activator;
import com.sap.sailing.gwt.ui.server.ProxiedRemoteServiceServlet;
import com.sap.sailing.news.EventNewsService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.gwt.dispatch.client.Action;
import com.sap.sse.gwt.dispatch.client.Result;
import com.sap.sse.gwt.dispatch.client.exceptions.DispatchException;
import com.sap.sse.gwt.dispatch.client.exceptions.ServerDispatchException;
import com.sap.sse.gwt.dispatch.client.rpcimpl.DispatchRPC;
import com.sap.sse.gwt.dispatch.client.rpcimpl.RequestWrapper;
import com.sap.sse.gwt.dispatch.client.rpcimpl.ResultWrapper;
import com.sap.sse.util.ServiceTrackerFactory;

public class DispatchRPCServletImpl extends ProxiedRemoteServiceServlet implements DispatchRPC<SailingDispatchContext> {
    private static final long serialVersionUID = -245230476512348999L;
    private static final Logger logger = Logger.getLogger(DispatchRPCServletImpl.class.getName());

    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    private final ServiceTracker<EventNewsService, EventNewsService> eventNewsServiceTracker;

    public DispatchRPCServletImpl() {
        final BundleContext context = Activator.getDefault();

        racingEventServiceTracker = ServiceTrackerFactory.createAndOpen(context, RacingEventService.class);
        eventNewsServiceTracker = ServiceTrackerFactory.createAndOpen(context, EventNewsService.class);
    }

    @Override
    public <R extends Result, A extends Action<R, SailingDispatchContext>> ResultWrapper<R> execute(
            RequestWrapper<R, A, SailingDispatchContext> request) throws DispatchException {
        A action = request.getAction();
        long start = System.currentTimeMillis();
        try {

            R executionResult = action.execute(
                    new SailingDispatchContextImpl(request.getCurrentClientTime(),
                    racingEventServiceTracker.getService(), eventNewsServiceTracker.getService(), request
                            .getClientLocaleName(), getThreadLocalRequest()));
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
