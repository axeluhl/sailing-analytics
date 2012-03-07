package com.sap.sailing.domain.persistence;

import java.util.Map;

import com.mongodb.DBObject;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.mongodb.MongoDBService;

/**
 * Offers methods to load domain objects from a Mongo DB
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface DomainObjectFactory {
    DomainObjectFactory INSTANCE = new DomainObjectFactoryImpl(MongoDBService.INSTANCE.getDB());

    WindTrack loadWindTrack(Event event, RaceDefinition race, WindSource windSource, long millisecondsOverWhichToAverage);

    Leaderboard loadLeaderboard(String name);

    Iterable<Leaderboard> getAllLeaderboards();

    RaceIdentifier loadRaceIdentifier(DBObject dbObject);
    
    /**
     * @return The group with the name <code>name</code>, or <code>null</code> if no such group exists.
     */
    LeaderboardGroup loadLeaderboardGroup(String name);
    
    /**
     * @return All groups in the database.
     */
    Iterable<LeaderboardGroup> getAllLeaderboardGroups();
    
    /**
     * @return All leaderboards in the database, which aren't contained by a leaderboard group
     */
    Iterable<Leaderboard> getLeaderboardsNotInGroup();

    Map<? extends WindSource, ? extends WindTrack> loadWindTracks(Event event, RaceDefinition race,
            long millisecondsOverWhichToAverageWind);

}
