package com.sap.sailing.gwt.ui.server.dispatch;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.gwt.ui.server.Activator;
import com.sap.sailing.gwt.ui.server.ProxiedRemoteServiceServlet;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchException;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetLiveRacesAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRacesDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.OtherAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.OtherDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.rpc.DispatchRPC;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.util.ServiceTrackerFactory;

public class DispatchRPCImpl extends ProxiedRemoteServiceServlet implements DispatchRPC {

    private static final long serialVersionUID = -245230476512348999L;
    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    public DispatchRPCImpl() {
        BundleContext context = Activator.getDefault();
        Activator activator = Activator.getInstance();
        if (context != null) {
            // TODO
            // activator.setSailingService(this); // register so this service is informed when the bundle shuts down
        }
        racingEventServiceTracker = ServiceTrackerFactory.createAndOpen(context, RacingEventService.class);
    }

    protected RacingEventService getService() {
        return racingEventServiceTracker.getService(); // grab the service
    }

    @Override
    public <R extends Result, A extends Action<R>> R execute(A action) throws DispatchException {
        // TODO implement
        return null;
    }

}
