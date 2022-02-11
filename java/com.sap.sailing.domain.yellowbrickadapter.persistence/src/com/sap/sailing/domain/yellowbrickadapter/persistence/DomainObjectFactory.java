package com.sap.sailing.domain.yellowbrickadapter.persistence;

import com.sap.sailing.domain.yellowbrickadapter.YellowBrickConfiguration;

/**
 * Offers methods to load domain objects from a Mongo DB
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface DomainObjectFactory {
    Iterable<YellowBrickConfiguration> getYellowBrickConfigurations();
}
