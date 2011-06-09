package com.sap.sailing.mongodb;

import com.mongodb.DBObject;
import com.sap.sailing.domain.tracking.Wind;
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
}
