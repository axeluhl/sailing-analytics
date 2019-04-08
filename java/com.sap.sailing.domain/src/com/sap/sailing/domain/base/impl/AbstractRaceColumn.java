package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AllEventsOfTypeFinder;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogUseCompetitorsFromRaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RegisteredCompetitorsAnalyzer;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RegisteredCompetitorsAndBoatsAnalyzer;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogRegisterCompetitorEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogUseCompetitorsFromRaceLogEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDefinedMarkAnalyzer;
import com.sap.sailing.domain.abstractlog.shared.events.RegisterCompetitorEvent;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.abstractlog.NotRevokableException;
import com.sap.sailing.domain.common.racelog.tracking.CompetitorRegistrationOnRaceLogDisabledException;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.impl.RaceLogIdentifierImpl;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public abstract class AbstractRaceColumn extends SimpleAbstractRaceColumn implements RaceColumn {
    private static final long serialVersionUID = -7801617988982540470L;

    private static final Logger logger = Logger.getLogger(AbstractRaceColumn.class.getName());
    private final AbstractLogEventAuthor raceLogEventAuthorForRaceColumn = new LogEventAuthorImpl(
            AbstractRaceColumn.class.getName(), 0);

    private TrackedRaces trackedRaces;
    private Map<Fleet, RaceIdentifier> raceIdentifiers;

    private ConcurrentMap<Fleet, RaceLog> raceLogs;
    
    private transient RaceLogStore raceLogStore;
    private RegattaLikeIdentifier regattaLikeParent;

    public AbstractRaceColumn() {
        this.trackedRaces = new TrackedRaces();
        this.raceIdentifiers = new HashMap<Fleet, RaceIdentifier>();
        this.raceLogs = new ConcurrentHashMap<Fleet, RaceLog>();
    }

    @Override
    public synchronized void setRaceLogInformation(RaceLogStore raceLogStore, RegattaLikeIdentifier regattaLikeParent) {
        this.raceLogStore = raceLogStore;
        this.regattaLikeParent = regattaLikeParent;
        for (final Fleet fleet : getFleets()) {
            reloadRaceLog(fleet);
        }
    }

    @Override
    public RaceLog getRaceLog(Fleet fleet) {
        return raceLogs.get(fleet);
    }

    @Override
    public RaceLogIdentifier getRaceLogIdentifier(Fleet fleet) {
        return new RaceLogIdentifierImpl(regattaLikeParent, getName(), fleet);
    }

    @Override
    public TrackedRace getTrackedRace(Fleet fleet) {
        return trackedRaces == null ? null : trackedRaces.get(fleet);
    }

    @Override
    public void setTrackedRace(Fleet fleet, TrackedRace trackedRace) {
        TrackedRace previouslyLinkedRace = this.trackedRaces.get(fleet);
        if (trackedRace != previouslyLinkedRace) {
            releaseTrackedRace(fleet);
            synchronized (this) {
                if (trackedRace != null) {
                    this.trackedRaces.put(fleet, trackedRace);
                    this.setRaceIdentifier(fleet, trackedRace.getRaceIdentifier());
                    logger.info(String.format("Linked race column %s with tracked race %s.", this.getName(),
                            trackedRace.getRace() == null ? "null" : trackedRace.getRace().getName()));
                }
            }
            if (trackedRace != null) {
                RegattaLog regattaLog = getRegattaLog();
                if (regattaLog != null) {
                    trackedRace.attachRegattaLog(regattaLog);
                }
                final RaceLog raceLog = getRaceLog(fleet);
                if (raceLog != null) {
                    trackedRace.attachRaceLog(raceLog);
                }
                trackedRace.attachRaceExecutionProvider(getRaceExecutionOrderProvider());
                getRaceColumnListeners().notifyListenersAboutTrackedRaceLinked(this, fleet, trackedRace);
            }
        }
    }

    @Override
    public RaceIdentifier getRaceIdentifier(Fleet fleet) {
        return raceIdentifiers.get(fleet);
    }

    @Override
    public void setRaceIdentifier(Fleet fleet, RaceIdentifier raceIdentifier) {
        if (raceIdentifier != null) {
            this.raceIdentifiers.put(fleet, raceIdentifier);
        }
    }

    @Override
    public synchronized void releaseTrackedRace(Fleet fleet) {
        TrackedRace previouslyLinkedRace = this.trackedRaces.get(fleet);
        this.trackedRaces.remove(fleet);
        if (previouslyLinkedRace != null && regattaLikeParent != null) {
            RaceLogIdentifier identifier = getRaceLogIdentifier(fleet);
            previouslyLinkedRace.detachRaceLog(identifier.getIdentifier());
            previouslyLinkedRace.detachRaceExecutionOrderProvider(getRaceExecutionOrderProvider());
            getRaceColumnListeners().notifyListenersAboutTrackedRaceUnlinked(this, fleet, previouslyLinkedRace);
        }
    }

    @Override
    public void removeRaceIdentifier(Fleet fleet) {
        releaseTrackedRace(fleet);
        this.raceIdentifiers.remove(fleet);
    }

    @Override
    public Fleet getFleetOfCompetitor(Competitor competitor) {
        for (final Fleet fleet : getFleets()) {
            if (Util.contains(getAllCompetitors(fleet), competitor)) {
                return fleet;
            }
        }
        return null;
    }

    @Override
    public Fleet getFleetByName(String fleetName) {
        for (Fleet fleet : getFleets()) {
            if (fleet.getName().equals(fleetName)) {
                return fleet;
            }
        }
        return null;
    }

    @Override
    public boolean hasTrackedRaces() {
        return !trackedRaces.isEmpty();
    }

    @Override
    public TrackedRace getTrackedRace(Competitor competitor) {
        for (TrackedRace trackedRace : trackedRaces.values()) {
            if (Util.contains(trackedRace.getRace().getCompetitors(), competitor)) {
                return trackedRace;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public void reloadRaceLog(Fleet fleet) {
        RaceLogIdentifier identifier = getRaceLogIdentifier(fleet);
        RaceLog newOrLoadedRaceLog = raceLogStore.getRaceLog(identifier, /* ignoreCache */true);
        RaceLog raceLogAvailable = raceLogs.get(fleet);
        if (raceLogAvailable == null) {
            RaceColumnRaceLogReplicator listener = new RaceColumnRaceLogReplicator(this, identifier);
            // FIXME Wouldn't this skip any listener notifications that a merge below would trigger if the race log already existed?
            // FIXME For example, how about the race log-provided score corrections that need application to the leaderboard and replication?
            newOrLoadedRaceLog.addListener(listener);
            raceLogs.put(fleet, newOrLoadedRaceLog);
            final TrackedRace trackedRace = getTrackedRace(fleet);
            if (trackedRace != null) {
                // need to attach race log
                trackedRace.attachRaceLog(newOrLoadedRaceLog);
            }
        } else {
            // now add all race log events from newOrLoadedRaceLog that are not already in raceLogAvailable
            raceLogAvailable.merge(newOrLoadedRaceLog);
        }
    }

    /**
     * When deserializing, replication listeners are registered on all race logs.
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        for (Entry<Fleet, RaceLog> entry : raceLogs.entrySet()) {
            Fleet fleet = entry.getKey();
            RaceLog raceLog = entry.getValue();
            raceLog.addListener(new RaceColumnRaceLogReplicator(this, getRaceLogIdentifier(fleet)));
            // now comes a little secrecy (see bug 2506) about how, after de-serialization, the connections
            // between race column, race log, tracked race and the listener pumping stuff from the race log
            // into the tracked race are re-established. The race log's listener structure is transient, and so
            // is the tracked race's attachedRaceLogs field and the logListener field. The collections and the
            // DynamicTrackedRaceLogListener are re-established after de-serialization in corresponding readObject(...)
            // methods. However, the connections are not. That's what we need to do here, simply by invoking:
            ois.registerValidation(() -> {
                TrackedRace trackedRace = getTrackedRace(fleet);
                if (trackedRace != null) {
                    trackedRace.attachRaceLog(raceLog);
                }
            }, /* prio */0);
            // because this will add the race log to the tracked race's attachedRaceLogs collection again, and
            // the new DynamicTrackedRaceLogListener that the readObject(...) method had constructed for the
            // tracked race will be added as a listener to the race log whose listeners collection otherwise would
            // only hold the RaceColumnRaceLogReplicator object added above.
        }
    }

    @Override
    public void setMasterDataExportOngoingThreadFlag(boolean flagValue) {
        trackedRaces.setMasterDataExportOngoingThreadFlag(flagValue);
    }

    @Override
    public Iterable<Competitor> getAllCompetitors() {
        return getAllCompetitorsWithRaceDefinitionsConsidered().getB();
    }

    @Override
    public Pair<Iterable<RaceDefinition>, Iterable<Competitor>> getAllCompetitorsWithRaceDefinitionsConsidered() {
        Set<Competitor> competitors = new HashSet<>();
        Set<RaceDefinition> raceDefinitionsConsidered = new HashSet<>();
        for (Fleet fleet : getFleets()) {
            final Pair<RaceDefinition, Iterable<Competitor>> allCompetitorsWithRaceDefinitionsConsidered = getAllCompetitorsWithRaceDefinitionsConsidered(fleet);
            Util.addAll(allCompetitorsWithRaceDefinitionsConsidered.getB(), competitors);
            if (allCompetitorsWithRaceDefinitionsConsidered.getA() != null) {
                raceDefinitionsConsidered.add(allCompetitorsWithRaceDefinitionsConsidered.getA());
            }
        }
        return new Pair<>(raceDefinitionsConsidered, competitors);
    }

    /**
     * Same as {@link #getAllCompetitors(Fleet)}, but also returns the {@link RaceDefinition} as the first
     * component of a pair if it contributed its {@link RaceDefinition#getCompetitors()}. If the resulting
     * iterable of competitors did not consider a {@link RaceDefinition}'s competitor set, {@code null}
     * is returned as the first component of the pair.
     */
    private Pair<RaceDefinition, Iterable<Competitor>> getAllCompetitorsWithRaceDefinitionsConsidered(final Fleet fleet) {
        final Iterable<Competitor> competitors;
        final RaceDefinition raceDefinition;
        TrackedRace trackedRace = getTrackedRace(fleet);
        if (trackedRace != null) {
            raceDefinition = trackedRace.getRace();
            competitors = raceDefinition.getCompetitors();
        } else {
            raceDefinition = null;
            // if no tracked race is found, use competitors from race/regatta log depending on whether
            // the mapping event is present or not; this assumes that if a tracked
            // race exists, its competitors set takes precedence over what's in the race log. Usually,
            // the tracked race will have the same competitors as those in the race log, or more because
            // those from the regatta log are added to the tracked race as well.
            Set<Competitor> viaRaceLog = new RegisteredCompetitorsAnalyzer(getRaceLog(fleet), getRegattaLog()).analyze();
            competitors = viaRaceLog;
        }
        return new Pair<>(raceDefinition, competitors);
    }
    
    @Override
    public Iterable<Competitor> getAllCompetitors(final Fleet fleet) {
        return getAllCompetitorsWithRaceDefinitionsConsidered(fleet).getB();
    }

    
    @Override
    public Map<Competitor, Boat> getAllCompetitorsAndTheirBoats() {
        Map<Competitor, Boat> result = new HashMap<>();
        for (Fleet fleet : getFleets()) {
            result.putAll(getAllCompetitorsAndTheirBoats(fleet));
        }
        return result;
    }        

    @Override
    public Map<Competitor, Boat> getAllCompetitorsAndTheirBoats(Fleet fleet) {
        final Map<Competitor, Boat> competitors;
        TrackedRace trackedRace = getTrackedRace(fleet);
        if (trackedRace != null) {
            competitors = trackedRace.getRace().getCompetitorsAndTheirBoats();
        } else {
            // if no tracked race is found, use competitors from race/regatta log depending on whether
            // the mapping event is present or not; this assumes that if a tracked
            // race exists, its competitors set takes precedence over what's in the race log. Usually,
            // the tracked race will have the same competitors as those in the race log, or more because
            // those from the regatta log are added to the tracked race as well.
            competitors = new RegisteredCompetitorsAndBoatsAnalyzer(getRaceLog(fleet), getRegattaLog()).analyze();
        }
        return competitors;
    }

    @Override
    public void registerCompetitor(CompetitorWithBoat competitorWithBoat, Fleet fleet) throws CompetitorRegistrationOnRaceLogDisabledException {
        Map<Competitor, Boat> competitorsAndBoats = new HashMap<>();
        competitorsAndBoats.put(competitorWithBoat, competitorWithBoat.getBoat());
        registerCompetitorsInternal(competitorsAndBoats, fleet);
    }

    @Override
    public void registerCompetitors(Iterable<CompetitorWithBoat> competitorWithBoats, Fleet fleet) throws CompetitorRegistrationOnRaceLogDisabledException {
        Map<Competitor, Boat> competitorsAndBoats = new HashMap<>();
        for (CompetitorWithBoat competitorWithBoat: competitorWithBoats) {
            competitorsAndBoats.put(competitorWithBoat, competitorWithBoat.getBoat());
        }
        registerCompetitorsInternal(competitorsAndBoats, fleet);
    }

    @Override
    public void registerCompetitor(Competitor competitor, Boat boat, Fleet fleet) throws CompetitorRegistrationOnRaceLogDisabledException {
        Map<Competitor, Boat> competitorsAndBoats = new HashMap<>();
        competitorsAndBoats.put(competitor, boat);
        registerCompetitorsInternal(competitorsAndBoats, fleet);
    }

    @Override
    public void registerCompetitors(Map<Competitor, Boat> competitorsAndBoats, Fleet fleet)
            throws CompetitorRegistrationOnRaceLogDisabledException {
        registerCompetitorsInternal(competitorsAndBoats, fleet);
    }

    private void registerCompetitorsInternal(Map<Competitor, Boat> competitorsAndBoats, Fleet fleet)
            throws CompetitorRegistrationOnRaceLogDisabledException {
        if (!isCompetitorRegistrationInRacelogEnabled(fleet)) {
            throw new CompetitorRegistrationOnRaceLogDisabledException("Competitor registration not allowed for fleet "+fleet+" in column "+this);
        }
        TimePoint now = MillisecondsTimePoint.now();
        RaceLog raceLog = getRaceLog(fleet);
        int passId = raceLog.getCurrentPassId();
        for (Entry<Competitor, Boat> competitorAndBoat : competitorsAndBoats.entrySet()) {
            raceLog.add(new RaceLogRegisterCompetitorEventImpl(now, now, raceLogEventAuthorForRaceColumn, 
                    UUID.randomUUID(), passId, competitorAndBoat.getKey(), competitorAndBoat.getValue()));
        }
    }
    

    @Override
    public void deregisterCompetitor(Competitor competitor, Fleet fleet)
            throws CompetitorRegistrationOnRaceLogDisabledException {
        deregisterCompetitors(Collections.singleton(competitor), fleet);
    }
    
    @Override
    public void deregisterCompetitors(Iterable<? extends Competitor> competitors, Fleet fleet)
            throws CompetitorRegistrationOnRaceLogDisabledException {
        if (!isCompetitorRegistrationInRacelogEnabled(fleet)) {
            throw new CompetitorRegistrationOnRaceLogDisabledException("Competitor registration not allowed for fleet "+fleet+" in column "+this);
        }
        Set<Competitor> competitorSet = new HashSet<Competitor>();
        Util.addAll(competitors, competitorSet);
        RaceLog raceLog = getRaceLog(fleet);
        for (RaceLogEvent event : raceLog.getUnrevokedEventsDescending()) {
            if (event instanceof RegisterCompetitorEvent) {
                RegisterCompetitorEvent<?> registerEvent = (RegisterCompetitorEvent<?>) event;
                if (competitorSet.contains(registerEvent.getCompetitor())) {
                    try {
                        raceLog.revokeEvent(raceLogEventAuthorForRaceColumn, event,
                                "unregistering competitor because no longer selected for registration");
                    } catch (NotRevokableException e) {
                        logger.log(Level.WARNING, "could not unregister competitor by adding RevokeEvent", e);
                    }
                }
            }
        }
    }

    @Override
    public Iterable<Mark> getCourseMarks() {
        final Set<Mark> result = new HashSet<>();
        for (Fleet fleet : getFleets()) {
            Util.addAll(getCourseMarks(fleet), result);
        }
        return result;
    }

    @Override
    public Iterable<Mark> getCourseMarks(Fleet fleet) {
        final Set<Mark> result = new HashSet<>();
        final TrackedRace trackedRace = getTrackedRace(fleet);
        if (trackedRace != null) {
            for (Waypoint waypoint : trackedRace.getRace().getCourse().getWaypoints()) {
                Util.addAll(waypoint.getMarks(), result);
            }
        } else {
            // if no tracked race is found, use marks from race course if present in racelog
            LastPublishedCourseDesignFinder courseDesginFinder = new LastPublishedCourseDesignFinder(getRaceLog(fleet), /* onlyCoursesWithValidWaypointList */ true);
            final CourseBase courseBase = courseDesginFinder.analyze();
            if (courseBase != null) {
                courseBase.getWaypoints().forEach((waypoint) -> Util.addAll(waypoint.getMarks(), result));
            }
        }
        return result;
    }
    
    @Override
    public Iterable<Mark> getAvailableMarks() {
        final Set<Mark> result = new HashSet<>();
        // add the marks from the regatta log to the result
        RegattaLog regattaLog = getRegattaLog();
        Collection<Mark> viaRegattaLog = new RegattaLogDefinedMarkAnalyzer(regattaLog).analyze();
        result.addAll(viaRegattaLog);
        for (Fleet fleet : getFleets()) {
            TrackedRace trackedRace = getTrackedRace(fleet);
            if (trackedRace != null) {
                Util.addAll(trackedRace.getMarks(), result);
            }
        }
        return result;
    }

    @Override
    public Iterable<Mark> getAvailableMarks(Fleet fleet) {
        final Set<Mark> result = new HashSet<>();
        // add the marks from the regatta log to the result
        RegattaLog regattaLog = getRegattaLog();
        Collection<Mark> viaRegattaLog = new RegattaLogDefinedMarkAnalyzer(regattaLog).analyze();
        result.addAll(viaRegattaLog);
        TrackedRace trackedRace = getTrackedRace(fleet);
        if (trackedRace != null) {
            Util.addAll(trackedRace.getMarks(), result);
        }
        return result;
    }

    @Override
    public void enableCompetitorRegistrationOnRaceLog(Fleet fleet) {
        TimePoint now = MillisecondsTimePoint.now();
        RaceLog raceLog = getRaceLog(fleet);
        int passId = raceLog.getCurrentPassId();
        raceLog.add(new RaceLogUseCompetitorsFromRaceLogEventImpl(now, raceLogEventAuthorForRaceColumn, now, UUID.randomUUID(), passId));
    }
    
    @Override
    public void disableCompetitorRegistrationOnRaceLog(Fleet fleet) throws NotRevokableException {
        RaceLog raceLog = getRaceLog(fleet);
        List<RaceLogEvent> events = new AllEventsOfTypeFinder<>(raceLog, true, RaceLogUseCompetitorsFromRaceLogEvent.class).analyze();
        for (RaceLogEvent event : events) {
            raceLog.lockForRead();
            try {
                event = raceLog.getEventById(event.getId());
            } finally {
                raceLog.unlockAfterRead();
            }
            if (event != null) {
                raceLog.revokeEvent(raceLogEventAuthorForRaceColumn, event, "revoke triggered by GWT user action");
            }
        }
    }

}