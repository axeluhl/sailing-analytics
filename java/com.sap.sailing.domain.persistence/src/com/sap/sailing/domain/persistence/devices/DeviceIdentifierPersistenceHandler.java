package com.sap.sailing.domain.persistence.devices;

import com.mongodb.DBObject;
import com.sap.sailing.domain.devices.DeviceIdentifier;

public interface DeviceIdentifierPersistenceHandler {
	/**
	 * Enrich the base object with the necessary data for this specific
	 * device identifier type.
	 * @param base
	 * @param deviceIdentifiers
	 * @return
	 */
	DBObject enrichDBObject(DBObject base, DeviceIdentifier deviceIdentifier) throws IllegalArgumentException;
	
	DeviceIdentifier loadFromDBObject(DBObject input);
}
