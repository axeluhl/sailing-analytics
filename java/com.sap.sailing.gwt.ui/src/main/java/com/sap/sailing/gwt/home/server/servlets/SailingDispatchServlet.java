package com.sap.sailing.gwt.home.server.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.common.sharding.ShardingType;
import com.sap.sailing.domain.sharding.ShardingContext;
import com.sap.sailing.domain.windfinder.WindFinderTrackerFactory;
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
    private final ServiceTracker<WindFinderTrackerFactory, WindFinderTrackerFactory> windFinderTrackerFactory;
    private final ServiceTracker<EventNewsService, EventNewsService> eventNewsServiceTracker;
    private final ServiceTracker<SecurityService, SecurityService> securityServiceTracker;
    private final ServiceTracker<UserStore, UserStore> userStoreTracker;
    private final ServiceTracker<TrackedRaceStatisticsCache, TrackedRaceStatisticsCache> trackedRaceStatisticsCacheTracker;

    public SailingDispatchServlet() {
        final BundleContext context = Activator.getDefault();
        racingEventServiceTracker = ServiceTrackerFactory.createAndOpen(context, RacingEventService.class);
        windFinderTrackerFactory = ServiceTrackerFactory.createAndOpen(context, WindFinderTrackerFactory.class);
        eventNewsServiceTracker = ServiceTrackerFactory.createAndOpen(context, EventNewsService.class);
        securityServiceTracker = ServiceTrackerFactory.createAndOpen(context, SecurityService.class);
        userStoreTracker = ServiceTrackerFactory.createAndOpen(context, UserStore.class);
        trackedRaceStatisticsCacheTracker = ServiceTrackerFactory.createAndOpen(context, TrackedRaceStatisticsCache.class);
    }

    @Override
    protected <R extends Result, A extends Action<R, SailingDispatchContext>> SailingDispatchContext createDispatchContextFor(
            RequestWrapper<R, A, SailingDispatchContext> request) {
        return new SailingDispatchContextImpl(request.getCurrentClientTime(), racingEventServiceTracker.getService(),
                windFinderTrackerFactory.getService(),
                eventNewsServiceTracker.getService(), securityServiceTracker.getService(),
                userStoreTracker.getService(), trackedRaceStatisticsCacheTracker.getService(),
                request.getClientLocaleName(), getThreadLocalRequest());
    }
    
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        ShardingType identifiedShardingType = null;
        try {
            if (req instanceof HttpServletRequest) {
                identifiedShardingType = ShardingContext.identifyAndSetShardingConstraint( ((HttpServletRequest) req).getPathInfo());
            }
            super.service(req, res);
        } finally {
            if (identifiedShardingType != null) {
                ShardingContext.clearShardingConstraint(identifiedShardingType);
            }
        }
    }
}
