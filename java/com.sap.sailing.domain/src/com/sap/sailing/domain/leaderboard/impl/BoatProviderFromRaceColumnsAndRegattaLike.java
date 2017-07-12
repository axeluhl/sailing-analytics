package com.sap.sailing.domain.leaderboard.impl;

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
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterBoatEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogUseBoatsFromRaceLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterBoatEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.regatta.impl.BaseRegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.shared.analyzing.BoatsInLogAnalyzer;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.impl.RaceColumnListenerWithDefaultAction;
import com.sap.sailing.domain.leaderboard.HasRaceColumnsAndRegattaLike;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

/**
 * A caching provider of a boat set, based on the tracked races and {@link RaceLog}s of the {@link RaceColumn}s of
 * a {@link HasRaceColumnsAndRegattaLike} and the {@link RegattaLog} of the same object. After an answer has been
 * provided, it is cached. The cache is invalidated when one of the following events occurs:
 * <ul>
 * <li>a boat is registered with or unregistered from a race log of any of the race columns or the regatta log</li>
 * <li>a race column is added or removed</li>
 * <li>a tracked race is linked to or unlinked from any of the race columns</li>
 * <li>the racelog is marked as providing it's own boats via the {@link RaceLogUseBoatsFromRaceLogEvent}</li>
 * <li>the racelog is marked as no longer providing it's own boats by revoking an event of type {@link RaceLogUseBoatsFromRaceLogEvent}</li>
 * </ul>
 * 
 * Note that objects of this type are not serializable. Classes using such objects shall not assign them to non-transient
 * fields if they want to be serializable themselves.
 * 
 * @author Frank Mittag
 *
 */
public class BoatProviderFromRaceColumnsAndRegattaLike {
    private static final Logger logger = Logger.getLogger(BoatProviderFromRaceColumnsAndRegattaLike.class
            .getName());

    private final HasRaceColumnsAndRegattaLike provider;

    private final RegattaLogEventVisitor regattaLogBoatsCacheInvalidationListener;

    private final RaceLogEventVisitor raceLogBoatsCacheInvalidationListener;

    private final RaceColumnListener raceColumnListener;

    private Iterable<Boat> allBoatsCache;

    private final ConcurrentMap<Pair<RaceColumn, Fleet>, Iterable<Boat>> allBoatsCacheByRace;

