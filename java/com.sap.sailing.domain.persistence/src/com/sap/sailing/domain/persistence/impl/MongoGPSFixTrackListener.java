package com.sap.sailing.domain.persistence.impl;

import com.sap.sailing.domain.devices.DeviceIdentifier;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.TrackListener;

public class MongoGPSFixTrackListener implements TrackListener<GPSFix> {
	private static final long serialVersionUID = -2680825936596106216L;
	private final DeviceIdentifier device;
	private final MongoObjectFactory mongoObjectFactory;

	public MongoGPSFixTrackListener(DeviceIdentifier device,
			MongoObjectFactory mongoObjectFactory) {
		this.device = device;
		this.mongoObjectFactory = mongoObjectFactory;
	}

	@Override
	public void fixReceived(GPSFix fix) {
		mongoObjectFactory.storeGPSFix(device, fix);
	}

	@Override
	public boolean isTransient() {
		return true;
	}

}
