package com.sap.sailing.domain.persistence.racelog.tracking.impl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.racelog.tracking.MongoGPSFixStore;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.GPSFix;

public class MongoGPSFixStoreImpl implements MongoGPSFixStore {
	private static final Logger logger = Logger.getLogger(MongoGPSFixStore.class.getName());
	
	private final MongoObjectFactory mongoObjectFactory;
	private final DomainObjectFactory domainObjectFactory;
	
    private final ConcurrentHashMap<DeviceIdentifier, DynamicTrack<GPSFix>> tracksByDevice = new ConcurrentHashMap<>();
    
    private void addListener(DeviceIdentifier device, DynamicTrack<GPSFix> track) {
        track.addTrackListener(new MongoGPSFixTrackListenerImpl(device, mongoObjectFactory));
    }

	public MongoGPSFixStoreImpl(MongoObjectFactory mongoObjectFactory,
			DomainObjectFactory domainObjectFactory) {
		this.mongoObjectFactory = mongoObjectFactory;
		this.domainObjectFactory = domainObjectFactory;
	}

	@Override
	public DynamicTrack<GPSFix> getTrack(DeviceIdentifier device) {
		DynamicTrack<GPSFix> track = tracksByDevice.get(device);
		if (track == null) {
			synchronized (tracksByDevice) {
				track = tracksByDevice.get(device);
				if (track == null) {
					try {
						track = domainObjectFactory.loadGPSFixTrack(device);
					} catch (Exception e) {
						logger.log(Level.WARNING, "Failed to get GPS track", e);
						e.printStackTrace();
					}
					addListener(device, track);
					tracksByDevice.put(device, track);
				}
			}
		}
		return track;
	}

	@Override
	public Map<DeviceIdentifier, DynamicTrack<GPSFix>> loadTracks() {
		/*
		 * TODO load tracks in seperate thread, or even better: use proxy
		 * tracks, that on first load only load metadata (beginning and end
		 * TimePoints of individual tracking sessions in track, and then load
		 * the fixes when really needed.
		 */
		try {
			for (Entry<DeviceIdentifier, DynamicTrack<GPSFix>> entry : domainObjectFactory
					.loadAllGPSFixTracks().entrySet()) {
				synchronized (tracksByDevice) {
					tracksByDevice.put(entry.getKey(), entry.getValue());
					addListener(entry.getKey(), entry.getValue());
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to load GPS tracks", e);
			e.printStackTrace();
		}
		return tracksByDevice;
	}

}
