package com.sap.sailing.domain.igtimiadapter.impl;

import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.declination.Declination;
import com.sap.sailing.declination.DeclinationService;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.base.impl.ScalableBearing;
import com.sap.sailing.domain.base.impl.ScalablePosition;
import com.sap.sailing.domain.base.impl.ScalableSpeed;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.confidence.ScalableValue;
import com.sap.sailing.domain.igtimiadapter.BulkFixReceiver;
import com.sap.sailing.domain.igtimiadapter.FixVisitor;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiFixReceiverAdapter;
import com.sap.sailing.domain.igtimiadapter.datatypes.AWA;
import com.sap.sailing.domain.igtimiadapter.datatypes.AWS;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.datatypes.GpsLatLong;
import com.sap.sailing.domain.igtimiadapter.datatypes.HDG;
import com.sap.sailing.domain.igtimiadapter.datatypes.HDGM;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.DynamicTrackImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;

public class IgtimiWindTrackImpl extends WindTrackImpl {
    private static final long serialVersionUID = 2362868507967460430L;
    private static final Logger logger = Logger.getLogger(IgtimiWindTrackImpl.class.getName());
    private final DynamicTrack<AWA> awaTrack;
    private final DynamicTrack<AWS> awsTrack;
    private final DynamicTrack<GpsLatLong> gpsTrack;
    private final DynamicTrack<HDG> hdgTrack;
    private final DynamicTrack<HDGM> hdgmTrack;
    private final BulkFixReceiver receiver;
    private final DeclinationService declinationService;

    public IgtimiWindTrackImpl(long millisecondsOverWhichToAverage, String deviceSerialNumber) {
        this(millisecondsOverWhichToAverage, DEFAULT_BASE_CONFIDENCE, deviceSerialNumber);
    }

    public IgtimiWindTrackImpl(long millisecondsOverWhichToAverage, double baseConfidence, String deviceSerialNumber) {
        // TODO probably hook up the receiver to the IgtimiConnection right away, asking for stored and live data
        // TODO consider batch updates which then would typically first fill the tracks and only the look for new wind fixes that may be generated from the additions to the tracks
        super(millisecondsOverWhichToAverage, baseConfidence, /* useSpeed */ true, /* nameForReadWriteLock */ "Igtimi wind track for device "+deviceSerialNumber);
        declinationService = DeclinationService.INSTANCE;
        awaTrack = new DynamicTrackImpl<>("AWA Track for Igtimi wind track for device "+deviceSerialNumber);
        awsTrack = new DynamicTrackImpl<>("AWS Track for Igtimi wind track for device "+deviceSerialNumber);
        gpsTrack = new DynamicTrackImpl<>("GPS Track for Igtimi wind track for device "+deviceSerialNumber);
        hdgTrack = new DynamicTrackImpl<>("HDG Track for Igtimi wind track for device "+deviceSerialNumber);
        hdgmTrack = new DynamicTrackImpl<>("HDGM Track for Igtimi wind track for device "+deviceSerialNumber);
        receiver = new FixVisitor(new IgtimiFixReceiverAdapter() {
            @Override
            public void received(AWA fix) {
                awaTrack.add(fix);
            }

            @Override
            public void received(AWS fix) {
                awsTrack.add(fix);
            }

            @Override
            public void received(GpsLatLong fix) {
                gpsTrack.add(fix);
            }

            @Override
            public void received(HDG fix) {
                hdgTrack.add(fix);
            }

            @Override
            public void received(HDGM fix) {
                hdgmTrack.add(fix);
            }
        }) {
            /**
             * Called when a batch of fixes was received, e.g., after loading from stored data or from a live web socket receiver.
             * The fixes are pumped into the tracks by the superclass implementation. Then, for all new {@link AWS} fixes, a
             * call to {@link IgtimiWindTrackImpl#getWind} is performed.
             */
            @Override
            public void received(Iterable<Fix> fixes) {
                super.received(fixes);
                for (Fix fix : fixes) {
                    fix.notify(new IgtimiFixReceiverAdapter() {
                        @Override
                        public void received(AWS fix) {
                            try {
                                final Wind wind = getWind(fix.getTimePoint());
                                if (wind != null) {
                                    add(wind);
                                }
                            } catch (ClassNotFoundException | IOException | ParseException e) {
                                logger.log(Level.INFO, "Exception while trying to construct Wind fix from Igtimi fix "+fix, e);
                            }
                        }
                    });
                }
            }
        };
    }
    
