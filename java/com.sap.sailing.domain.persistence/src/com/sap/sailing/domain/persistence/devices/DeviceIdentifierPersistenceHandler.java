package com.sap.sailing.domain.persistence.devices;

import com.mongodb.DBObject;
import com.sap.sailing.domain.devices.DeviceIdentifier;

public interface DeviceIdentifierPersistenceHandler {
	DBObject store(DeviceIdentifier deviceIdentifier) throws IllegalArgumentException;
	DeviceIdentifier load(DBObject input);
}
