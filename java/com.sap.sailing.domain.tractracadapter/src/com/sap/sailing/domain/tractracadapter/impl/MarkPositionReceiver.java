package com.sap.sailing.domain.tractracadapter.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.util.Util.Triple;
import com.tractrac.clientmodule.ControlPoint;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.data.ControlPointPositionData;
import com.tractrac.clientmodule.data.ICallbackData;

/**
 * The positions of the {@link ControlPoint}s of a {@link Course} are received dynamically through a callback interface.
 * Therefore, when connected to an {@link Event}, and even after receiving the order of the marks for a race course,
 * these orders are not yet defined. An instance of this class can be used to create the listeners needed to receive
 * this information and set it on an {@link Event}'s {@link ControlPoint}s.
 * <p>
 * 
 * As a {@link MarkPositionReceiver} requires a tracked race in order to update the mark positions received, and since a
 * {@link TrackedRace} depends on the {@link RaceDefinition} which in turn depends on having received the race course
 * description, at least a {@link RaceCourseReceiver} has to be active as well. Otherwise, the first call to
 * {@link #getTrackedRace} will block forever.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class MarkPositionReceiver extends AbstractReceiverWithQueue<ControlPoint, ControlPointPositionData, Boolean> {
    private static final Logger logger = Logger.getLogger(MarkPositionReceiver.class.getName());
    
    private TrackedRace trackedRace;
    private final com.tractrac.clientmodule.Event tractracEvent;
    private int received;
    
    public MarkPositionReceiver(final TrackedEvent trackedEvent, com.tractrac.clientmodule.Event tractracEvent, final DomainFactory domainFactory) {
        super();
        this.tractracEvent = tractracEvent;
        // assumption: there is currently only one race per TracTrac Event object
        final Race race = tractracEvent.getRaceList().iterator().next();
        new Thread("MarkPositionReceiver waiting for RaceDefinition for "+race.getName()) {
            public void run() {
                RaceDefinition raceDefinition = domainFactory.getRaceDefinition(race);
                // the following call blocks until a tracked race for the race definition was entered into the tracked event
                TrackedRace blockingTrackedRace = trackedEvent.getTrackedRace(raceDefinition);
                synchronized(MarkPositionReceiver.this) {
                    trackedRace = blockingTrackedRace;
                    MarkPositionReceiver.this.notifyAll();
                }
            }
        }.start();
    }
    
    private synchronized TrackedRace getTrackedRaceBlocking() {
        while (trackedRace == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.warning("Interrupted wait for trackedRace: "+e.getMessage()+". Continuing to wait.");
            }
        }
        return trackedRace;
    }

    /**
     * The listeners returned will, when added to a controller, receive events about the
     * position changes of marks during a race. Receiving such an event updates the Buoy's
     * {@link GPSFixTrack} in the {@link TrackedEvent}. Starts a thread for this receiver,
     * blocking for events received.
     */
    @Override
    public Iterable<TypeController> getTypeControllers() {
        List<TypeController> result = new ArrayList<TypeController>();
        TypeController controlPointListener = ControlPointPositionData.subscribe(tractracEvent,
                new ICallbackData<ControlPoint, ControlPointPositionData>() {
                    @Override
                    public void gotData(ControlPoint controlPoint, ControlPointPositionData record, boolean isLiveData) {
                        enqueue(new Triple<ControlPoint, ControlPointPositionData, Boolean>(controlPoint, record, isLiveData));
                    }
                }, /* fromTime */0l, /* toTime */Long.MAX_VALUE);
        result.add(controlPointListener);
        new Thread(this, getClass().getName()).start();
        return result;
    }

    @Override
    protected void handleEvent(Triple<ControlPoint, ControlPointPositionData, Boolean> event) {
        if (received++ % 1000 == 0) {
            System.out.print("M");
            if ((received / 1000 + 1) % 80 == 0) {
                System.out.println();
            }
        }
        Buoy buoy = DomainFactory.INSTANCE.getBuoy(event.getA(), event.getB());
        // FIXME during getTrackedRaceBlocking it seems as if trackedRace gets assigned a new, invalid instance
        ((DynamicTrack<Buoy, GPSFix>) getTrackedRaceBlocking().getTrack(buoy)).addGPSFix(DomainFactory.INSTANCE
                .createGPSFixMoving(event.getB()));
    }

}
