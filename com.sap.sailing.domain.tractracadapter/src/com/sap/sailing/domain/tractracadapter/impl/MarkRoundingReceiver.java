package com.sap.sailing.domain.tractracadapter.impl;

import java.util.ArrayList;
import java.util.List;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedLeg;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.RaceCompetitor;
import com.tractrac.clientmodule.data.ICallbackData;
import com.tractrac.clientmodule.data.MarkPassingsData;

public class MarkRoundingReceiver {
    private final DynamicTrackedEvent trackedEvent;
    private final com.tractrac.clientmodule.Event tractracEvent;
    
    public MarkRoundingReceiver(DynamicTrackedEvent trackedEvent, com.tractrac.clientmodule.Event tractracEvent) {
        super();
        this.trackedEvent = trackedEvent;
        this.tractracEvent = tractracEvent;
    }

    /**
     * The listeners returned will, when added to a controller, receive events about the
     * course definition of a race. When this happens, a new {@link RaceDefinition} is
     * created with the respective {@link Course} and added to the {@link #event event}.
     */
    public Iterable<TypeController> getMarkRoundingListeners() {
        List<TypeController> result = new ArrayList<TypeController>();
        for (final Race race : tractracEvent.getRaceList()) {
            TypeController controlPointListener = MarkPassingsData.subscribe(race,
                new ICallbackData<RaceCompetitor, MarkPassingsData>() {
                    @Override
                    public void gotData(RaceCompetitor competitor, MarkPassingsData record) {
                        Competitor myCompetitor = DomainFactory.INSTANCE.getCompetitor(competitor.getCompetitor());
                        DynamicTrackedRace trackedRace = trackedEvent.getTrackedRace(DomainFactory.INSTANCE.getRaceDefinition(
                                competitor.getRace()));
                        for (MarkPassingsData.Entry passing : record.getPassings()) {
                            TimePoint time = new MillisecondsTimePoint(passing.getTimestamp());
                            Waypoint passed = DomainFactory.INSTANCE.getWaypoint(passing.getControlPoint());
                            DynamicTrackedLeg finished = trackedRace.getTrackedLegFinishingAt(passed);
                            finished.completed(myCompetitor, time);
                            DynamicTrackedLeg begun = trackedRace.getTrackedLegStartingAt(passed);
                            // TODO do something smart with the pre-aggregated passing data
                        }
                    }
                });
            result.add(controlPointListener);
        }
        return result;
    }
}
