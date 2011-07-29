package com.sap.sailing.mongodb;

import com.mongodb.DBObject;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.mongodb.impl.MongoObjectFactoryImpl;
import com.sap.sailing.mongodb.impl.MongoWindStoreFactoryImpl;

/**
 * Offers methods to construct {@link DBObject MongoDB objects} from domain objects.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface MongoObjectFactory {
    MongoObjectFactory INSTANCE = new MongoObjectFactoryImpl(MongoWindStoreFactoryImpl.getDefaultInstance().getDB());

    /**
     * Registers for changes of the wind coming from <code>windSource</code> on the <code>trackedRace</code>. Each
     * update received will be appended to the MongoDB and can later be retrieved. The key used to identify the race is
     * the {@link RaceDefinition#getName() race name} and the {@link Event#getName() event name}.
     */
    void addWindTrackDumper(TrackedEvent trackedEvent, TrackedRace trackedRace, WindSource windSource);

    /**
     * Inserts/updates based on the {@link TracTracConfiguration#getName() name}. Any equally-named
     * config previously contained in the DB will be replaced / updated. Afterwards,
     * {@link DomainObjectFactory#getTracTracConfigurations()} called for the same <code>database</code>
     * will return an equal <code>tracTracConfiguration</code> in its results.
     */
    void storeTracTracConfiguration(TracTracConfiguration tracTracConfiguration);
}