    /**
     * Obtains the fix receiver that can be used to subscribe to a web socket-based live data connection as well as for calls to
     * {@link IgtimiConnection#getAndNotifyResourceData(TimePoint, TimePoint, Iterable, BulkFixReceiver, com.sap.sailing.domain.igtimiadapter.datatypes.Type...)}.
     * When sending fixes to the receiver returned, those fixes will be recorded in this object, and after the batch has been received, for all {@link AWS} fixes
     * received in the batch, a new {@link Wind} fix is added to this wind track.
     */
    public BulkFixReceiver getReceiver() {
        return receiver;
    }

    private Wind getWind(TimePoint timePoint) throws ClassNotFoundException, IOException, ParseException {
        final Wind result;
        Pair<AWA, AWA> awaPair = getSurroundingFixes(awaTrack, timePoint);
        Bearing awa = getAWA(timePoint, awaPair);
        Pair<AWS, AWS> awsPair = getSurroundingFixes(awsTrack, timePoint);
        Speed aws = getAWS(timePoint, awsPair);
        Pair<GpsLatLong, GpsLatLong> gpsPair = getSurroundingFixes(gpsTrack, timePoint);
        Position pos = getPosition(timePoint, gpsPair);
        Pair<HDG, HDG> hdgPair = getSurroundingFixes(hdgTrack, timePoint);
        Pair<HDGM, HDGM> hdgmPair = getSurroundingFixes(hdgmTrack, timePoint);
        Bearing heading = getHeading(timePoint, hdgPair, hdgmPair, pos);
        if (awa != null && aws != null && pos != null && heading != null) {
            Bearing trueWindDirection = heading.add(awa);
            result = new WindImpl(pos, timePoint, new KnotSpeedWithBearingImpl(aws.getKnots(), trueWindDirection));
        } else {
            result = null;
        }
        return result;
    }

    private Bearing getAWA(TimePoint timePoint, Pair<AWA, AWA> awaPair) {
        final Bearing awa;
        if (awaPair.getA() == null) {
            if (awaPair.getB() == null) {
                awa = null;
            } else {
                awa = awaPair.getB().getApparentWindAngle();
            }
        } else {
            if (awaPair.getB() == null) {
                awa = awaPair.getA().getApparentWindAngle();
            } else {
                awa = timeBasedAgerage(timePoint,
                        new ScalableBearing(awaPair.getA().getApparentWindAngle()), awaPair.getA().getTimePoint(),
                        new ScalableBearing(awaPair.getB().getApparentWindAngle()), awaPair.getB().getTimePoint());
            }
        }
        return awa;
    }

    private Speed getAWS(TimePoint timePoint, Pair<AWS, AWS> awsPair) {
        final Speed aws;
        if (awsPair.getA() == null) {
            if (awsPair.getB() == null) {
                aws = null;
            } else {
                aws = awsPair.getB().getApparentWindSpeed();
            }
        } else {
            if (awsPair.getB() == null) {
                aws = awsPair.getA().getApparentWindSpeed();
            } else {
                aws = timeBasedAgerage(timePoint,
                        new ScalableSpeed(awsPair.getA().getApparentWindSpeed()), awsPair.getA().getTimePoint(),
                        new ScalableSpeed(awsPair.getB().getApparentWindSpeed()), awsPair.getB().getTimePoint());
            }
        }
        return aws;
    }

