package com.sap.sailing.domain.tractracadapter.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.tractrac.model.lib.api.data.IControlPassing;
import com.tractrac.model.lib.api.data.IControlPassings;
import com.tractrac.model.lib.api.event.IEvent;
import com.tractrac.model.lib.api.event.IRace;
import com.tractrac.model.lib.api.event.IRaceCompetitor;
import com.tractrac.model.lib.api.route.IControl;
import com.tractrac.subscription.lib.api.IEventSubscriber;
import com.tractrac.subscription.lib.api.IRaceSubscriber;

public class MarkPassingReceiver extends AbstractReceiverWithQueue<IRaceCompetitor, IControlPassings, Void> {
    private static final Logger logger = Logger.getLogger(MarkPassingReceiver.class.getName());
    
    public MarkPassingReceiver(DynamicTrackedRegatta trackedRegatta, IEvent tractracEvent,
            Simulator simulator, DomainFactory domainFactory, IEventSubscriber eventSubscriber, IRaceSubscriber raceSubscriber) {
        super(domainFactory, tractracEvent, trackedRegatta, simulator, eventSubscriber, raceSubscriber);
    }

    /**
     * The listeners returned will, when added to a controller, receive events about the
     * course definition of a race. When this happens, a new {@link RaceDefinition} is
     * created with the respective {@link Course} and added to the {@link #event event}.
     * Starts a thread for this object, blocking for events received which are then handled
     * asynchronously.
     */
    @Override
    public Iterable<TypeController> getTypeControllersAndStart() {
        List<TypeController> result = new ArrayList<TypeController>();
        for (final IRace race : getTracTracEvent().getRaces()) {
            TypeController controlPointListener = MarkPassingsData.subscribe();
            result.add(controlPointListener);
        }
        setAndStartThread(new Thread(this, getClass().getName()));
        return result;
    }
    
    protected void handleEvent(Triple<IRaceCompetitor, IControlPassings, Void> event) {
        System.out.print("L"); // as in "Leg"
        DynamicTrackedRace trackedRace = getTrackedRace(event.getA().getRace());
        if (trackedRace != null) {
            Course course = trackedRace.getRace().getCourse();
            Iterator<Waypoint> waypointsIter = course.getWaypoints().iterator();
            Map<Waypoint, MarkPassing> passingsByWaypoint = new HashMap<Waypoint, MarkPassing>();
            // Note: the entries always describe all mark passings for the competitor so far in the current race in
            // order
            for (IControlPassing passing : event.getB().getPassings()) {
                IControl controlPointPassed = passing.getControl();
                com.sap.sailing.domain.base.ControlPoint domainControlPoint = getDomainFactory()
                        .getOrCreateControlPoint(new ControlPointAdapter(controlPointPassed));
                Waypoint passed = findWaypointForControlPoint(trackedRace, waypointsIter, domainControlPoint,
                        getDomainFactory().getOrCreateCompetitor(event.getA().getCompetitor()));
                if (passed != null) {
                    TimePoint time = new MillisecondsTimePoint(passing.getTimestamp());
                    MarkPassing markPassing = getDomainFactory().createMarkPassing(time, passed,
                            getDomainFactory().getOrCreateCompetitor(event.getA().getCompetitor()));
                    passingsByWaypoint.put(passed, markPassing);
                } else {
                    logger.warning("Didn't find waypoint in course " + course + " for mark passing around "
                            + passing.getControl());
                }
            }
            List<MarkPassing> markPassings = new ArrayList<MarkPassing>();
            for (Waypoint waypoint : course.getWaypoints()) {
                MarkPassing passing = passingsByWaypoint.get(waypoint);
                if (passing != null) {
                    markPassings.add(passing);
                }
            }
            logger.fine("Received mark passings in race "+trackedRace.getRace().getName()+": "+markPassings);
            if (getSimulator() != null) {
                getSimulator().delayMarkPassings(getDomainFactory().getOrCreateCompetitor(event.getA().getCompetitor()), markPassings);
            } else {
                trackedRace.updateMarkPassings(getDomainFactory().getOrCreateCompetitor(event.getA().getCompetitor()),
                        markPassings);
            }
        } else {
            logger.warning("Couldn't find tracked race for race " + event.getA().getRace().getName()
                    + ". Dropping mark passing event " + event);
        }
    }

    /**
     * Starts searching in <code>waypointsIter</code> for a waypoint that has the given <code>controlPoint</code>.
     * The <code>waypointsIter</code> is advanced to that point or to the point where <code>waypointsIter.hasNext()</code>
     * returns <code>false</code>.
     */
    private Waypoint findWaypointForControlPoint(TrackedRace trackedRace, Iterator<Waypoint> waypointsIter,
            com.sap.sailing.domain.base.ControlPoint domainControlPoint, Competitor competitor) {
        while (waypointsIter.hasNext()) {
            Waypoint waypoint = waypointsIter.next();
            if (waypoint.getControlPoint() == domainControlPoint) {
                return waypoint;
            }
        }
        return null;
    }
}
