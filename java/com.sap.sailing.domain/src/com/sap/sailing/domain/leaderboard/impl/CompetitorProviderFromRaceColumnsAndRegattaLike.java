package com.sap.sailing.domain.leaderboard.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.race.impl.BaseRaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogUseCompetitorsFromRaceLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterBoatEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.regatta.impl.BaseRegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.shared.analyzing.CompetitorsInLogAnalyzer;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.impl.RaceColumnListenerWithDefaultAction;
import com.sap.sailing.domain.leaderboard.HasRaceColumnsAndRegattaLike;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

/**
 * A caching provider of a competitor set, based on the {@link TrackedRace TrackedRaces} and {@link RaceLog}s of the {@link RaceColumn}s of
 * a {@link HasRaceColumnsAndRegattaLike} and the {@link RegattaLog} of the same object. After an answer has been
 * provided, it is cached. The cache is invalidated when one of the following events occurs:
 * <ul>
 * <li>a competitor is registered with or unregistered from a race log of any of the race columns or the regatta log</li>
 * <li>a race column is added or removed</li>
 * <li>a tracked race is linked to or unlinked from any of the race columns</li>
 * <li>the racelog is marked as providing it's own competitors via the {@link RaceLogUseCompetitorsFromRaceLogEvent}</li>
 * <li>the racelog is marked as no longer providing it's own competitors by revoking an event of type {@link RaceLogUseCompetitorsFromRaceLogEvent}</li>
 * </ul>
 * 
 * Note that objects of this type are not serializable. Classes using such objects shall not assign them to non-transient
 * fields if they want to be serializable themselves.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CompetitorProviderFromRaceColumnsAndRegattaLike {
    private static final Logger logger = Logger.getLogger(CompetitorProviderFromRaceColumnsAndRegattaLike.class
            .getName());

    private final HasRaceColumnsAndRegattaLike provider;

    private final RegattaLogEventVisitor regattaLogCompetitorsCacheInvalidationListener;

    private final RaceLogEventVisitor raceLogCompetitorsCacheInvalidationListener;

    private final RaceColumnListener raceColumnListener;

    private Iterable<Competitor> allCompetitorsCache;
    
    private Set<RaceDefinition> raceDefinitionsConsidered;

    private final ConcurrentMap<Pair<RaceColumn, Fleet>, Iterable<Competitor>> allCompetitorsCacheByRace;

    public CompetitorProviderFromRaceColumnsAndRegattaLike(HasRaceColumnsAndRegattaLike provider) {
        super();
        this.provider = provider;
        this.raceDefinitionsConsidered = new HashSet<>();
        this.allCompetitorsCacheByRace = new ConcurrentHashMap<>();
        // A note regarding listener serializability: RaceLogListener and RegattaLogListener objects
        // don't need to be serializable as the log listeners are a transient structure. The race column
        // listeners, however, need to explicitly declare that they are transient.
        // This enclosing object is intended not to be serialized and shall be re-constructed by
        // its users after de-serialization. Therefore, the transient-ness of the log listeners works
        // as intended, and the race column listener explicitly returns true from its isTransient()
        // method.
        regattaLogCompetitorsCacheInvalidationListener = new BaseRegattaLogEventVisitor() {
            @Override
            public void visit(RegattaLogRegisterCompetitorEvent event) {
                invalidateAllCompetitorsCaches();
            }

            public void visit(RegattaLogRegisterBoatEvent event) {
                invalidateAllCompetitorsCaches();
            }
            
            @Override
            public void visit(RegattaLogRevokeEvent event) {
                try {
                    if (RegattaLogRegisterCompetitorEvent.class.isAssignableFrom(Class.forName(event.getRevokedEventType())) ||
                            RegattaLogRegisterBoatEvent.class.isAssignableFrom(Class.forName(event.getRevokedEventType()))) {
                        invalidateAllCompetitorsCaches();
                    }
                } catch (ClassNotFoundException e) {
                    logger.log(Level.WARNING,
                            "Problem occurred trying to resolve revoked event class " + event.getRevokedEventType(), e);
                }
            }
        };
        raceLogCompetitorsCacheInvalidationListener = new BaseRaceLogEventVisitor() {
            @Override
            public void visit(RaceLogRegisterCompetitorEvent event) {
                invalidateAllCompetitorsCaches();
            }

            @Override
            public void visit(RaceLogUseCompetitorsFromRaceLogEvent event) {
                invalidateAllCompetitorsCaches();
            }

            @Override
            public void visit(RaceLogRevokeEvent event) {
                try {
                    final Class<?> revokedEventClass = Class.forName(event.getRevokedEventType());
                    if (RaceLogRegisterCompetitorEvent.class.isAssignableFrom(revokedEventClass) ||
                            RaceLogUseCompetitorsFromRaceLogEvent.class.isAssignableFrom(revokedEventClass)) {
                        invalidateAllCompetitorsCaches();
                    }
                } catch (ClassNotFoundException e) {
                    logger.log(Level.WARNING,
                            "Problem occurred trying to resolve revoked event class " + event.getRevokedEventType(), e);
                }
            }
        };
        raceColumnListener = new RaceColumnListenerWithDefaultAction() {
            private static final long serialVersionUID = -8678230058730043052L;

            @Override
            public void defaultAction() {
            }

            /**
             * As the entire provider object is considered transient, its listeners are so, too.
             */
            @Override
            public boolean isTransient() {
                return true;
            }

            @Override
            public void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
                invalidateAllCompetitorsCaches();
            }

            @Override
            public void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
                invalidateAllCompetitorsCaches();
            }

            @Override
            public void raceColumnAddedToContainer(RaceColumn raceColumn) {
                invalidateAllCompetitorsCaches();
            }

            @Override
            public void raceColumnRemovedFromContainer(RaceColumn raceColumn) {
                invalidateAllCompetitorsCaches();
            }
        };
    }

    /**
     * Returns a Collection of all {@link Competitor Competitors} collected over {@link RegattaLog}, {@link RaceLog} as
     * well as the {@link RaceDefinition RaceDefinitions} of all {@link TrackedRace TrackedRaces} attached.
     * While subsequent calls may return different {@link Collection Collections} the contents of a {@link Collection} returned may never change.
     */
    public Iterable<Competitor> getAllCompetitors() {
        return getAllCompetitorsWithRaceDefinitionsConsidered().getB();
    }

    /**
     * Returns a Collection of all {@link Boat boats} collected over {@link RegattaLog}, {@link RaceLog} as well as the
     * {@link RaceDefinition RaceDefinitions} of all {@link TrackedRace TrackedRaces} attached. While subsequent calls
     * may return different {@link Collection Collections} the contents of a {@link Collection} returned may never
     * change.
     */
    public Iterable<Boat> getAllBoats() {
        Set<Boat> result = new HashSet<>();
        for (RaceColumn rc : provider.getRaceColumns()) {
            result.addAll(rc.getAllCompetitorsAndTheirBoats().values());
        }
        for (Boat boat : provider.getBoatsRegisteredInRegattaLog()) {
            result.add(boat);
        }
        return result; 
    }

    /**
     * Returns a Collection of all {@link Competitor Competitors} collected over {@link RegattaLog}, {@link RaceLog} as
     * well as the {@link RaceDefinition RaceDefinitions} of all {@link TrackedRace TrackedRaces} attached. While
     * subsequent calls may return different {@link Collection Collections} the contents of a {@link Collection}
     * returned may never change. The {@link RaceDefinition}s touched by this are returned as the first element
     * of the resulting pair; the competitors as the second.
     */
    public Pair<Iterable<RaceDefinition>, Iterable<Competitor>> getAllCompetitorsWithRaceDefinitionsConsidered() {
        if (allCompetitorsCache == null) {
            final Set<Competitor> result = new HashSet<>();
            final Set<RaceDefinition> raceDefinitions = new HashSet<>();
            for (RaceColumn rc : provider.getRaceColumns()) {
                final Pair<Iterable<RaceDefinition>, Iterable<Competitor>> allCompetitorsInRaceColumnWithRaceDefinitionsConsidered = rc.getAllCompetitorsWithRaceDefinitionsConsidered();
                Util.addAll(allCompetitorsInRaceColumnWithRaceDefinitionsConsidered.getB(), result);
                Util.addAll(allCompetitorsInRaceColumnWithRaceDefinitionsConsidered.getA(), raceDefinitions);
                for (final Fleet fleet : rc.getFleets()) {
                    rc.getRaceLog(fleet).addListener(raceLogCompetitorsCacheInvalidationListener);
                }
            }
            final RegattaLog regattaLog = provider.getRegattaLike().getRegattaLog();
            // If no race exists, the regatta log-provided competitor registrations will not have
            // been considered yet; add them:
            final Set<Competitor> regattaLogProvidedCompetitors = new CompetitorsInLogAnalyzer<>(regattaLog).analyze();
            result.addAll(regattaLogProvidedCompetitors);
            // else, don't add regatta log competitors because they have been added in each column already.
            // The competitors are collected from the races. Those, however, will be the regatta log
            // competitors if the race does not define its own.
            // Note: adding listeners is idempotent; at most one occurrence of this listener exists in the race/regatta log's
            // listeners set
            provider.addRaceColumnListener(raceColumnListener);
            // consider {@link RegattaLog} competitor changes because the RaceColumns may have added the competitors from there
            regattaLog.addListener(regattaLogCompetitorsCacheInvalidationListener);
            synchronized (this) {
                allCompetitorsCache = result;
                raceDefinitionsConsidered = raceDefinitions;
            }
        }
        synchronized (this) {
            return new Pair<>(raceDefinitionsConsidered, allCompetitorsCache);
        }
    }

    /**
     * Obtains competitors from the regatta log and adds those from the tracked race for the raceColumn/fleet
     * combination or the race log, if no tracked race is attached for this combination. Does <em>not</em> eliminate
     * suppressed competitors.
     */
    public Iterable<Competitor> getAllCompetitors(final RaceColumn raceColumn, final Fleet fleet) {
        Pair<RaceColumn, Fleet> key = new Pair<>(raceColumn, fleet);
        Iterable<Competitor> result = allCompetitorsCacheByRace.get(key);
        if (result == null) {
            final Set<Competitor> resultSet = new HashSet<>(); 
            // raceColumn already considers trackedRace, RaceLog and RegattaLog
            Util.addAll(raceColumn.getAllCompetitors(fleet), resultSet);
            // note: adding listeners is idempotent; at most one occurrence of this listener exists in the race/regatta log's
            // listeners set
            raceColumn.getRaceLog(fleet).addListener(raceLogCompetitorsCacheInvalidationListener);
            provider.addRaceColumnListener(raceColumnListener);
            // consider {@link RegattaLog} competitor changes because the RaceColumns may have added the competitors from there
            provider.getRegattaLike().getRegattaLog().addListener(regattaLogCompetitorsCacheInvalidationListener);
            result = resultSet;
            allCompetitorsCacheByRace.put(key, result);
        }
        return result;
    }

    private void invalidateAllCompetitorsCaches() {
        allCompetitorsCache = null;
        // note: adding listeners was idempotent; at most one occurrence of this listener exists in the regatta log's
        // listeners set
        provider.getRegattaLike().getRegattaLog().removeListener(regattaLogCompetitorsCacheInvalidationListener);
        for (final RaceColumn rc : provider.getRaceColumns()) {
            for (final Fleet fleet : rc.getFleets()) {
                // note: adding listeners was idempotent; at most one occurrence of this listener exists in the race
                // log's listeners set
                rc.getRaceLog(fleet).removeListener(raceLogCompetitorsCacheInvalidationListener);
            }
        }
        // note: adding listeners was idempotent; at most one occurrence of this listener exists in the race column
        // listeners set
        provider.removeRaceColumnListener(raceColumnListener);
        allCompetitorsCacheByRace.clear(); // this is a little coarse-grained; but given the typical access patterns it
                                           // should be good enough.
        // The change frequency of competitors lists on individual races is much lower than the access frequency to this
        // structure.
    }

}
