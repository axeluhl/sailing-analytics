package com.sap.sailing.mongodb;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.mongodb.impl.MongoObjectFactoryImpl;

/**
 * Offers methods to construct {@link DBObject MongoDB objects} from domain objects.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface MongoObjectFactory {
    MongoObjectFactory INSTANCE = new MongoObjectFactoryImpl();

    DBObject storeWind(Wind wind);

    /**
     * Registers for changes of the wind coming from <code>windSource</code> on the <code>trackedRace</code>. Each
     * update received will be appended to the MongoDB and can later be retrieved. The key used to identify the race is
     * the {@link RaceDefinition#getName() race name} and the {@link Event#getName() event name}.
     * 
     * @param database
     *            the MongoDB database to dump the wind received by the {@link TrackedRace}'s <code>windSource</code> to
     */
    void addWindTrackDumper(TrackedEvent trackedEvent, TrackedRace trackedRace, WindSource windSource, DB database);

    DBCollection getWindTrackCollection(DB database);

    DBObject storeWindTrackEntry(Event event, RaceDefinition race, WindSource windSource, Wind wind);
}
