package com.sap.sailing.gwt.home.server.servlets;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.server.SailingDispatchContextImpl;
import com.sap.sailing.gwt.ui.server.Activator;
import com.sap.sailing.news.EventNewsService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.statistics.TrackedRaceStatisticsCache;
import com.sap.sse.gwt.dispatch.client.transport.gwtrpc.RequestWrapper;
import com.sap.sse.gwt.dispatch.servlets.AbstractDispatchServlet;
import com.sap.sse.gwt.dispatch.shared.commands.Action;
import com.sap.sse.gwt.dispatch.shared.commands.Result;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.UserStore;
import com.sap.sse.util.ServiceTrackerFactory;

public class SailingDispatchServlet extends AbstractDispatchServlet<SailingDispatchContext> {
    private static final long serialVersionUID = -245230476512348999L;

    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    private final ServiceTracker<EventNewsService, EventNewsService> eventNewsServiceTracker;
    private final ServiceTracker<SecurityService, SecurityService> securityServiceTracker;
    private final ServiceTracker<UserStore, UserStore> userStoreTracker;
    private final ServiceTracker<TrackedRaceStatisticsCache, TrackedRaceStatisticsCache> trackedRaceStatisticsCacheTracker;

    public SailingDispatchServlet() {
        final BundleContext context = Activator.getDefault();
        racingEventServiceTracker = ServiceTrackerFactory.createAndOpen(context, RacingEventService.class);
        eventNewsServiceTracker = ServiceTrackerFactory.createAndOpen(context, EventNewsService.class);
        securityServiceTracker = ServiceTrackerFactory.createAndOpen(context, SecurityService.class);
        userStoreTracker = ServiceTrackerFactory.createAndOpen(context, UserStore.class);
        trackedRaceStatisticsCacheTracker = ServiceTrackerFactory.createAndOpen(context, TrackedRaceStatisticsCache.class);
    }

    @Override
    protected <R extends Result, A extends Action<R, SailingDispatchContext>> SailingDispatchContext createDispatchContextFor(
            RequestWrapper<R, A, SailingDispatchContext> request) {
        return new SailingDispatchContextImpl(request.getCurrentClientTime(), racingEventServiceTracker.getService(),
                eventNewsServiceTracker.getService(), securityServiceTracker.getService(),
                userStoreTracker.getService(), trackedRaceStatisticsCacheTracker.getService(),
                request.getClientLocaleName(), getThreadLocalRequest());
    }
}
