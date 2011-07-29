package com.sap.sailing.mongodb;

import com.mongodb.DBObject;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.mongodb.impl.DomainObjectFactoryImpl;
import com.sap.sailing.mongodb.impl.MongoWindStoreFactoryImpl;

/**
 * Offers methods to construct domain objects from {@link DBObject MongoDB objects}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface DomainObjectFactory {
    DomainObjectFactory INSTANCE = new DomainObjectFactoryImpl(MongoWindStoreFactoryImpl.getDefaultInstance().getDB());

    WindTrack loadWindTrack(Event event, RaceDefinition race, WindSource windSource, long millisecondsOverWhichToAverage);

    Iterable<TracTracConfiguration> getTracTracConfigurations();
}
