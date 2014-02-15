package com.sap.sailing.domain.racelog.tracking;

import java.util.Collections;
import java.util.Map;

import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.impl.DynamicTrackImpl;

public enum EmptyGPSFixStore implements GPSFixStore {
    INSTANCE;

	@Override
	public DynamicTrack<GPSFix> getTrack(DeviceIdentifier device) {
		return new DynamicTrackImpl<GPSFix>(DynamicTrackImpl.class.getName());
	}

	@Override
	public Map<DeviceIdentifier, DynamicTrack<GPSFix>> loadTracks() {
		return Collections.emptyMap();
	}

}
