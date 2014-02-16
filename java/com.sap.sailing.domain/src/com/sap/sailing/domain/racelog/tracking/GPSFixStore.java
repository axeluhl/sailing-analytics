package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;


public interface GPSFixStore {
	/**
	 * Load all fixes that correspond to the {@link DeviceCompetitorMappingEvent}s found in the {@code raceLog}.
	 */
    void loadCompetitorTrack(DynamicGPSFixTrack<Competitor, GPSFixMoving> track, RaceLog raceLog,
            Competitor competitor);

	/**
	 * Load all fixes that correspond to the {@link DeviceMarkMappingEvent}s found in the {@code raceLog}.
	 */
    void loadMarkTrack(DynamicGPSFixTrack<Mark, GPSFix> track, RaceLog raceLog, Mark mark);
    
	/**
	 * Load all fixes that correspond to the {@code mapping}.
	 */
    void loadCompetitorTrack(DynamicGPSFixTrack<Competitor, GPSFixMoving> track, DeviceMapping<Competitor> mapping);

	/**
	 * Load all fixes that correspond to the {@code mapping}.
	 */
    void loadMarkTrack(DynamicGPSFixTrack<Mark, GPSFix> track, DeviceMapping<Mark> mapping);

    void storeFix(DeviceIdentifier device, GPSFix fix);
    
    /**
     * Listeners are notified, whenever a {@link GPSFix} is stored through the {@link #storeFix(DeviceIdentifier, GPSFix)}
     * method.
     */
    void addListener(GPSFixReceivedListener listener);
    
    void removeListener(GPSFixReceivedListener listener);
}
