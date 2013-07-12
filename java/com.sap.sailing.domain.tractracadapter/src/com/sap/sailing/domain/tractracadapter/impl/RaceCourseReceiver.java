package com.sap.sailing.domain.tractracadapter.impl;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
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
        final LinkedHashMap<com.tractrac.clientmodule.ControlPoint, TracTracControlPoint> ttControlPointsForOriginalControlPoints = new LinkedHashMap<>();
        for (com.tractrac.clientmodule.ControlPoint cp : event.getB().getPoints()) {
            ttControlPointsForOriginalControlPoints.put(cp, new ControlPointAdapter(cp));
        }
        Map<Integer, NauticalSide> courseWaypointPassingSides = parseAdditionalCourseDataFromMetadata(ttControlPointsForOriginalControlPoints.values(), routeMetadataString);
        List<Util.Pair<TracTracControlPoint, NauticalSide>> ttControlPoints = new ArrayList<>();
        int i = 1;
        for (com.tractrac.clientmodule.ControlPoint cp : event.getB().getPoints()) {
            NauticalSide nauticalSide = courseWaypointPassingSides.containsKey(i) ? courseWaypointPassingSides.get(i) : null;
            ttControlPoints.add(new Pair<TracTracControlPoint, NauticalSide>(ttControlPointsForOriginalControlPoints.get(cp), nauticalSide));
            i++;
        }
        Course course = getDomainFactory().createCourse(route.getName(), ttControlPoints);
        List<Sideline> sidelines = new ArrayList<Sideline>();
        Map<String, Iterable<TracTracControlPoint>> sidelinesMetadata = parseSidelinesFromRaceMetadata(event.getC(),
                event.getC().getEvent().getControlPointList());
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
    
    /**
     * Parses the route metadata for additional course information
     * The 'passing side' for each course waypoint is encoded like this...
     * <pre>
     *  Seq.1=GATE
     *  Seq.2=PORT
     *  Seq.3=GATE
     *  Seq.4=STARBOARD
     * </pre>
     */
    private Map<Integer, NauticalSide> parseAdditionalCourseDataFromMetadata(Iterable<TracTracControlPoint> controlPoints,
            String routeMetadataString) {
        Map<Integer, NauticalSide> result = new HashMap<Integer, NauticalSide>();
        if (routeMetadataString != null) {
            Map<String, String> routeMetadata = parseMetadata(routeMetadataString);
            Iterator<TracTracControlPoint> iter = controlPoints.iterator();
            int i=1;
            while (iter.hasNext()) {
                String seqValue = routeMetadata.get("Seq." + i);
                TracTracControlPoint controlPoint = iter.next();
                if (!controlPoint.getHasTwoPoints() && seqValue != null) {
                    if ("PORT".equalsIgnoreCase(seqValue)) {
                        result.put(i, NauticalSide.PORT);
                    } else if ("STARBOARD".equalsIgnoreCase(seqValue)) {
                        result.put(i, NauticalSide.STARBOARD);
                    }
                }
                i++;
            }
        }
        return result;
    }
    
    /**
     * Parses the race metadata for sideline information
     * The sidelines of a race (course) are encoded like this...
     * <pre>
     *  SIDELINE1=(TR-A) 3
     *  SIDELINE2=(TR-A) Start
     * </pre>
     * Each sideline is defined right now through a simple gate, but this might change in the future
     */
    private Map<String, Iterable<TracTracControlPoint>> parseSidelinesFromRaceMetadata(Race race,
            Collection<com.tractrac.clientmodule.ControlPoint> controlPoints) {
        Map<String, Iterable<TracTracControlPoint>> result = new HashMap<String, Iterable<TracTracControlPoint>>();
        String raceMetadataString = race.getMetadata() != null ? race.getMetadata().getText() : null;
        if (raceMetadataString != null) {
            Map<String, String> sidelineMetadata = parseMetadata(raceMetadataString);
            for (Entry<String, String> entry : sidelineMetadata.entrySet()) {
                if (entry.getKey().startsWith("SIDELINE")) {
                    List<TracTracControlPoint> sidelineCPs = new ArrayList<>();
                    result.put(entry.getKey(), sidelineCPs);
                    for (com.tractrac.clientmodule.ControlPoint cp : controlPoints) {
                        String cpName = cp.getName().trim();
                        if (cpName.equals(entry.getValue())) {
                            sidelineCPs.add(new ControlPointAdapter(cp));
                        }
                    }
                }
            }
        }
        return result;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map<String, String> parseMetadata(String metadata) {
        Map<String, String> metadataMap = new HashMap<String, String>();
        try {
            Properties p = new Properties();
            p.load(new StringReader(metadata));
            metadataMap = new HashMap<String, String>((Map) p);
        } catch (IOException e) {
            // do nothing
        }
        return metadataMap;
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
