package com.sap.sailing.domain.persistence.devices;

import com.sap.sailing.domain.devices.DeviceIdentifier;

public interface DeviceIdentifierPersistenceHandler {
	Object store(DeviceIdentifier deviceIdentifier) throws IllegalArgumentException;
	DeviceIdentifier load(Object input);
}
