package com.sap.sailing.domain.persistence.racelog.tracking;

import com.mongodb.DBObject;
import com.sap.sailing.domain.common.racelog.tracking.TransformationHandler;
import com.sap.sailing.domain.common.racelog.tracking.TypeBasedServiceFinder;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;

/**
 * {@link DeviceIdentifier} objects need to be stored in the database, e.g., for
 * {@link RaceLog} persistence, when writing an object of type
 * {@link DeviceCompetitorMappingEvent}. In conjunction with a
 * {@link TypeBasedServiceFinder} it is possible to extend the set of device
 * types whose device identifiers can be stored to and loaded from the database
 * by providing specific implementations of this handler interface. In an OSGi
 * context, bundles can register implementations of this handler interface with
 * the OSGi service registry to extend the set of supported device types without
 * modifications to the existing code base.
 * 
 * The resulting {@link Object} is expected to be either a simple {@link Object}
 * that can serialized, or a structured {@link DBObject}.
 * 
 * @author Fredrik Teschke
 * 
 */
public interface DeviceIdentifierMongoHandler extends
TransformationHandler<DeviceIdentifier, Object> {
}
