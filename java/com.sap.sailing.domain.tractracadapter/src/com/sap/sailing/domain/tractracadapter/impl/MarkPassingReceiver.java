package com.sap.sailing.domain.tractracadapter.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.util.Util.Triple;
import com.tractrac.clientmodule.ControlPoint;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.RaceCompetitor;
import com.tractrac.clientmodule.data.ICallbackData;
import com.tractrac.clientmodule.data.MarkPassingsData;

public class MarkPassingReceiver extends AbstractReceiverWithQueue<RaceCompetitor, MarkPassingsData, Boolean> {
    private static final Logger logger = Logger.getLogger(MarkPassingReceiver.class.getName());
    private final DynamicTrackedEvent trackedEvent;
    private final com.tractrac.clientmodule.Event tractracEvent;
    
    public MarkPassingReceiver(DynamicTrackedEvent trackedEvent, com.tractrac.clientmodule.Event tractracEvent,
            DomainFactory domainFactory) {
        super(domainFactory);
        this.trackedEvent = trackedEvent;
        this.tractracEvent = tractracEvent;
    }

    /**
     * The listeners returned will, when added to a controller, receive events about the
     * course definition of a race. When this happens, a new {@link RaceDefinition} is
     * created with the respective {@link Course} and added to the {@link #event event}.
     * Starts a thread for this object, blocking for events received which are then handled
     * asynchronously.
     */
    @Override
    public Iterable<TypeController> getTypeControllers() {
        List<TypeController> result = new ArrayList<TypeController>();
        for (final Race race : tractracEvent.getRaceList()) {
            TypeController controlPointListener = MarkPassingsData.subscribe(race,
                new ICallbackData<RaceCompetitor, MarkPassingsData>() {
                    @Override
                    public void gotData(RaceCompetitor competitor, MarkPassingsData record, boolean isLiveData) {
                        enqueue(new Triple<RaceCompetitor, MarkPassingsData, Boolean>(competitor, record, isLiveData));
                    }
                });
            result.add(controlPointListener);
        }
        new Thread(this, getClass().getName()).start();
        return result;
    }
    
    protected void handleEvent(Triple<RaceCompetitor, MarkPassingsData, Boolean> event) {
        System.out.print("L"); // as in "Leg"
        DynamicTrackedRace trackedRace = trackedEvent.getTrackedRace(getDomainFactory().getRaceDefinition(event
                .getA().getRace()));
        Course course = trackedRace.getRace().getCourse();
        Iterable<Waypoint> waypoints = course.getWaypoints();
        Map<com.sap.sailing.domain.base.ControlPoint, MarkPassing> passingsByControlPoint = new HashMap<com.sap.sailing.domain.base.ControlPoint, MarkPassing>();
        // Note: the entries always describe all mark passings for the competitor so far in the current race in order
        for (MarkPassingsData.Entry passing : event.getB().getPassings()) {
            ControlPoint controlPointPassed = passing.getControlPoint();
            com.sap.sailing.domain.base.ControlPoint domainControlPoint = getDomainFactory().getControlPoint(controlPointPassed);
            Waypoint passed = findWaypointForControlPoint(waypoints, domainControlPoint);
            if (passed != null) {
            TimePoint time = new MillisecondsTimePoint(passing.getTimestamp());
            MarkPassing markPassing = getDomainFactory().createMarkPassing(event.getA().getCompetitor(), passed, time);
            passingsByControlPoint.put(domainControlPoint, markPassing);
            } else {
                logger.warning("Didn't find waypoint in course "+course+" for mark passing around "+passing.getControlPoint());
            }
        }
        List<MarkPassing> markPassings = new ArrayList<MarkPassing>();
        for (Waypoint waypoint : waypoints) {
            MarkPassing passing = passingsByControlPoint.get(waypoint.getControlPoint());
            if (passing != null) {
                markPassings.add(passing);
            }
        }
        trackedRace.updateMarkPassings(getDomainFactory().getCompetitor(event.getA().getCompetitor()), markPassings);
    }

    private Waypoint findWaypointForControlPoint(Iterable<Waypoint> waypoints, com.sap.sailing.domain.base.ControlPoint domainControlPoint) {
        for (Waypoint waypoint : waypoints) {
            if (waypoint.getControlPoint() == domainControlPoint) {
                return waypoint;
            }
        }
        return null;
    }
}
