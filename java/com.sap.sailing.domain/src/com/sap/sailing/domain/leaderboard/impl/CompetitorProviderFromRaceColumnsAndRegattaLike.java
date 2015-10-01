package com.sap.sailing.domain.leaderboard.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.race.impl.BaseRaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.regatta.impl.BaseRegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.shared.analyzing.RegisteredCompetitorsAnalyzer;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.impl.RaceColumnListenerWithDefaultAction;
import com.sap.sailing.domain.leaderboard.HasRaceColumnsAndRegattaLike;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

/**
 * A caching provider of a competitor set, based on the tracked races and {@link RaceLog}s of the {@link RaceColumn}s of
 * a {@link HasRaceColumnsAndRegattaLike} and the {@link RegattaLog} of the same object. After an answer has been provided,
 * it is cached. The cache is invalidated when one of the following events occurs:
 * <ul>
 * <li>a competitor is registered with or unregistered from a race log of any of the race columns or the regatta log</li>
 * <li>a race column is added or removed</li>
 * <li>a tracked race is linked to or unlinked from any of the race columns</li>
 * </ul>
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CompetitorProviderFromRaceColumnsAndRegattaLike {
    private static final Logger logger = Logger.getLogger(CompetitorProviderFromRaceColumnsAndRegattaLike.class.getName());
    
    private final HasRaceColumnsAndRegattaLike provider;

    private final RegattaLogEventVisitor regattaLogCompetitorsCacheInvalidationListener;

    private final RaceLogEventVisitor raceLogCompetitorsCacheInvalidationListener;
    
    private final RaceColumnListener raceColumnListener;

    private Iterable<Competitor> allCompetitorsCache;
    
    private final ConcurrentHashMap<Pair<RaceColumn, Fleet>, Iterable<Competitor>> allCompetitorsCacheByRace;

    public CompetitorProviderFromRaceColumnsAndRegattaLike(HasRaceColumnsAndRegattaLike provider) {
        super();
        this.provider = provider;
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

            @Override
            public void visit(RegattaLogRevokeEvent event) {
                try {
                    if (RegattaLogRegisterCompetitorEvent.class.isAssignableFrom(Class.forName(event.getRevokedEventType()))) {
                        invalidateAllCompetitorsCaches();
                    }
                } catch (ClassNotFoundException e) {
                    logger.log(Level.WARNING, "Problem occurred trying to resolve revoked event class "+event.getRevokedEventType(), e);
                }
            }
        };
        raceLogCompetitorsCacheInvalidationListener = new BaseRaceLogEventVisitor() {
            @Override
            public void visit(RaceLogRegisterCompetitorEvent event) {
                invalidateAllCompetitorsCaches();
            }

            @Override
            public void visit(RaceLogRevokeEvent event) {
                try {
                    if (RaceLogRegisterCompetitorEvent.class.isAssignableFrom(Class.forName(event.getRevokedEventType()))) {
                        invalidateAllCompetitorsCaches();
                    }
                } catch (ClassNotFoundException e) {
                    logger.log(Level.WARNING, "Problem occurred trying to resolve revoked event class "+event.getRevokedEventType(), e);
                }
            }
        };
        raceColumnListener = new RaceColumnListenerWithDefaultAction() {
            private static final long serialVersionUID = -8678230058730043052L;

            @Override
            public void defaultAction() {}

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
    
    public Iterable<Competitor> getAllCompetitors() {
        if (allCompetitorsCache == null) {
            final Set<Competitor> result = new HashSet<>();
            for (RaceColumn rc : provider.getRaceColumns()) {
                Util.addAll(rc.getAllCompetitors(), result);
                for (final Fleet fleet : rc.getFleets()) {
                    rc.getRaceLog(fleet).addListener(raceLogCompetitorsCacheInvalidationListener);
                }
            }
            provider.addRaceColumnListener(raceColumnListener);
            // consider {@link RegattaLog}
            Set<Competitor> viaLog = getCompetitorsFromRegattaLogAndRegisterAsRegattaLogListener();
            result.addAll(viaLog);
            allCompetitorsCache = result;
        }
        return allCompetitorsCache;
    }
    
    /**
     * Obtains competitors from the regatta log and adds those from the tracked race for the
     * raceColumn/fleet combination or the race log, if no tracked race is attached for this
     * combination. Does <em>not</em> eliminate suppressed competitors.
     */
    public Iterable<Competitor> getAllCompetitors(final RaceColumn raceColumn, final Fleet fleet) {
        Pair<RaceColumn, Fleet> key = new Pair<>(raceColumn, fleet);
        Iterable<Competitor> result = allCompetitorsCacheByRace.get(key);
        if (result == null) {
            final Set<Competitor> resultSet = new HashSet<>();
            Util.addAll(raceColumn.getAllCompetitors(fleet), resultSet);
            // note: adding listeners is idempotent; at most one occurrence of this listener exists in the race log's listeners set
            raceColumn.getRaceLog(fleet).addListener(raceLogCompetitorsCacheInvalidationListener);
            provider.addRaceColumnListener(raceColumnListener);
            // consider {@link RegattaLog}
            Set<Competitor> viaLog = getCompetitorsFromRegattaLogAndRegisterAsRegattaLogListener();
            resultSet.addAll(viaLog);
            result = resultSet;
            allCompetitorsCacheByRace.put(key, result);
        }
        return result;
    }

    private Set<Competitor> getCompetitorsFromRegattaLogAndRegisterAsRegattaLogListener() {
        Set<Competitor> viaLog = new RegisteredCompetitorsAnalyzer<>(provider.getRegattaLike().getRegattaLog()).analyze();
        // note: adding listeners is idempotent; at most one occurrence of this listener exists in the regatta log's listeners set
        provider.getRegattaLike().getRegattaLog().addListener(regattaLogCompetitorsCacheInvalidationListener);
        return viaLog;
    }

    private void invalidateAllCompetitorsCaches() {
        allCompetitorsCache = null;
        // note: adding listeners was idempotent; at most one occurrence of this listener exists in the regatta log's listeners set
        provider.getRegattaLike().getRegattaLog().removeListener(regattaLogCompetitorsCacheInvalidationListener);
        for (final RaceColumn rc : provider.getRaceColumns()) {
            for (final Fleet fleet : rc.getFleets()) {
                // note: adding listeners was idempotent; at most one occurrence of this listener exists in the race log's listeners set
                rc.getRaceLog(fleet).removeListener(raceLogCompetitorsCacheInvalidationListener);
            }
        }
        // note: adding listeners was idempotent; at most one occurrence of this listener exists in the race column listeners set
        provider.removeRaceColumnListener(raceColumnListener);
        allCompetitorsCacheByRace.clear(); // this is a little coarse-grained; but given the typical access patterns it should be good enough.
        // The change frequency of competitors lists on individual races is much lower than the access frequency to this structure.
    }

}
