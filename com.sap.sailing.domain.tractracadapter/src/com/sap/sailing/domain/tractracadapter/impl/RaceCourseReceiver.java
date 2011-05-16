package com.sap.sailing.domain.tractracadapter.impl;

import java.util.ArrayList;
import java.util.List;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
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
public class RaceCourseReceiver {
    private final TrackedEvent trackedEvent;
    private final com.tractrac.clientmodule.Event tractracEvent;
    
    public RaceCourseReceiver(TrackedEvent trackedEvent, com.tractrac.clientmodule.Event tractracEvent) {
        super();
        this.trackedEvent = trackedEvent;
        this.tractracEvent = tractracEvent;
    }

    /**
     * The listeners returned will, when added to a controller, receive events about the
     * course definition of a race. When this happens, a new {@link RaceDefinition} is
     * created with the respective {@link Course} and added to the {@link #event event}.
     */
    public Iterable<TypeController> getRouteListeners() {
        List<TypeController> result = new ArrayList<TypeController>();
        for (final Race race : tractracEvent.getRaceList()) {
            TypeController routeListener = RouteData.subscribe(race,
                    new ICallbackData<Route, RouteData>() {
                        @Override
                        public void gotData(Route route, RouteData record) {
                            Course course = DomainFactory.INSTANCE.createCourse(route.getName(), record.getPoints());
                            RaceDefinition raceDefinition = DomainFactory.INSTANCE.createRaceDefinition(race, course);
                            trackedEvent.getEvent().addRace(raceDefinition);
                            trackedEvent.addTrackedRace(new DynamicTrackedRaceImpl(raceDefinition));
                        }
                    });
            result.add(routeListener);
        }
        return result;
    }

}
