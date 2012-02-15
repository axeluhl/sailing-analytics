package com.sap.sailing.expeditionconnector;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.declination.Declination;
import com.sap.sailing.declination.DeclinationService;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTracker;
import com.sap.sailing.domain.tracking.impl.WindImpl;

/**
 * Can be subscribed to a {@link UDPExpeditionReceiver} and forwards the wind information
 * received to the {@link DynamicTrackedRace} passed to the constructor.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class ExpeditionWindTracker implements ExpeditionListener, WindTracker {
    private static final Logger logger = Logger.getLogger(ExpeditionWindTracker.class.getName());
    
    private final DynamicTrackedRace race;
    
    private final DeclinationService declinationService;
    
    private Position lastKnownPosition;
    
    private final UDPExpeditionReceiver receiver;
    
    private final ExpeditionWindTrackerFactory factory;

    /**
     * @param declinationService
     *            An optional service to convert the Expedition-provided wind bearings (which Expedition believes to be
     *            true bearings) from magnetic to true bearings. Can be <code>null</code> in which case the Expedition
     *            true bearings are used as true bearings.
     * @param receiver
     *            receive wind data from this receiver by adding the new object as a listener to the receiver; when
     *            calling {@link #stop}, this subscription will be removed again.
     */
    public ExpeditionWindTracker(DynamicTrackedRace race, DeclinationService declinationService,
            UDPExpeditionReceiver receiver, ExpeditionWindTrackerFactory factory) {
        super();
        this.race = race;
        this.declinationService = declinationService;
        this.receiver = receiver;
        this.factory = factory;
        receiver.addListener(this, /* validMessagesOnly */ true);
    }
    
    @Override
    public void stop() {
        synchronized (factory) {
            receiver.removeListener(this);
            factory.windTrackerStopped(race.getRace(), this);
        }
    }
    
    UDPExpeditionReceiver getReceiver() {
        return receiver;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+" on UDP port "+getReceiver().getPort();
    }

    @Override
    public void received(ExpeditionMessage message) {
        if (message.getGPSFix() != null) {
            lastKnownPosition = message.getGPSFix().getPosition();
        }
        SpeedWithBearing windSpeed = message.getTrueWind();
        if (windSpeed != null) {
            if (declinationService != null) {
                // this tells us that the wind bearing delivered by Expedition hasn't been corrected by the
                // current local declination, so this has to be done here using the declination service:
                if (lastKnownPosition != null) {
                    try {
                        Declination declination = declinationService.getDeclination(message.getTimePoint(),
                                lastKnownPosition,
                                /* timeoutForOnlineFetchInMilliseconds */5000);
                        if (declination != null) {
                            windSpeed = new KnotSpeedWithBearingImpl(windSpeed.getKnots(), new DegreeBearingImpl(
                                    windSpeed.getBearing().getDegrees()
                                            + declination.getBearingCorrectedTo(message.getTimePoint()).getDegrees()));
                        } else {
                            logger.warning("Unable to obtain declination for wind bearing correction for time point "
                                    + message.getTimePoint() + " and position " + lastKnownPosition);
                            windSpeed = null;
                        }
                    } catch (Exception e) {
                        logger.log(Level.INFO,
                                "Unable to correct wind bearing by declination. Exception while computing declination: "
                                        + e.getMessage());
                        logger.throwing(ExpeditionWindTracker.class.getName(), "received", e);
                        windSpeed = null;
                    }
                } else {
                    // no position known
                    windSpeed = null;
                    logger.log(Level.INFO,
                            "Unable to use wind direction because declination correction requested and position not known");
                }
            }
            if (windSpeed != null && lastKnownPosition != null) {
                Wind wind = new WindImpl(lastKnownPosition, message.getTimePoint(), windSpeed);
                race.recordWind(wind, WindSource.EXPEDITION);
            }
        }
    }

}
