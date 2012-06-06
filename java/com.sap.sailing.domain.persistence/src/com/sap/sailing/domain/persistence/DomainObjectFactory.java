package com.sap.sailing.domain.persistence;

import java.util.Map;

import com.mongodb.DBObject;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RegattaRegistry;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindTrack;

/**
 * Offers methods to load domain objects from a Mongo DB
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface DomainObjectFactory {
    WindTrack loadWindTrack(Regatta regatta, RaceDefinition race, WindSource windSource, long millisecondsOverWhichToAverage);

    Leaderboard loadLeaderboard(String name, RegattaRegistry regattaRegistry);

    Iterable<Leaderboard> getAllLeaderboards(RegattaRegistry regattaRegistry);

    RaceIdentifier loadRaceIdentifier(DBObject dbObject);
    
    /**
     * @return The group with the name <code>name</code>, or <code>null</code> if no such group exists.
     */
    LeaderboardGroup loadLeaderboardGroup(String name, RegattaRegistry regattaRegistry);
    
    /**
     * @return All groups in the database.
     */
    Iterable<LeaderboardGroup> getAllLeaderboardGroups(RegattaRegistry regattaRegistry);
    
    /**
     * @return All leaderboards in the database, which aren't contained by a leaderboard group
     */
    Iterable<Leaderboard> getLeaderboardsNotInGroup(RegattaRegistry regattaRegistry);

    Map<? extends WindSource, ? extends WindTrack> loadWindTracks(Regatta regatta, RaceDefinition race,
            long millisecondsOverWhichToAverageWind);

    Event loadEvent(String name);

    Iterable<Event> loadAllEvents();

    Regatta loadRegatta(String name, TrackedRegattaRegistry trackedRegattaRegistry);

    Iterable<Regatta> loadAllRegattas(TrackedRegattaRegistry trackedRegattaRegistry);

    Map<String, Regatta> loadRaceIDToRegattaAssociations(RegattaRegistry regattaRegistry);
}
