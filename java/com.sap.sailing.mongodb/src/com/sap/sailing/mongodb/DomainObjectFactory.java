package com.sap.sailing.mongodb;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.mongodb.impl.DomainObjectFactoryImpl;

/**
 * Offers methods to construct domain objects from {@link DBObject MongoDB objects}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface DomainObjectFactory {
    DomainObjectFactory INSTANCE = new DomainObjectFactoryImpl();

    Wind loadWind(DBObject object);
    
    WindTrack loadWindTrack(Event event, RaceDefinition race, WindSource windSource, long millisecondsOverWhichToAverage, DB database);

    Iterable<TracTracConfiguration> getTracTracConfigurations(DB database);
    
    DB getDefaultDatabase() throws UnknownHostException;
}
