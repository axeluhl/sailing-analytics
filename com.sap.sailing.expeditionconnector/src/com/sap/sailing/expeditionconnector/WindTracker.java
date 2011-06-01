package com.sap.sailing.expeditionconnector;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.impl.WindImpl;

/**
 * Can be subscribed to a {@link UDPExpeditionReceiver} and forwards the wind information
 * received to the {@link DynamicTrackedRace} passed to the constructor.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class WindTracker implements ExpeditionListener {
    private final DynamicTrackedRace race;
    
    public WindTracker(DynamicTrackedRace race) {
        super();
        this.race = race;
    }

    @Override
    public void received(ExpeditionMessage message) {
        SpeedWithBearing windSpeed = message.getTrueWind();
        GPSFix positionAndTime = message.getGPSFix();
        if (windSpeed != null && positionAndTime != null) {
            Wind wind = new WindImpl(positionAndTime.getPosition(), positionAndTime.getTimePoint(), windSpeed);
            race.recordWind(wind, WindSource.EXPEDITION);
        }
    }

}
