package com.sap.sailing.domain.tractracadapter.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.RaceCompetitor;
import com.tractrac.clientmodule.data.ICallbackData;
import com.tractrac.clientmodule.data.MarkPassingsData;

public class MarkPassingReceiver extends AbstractReceiverWithQueue<RaceCompetitor, MarkPassingsData, Boolean> {
    private final DynamicTrackedEvent trackedEvent;
    private final com.tractrac.clientmodule.Event tractracEvent;
    
    public MarkPassingReceiver(DynamicTrackedEvent trackedEvent, com.tractrac.clientmodule.Event tractracEvent) {
        super();
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
        DynamicTrackedRace trackedRace = trackedEvent.getTrackedRace(DomainFactory.INSTANCE.getRaceDefinition(event
                .getA().getRace()));
        Course course = trackedRace.getRace().getCourse();
        Iterator<Waypoint> waypointIter = course.getWaypoints().iterator();
        List<MarkPassing> markPassings = new ArrayList<MarkPassing>();
        // Note: the entries always describe all mark passings for the competitor so far in the current race in order
        for (MarkPassingsData.Entry passing : event.getB().getPassings()) {
            Waypoint passed = waypointIter.next();
            TimePoint time = new MillisecondsTimePoint(passing.getTimestamp());
            MarkPassing markPassing = DomainFactory.INSTANCE.createMarkPassing(event.getA().getCompetitor(), passed, time);
            markPassings.add(markPassing);
        }
        trackedRace.updateMarkPassings(DomainFactory.INSTANCE.getCompetitor(event.getA().getCompetitor()), markPassings);
    }
}
