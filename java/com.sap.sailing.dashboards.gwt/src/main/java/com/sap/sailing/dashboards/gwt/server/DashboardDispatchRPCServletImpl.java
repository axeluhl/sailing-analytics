package com.sap.sailing.dashboards.gwt.server;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.dashboards.gwt.shared.DashboardLiveRaceProvider;
import com.sap.sailing.dashboards.gwt.shared.MovingAveragesCache;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchContext;
import com.sap.sailing.dashboards.gwt.shared.dispatch.impl.DashboardDispatchContextImpl;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.gwt.dispatch.client.Action;
import com.sap.sailing.gwt.dispatch.client.Result;
import com.sap.sailing.gwt.dispatch.client.ResultWrapper;
import com.sap.sailing.gwt.dispatch.client.exceptions.DispatchException;
import com.sap.sailing.gwt.dispatch.client.exceptions.ServerDispatchException;
import com.sap.sailing.gwt.dispatch.client.rpcimpl.DispatchRPC;
import com.sap.sailing.gwt.dispatch.client.rpcimpl.RequestWrapper;
import com.sap.sailing.gwt.ui.server.Activator;
import com.sap.sailing.gwt.ui.server.ProxiedRemoteServiceServlet;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.util.ServiceTrackerFactory;

public class DashboardDispatchRPCServletImpl extends ProxiedRemoteServiceServlet implements DispatchRPC<DashboardDispatchContext> {
    private static final long serialVersionUID = -245230476512348999L;
    private static final Logger logger = Logger.getLogger(DashboardDispatchRPCServletImpl.class.getName());

    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    private final PolarDataService polarDataService;
    private final DashboardLiveRaceProvider dashboardLiveRaceProvider;
    private final MovingAveragesCache movingAveragesCache;

    public DashboardDispatchRPCServletImpl() {
        final BundleContext context = Activator.getDefault();
        racingEventServiceTracker = ServiceTrackerFactory.createAndOpen(context, RacingEventService.class);
        polarDataService = getPolarDataService();
        dashboardLiveRaceProvider = new DashboardLiveRaceProvider(racingEventServiceTracker.getService());
        movingAveragesCache = new MovingAveragesCache();
    }

    @Override
    public <R extends Result, A extends Action<R, DashboardDispatchContext>> ResultWrapper<R> execute(
            RequestWrapper<R, A, DashboardDispatchContext> request) throws DispatchException {
        A action = request.getAction();
        long start = System.currentTimeMillis();
        try {
            R executionResult = action.execute(new DashboardDispatchContextImpl(request.getCurrentClientTime(),
                    racingEventServiceTracker.getService(), polarDataService, dashboardLiveRaceProvider,
                    movingAveragesCache, request.getClientLocaleName(), getThreadLocalRequest()));
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

    @Override
    protected void doUnexpectedFailure(Throwable e) {
        logger.log(Level.WARNING, "GWT RPC Exception: " + e.getMessage(), e);
        super.doUnexpectedFailure(e);
    }
    
    private PolarDataService getPolarDataService() {
        BundleContext context = Activator.getDefault();
        ServiceReference<PolarDataService> polarServiceReference = context.getServiceReference(PolarDataService.class);
        return context.getService(polarServiceReference);
    }
}
