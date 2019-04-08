package com.sap.sailing.domain.tractracadapter.persistence;

import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;

/**
 * Offers methods to load domain objects from a Mongo DB
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface DomainObjectFactory {
    Iterable<TracTracConfiguration> getTracTracConfigurations();
}
