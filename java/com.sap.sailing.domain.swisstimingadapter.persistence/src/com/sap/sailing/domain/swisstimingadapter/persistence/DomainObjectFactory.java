package com.sap.sailing.domain.swisstimingadapter.persistence;

import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;
import com.sap.sailing.domain.swisstimingadapter.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.mongodb.Activator;

/**
 * Offers methods to load domain objects from a Mongo DB
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface DomainObjectFactory {
    DomainObjectFactory INSTANCE = new DomainObjectFactoryImpl(Activator.getDefaultInstance().getDB());

    Iterable<SwissTimingConfiguration> getSwissTimingConfigurations();
}
