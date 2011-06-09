package com.sap.sailing.mongodb;

import com.mongodb.DBObject;
import com.sap.sailing.domain.tracking.Wind;
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

}
