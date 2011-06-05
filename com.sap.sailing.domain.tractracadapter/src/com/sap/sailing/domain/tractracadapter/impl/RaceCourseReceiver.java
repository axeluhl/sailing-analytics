package com.sap.sailing.domain.tractracadapter.impl;

import java.util.ArrayList;
import java.util.List;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.impl.Util.Triple;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.Route;
import com.tractrac.clientmodule.data.ICallbackData;
import com.tractrac.clientmodule.data.RouteData;

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
public class RaceCourseReceiver extends AbstractReceiverWithQueue<Route, RouteData, Race>  {
    private final TrackedEvent trackedEvent;
    private final com.tractrac.clientmodule.Event tractracEvent;
    private final long millisecondsOverWhichToAverageWind;
    private final long millisecondsOverWhichToAverageSpeed;
    
    public RaceCourseReceiver(TrackedEvent trackedEvent, com.tractrac.clientmodule.Event tractracEvent,
            long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed) {
        super();
        this.trackedEvent = trackedEvent;
        this.tractracEvent = tractracEvent;
        this.millisecondsOverWhichToAverageWind = millisecondsOverWhichToAverageWind;
        this.millisecondsOverWhichToAverageSpeed = millisecondsOverWhichToAverageSpeed;
    }

    /**
     * The listeners returned will, when added to a controller, receive events about the
     * course definition of a race. When this happens, a new {@link RaceDefinition} is
     * created with the respective {@link Course} and added to the {@link #event event}.
     */
    @Override
    public Iterable<TypeController> getTypeControllers() {
        List<TypeController> result = new ArrayList<TypeController>();
        for (final Race race : tractracEvent.getRaceList()) {
            TypeController routeListener = RouteData.subscribe(race, new ICallbackData<Route, RouteData>() {
                @Override
                public void gotData(Route route, RouteData record, boolean isLiveData) {
                    enqueue(new Triple<Route, RouteData, Race>(route, record, race));
                }
            });
            new Thread(this, getClass().getName()).start();
            result.add(routeListener);
        }
        return result;
    }
    
    @Override
    protected void handleEvent(Triple<Route, RouteData, Race> event) {
        System.out.print("R");
        // FIXME we learned by e-mail from Lasse (2011-06-04T20:38:00CET) that courses may change during a race; how to handle???
        Course course = DomainFactory.INSTANCE.createCourse(event.getA().getName(), event.getB().getPoints());
        RaceDefinition raceDefinition = DomainFactory.INSTANCE.createRaceDefinition(event.getC(), course);
        trackedEvent.getEvent().addRace(raceDefinition);
        DynamicTrackedRace trackedRace = DomainFactory.INSTANCE.trackRace(trackedEvent, raceDefinition,
                millisecondsOverWhichToAverageWind, millisecondsOverWhichToAverageSpeed, tractracEvent);
        trackedEvent.addTrackedRace(trackedRace);
    }

}
