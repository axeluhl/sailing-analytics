package com.sap.sailing.gwt.ui.server.dispatch;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.gwt.ui.server.Activator;
import com.sap.sailing.gwt.ui.server.ProxiedRemoteServiceServlet;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContextImpl;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchException;
import com.sap.sailing.gwt.ui.shared.dispatch.RequestWrapper;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWrapper;
import com.sap.sailing.gwt.ui.shared.dispatch.ServerDispatchException;
import com.sap.sailing.gwt.ui.shared.dispatch.rpc.DispatchRPC;
import com.sap.sailing.news.EventNewsService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.util.ServiceTrackerFactory;

public class DispatchRPCImpl extends ProxiedRemoteServiceServlet implements DispatchRPC {
    private static final long serialVersionUID = -245230476512348999L;
    private static final Logger logger = Logger.getLogger(DispatchRPCImpl.class.getName());

    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    private final ServiceTracker<EventNewsService, EventNewsService> eventNewsServiceTracker;

    public DispatchRPCImpl() {
        final BundleContext context = Activator.getDefault();

        racingEventServiceTracker = ServiceTrackerFactory.createAndOpen(context, RacingEventService.class);
        eventNewsServiceTracker = ServiceTrackerFactory.createAndOpen(context, EventNewsService.class);
    }

    @Override
    public <R extends Result, A extends Action<R>> ResultWrapper<R> execute(RequestWrapper<R, A> request) throws DispatchException {
        A action = request.getAction();
        long start = System.currentTimeMillis();
        try {
            R executionResult = action.execute(
                    new DispatchContextImpl(request.getCurrentClientTime(),
                    racingEventServiceTracker.getService(), eventNewsServiceTracker.getService(), request
                            .getClientLocaleName()));
            return new ResultWrapper<R>(executionResult);
        } catch (DispatchException d) {
            logger.log(Level.WARNING, "Server exception", d);
            throw d;
        } catch (Throwable t) {
            String serverExceptionUUID = UUID.randomUUID().toString();
            logger.log(Level.SEVERE, "Uncaught server exception id: " + serverExceptionUUID, t);
            throw new ServerDispatchException(serverExceptionUUID, t);
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

}
