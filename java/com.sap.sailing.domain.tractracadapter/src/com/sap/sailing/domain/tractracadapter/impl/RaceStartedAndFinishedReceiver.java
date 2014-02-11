package com.sap.sailing.domain.tractracadapter.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.data.ICallbackData;
import com.tractrac.clientmodule.data.StartStopTimesData;

/**
 * The ordering of the {@link ControlPoint}s of a {@link Course} are received
 * dynamically through a callback interface. Therefore, when connected to an
 * {@link Regatta}, these orders are not yet defined. An instance of this class
 * can be used to create the listeners needed to receive this information and
 * set it on an {@link Regatta}.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class RaceStartedAndFinishedReceiver extends AbstractReceiverWithQueue<Race, StartStopTimesData, Boolean> {
    private static final Logger logger = Logger.getLogger(RaceStartedAndFinishedReceiver.class.getName());

    public RaceStartedAndFinishedReceiver(DynamicTrackedRegatta trackedRegatta,
            com.tractrac.clientmodule.Event tractracEvent, Simulator simulator, DomainFactory domainFactory) {
        super(domainFactory, tractracEvent, trackedRegatta, simulator, eventSubscriber, raceSubscriber);
    }

    /**
     * The listeners returned will, when added to a controller, receive events about the
     * course definition of a race. When this happens, a new {@link RaceDefinition} is
     * created with the respective {@link Course} and added to the {@link #event event}.
     */
    @Override
    public Iterable<TypeController> getTypeControllersAndStart() {
        List<TypeController> result = new ArrayList<TypeController>();
        for (final Race race : getTracTracEvent().getRaceList()) {
            TypeController startStopListener = StartStopTimesData.subscribeRace(race, new ICallbackData<Race, StartStopTimesData>() {
                @Override
                public void gotData(Race race, StartStopTimesData record, boolean isLiveData) {
                    enqueue(new Triple<Race, StartStopTimesData, Boolean>(race, record, isLiveData));
                }
            });
            result.add(startStopListener);
        }
        setAndStartThread(new Thread(this, getClass().getName()));
        return result;
    }

    @Override
    protected void handleEvent(Triple<Race, StartStopTimesData, Boolean> event) {
        System.out.print("StartStop");
        DynamicTrackedRace trackedRace = getTrackedRace(event.getA());
        if (trackedRace != null) {
            StartStopTimesData startStopTimesData = event.getB();
            if (startStopTimesData != null) {
                final long startTime = startStopTimesData.getStartTime();
                TimePoint startOfTracking = getSimulator() == null ?
                        new MillisecondsTimePoint(startTime) :
                            getSimulator().advance(new MillisecondsTimePoint(startTime));
                if (startTime > 0) {
                    trackedRace.setStartOfTrackingReceived(startOfTracking);
                }
                final long stopTime = startStopTimesData.getStopTime();
                TimePoint endOfTracking = getSimulator() == null ?
                        new MillisecondsTimePoint(stopTime) :
                            getSimulator().advance(new MillisecondsTimePoint(stopTime));
                if (stopTime > 0) {
                    trackedRace.setEndOfTrackingReceived(endOfTracking);
                }
            }
        } else {
            logger.warning("Couldn't find tracked race for race " + event.getA().getName()
                    + ". Dropping start/stop event " + event);
        }
    }

}
