package com.sap.sailing.domain.tractracadapter.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.TracTracControlPoint;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.Route;
import com.tractrac.clientmodule.data.ICallbackData;
import com.tractrac.clientmodule.data.RouteData;

import difflib.PatchFailedException;

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
public class RaceCourseReceiver extends AbstractReceiverWithQueue<Route, RouteData, Race>  {
    private final static Logger logger = Logger.getLogger(RaceCourseReceiver.class.getName());
    
    private final long millisecondsOverWhichToAverageWind;
    private final long delayToLiveInMillis;
    private final WindStore windStore;
    private final DynamicRaceDefinitionSet raceDefinitionSetToUpdate;
    private final URI courseDesignUpdateURI;
    private final String tracTracUsername;
    private final String tracTracPassword;
    
    public RaceCourseReceiver(DomainFactory domainFactory, DynamicTrackedRegatta trackedRegatta,
            com.tractrac.clientmodule.Event tractracEvent, WindStore windStore,
            DynamicRaceDefinitionSet raceDefinitionSetToUpdate, long delayToLiveInMillis,
            long millisecondsOverWhichToAverageWind, Simulator simulator, URI courseDesignUpdateURI, String tracTracUsername, String tracTracPassword) {
        super(domainFactory, tractracEvent, trackedRegatta, simulator);
        this.millisecondsOverWhichToAverageWind = millisecondsOverWhichToAverageWind;
        this.delayToLiveInMillis = delayToLiveInMillis;
        if (simulator == null) {
            this.windStore = windStore;
        } else {
            this.windStore = simulator.simulatingWindStore(windStore);
        }
        this.raceDefinitionSetToUpdate = raceDefinitionSetToUpdate;
        this.courseDesignUpdateURI = courseDesignUpdateURI;
        this.tracTracUsername = tracTracUsername;
        this.tracTracPassword = tracTracPassword;
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
        final Route route = event.getA();
        final String routeMetadataString = route.getMetadata() != null ? route.getMetadata().getText() : null;
        final LinkedHashMap<com.tractrac.clientmodule.ControlPoint, TracTracControlPoint> ttControlPointsForAllOriginalEventControlPoints = new LinkedHashMap<>();
        for (com.tractrac.clientmodule.ControlPoint cp : event.getC().getEvent().getControlPointList()) {
            ttControlPointsForAllOriginalEventControlPoints.put(cp, new ControlPointAdapter(cp));
        }
        final List<TracTracControlPoint> routeControlPoints = new ArrayList<>();
        for (com.tractrac.clientmodule.ControlPoint cp : event.getB().getPoints()) {
            routeControlPoints.add(ttControlPointsForAllOriginalEventControlPoints.get(cp));
        }
        Map<Integer, NauticalSide> courseWaypointPassingSides = getDomainFactory().getMetadataParser().parsePassingSideData(routeMetadataString, routeControlPoints);
        List<Util.Pair<TracTracControlPoint, NauticalSide>> ttControlPoints = new ArrayList<>();
        int i = 1;
        for (com.tractrac.clientmodule.ControlPoint cp : event.getB().getPoints()) {
            NauticalSide nauticalSide = courseWaypointPassingSides.containsKey(i) ? courseWaypointPassingSides.get(i) : null;
            ttControlPoints.add(new Pair<TracTracControlPoint, NauticalSide>(ttControlPointsForAllOriginalEventControlPoints.get(cp), nauticalSide));
            i++;
        }
        Course course = getDomainFactory().createCourse(route.getName(), ttControlPoints);
        List<Sideline> sidelines = new ArrayList<Sideline>();
        Race race = event.getC();
        Map<String, Iterable<TracTracControlPoint>> sidelinesMetadata = getDomainFactory().getMetadataParser().parseSidelinesFromRaceMetadata(
                race.getMetadata() != null ? race.getMetadata().getText() : null, ttControlPointsForAllOriginalEventControlPoints.values());
        for (Entry<String, Iterable<TracTracControlPoint>> sidelineEntry : sidelinesMetadata.entrySet()) {
            if (Util.size(sidelineEntry.getValue()) > 0) {
                sidelines.add(getDomainFactory().createSideline(sidelineEntry.getKey(), sidelineEntry.getValue()));
            }
        }

        RaceDefinition existingRaceDefinitionForRace = getDomainFactory().getExistingRaceDefinitionForRace(event.getC());
        if (existingRaceDefinitionForRace != null) {
            logger.log(Level.INFO, "Received course update for existing race "+event.getC().getName()+": "+
                    event.getB().getPoints());
            // Race already exists; this means that we obviously found a course change.
            // Therefore, don't create TrackedRace again because it already exists.
            try {
                getDomainFactory().updateCourseWaypoints(existingRaceDefinitionForRace.getCourse(), ttControlPoints);
                if (getTrackedRegatta().getExistingTrackedRace(existingRaceDefinitionForRace) == null) {
                    createTrackedRace(existingRaceDefinitionForRace, sidelines);
                }
            } catch (PatchFailedException e) {
                logger.log(Level.SEVERE, "Internal error updating race course "+course+": "+e.getMessage());
                logger.log(Level.SEVERE, "handleEvent", e);
            }
        } else {
            logger.log(Level.INFO, "Received course for non-existing race "+event.getC().getName()+". Creating RaceDefinition.");
            // create race definition and add to event
            DynamicTrackedRace trackedRace = getDomainFactory().getOrCreateRaceDefinitionAndTrackedRace(
                    getTrackedRegatta(), event.getC(), course, sidelines, windStore, delayToLiveInMillis,
                    millisecondsOverWhichToAverageWind, raceDefinitionSetToUpdate, courseDesignUpdateURI, 
                    getTracTracEvent().getId(), tracTracUsername, tracTracPassword);
            if (getSimulator() != null) {
                getSimulator().setTrackedRace(trackedRace);
            }
        }
    }
    
    private void createTrackedRace(RaceDefinition race, Iterable<Sideline> sidelines) {
        DynamicTrackedRace trackedRace = getTrackedRegatta().createTrackedRace(race, sidelines,
                windStore, delayToLiveInMillis, millisecondsOverWhichToAverageWind,
                /* time over which to average speed: */ race.getBoatClass().getApproximateManeuverDurationInMilliseconds(),
                raceDefinitionSetToUpdate);
        
        CourseDesignChangedByRaceCommitteeHandler courseDesignHandler = new CourseDesignChangedByRaceCommitteeHandler(courseDesignUpdateURI, 
                tracTracUsername, tracTracPassword,
                getTracTracEvent().getId(), race.getId());
        trackedRace.addCourseDesignChangedListener(courseDesignHandler);
    }

}