    public BoatProviderFromRaceColumnsAndRegattaLike(HasRaceColumnsAndRegattaLike provider) {
        super();
        this.provider = provider;
        this.allBoatsCacheByRace = new ConcurrentHashMap<>();
        // A note regarding listener serializability: RaceLogListener and RegattaLogListener objects
        // don't need to be serializable as the log listeners are a transient structure. The race column
        // listeners, however, need to explicitly declare that they are transient.
        // This enclosing object is intended not to be serialized and shall be re-constructed by
        // its users after de-serialization. Therefore, the transient-ness of the log listeners works
        // as intended, and the race column listener explicitly returns true from its isTransient()
        // method.
        regattaLogBoatsCacheInvalidationListener = new BaseRegattaLogEventVisitor() {
            @Override
            public void visit(RegattaLogRegisterBoatEvent event) {
                invalidateAllBoatsCaches();
            }

            @Override
            public void visit(RegattaLogRevokeEvent event) {
                try {
                    if (RegattaLogRegisterBoatEvent.class.isAssignableFrom(Class.forName(event
                            .getRevokedEventType()))) {
                        invalidateAllBoatsCaches();
                    }
                } catch (ClassNotFoundException e) {
                    logger.log(Level.WARNING,
                            "Problem occurred trying to resolve revoked event class " + event.getRevokedEventType(), e);
                }
            }
        };
        raceLogBoatsCacheInvalidationListener = new BaseRaceLogEventVisitor() {
            @Override
            public void visit(RaceLogRegisterBoatEvent event) {
                invalidateAllBoatsCaches();
            }

            @Override
            public void visit(RaceLogUseBoatsFromRaceLogEvent event) {
                invalidateAllBoatsCaches();
            }

            @Override
            public void visit(RaceLogRevokeEvent event) {
                try {
                    final Class<?> revokedEventClass = Class.forName(event.getRevokedEventType());
                    // 
                    if (RaceLogRegisterBoatEvent.class.isAssignableFrom(revokedEventClass) ||
                            RaceLogUseBoatsFromRaceLogEvent.class.isAssignableFrom(revokedEventClass)) {
                        invalidateAllBoatsCaches();
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
                invalidateAllBoatsCaches();
            }

            @Override
            public void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
                invalidateAllBoatsCaches();
            }

            @Override
            public void raceColumnAddedToContainer(RaceColumn raceColumn) {
                invalidateAllBoatsCaches();
            }

            @Override
            public void raceColumnRemovedFromContainer(RaceColumn raceColumn) {
                invalidateAllBoatsCaches();
            }
        };
    }

    public Iterable<Boat> getAllBoats() {
        if (allBoatsCache == null) {
            final Set<Boat> result = new HashSet<>();
            boolean hasRaceColumns = false;
            for (RaceColumn rc : provider.getRaceColumns()) {
                hasRaceColumns = true;
                Util.addAll(rc.getAllCompetitorsAndTheirBoats().values(), result);
                for (final Fleet fleet : rc.getFleets()) {
                    rc.getRaceLog(fleet).addListener(raceLogBoatsCacheInvalidationListener);
                }
            }
            final RegattaLog regattaLog = provider.getRegattaLike().getRegattaLog();
            if (!hasRaceColumns) {
                // If no race exists, the regatta log-provided boat registrations will not have
                // been considered yet; add them:
                final Set<Boat> regattaLogProvidedBoats = new BoatsInLogAnalyzer<>(regattaLog).analyze();
                result.addAll(regattaLogProvidedBoats);
            }
            // else, don't add regatta log boats because they have been added in each column already.
            // The boats are collected from the races. Those, however, will be the regatta log
            // boats if the race does not define its own.
            // Note: adding listeners is idempotent; at most one occurrence of this listener exists in the race/regatta log's
            // listeners set
            provider.addRaceColumnListener(raceColumnListener);
            // consider {@link RegattaLog} competitor changes because the RaceColumns may have added the competitors from there
            regattaLog.addListener(regattaLogBoatsCacheInvalidationListener);
            allBoatsCache = result;
        }
        return allBoatsCache;
    }

    /**
     * Obtains boats from the regatta log and adds those from the tracked race for the raceColumn/fleet
     * combination or the race log, if no tracked race is attached for this combination.
     */
    public Iterable<Boat> getAllBoats(final RaceColumn raceColumn, final Fleet fleet) {
        Pair<RaceColumn, Fleet> key = new Pair<>(raceColumn, fleet);
        Iterable<Boat> result = allBoatsCacheByRace.get(key);
        if (result == null) {
            final Set<Boat> resultSet = new HashSet<>(); 
            // raceColumn already considers trackedRace, RaceLog and RegattaLog
            Util.addAll(raceColumn.getAllCompetitorsAndTheirBoats(fleet).values(), resultSet);
            // note: adding listeners is idempotent; at most one occurrence of this listener exists in the race/regatta log's
            // listeners set
            raceColumn.getRaceLog(fleet).addListener(raceLogBoatsCacheInvalidationListener);
            provider.addRaceColumnListener(raceColumnListener);
            // consider {@link RegattaLog} competitor changes because the RaceColumns may have added the competitors from there
            provider.getRegattaLike().getRegattaLog().addListener(regattaLogBoatsCacheInvalidationListener);
            result = resultSet;
            allBoatsCacheByRace.put(key, result);
        }
        return result;
    }

    private void invalidateAllBoatsCaches() {
        allBoatsCache = null;
        // note: adding listeners was idempotent; at most one occurrence of this listener exists in the regatta log's
        // listeners set
        provider.getRegattaLike().getRegattaLog().removeListener(regattaLogBoatsCacheInvalidationListener);
        for (final RaceColumn rc : provider.getRaceColumns()) {
            for (final Fleet fleet : rc.getFleets()) {
                // note: adding listeners was idempotent; at most one occurrence of this listener exists in the race
                // log's listeners set
                rc.getRaceLog(fleet).removeListener(raceLogBoatsCacheInvalidationListener);
            }
        }
        // note: adding listeners was idempotent; at most one occurrence of this listener exists in the race column
        // listeners set
        provider.removeRaceColumnListener(raceColumnListener);
        allBoatsCacheByRace.clear(); // this is a little coarse-grained; but given the typical access patterns it
                                           // should be good enough.
        // The change frequency of boats lists on individual races is much lower than the access frequency to this
        // structure.
    }

}
