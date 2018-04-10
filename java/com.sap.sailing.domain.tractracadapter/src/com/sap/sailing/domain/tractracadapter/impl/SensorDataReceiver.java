package com.sap.sailing.domain.tractracadapter.impl;

import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.sensordata.BravoSensorDataMetadata;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.common.tracking.impl.BravoFixImpl;
import com.sap.sailing.domain.common.tracking.impl.DoubleVectorFixImpl;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sailing.domain.tracking.DynamicBravoFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.impl.CompetitorBravoFixTrackImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.tractrac.model.lib.api.event.IEvent;
import com.tractrac.model.lib.api.event.IRace;
import com.tractrac.model.lib.api.event.IRaceCompetitor;
import com.tractrac.model.lib.api.sensor.ISensorData;
import com.tractrac.subscription.lib.api.IEventSubscriber;
import com.tractrac.subscription.lib.api.IRaceSubscriber;
import com.tractrac.subscription.lib.api.competitor.ICompetitorSensorDataListener;

/**
 * Subscribes an 
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class SensorDataReceiver extends AbstractReceiverWithQueue<IRaceCompetitor, ISensorData, Void> {
    private static final Logger logger = Logger.getLogger(SensorDataReceiver.class.getName());
    private final ICompetitorSensorDataListener listener;
    private int received;

    public SensorDataReceiver(DynamicTrackedRegatta trackedRegatta, IEvent tractracEvent,
            Simulator simulator, DomainFactory domainFactory, IEventSubscriber eventSubscriber,
            IRaceSubscriber raceSubscriber, long timeoutInMilliseconds) {
        super(domainFactory, tractracEvent, trackedRegatta, simulator, eventSubscriber, raceSubscriber, timeoutInMilliseconds);
        listener = new ICompetitorSensorDataListener() {
            @Override
            public void gotSensorData(IRaceCompetitor raceCompetitor, ISensorData sensorData) {
                enqueue(new Triple<>(raceCompetitor, sensorData, null));
            }
        };
    }

    @Override
    public void subscribe() {
        getRaceSubscriber().subscribeCompetitorSensorData(listener);
        startThread();
    }
    
    @Override
    protected void unsubscribe() {
        getRaceSubscriber().unsubscribeCompetitorSensorData(listener);
    }

    /**
     * Looks for valid, non-{@code null} components for ride height port/starboard, heel and trim
     * in the {@link ISensorData} object. All those values are copied into a {@link BravoFix} and
     * inserted into the competitor's bravo fix track.
     */
    @Override
    protected void handleEvent(Triple<IRaceCompetitor, ISensorData, Void> event) {
        if (received++ % 1000 == 0) {
            System.out.print("S");
            if ((received / 1000 + 1) % 80 == 0) {
                System.out.println();
            }
        }
        final IRace race = event.getA().getRace();
        DynamicTrackedRace trackedRace = getTrackedRace(race);
        if (trackedRace != null) {
            final ISensorData sensorData = event.getB();
            if (sensorData != null) {
                final Float rideHeightPortInMeters = sensorData.getRideHeightPort();
                final Float rideHeightStarboardInMeters = sensorData.getRideHeightStarboard();
                final Float heelInDegrees = sensorData.getHeel();
                final Float trimInDegrees = sensorData.getTrim();
                final Double[] fixData = new Double[BravoSensorDataMetadata.getTrackColumnCount()];
                boolean create = false; // only create fix if at least one metric is present
                // fill the port/starboard columns as well because their minimum defines the true ride height
                if (rideHeightPortInMeters != null) {
                    fixData[BravoSensorDataMetadata.RIDE_HEIGHT_PORT_HULL.getColumnIndex()] = Double.valueOf(rideHeightPortInMeters);
                    create = true;
                }
                if (rideHeightStarboardInMeters != null) {
                    fixData[BravoSensorDataMetadata.RIDE_HEIGHT_STBD_HULL.getColumnIndex()] = Double.valueOf(rideHeightStarboardInMeters);
                    create = true;
                }
                if (heelInDegrees != null) {
                    fixData[BravoSensorDataMetadata.HEEL.getColumnIndex()] = Double.valueOf(heelInDegrees);
                    create = true;
                }
                if (trimInDegrees != null) {
                    fixData[BravoSensorDataMetadata.PITCH.getColumnIndex()] = Double.valueOf(trimInDegrees);
                    create = true;
                }
                if (create) {
                    final BravoFix fix = new BravoFixImpl(new DoubleVectorFixImpl(new MillisecondsTimePoint(sensorData.getTimestamp()), fixData));
                    final Competitor competitor = getDomainFactory().resolveCompetitor(event.getA().getCompetitor());
                    DynamicBravoFixTrack<Competitor> bravoFixTrack =
                            trackedRace.getOrCreateSensorTrack(competitor, BravoFixTrack.TRACK_NAME,
                                    () -> new CompetitorBravoFixTrackImpl(competitor, BravoFixTrack.TRACK_NAME, /* hasExtendedFixes */ false,
                                            trackedRace.getTrack(competitor)));
                    if (getSimulator() != null) {
                        getSimulator().scheduleCompetitorSensorData(bravoFixTrack, fix);
                    } else {
                        bravoFixTrack.add(fix);
                    }
                }
            }
        } else {
            logger.warning("Couldn't find tracked race for race " + race.getName()
                    + ". Dropping sensor event " + event);
        }
    }

}
