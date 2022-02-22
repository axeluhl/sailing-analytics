package com.sap.sailing.domain.tracking;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.abstractlog.race.impl.BaseRaceLogEventVisitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sse.common.Util;

import difflib.PatchFailedException;

/**
 * Base class for all {@link RaceTracker}s that must implement listener notifications
 */
public abstract class AbstractRaceTrackerBaseImpl<RTCP extends RaceTrackingConnectivityParameters> implements RaceTracker {
    private static final Logger logger = Logger.getLogger(AbstractRaceTrackerBaseImpl.class.getName());
    private final RaceTrackerListeners listeners = new RaceTrackerListeners();
    private final Set<RaceTracker.RaceCreationListener> raceCreationListeners = Collections
            .newSetFromMap(new ConcurrentHashMap<RaceTracker.RaceCreationListener, Boolean>());
    
    private final RTCP connectivityParams;

    public AbstractRaceTrackerBaseImpl(RTCP connectivityParams) {
        super();
        this.connectivityParams = connectivityParams;
    }
    
    /**
     * Can be used to implement a {@link BaseRaceLogEventVisitor} regarding the handling of a
     * {@link RaceLogCourseDesignChangedEvent}. The method will update the {@link #getTrackedRace() tracked race's}
     * course from the course design change event.
     */
    protected void onCourseDesignChangedEvent(RaceLogCourseDesignChangedEvent event, RaceLog raceLog, DomainFactory baseDomainFactory, TrackedRace trackedRace) {
        if (trackedRace != null) {
            CourseBase base = new LastPublishedCourseDesignFinder(raceLog, /* onlyCoursesWithValidWaypointList */ true).analyze();
            List<Util.Pair<ControlPoint, PassingInstruction>> update = new ArrayList<>();
            for (Waypoint waypoint : base.getWaypoints()) {
                update.add(new Util.Pair<>(waypoint.getControlPoint(), waypoint.getPassingInstructions()));
            }
            try {
                final Course course = trackedRace.getRace().getCourse();
                course.update(update, event.getCourseDesign().getAssociatedRoles(),
                        event.getCourseDesign().getOriginatingCourseTemplateIdOrNull(), baseDomainFactory);
                if (base.getName() != null) {
                    course.setName(base.getName());
                }
            } catch (PatchFailedException e) {
                logger.log(Level.WARNING, "Could not update course for race " + trackedRace.getRace().getName());
            }
        }
    }

    /**
     * Ensure stop method does notify all listeners after tracker stopped.
     */
    @Override
    public final void stop(boolean preemptive) throws MalformedURLException, IOException, InterruptedException {
        this.stop(preemptive, /* willBeRemoved */ false);
    }

    /**
     * Ensure stop method does notify all listeners after tracker stopped. If the race will be removed afterwards
     * by the caller, the caller should pass {@code true} for the {@code willBeRemoved} parameter. This will, in
     * particular, avoid that unnecessary re-calculations will be triggered.
     */
    @Override
    public final void stop(boolean preemptive, boolean willBeRemoved) throws MalformedURLException, IOException, InterruptedException {
        try {
            listeners.onTrackerWillStop(preemptive, willBeRemoved);
        } finally {
            onStop(preemptive, willBeRemoved);
        }
    }

    /**
     * Template stop method for subclasses.
     * 
     * @param willBeRemoved
     *            If the race will be removed afterwards by the caller, the caller should pass {@code true} for the
     *            {@code willBeRemoved} parameter. This will, in particular, avoid that unnecessary re-calculations will
     *            be triggered.
     */
    protected void onStop(boolean preemptive, boolean willBeRemoved) throws MalformedURLException, IOException, InterruptedException {
    }

    @Override
    public boolean add(Listener listener) {
        return listeners.addListener(listener);
    }

    @Override
    public void remove(Listener listener) {
        listeners.removeListener(listener);
    }

    @Override
    public void add(RaceCreationListener listener) {
        final RaceDefinition race;
        synchronized (raceCreationListeners) {
            race = getRace();
            if (race == null) {
                raceCreationListeners.add(listener);
            }
        }
        if (race != null) { // listener hasn't been added above; notify immediately
            listener.onRaceCreated(this);
        }
    }

    @Override
    public void remove(RaceCreationListener listener) {
        synchronized (raceCreationListeners) {
            raceCreationListeners.remove(listener);
        }
    }
    
    /**
     * Notifies all {@link RaceCreationListener}s registered and removes them. Must be called
     * after {@link #getRace()} has started returning a valid, non-{@code null} race.
     */
    protected void notifyRaceCreationListeners() {
        assert getRace() != null;
        final Set<RaceCreationListener> listenersToNotify;
        synchronized (raceCreationListeners) {
            listenersToNotify = new HashSet<>(raceCreationListeners);
            raceCreationListeners.clear();
        }
        listenersToNotify.forEach(l->l.onRaceCreated(this));
    }

    @Override
    public RTCP getConnectivityParams() {
        return connectivityParams;
    }
}
