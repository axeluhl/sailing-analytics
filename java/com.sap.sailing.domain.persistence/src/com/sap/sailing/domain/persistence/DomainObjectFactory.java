package com.sap.sailing.domain.persistence;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.mongodb.DBObject;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RegattaRegistry;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.LeaderboardRegistry;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindTrack;

/**
 * Offers methods to load domain objects from a Mongo DB
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface DomainObjectFactory {
    /**
     * @param regattaName only needed for backward compatibility because old wind tracks used the regatta name as part of the key
     */
    WindTrack loadWindTrack(String regattaName, RaceDefinition race, WindSource windSource, long millisecondsOverWhichToAverage);

    /**
     * @return the leaderboard loaded, if successful, or <code>null</code> if the leaderboard couldn't be loaded,
     * e.g., because the regatta for a regatta leaderboard couldn't be found
     */
    Leaderboard loadLeaderboard(String name, RegattaRegistry regattaRegistry);

    Iterable<Leaderboard> getAllLeaderboards(RegattaRegistry regattaRegistry, LeaderboardRegistry leaderboardRegistry);

    RaceIdentifier loadRaceIdentifier(DBObject dbObject);
    
    /**
     * @param leaderboardRegistry
     *            if not <code>null</code>, then before creating and loading the leaderboard it is looked up in this
     *            registry and only loaded if not found there. If <code>leaderboardRegistry</code> is <code>null</code>,
     *            the leaderboard is loaded in any case. If the leaderboard is loaded and
     *            <code>leaderboardRegistry</code> is not <code>null</code>, the leaderboard loaded is
     *            {@link LeaderboardRegistry#addLeaderboard(Leaderboard) added to the registry}.
     * @return The group with the name <code>name</code>, or <code>null</code> if no such group exists.
     */
    LeaderboardGroup loadLeaderboardGroup(String name, RegattaRegistry regattaRegistry, LeaderboardRegistry leaderboardRegistry);
    
    /**
     * @param leaderboardRegistry
     *            if not <code>null</code>, then before creating and loading the leaderboard it is looked up in this
     *            registry and only loaded if not found there. If <code>leaderboardRegistry</code> is <code>null</code>,
     *            the leaderboard is loaded in any case. If the leaderboard is loaded and
     *            <code>leaderboardRegistry</code> is not <code>null</code>, the leaderboard loaded is
     *            {@link LeaderboardRegistry#addLeaderboard(Leaderboard) added to the registry}.
     * @return All groups in the database.
     */
    Iterable<LeaderboardGroup> getAllLeaderboardGroups(RegattaRegistry regattaRegistry, LeaderboardRegistry leaderboardRegistry);
    
    /**
     * @param leaderboardRegistry
     *            if not <code>null</code>, then before creating and loading the leaderboard it is looked up in this
     *            registry and only loaded if not found there. If <code>leaderboardRegistry</code> is <code>null</code>,
     *            the leaderboard is loaded in any case. If the leaderboard is loaded and
     *            <code>leaderboardRegistry</code> is not <code>null</code>, the leaderboard loaded is
     *            {@link LeaderboardRegistry#addLeaderboard(Leaderboard) added to the registry}.
     * @return All leaderboards in the database, which aren't contained by a leaderboard group
     */
    Iterable<Leaderboard> getLeaderboardsNotInGroup(RegattaRegistry regattaRegistry, LeaderboardRegistry leaderboardRegistry);

    Map<? extends WindSource, ? extends WindTrack> loadWindTracks(String regattaName, RaceDefinition race,
            long millisecondsOverWhichToAverageWind);

    Event loadEvent(String name);

    Iterable<Event> loadAllEvents();

    Regatta loadRegatta(String name, TrackedRegattaRegistry trackedRegattaRegistry);

    Iterable<Regatta> loadAllRegattas(TrackedRegattaRegistry trackedRegattaRegistry);

    Map<String, Regatta> loadRaceIDToRegattaAssociations(RegattaRegistry regattaRegistry);

    RaceLog loadRaceLog(RaceLogIdentifier identifier);
    
    /**
     * Loads all competitors, and resolves them via the domain factory.
     * @return
     */
    Collection<Competitor> loadAllCompetitors();

    DomainFactory getBaseDomainFactory();

    Iterable<Entry<DeviceConfigurationMatcher, DeviceConfiguration>> loadAllDeviceConfigurations();
}
