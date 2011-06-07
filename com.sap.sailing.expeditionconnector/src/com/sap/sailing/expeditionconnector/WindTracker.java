package com.sap.sailing.expeditionconnector;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.declination.Declination;
import com.sap.sailing.declination.DeclinationService;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
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
    private static final Logger logger = Logger.getLogger(WindTracker.class.getName());
    
    private final DynamicTrackedRace race;
    
    private final DeclinationService declinationService;

    /**
     * @param declinationService
     *            An optional service to convert the Expedition-provided wind bearings (which Expedition
     *            believes to be true bearings) from magnetic to true bearings. Can be <code>null</code>
     *            in which case the Expedition true bearings are used as true bearings.
     */
    public WindTracker(DynamicTrackedRace race, DeclinationService declinationService) {
        super();
        this.race = race;
        this.declinationService = declinationService;
    }

    @Override
    public void received(ExpeditionMessage message) {
        SpeedWithBearing windSpeed = message.getTrueWind();
        if (declinationService != null) {
            // this tells us that the wind bearing delivered by Expedition hasn't been corrected by the
            // current local declination, so this has to be done here using the declination service:
            try {
                Declination declination = declinationService.getDeclination(message.getTimePoint(), message.getGPSFix().getPosition(),
                        /* timeoutForOnlineFetchInMilliseconds */ 5000);
                if (declination != null) {
                    windSpeed = new KnotSpeedWithBearingImpl(windSpeed.getKnots(),
                            new DegreeBearingImpl(windSpeed.getBearing().getDegrees() +
                            declination.getBearingCorrectedTo(message.getTimePoint()).getDegrees()));
                } else {
                    logger.warning("Unable to obtain declination for wind bearing correction for time point "+message.getTimePoint()+
                            " and position "+message.getGPSFix().getPosition());
                    windSpeed = null;
                }
            } catch (Exception e) {
                logger.log(Level.INFO, "Unable to correct wind bearing by declination. Exception while computing declination.");
                logger.throwing(WindTracker.class.getName(), "received", e);
                windSpeed = null;
            }
        }
        if (windSpeed != null) {
            GPSFix positionAndTime = message.getGPSFix();
            if (windSpeed != null && positionAndTime != null) {
                Wind wind = new WindImpl(positionAndTime.getPosition(), positionAndTime.getTimePoint(), windSpeed);
                race.recordWind(wind, WindSource.EXPEDITION);
            }
        }
    }

}
