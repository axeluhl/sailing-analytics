package com.sap.sailing.domain.persistence.racelog.tracking.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.TrackListener;

public class MongoGPSFixTrackListenerImpl implements TrackListener<GPSFix> {
	private static final Logger logger = Logger.getLogger(MongoGPSFixTrackListenerImpl.class.getSimpleName());
	private static final long serialVersionUID = -2680825936596106216L;
	private final DeviceIdentifier device;
	private final MongoObjectFactory mongoObjectFactory;

	public MongoGPSFixTrackListenerImpl(DeviceIdentifier device,
			MongoObjectFactory mongoObjectFactory) {
		this.device = device;
		this.mongoObjectFactory = mongoObjectFactory;
	}

	@Override
	public void fixReceived(GPSFix fix) {
		try {
			mongoObjectFactory.storeGPSFix(device, fix);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Could not store fix", e);
			e.printStackTrace();
		}
	}

	@Override
	public boolean isTransient() {
		return true;
	}

}
