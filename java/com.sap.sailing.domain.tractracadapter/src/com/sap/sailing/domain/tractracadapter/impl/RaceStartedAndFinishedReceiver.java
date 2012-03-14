package com.sap.sailing.domain.tractracadapter.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.data.ICallbackData;
import com.tractrac.clientmodule.data.StartStopTimesData;

/**
 * The ordering of the {@link ControlPoint}s of a {@link Course} are received
 * dynamically through a callback interface. Therefore, when connected to an
 * {@link Event}, these orders are not yet defined. An instance of this class
 * can be used to create the listeners needed to receive this information and
 * set it on an {@link Event}.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class RaceStartedAndFinishedReceiver extends AbstractReceiverWithQueue<Race, StartStopTimesData, Boolean> {
    private static final Logger logger = Logger.getLogger(RaceStartedAndFinishedReceiver.class.getName());

    public RaceStartedAndFinishedReceiver(DynamicTrackedEvent trackedEvent, com.tractrac.clientmodule.Event tractracEvent, DomainFactory domainFactory) {
        super(domainFactory, tractracEvent, trackedEvent);
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
            MillisecondsTimePoint start = new MillisecondsTimePoint(event.getB().getStartTime());
            if (trackedRace.getStart() == null || !trackedRace.getStart().equals(start)) {
                trackedRace.setStartTimeReceived(start);
            }
            // TODO forward race stop time, event.getB().getStopTime()
        } else {
            logger.warning("Couldn't find tracked race for race " + event.getA().getName()
                    + ". Dropping start/stop event " + event);
        }
    }

}
