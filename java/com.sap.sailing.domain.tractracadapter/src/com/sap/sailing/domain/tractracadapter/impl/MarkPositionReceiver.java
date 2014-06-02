package com.sap.sailing.domain.tractracadapter.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sse.common.Util;
import com.tractrac.clientmodule.ControlPoint;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.data.ControlPointPositionData;
import com.tractrac.clientmodule.data.ICallbackData;

/**
 * The positions of the {@link ControlPoint}s of a {@link Course} are received dynamically through a callback interface.
 * Therefore, when connected to an {@link Regatta}, and even after receiving the order of the marks for a race course,
 * these orders are not yet defined. An instance of this class can be used to create the listeners needed to receive
 * this information and set it on an {@link Regatta}'s {@link ControlPoint}s.
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
    
    private int received;

    /**
     * if <code>null</code>, all stored data from the "beginning of time" will be loaded that the event has to provide,
     * particularly for the mark positions which are stored per event, not per race; otherwise, particularly the mark
     * position loading will be constrained to this start time.
     */
    private final TimePoint startOfTracking;

    /**
     * if <code>null</code>, all stored data until the "end of time" will be loaded that the event has to provide,
     * particularly for the mark positions which are stored per event, not per race; otherwise, particularly the mark
     * position loading will be constrained to this end time.
     */
    private final TimePoint endOfTracking;

    /**
     * @param startOfTracking
     *            if <code>null</code>, all stored data from the "beginning of time" will be loaded that the event has
     *            to provide, particularly for the mark positions which are stored per event, not per race; otherwise,
     *            particularly the mark position loading will be constrained to this start time.
     * @param endOfTracking
     *            if <code>null</code>, all stored data until the "end of time" will be loaded that the event has to
     *            provide, particularly for the mark positions which are stored per event, not per race; otherwise,
     *            particularly the mark position loading will be constrained to this end time.
     */
    public MarkPositionReceiver(final DynamicTrackedRegatta trackedRegatta, com.tractrac.clientmodule.Event tractracEvent,
            TimePoint startOfTracking, TimePoint endOfTracking, Simulator simulator, final DomainFactory domainFactory) {
        super(domainFactory, tractracEvent, trackedRegatta, simulator);
        this.startOfTracking = startOfTracking;
        this.endOfTracking = endOfTracking;
        // assumption: there is currently only one race per TracTrac Event object
        if (tractracEvent.getRaceList().isEmpty()) {
            throw new IllegalArgumentException("Can't receive mark positions from event "+tractracEvent.getName()+" that has no race");
        }
    }
    
    /**
     * The listeners returned will, when added to a controller, receive events about the
     * position changes of marks during a race. Receiving such an event updates the mark's
     * {@link GPSFixTrack} in the {@link TrackedRegatta}. Starts a thread for this receiver,
     * blocking for events received.
     */
    @Override
    public Iterable<TypeController> getTypeControllersAndStart() {
        List<TypeController> result = new ArrayList<TypeController>();
        TimePoint now = MillisecondsTimePoint.now();
        long fromTime = startOfTracking == null ? 0l : startOfTracking.compareTo(now) > 0 ? now.asMillis() : startOfTracking.asMillis();
        long toTime = endOfTracking == null ? Long.MAX_VALUE : endOfTracking.asMillis();
        Collection<Race> races = getTracTracEvent().getRaceList();
        for (Race race : races) {
            logger.info("Subscribing to ControlPointPositionData for time range "+new MillisecondsTimePoint(fromTime)+"/"+
                new MillisecondsTimePoint(toTime) + " for event " + getTracTracEvent().getName() + " " + race.getName());
        }
        TypeController controlPointListener = ControlPointPositionData.subscribe(getTracTracEvent(),
                new ICallbackData<ControlPoint, ControlPointPositionData>() {
                    @Override
                    public void gotData(ControlPoint controlPoint, ControlPointPositionData record, boolean isLiveData) {
                        enqueue(new Util.Triple<ControlPoint, ControlPointPositionData, Boolean>(controlPoint, record, isLiveData));
                    }
                }, fromTime, toTime);
        result.add(controlPointListener);
        setAndStartThread(new Thread(this, getClass().getName()));
        return result;
    }

    @Override
    protected void handleEvent(Util.Triple<ControlPoint, ControlPointPositionData, Boolean> event) {
        if (received++ % 1000 == 0) {
            System.out.print("M");
            if ((received / 1000 + 1) % 80 == 0) {
                System.out.println();
            }
        }
        Mark mark = getDomainFactory().getMark(new ControlPointAdapter(event.getA()), event.getB().getIndex());
        for (Race tractracRace : getTracTracEvent().getRaceList()) {
            DynamicTrackedRace trackedRace = getTrackedRace(tractracRace);
            if (trackedRace != null) {
                GPSFixMoving markPosition = getDomainFactory().createGPSFixMoving(event.getB());
                if (getSimulator() != null) {
                    getSimulator().scheduleMarkPosition(mark, markPosition);
                } else {
                    trackedRace.recordFix(mark, markPosition);
                }
            } else {
                logger.warning("Couldn't find tracked race for race " + tractracRace.getName()
                        + ". Dropping mark position event " + event);
            }
        }
    }

}
