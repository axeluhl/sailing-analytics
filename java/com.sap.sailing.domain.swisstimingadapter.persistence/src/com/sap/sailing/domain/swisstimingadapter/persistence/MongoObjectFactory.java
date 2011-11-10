package com.sap.sailing.domain.swisstimingadapter.persistence;

import com.mongodb.DBObject;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;
import com.sap.sailing.domain.swisstimingadapter.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.mongodb.Activator;

/**
 * Offers methods to construct {@link DBObject MongoDB objects} from domain objects.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface MongoObjectFactory {
    MongoObjectFactory INSTANCE = new MongoObjectFactoryImpl(Activator.getDefaultInstance().getDB());

    void storeSwissTimingConfiguration(SwissTimingConfiguration swissTimingConfiguration);
    
    void storeRawSailMasterMessage(SailMasterMessage message);

    void storeRace(Race race);
}
