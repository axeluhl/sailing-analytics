package com.sap.sailing.domain.tractracadapter.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.util.Util.Triple;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.Route;
import com.tractrac.clientmodule.data.ICallbackData;
import com.tractrac.clientmodule.data.RouteData;

import difflib.PatchFailedException;

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
    private final static Logger logger = Logger.getLogger(RaceCourseReceiver.class.getName());
    
    private final TrackedEvent trackedEvent;
    private final com.tractrac.clientmodule.Event tractracEvent;
    private final long millisecondsOverWhichToAverageWind;
    private final long millisecondsOverWhichToAverageSpeed;
    private final WindStore windStore;
    private final Object tokenToRetrieveAssociatedRace;
    
    public RaceCourseReceiver(DomainFactory domainFactory, TrackedEvent trackedEvent,
            com.tractrac.clientmodule.Event tractracEvent, WindStore windStore,
            Object tokenToRetrieveAssociatedRace,
            long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed) {
        super(domainFactory);
        this.trackedEvent = trackedEvent;
        this.tractracEvent = tractracEvent;
        this.millisecondsOverWhichToAverageWind = millisecondsOverWhichToAverageWind;
        this.millisecondsOverWhichToAverageSpeed = millisecondsOverWhichToAverageSpeed;
        this.windStore = windStore;
        this.tokenToRetrieveAssociatedRace = tokenToRetrieveAssociatedRace;
    }

    /**
     * The listeners returned will, when added to a controller, receive events about the
     * course definition of a race. When this happens, a new {@link RaceDefinition} is
     * created with the respective {@link Course} and added to the {@link #event event}.
     */
    @Override
    public Iterable<TypeController> getTypeControllersAndStart() {
        List<TypeController> result = new ArrayList<TypeController>();
        for (final Race race : tractracEvent.getRaceList()) {
            TypeController routeListener = RouteData.subscribe(race, new ICallbackData<Route, RouteData>() {
                @Override
                public void gotData(Route route, RouteData record, boolean isLiveData) {
                    enqueue(new Triple<Route, RouteData, Race>(route, record, race));
                }
            });
            setAndStartThread(new Thread(this, getClass().getName()));
            result.add(routeListener);
        }
        return result;
    }
    
    @Override
    protected void handleEvent(Triple<Route, RouteData, Race> event) {
        System.out.print("R");
        Course course = getDomainFactory().createCourse(event.getA().getName(), event.getB().getPoints());
        RaceDefinition existingRaceDefinitionForRace = getDomainFactory().getExistingRaceDefinitionForRace(event.getC());
        if (existingRaceDefinitionForRace != null) {
            logger.log(Level.INFO, "Received course update for existing race "+event.getC().getName());
            // race already exists; this means that we obviously found a course re-definition (yuck...)
            // Therefore, don't create TrackedRace again because it already exists.
            try {
                getDomainFactory().updateCourseWaypoints(course, event.getB().getPoints());
                if (trackedEvent.getExistingTrackedRace(existingRaceDefinitionForRace) == null) {
                    createTrackedRace(existingRaceDefinitionForRace);
                }
            } catch (PatchFailedException e) {
                logger.log(Level.SEVERE, "Internal error updating race course "+course+": "+e.getMessage());
                logger.throwing(RaceCourseReceiver.class.getName(), "handleEvent", e);
            }
        } else {
            logger.log(Level.INFO, "Received course for non-existing race "+event.getC().getName()+". Creating RaceDefinition.");
            // create race definition
            RaceDefinition raceDefinition = getDomainFactory().createRaceDefinition(event.getC(), course);
            // add race only if boat class matches
            if (raceDefinition.getBoatClass() == trackedEvent.getEvent().getBoatClass()) {
                trackedEvent.getEvent().addRace(raceDefinition);
                createTrackedRace(raceDefinition);
            } else {
                logger.warning("Not adding race "+raceDefinition+" to event "+trackedEvent.getEvent()+
                        " because boat class "+raceDefinition.getBoatClass()+" doesn't match event's boat class "+
                        trackedEvent.getEvent().getBoatClass());
            }
        }
    }

    private void createTrackedRace(RaceDefinition race) {
        DynamicTrackedRace trackedRace = getDomainFactory().trackRace(trackedEvent, race,
                windStore, millisecondsOverWhichToAverageWind, millisecondsOverWhichToAverageSpeed, tractracEvent,
                tokenToRetrieveAssociatedRace);
        trackedEvent.addTrackedRace(trackedRace);
    }

}
