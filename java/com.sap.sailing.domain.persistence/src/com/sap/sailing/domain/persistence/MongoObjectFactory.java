package com.sap.sailing.domain.persistence;

import com.mongodb.DBObject;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;

/**
 * Offers methods to construct {@link DBObject MongoDB objects} from domain objects.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface MongoObjectFactory {
    /**
     * Registers for changes of the wind coming from <code>windSource</code> on the <code>trackedRace</code>. Each
     * update received will be appended to the MongoDB and can later be retrieved. The key used to identify the race is
     * the {@link RaceDefinition#getName() race name} and the {@link Regatta#getName() regatta name}.
     */
    void addWindTrackDumper(TrackedRegatta trackedRegatta, TrackedRace trackedRace, WindSource windSource);

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

    /**
     * Stores the group, if it doesn't exist or updates it.<br />
     * Leaderboards in the group, which aren't stored in the database, will be stored.
     */
    void storeLeaderboardGroup(LeaderboardGroup leaderboardGroup);
    
    /**
     * Removes the group with the name <code>groupName</code> from the database.
     */
    void removeLeaderboardGroup(String groupName);

    /**
     * Renames the group with the name <code>oldName</code>.
     */
    void renameLeaderboardGroup(String oldName, String newName);

    /**
     * Stores the event with its name, venue and the venue's course areas. The regattas obtained by
     * {@link Event#getRegattas()} are <em>not</em> stored by this call. They need to be stored separately
     * by calls to {@link #storeRegatta} where a reference to their owning event is stored. 
     */
    void storeEvent(Event event);

    /**
     * Renames the event with the name <code>oldName</code>.
     */
    void renameEvent(String oldName, String newName);

    /**
     * Removes the event named <code>eventName</code> from the database.
     */
    void removeEvent(String eventName);

    /**
     * Stores the regatta together with its name, {@link Series} definitions and an optional link to the
     * {@link Event} to which the regatta belongs.
     */
    void storeRegatta(Regatta regatta);

    void removeRegatta(Regatta regatta);

    void storeRegattaForRaceID(String id, Regatta regatta);

}
