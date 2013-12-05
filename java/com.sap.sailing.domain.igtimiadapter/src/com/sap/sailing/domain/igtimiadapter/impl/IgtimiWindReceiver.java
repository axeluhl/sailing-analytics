package com.sap.sailing.domain.igtimiadapter.impl;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
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
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.confidence.ScalableValue;
import com.sap.sailing.domain.igtimiadapter.BulkFixReceiver;
import com.sap.sailing.domain.igtimiadapter.IgtimiFixReceiverAdapter;
import com.sap.sailing.domain.igtimiadapter.datatypes.AWA;
import com.sap.sailing.domain.igtimiadapter.datatypes.AWS;
import com.sap.sailing.domain.igtimiadapter.datatypes.COG;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.datatypes.GpsLatLong;
import com.sap.sailing.domain.igtimiadapter.datatypes.HDG;
import com.sap.sailing.domain.igtimiadapter.datatypes.HDGM;
import com.sap.sailing.domain.igtimiadapter.datatypes.SOG;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindListener;
import com.sap.sailing.domain.tracking.impl.DynamicTrackImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;

/**
 * Receives Igtimi {@link Fix}es and tries to generate a {@link Wind} object from each {@link AWS} fix. For this to
 * work, the time-wise adjacent {@link AWA}, {@link HDG}/{@link HDGM} and {@link GpsLatLong} fixes are used. If only
 * a magnetic heading ({@link HDGM}) is available, the {@link DeclinationService} is used to map that to a true heading.
 * The true wind direction is determined by adding the boat speed vector onto the apparent wind vector. 
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class IgtimiWindReceiver implements BulkFixReceiver {
    private static final Logger logger = Logger.getLogger(IgtimiWindReceiver.class.getName());
    private final DynamicTrack<AWA> awaTrack;
    private final DynamicTrack<AWS> awsTrack;
    private final DynamicTrack<GpsLatLong> gpsTrack;
    private final DynamicTrack<COG> cogTrack;
    private final DynamicTrack<SOG> sogTrack;
    private final DynamicTrack<HDG> hdgTrack;
    private final DynamicTrack<HDGM> hdgmTrack;
    private final FixReceiver receiver;
    private final DeclinationService declinationService;
    private final Set<WindListener> listeners;
    
    private class FixReceiver extends IgtimiFixReceiverAdapter {
        @Override
        public void received(AWA fix) {
            getAwaTrack().add(fix);
        }

        @Override
        public void received(AWS fix) {
            getAwsTrack().add(fix);
        }

        @Override
        public void received(GpsLatLong fix) {
            getGpsTrack().add(fix);
        }

        @Override
        public void received(COG fix) {
            getCogTrack().add(fix);
        }

        @Override
        public void received(SOG fix) {
            getSogTrack().add(fix);
        }

        @Override
        public void received(HDG fix) {
            getHdgTrack().add(fix);
        }

        @Override
        public void received(HDGM fix) {
            getHdgmTrack().add(fix);
        }
    }

    public IgtimiWindReceiver(String deviceSerialNumber) {
        receiver = new FixReceiver();
        declinationService = DeclinationService.INSTANCE;
        listeners = new ConcurrentSkipListSet<>();
        awaTrack = new DynamicTrackImpl<>("AWA Track for Igtimi wind track for device "+deviceSerialNumber);
        awsTrack = new DynamicTrackImpl<>("AWS Track for Igtimi wind track for device "+deviceSerialNumber);
        gpsTrack = new DynamicTrackImpl<>("GPS Track for Igtimi wind track for device "+deviceSerialNumber);
        cogTrack = new DynamicTrackImpl<>("COG Track for Igtimi wind track for device "+deviceSerialNumber);
        sogTrack = new DynamicTrackImpl<>("SOG Track for Igtimi wind track for device "+deviceSerialNumber);
        hdgTrack = new DynamicTrackImpl<>("HDG Track for Igtimi wind track for device "+deviceSerialNumber);
        hdgmTrack = new DynamicTrackImpl<>("HDGM Track for Igtimi wind track for device "+deviceSerialNumber);
    }
    
    /**
     * Called when a batch of fixes was received, e.g., after loading from stored data or from a live web socket receiver.
     * The fixes are pumped into the tracks by the superclass implementation. Then, for all new {@link AWS} fixes, a
     * call to {@link IgtimiWindReceiver#getWind} is performed.
     */
    @Override
    public void received(Iterable<Fix> fixes) {
        final List<AWS> awsFixes = new ArrayList<>();
        for (Fix fix : fixes) {
            fix.notify(receiver);
            fix.notify(new IgtimiFixReceiverAdapter() {
                @Override
                public void received(AWS fix) {
                    awsFixes.add(fix);
                }
            });
        }
        for (AWS aws : awsFixes) {
            try {
                final Wind wind = getWind(aws.getTimePoint());
                if (wind != null) {
                    notifyListeners(wind);
                }
            } catch (ClassNotFoundException | IOException | ParseException e) {
                logger.log(Level.INFO, "Exception while trying to construct Wind fix from Igtimi fix " + aws, e);
            }
        }
    }

    public void addListener(WindListener listener) {
        listeners.add(listener);
    }
    
    public void notifyListeners(Wind wind) {
        for (WindListener listener : listeners) {
            listener.windDataReceived(wind);
        }
    }
    
    private Wind getWind(TimePoint timePoint) throws ClassNotFoundException, IOException, ParseException {
        final Wind result;
        Pair<AWA, AWA> awaPair = getSurroundingFixes(getAwaTrack(), timePoint);
        Bearing awa = getAWA(timePoint, awaPair);
        Pair<AWS, AWS> awsPair = getSurroundingFixes(getAwsTrack(), timePoint);
        Speed aws = getAWS(timePoint, awsPair);
        Pair<GpsLatLong, GpsLatLong> gpsPair = getSurroundingFixes(getGpsTrack(), timePoint);
        Position pos = getPosition(timePoint, gpsPair);
        Pair<HDG, HDG> hdgPair = getSurroundingFixes(getHdgTrack(), timePoint);
        Pair<HDGM, HDGM> hdgmPair = getSurroundingFixes(getHdgmTrack(), timePoint);
        Bearing heading = getHeading(timePoint, hdgPair, hdgmPair, pos);
        if (awa != null && aws != null && pos != null && heading != null) {
            Bearing apparentWindDirection = heading.add(awa);
            SpeedWithBearing apparentWindSpeedWithDirection = new KnotSpeedWithBearingImpl(aws.getKnots(), apparentWindDirection);
            Pair<SOG, SOG> sogPair = getSurroundingFixes(getSogTrack(), timePoint);
            Speed sog = getSOG(timePoint, sogPair);
            Pair<COG, COG> cogPair = getSurroundingFixes(getCogTrack(), timePoint);
            Bearing cog = getCOG(timePoint, cogPair);
            SpeedWithBearing sogCog = new KnotSpeedWithBearingImpl(sog.getKnots(), cog);
            result = new WindImpl(pos, timePoint, new KnotSpeedWithBearingImpl(aws.getKnots(), apparentWindDirection));
        } else {
            result = null;
        }
        return result;
    }

    private Bearing getAWA(TimePoint timePoint, Pair<AWA, AWA> awaPair) {
        final Bearing awaFrom;
        if (awaPair.getA() == null) {
            if (awaPair.getB() == null) {
                awaFrom = null;
            } else {
                awaFrom = awaPair.getB().getApparentWindAngle();
            }
        } else {
            if (awaPair.getB() == null) {
                awaFrom = awaPair.getA().getApparentWindAngle();
            } else {
                awaFrom = timeBasedAgerage(timePoint,
                        new ScalableBearing(awaPair.getA().getApparentWindAngle()), awaPair.getA().getTimePoint(),
                        new ScalableBearing(awaPair.getB().getApparentWindAngle()), awaPair.getB().getTimePoint());
            }
        }
        return awaFrom.reverse();
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

    private Speed getSOG(TimePoint timePoint, Pair<SOG, SOG> sogPair) {
        final Speed sog;
        if (sogPair.getA() == null) {
            if (sogPair.getB() == null) {
                sog = null;
            } else {
                sog = sogPair.getB().getSpeedOverGround();
            }
        } else {
            if (sogPair.getB() == null) {
                sog = sogPair.getA().getSpeedOverGround();
            } else {
                sog = timeBasedAgerage(timePoint,
                        new ScalableSpeed(sogPair.getA().getSpeedOverGround()), sogPair.getA().getTimePoint(),
                        new ScalableSpeed(sogPair.getB().getSpeedOverGround()), sogPair.getB().getTimePoint());
            }
        }
        return sog;
    }

    private Bearing getCOG(TimePoint timePoint, Pair<COG, COG> cogPair) {
        final Bearing sog;
        if (cogPair.getA() == null) {
            if (cogPair.getB() == null) {
                sog = null;
            } else {
                sog = cogPair.getB().getCourseOverGround();
            }
        } else {
            if (cogPair.getB() == null) {
                sog = cogPair.getA().getCourseOverGround();
            } else {
                sog = timeBasedAgerage(timePoint,
                        new ScalableBearing(cogPair.getA().getCourseOverGround()), cogPair.getA().getTimePoint(),
                        new ScalableBearing(cogPair.getB().getCourseOverGround()), cogPair.getB().getTimePoint());
            }
        }
        return sog;
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

    private DynamicTrack<AWA> getAwaTrack() {
        return awaTrack;
    }

    private DynamicTrack<AWS> getAwsTrack() {
        return awsTrack;
    }

    private DynamicTrack<GpsLatLong> getGpsTrack() {
        return gpsTrack;
    }

    private DynamicTrack<COG> getCogTrack() {
        return cogTrack;
    }

    private DynamicTrack<SOG> getSogTrack() {
        return sogTrack;
    }

    private DynamicTrack<HDG> getHdgTrack() {
        return hdgTrack;
    }

    private DynamicTrack<HDGM> getHdgmTrack() {
        return hdgmTrack;
    }
}
