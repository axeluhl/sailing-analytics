package com.sap.sailing.domain.tractracadapter.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.markpassingcalculation.MarkPassingCalculator;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.TracTracControlPoint;
import com.sap.sailing.domain.tractracadapter.impl.ClientParamsPHP.Race;
import com.tractrac.model.lib.api.event.IEvent;
import com.tractrac.model.lib.api.event.IRace;
import com.tractrac.model.lib.api.route.IControl;
import com.tractrac.model.lib.api.route.IControlPoint;
import com.tractrac.model.lib.api.route.IRoute;
import com.tractrac.subscription.lib.api.IEventSubscriber;
import com.tractrac.subscription.lib.api.IRaceSubscriber;

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
public class RaceCourseReceiver extends AbstractReceiverWithQueue<IRoute, RouteData, IRace>  {
    private final static Logger logger = Logger.getLogger(RaceCourseReceiver.class.getName());
    
    private final long millisecondsOverWhichToAverageWind;
    private final long delayToLiveInMillis;
    private final WindStore windStore;
    private final DynamicRaceDefinitionSet raceDefinitionSetToUpdate;
    private final URI tracTracUpdateURI;
    private final String tracTracUsername;
    private final String tracTracPassword;
    
    public RaceCourseReceiver(DomainFactory domainFactory, DynamicTrackedRegatta trackedRegatta,
            IEvent tractracEvent, WindStore windStore,
            DynamicRaceDefinitionSet raceDefinitionSetToUpdate, long delayToLiveInMillis,
            long millisecondsOverWhichToAverageWind, Simulator simulator, URI courseDesignUpdateURI, String tracTracUsername, String tracTracPassword, IEventSubscriber eventSubscriber, IRaceSubscriber raceSubscriber) {
        super(domainFactory, tractracEvent, trackedRegatta, simulator, eventSubscriber, raceSubscriber);
        this.millisecondsOverWhichToAverageWind = millisecondsOverWhichToAverageWind;
        this.delayToLiveInMillis = delayToLiveInMillis;
        if (simulator == null) {
            this.windStore = windStore;
        } else {
            this.windStore = simulator.simulatingWindStore(windStore);
        }
        this.raceDefinitionSetToUpdate = raceDefinitionSetToUpdate;
        this.tracTracUpdateURI = courseDesignUpdateURI;
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
            TypeController routeListener = RouteData.subscribe();
            setAndStartThread(new Thread(this, getClass().getName()));
            result.add(routeListener);
        }
        return result;
    }
    
    @Override
    protected void handleEvent(Triple<IRoute, RouteData, IRace> event) {
        System.out.print("R");
        final IRoute route = event.getA();
        final String routeMetadataString = route.getMetadata() != null ? route.getMetadata().getText() : null;
        final LinkedHashMap<IControlPoint, TracTracControlPoint> ttControlPointsForAllOriginalEventControlPoints = new LinkedHashMap<>();
        for (IControl cp : event.getC().getEvent().getControlPointList()) {
            ttControlPointsForAllOriginalEventControlPoints.put(cp, new ControlPointAdapter(cp));
        }
        final List<TracTracControlPoint> routeControlPoints = new ArrayList<>();
        for (IControl cp : event.getB().getPoints()) {
            routeControlPoints.add(ttControlPointsForAllOriginalEventControlPoints.get(cp));
        }
        Map<Integer, PassingInstruction> courseWaypointPassingInstructions = getDomainFactory().getMetadataParser().parsePassingInstructionData(routeMetadataString, routeControlPoints);
        List<Util.Pair<TracTracControlPoint, PassingInstruction>> ttControlPoints = new ArrayList<>();
        int i = 1;
        for (IControl cp : event.getB().getPoints()) {
            PassingInstruction passingInstructions = courseWaypointPassingInstructions.containsKey(i) ? courseWaypointPassingInstructions.get(i) : null;
            ttControlPoints.add(new Pair<TracTracControlPoint, PassingInstruction>(ttControlPointsForAllOriginalEventControlPoints.get(cp), passingInstructions));
            i++;
        }

        Course course = getDomainFactory().createCourse(route.getName(), ttControlPoints);
        IRace race = event.getC();
        List<Sideline> sidelines = getDomainFactory().createSidelines(
                race.getMetadata() != null ? race.getMetadata().getText() : null,
                ttControlPointsForAllOriginalEventControlPoints.values());

        RaceDefinition existingRaceDefinitionForRace = getDomainFactory().getExistingRaceDefinitionForRace(race.getId());
        if (existingRaceDefinitionForRace != null) {
            logger.log(Level.INFO, "Received course update for existing race "+race.getName()+": "+
                    event.getB().getPoints());
            // Race already exists; this means that we obviously found a course change.
            // Create TrackedRace only if it doesn't exist (which is unlikely because it is usually created
            // in the else block below together with the RaceDefinition).
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
            logger.log(Level.INFO, "Received course for non-existing race "+race.getName()+". Creating RaceDefinition.");
            // create race definition and add to event
            Pair<Iterable<Competitor>, BoatClass> competitorsAndDominantBoatClass = getDomainFactory().getCompetitorsAndDominantBoatClass(race);
            DynamicTrackedRace trackedRace = getDomainFactory().getOrCreateRaceDefinitionAndTrackedRace(
                    getTrackedRegatta(), race.getId(), race.getName(), competitorsAndDominantBoatClass.getA(),
                    competitorsAndDominantBoatClass.getB(), course, sidelines, windStore, delayToLiveInMillis,
                    millisecondsOverWhichToAverageWind, raceDefinitionSetToUpdate, tracTracUpdateURI,
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
        
        TracTracCourseDesignUpdateHandler courseDesignHandler = new TracTracCourseDesignUpdateHandler(tracTracUpdateURI, 
                tracTracUsername, tracTracPassword,
                getTracTracEvent().getId(), race.getId());
        trackedRace.addCourseDesignChangedListener(courseDesignHandler);
        
        TracTracStartTimeUpdateHandler startTimeHandler = new TracTracStartTimeUpdateHandler(tracTracUpdateURI, 
                tracTracUsername, tracTracPassword, getTracTracEvent().getId(), race.getId());
        trackedRace.addStartTimeChangedListener(startTimeHandler);
        if(!Activator.getInstance().isUseTracTracMarkPassings()){
            new MarkPassingCalculator(trackedRace, true);
        }
    }

}
