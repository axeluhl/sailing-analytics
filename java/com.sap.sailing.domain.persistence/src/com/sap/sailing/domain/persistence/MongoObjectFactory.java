package com.sap.sailing.domain.persistence;

import com.mongodb.DBObject;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.mongodb.MongoDBService;

/**
 * Offers methods to construct {@link DBObject MongoDB objects} from domain objects.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface MongoObjectFactory {
    MongoObjectFactory INSTANCE = new MongoObjectFactoryImpl(MongoDBService.INSTANCE.getDB());

    /**
     * Registers for changes of the wind coming from <code>windSource</code> on the <code>trackedRace</code>. Each
     * update received will be appended to the MongoDB and can later be retrieved. The key used to identify the race is
     * the {@link RaceDefinition#getName() race name} and the {@link Event#getName() event name}.
     */
    void addWindTrackDumper(TrackedEvent trackedEvent, TrackedRace trackedRace, WindSource windSource);

    /**
     * Stores the configuration data of <code>leaderboard</code> in the Mongo DB associated with this
     * factory. 
     */
    void storeLeaderboard(Leaderboard leaderboard);
    
    /**
     * Removes the leaderboard named <code>name</code> from the database.
     */
    void removeLeaderboard(String leaderboardName);

    void renameLeaderboard(String oldName, String newName);

    void storeRaceIdentifier(RaceIdentifier raceIdentifier, DBObject dbObject);
    
    void storeLeaderboardGroup(LeaderboardGroup leaderboardGroup);

}
