package com.sap.sailing.dashboards.gwt.server;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.dashboards.gwt.shared.DashboardLiveRaceProvider;
import com.sap.sailing.dashboards.gwt.shared.MovingAveragesCache;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchContext;
import com.sap.sailing.dashboards.gwt.shared.dispatch.impl.DashboardDispatchContextImpl;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.gwt.ui.server.Activator;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.gwt.dispatch.client.transport.gwtrpc.RequestWrapper;
import com.sap.sse.gwt.dispatch.servlets.AbstractDispatchServlet;
import com.sap.sse.gwt.dispatch.shared.commands.Action;
import com.sap.sse.gwt.dispatch.shared.commands.Result;
import com.sap.sse.util.ServiceTrackerFactory;

public class DashboardDispatchServlet extends AbstractDispatchServlet<DashboardDispatchContext> {
    private static final long serialVersionUID = -245230476512348999L;

    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    private final PolarDataService polarDataService;
    private final DashboardLiveRaceProvider dashboardLiveRaceProvider;
    private final MovingAveragesCache movingAveragesCache;

    public DashboardDispatchServlet() {
        final BundleContext context = Activator.getDefault();
        racingEventServiceTracker = ServiceTrackerFactory.createAndOpen(context, RacingEventService.class);
        polarDataService = getPolarDataService();
        dashboardLiveRaceProvider = new DashboardLiveRaceProvider(racingEventServiceTracker.getService());
        movingAveragesCache = new MovingAveragesCache();
    }

    @Override
    protected <R extends Result, A extends Action<R, DashboardDispatchContext>> DashboardDispatchContext createDispatchContextFor(
            RequestWrapper<R, A, DashboardDispatchContext> request) {
       return new DashboardDispatchContextImpl(request.getCurrentClientTime(),
 racingEventServiceTracker.getService(),
                polarDataService, dashboardLiveRaceProvider, movingAveragesCache, request.getClientLocaleName(),
                getThreadLocalRequest());
    }
    
    private PolarDataService getPolarDataService() {
        BundleContext context = Activator.getDefault();
        ServiceReference<PolarDataService> polarServiceReference = context.getServiceReference(PolarDataService.class);
        return context.getService(polarServiceReference);
    }
}