    private Position getPosition(TimePoint timePoint, Pair<GpsLatLong, GpsLatLong> gpsPair) {
        final Position pos;
        if (gpsPair.getA() == null) {
            if (gpsPair.getB() == null) {
                pos = null;
            } else {
                pos = gpsPair.getB().getPosition();
            }
        } else {
            if (gpsPair.getB() == null) {
                pos = gpsPair.getA().getPosition();
            } else {
                pos = timeBasedAgerage(timePoint, new ScalablePosition(gpsPair.getA().getPosition()), gpsPair.getA()
                        .getTimePoint(), new ScalablePosition(gpsPair.getB().getPosition()), gpsPair.getB()
                        .getTimePoint());
            }
        }
        return pos;
    }

    private Bearing getHDG(TimePoint timePoint, Pair<HDG, HDG> hdgPair) {
        final Bearing hdg;
        if (hdgPair.getA() == null) {
            if (hdgPair.getB() == null) {
                hdg = null;
            } else {
                hdg = hdgPair.getB().getTrueHeading();
            }
        } else {
            if (hdgPair.getB() == null) {
                hdg = hdgPair.getA().getTrueHeading();
            } else {
                hdg = timeBasedAgerage(timePoint,
                        new ScalableBearing(hdgPair.getA().getTrueHeading()), hdgPair.getA().getTimePoint(),
                        new ScalableBearing(hdgPair.getB().getTrueHeading()), hdgPair.getB().getTimePoint());
            }
        }
        return hdg;
    }

    private Bearing getHDGM(TimePoint timePoint, Pair<HDGM, HDGM> hdgmPair) {
        final Bearing hdgm;
        if (hdgmPair.getA() == null) {
            if (hdgmPair.getB() == null) {
                hdgm = null;
            } else {
                hdgm = hdgmPair.getB().getMagnetigHeading();
            }
        } else {
            if (hdgmPair.getB() == null) {
                hdgm = hdgmPair.getA().getMagnetigHeading();
            } else {
                hdgm = timeBasedAgerage(timePoint,
                        new ScalableBearing(hdgmPair.getA().getMagnetigHeading()), hdgmPair.getA().getTimePoint(),
                        new ScalableBearing(hdgmPair.getB().getMagnetigHeading()), hdgmPair.getB().getTimePoint());
            }
        }
        return hdgm;
    }

    private Bearing getHeading(TimePoint timePoint, Pair<HDG, HDG> hdgPair, Pair<HDGM, HDGM> hdgmPair, Position position) throws ClassNotFoundException, IOException, ParseException {
        final Bearing trueHeading;
        Bearing hdg = getHDG(timePoint, hdgPair);
        if (hdg != null) {
            trueHeading = hdg;
        } else {
            Bearing hdgm = getHDGM(timePoint, hdgmPair);
            Declination declination = declinationService.getDeclination(timePoint, position, /* timeoutForOnlineFetchInMilliseconds 5s */ 5000);
            trueHeading = hdgm.add(declination.getBearingCorrectedTo(timePoint));
        }
        return trueHeading;
    }

    private <V, T> T timeBasedAgerage(TimePoint timePoint, ScalableValue<V, T> value1, TimePoint timePoint1, ScalableValue<V, T> value2, TimePoint timePoint2) {
        final T acc;
        long timeDiff1 = Math.abs(timePoint1.asMillis() - timePoint.asMillis());
        long timeDiff2 = Math.abs(timePoint2.asMillis() - timePoint.asMillis());
        acc = value1.multiply(timeDiff2).add(value2.multiply(timeDiff1)).divide(timeDiff1 + timeDiff2);
        return acc;
    }

    private <T extends Timed> Pair<T, T> getSurroundingFixes(Track<T> track, TimePoint timePoint) {
        T left = track.getLastFixAtOrBefore(timePoint);
        T right = track.getFirstFixAtOrAfter(timePoint);
        Pair<T, T> result = new Pair<>(left, right);
        return result;
    }
}
