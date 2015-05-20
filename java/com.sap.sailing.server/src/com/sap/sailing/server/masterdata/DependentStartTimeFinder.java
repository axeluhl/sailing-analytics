package com.sap.sailing.server.masterdata;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.util.ServiceTrackerFactory;

public class DependentStartTimeFinder extends RaceLogAnalyzer<TimePoint> {

    private BundleContext context;

    public DependentStartTimeFinder(RaceLog raceLog, BundleContext context) {
        super(raceLog);
        this.context = context;
    }

    @Override
    protected TimePoint performAnalysis() {
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (event instanceof RaceLogStartTimeEvent) {
                return ((RaceLogStartTimeEvent) event).getStartTime();
            } else if (event instanceof RaceLogDependentStartTimeEvent) {
                SimpleRaceLogIdentifier identifier = ((RaceLogDependentStartTimeEvent) event).getDependentOnRaceIdentifier();
                Duration startTimeDifference = ((RaceLogDependentStartTimeEvent) event).getStartTimeDifference();
                ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker = ServiceTrackerFactory
                        .createAndOpen(context, RacingEventService.class);
                
                RacingEventService racingEventService = racingEventServiceTracker.getService();
                Leaderboard leaderboard = racingEventService.getLeaderboardByName(identifier.getRegattaLikeParentName());
                RaceColumn raceColumn = leaderboard.getRaceColumnByName(identifier.getRaceColumnName());
                Fleet fleet = raceColumn.getFleetByName(identifier.getFleetName());
                RaceLog raceLog = raceColumn.getRaceLog(fleet);
                
                DependentStartTimeFinder dependentStartTimeFinder = new DependentStartTimeFinder(raceLog, context);
                TimePoint startTimeOfDependentRace = dependentStartTimeFinder.analyze();
                
                return startTimeOfDependentRace.plus(startTimeDifference);
            }
        }
        return null;
    }
}
