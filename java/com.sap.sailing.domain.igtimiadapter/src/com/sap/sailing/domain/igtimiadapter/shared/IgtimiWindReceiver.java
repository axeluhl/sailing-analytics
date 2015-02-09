package com.sap.sailing.domain.igtimiadapter.shared;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.declination.Declination;
import com.sap.sailing.declination.DeclinationService;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalableBearing;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalablePosition;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalableSpeed;
import com.sap.sailing.domain.igtimiadapter.BulkFixReceiver;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiFixReceiverAdapter;
import com.sap.sailing.domain.igtimiadapter.IgtimiWindListener;
import com.sap.sailing.domain.igtimiadapter.datatypes.AWA;
import com.sap.sailing.domain.igtimiadapter.datatypes.AWS;
import com.sap.sailing.domain.igtimiadapter.datatypes.COG;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.datatypes.GpsLatLong;
import com.sap.sailing.domain.igtimiadapter.datatypes.HDG;
import com.sap.sailing.domain.igtimiadapter.datatypes.HDGM;
import com.sap.sailing.domain.igtimiadapter.datatypes.SOG;
import com.sap.sailing.domain.igtimiadapter.datatypes.Type;
import com.sap.sailing.domain.igtimiadapter.websocket.WebSocketConnectionManager;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindListener;
import com.sap.sailing.domain.tracking.impl.DynamicTrackImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.scalablevalue.ScalableValue;

/**
 * Receives Igtimi {@link Fix}es and tries to generate a {@link Wind} object from each {@link AWS} fix. For this to
 * work, the time-wise adjacent {@link AWA}, {@link HDG}/{@link HDGM} and {@link GpsLatLong} fixes are used. If only a
 * magnetic heading ({@link HDGM}) is available, the {@link DeclinationService} is used to map that to a true heading.
 * The true wind direction is determined by adding the boat speed vector onto the apparent wind vector.
 * <p>
 * 
 * Use the class by hooking it up to a {@link WebWocketConnectionManager} using
 * {@link WebSocketConnectionManager#addListener(BulkFixReceiver)} and {@link #addListener(WindListener) add} a
 * {@link WindListener} to this instance.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class IgtimiWindReceiver implements BulkFixReceiver {
    private static final Logger logger = Logger.getLogger(IgtimiWindReceiver.class.getName());
    private final Map<String, DynamicTrack<AWA>> awaTrack;
    private final Map<String, DynamicTrack<AWS>> awsTrack;
    private final Map<String, DynamicTrack<GpsLatLong>> gpsTrack;
    private final Map<String, DynamicTrack<COG>> cogTrack;
    private final Map<String, DynamicTrack<SOG>> sogTrack;
    private final Map<String, DynamicTrack<HDG>> hdgTrack;
    private final Map<String, DynamicTrack<HDGM>> hdgmTrack;
    private final FixReceiver receiver;
    private final DeclinationService declinationService;
    private final ConcurrentHashMap<IgtimiWindListener, IgtimiWindListener> listeners;
    
    private class FixReceiver extends IgtimiFixReceiverAdapter {
        @Override
        public void received(AWA fix) {
            getAwaTrack(fix.getSensor().getDeviceSerialNumber()).add(fix);
        }

        @Override
        public void received(AWS fix) {
            getAwsTrack(fix.getSensor().getDeviceSerialNumber()).add(fix);
        }

        @Override
        public void received(GpsLatLong fix) {
            getGpsTrack(fix.getSensor().getDeviceSerialNumber()).add(fix);
        }

        @Override
        public void received(COG fix) {
            getCogTrack(fix.getSensor().getDeviceSerialNumber()).add(fix);
        }

        @Override
        public void received(SOG fix) {
            getSogTrack(fix.getSensor().getDeviceSerialNumber()).add(fix);
        }

        @Override
        public void received(HDG fix) {
            getHdgTrack(fix.getSensor().getDeviceSerialNumber()).add(fix);
        }

        @Override
        public void received(HDGM fix) {
            getHdgmTrack(fix.getSensor().getDeviceSerialNumber()).add(fix);
        }
    }

    public IgtimiWindReceiver(DeclinationService declinationService) {
        receiver = new FixReceiver();
        this.declinationService = declinationService;
        listeners = new ConcurrentHashMap<>();
        awaTrack = new HashMap<>();
        awsTrack = new HashMap<>();
        gpsTrack = new HashMap<>();
        cogTrack = new HashMap<>();
        sogTrack = new HashMap<>();
        hdgTrack = new HashMap<>();
        hdgmTrack = new HashMap<>();
    }
    
    private <T extends Fix> DynamicTrack<T> getTrack(String deviceSerialNumber, Map<String, DynamicTrack<T>> tracksByDeviceSerialNumber) {
        DynamicTrack<T> result = tracksByDeviceSerialNumber.get(deviceSerialNumber);
        if (result == null) {
            result = new DynamicTrackImpl<T>("Track for Igtimi wind track for device "+deviceSerialNumber);
            tracksByDeviceSerialNumber.put(deviceSerialNumber, result);
        }
        return result;
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
        logger.finest("Received "+Util.size(awsFixes)+" wind fixes");
        boolean loggedWindFixGenerationProblem = false;
        for (AWS aws : awsFixes) {
            try {
                final Wind wind = getWind(aws.getTimePoint(), aws.getSensor().getDeviceSerialNumber());
                if (wind != null) {
                    notifyListeners(wind, aws.getSensor().getDeviceSerialNumber());
                } else {
                    if (!loggedWindFixGenerationProblem) {
                        logger.info("Not enough information to build a Wind fix out of data provided. AWS received but most probably HDG or HDGM not received (yet) - check your compass.");
                        loggedWindFixGenerationProblem = true;
                    }
                }
            } catch (ClassNotFoundException | IOException | ParseException e) {
                logger.log(Level.INFO, "Exception while trying to construct Wind fix from Igtimi fix " + aws, e);
            }
        }
    }

    public void addListener(IgtimiWindListener listener) {
        listeners.put(listener, listener);
    }
    
    public void notifyListeners(Wind wind, String deviceSerialNumber) {
        for (IgtimiWindListener listener : listeners.keySet()) {
            listener.windDataReceived(wind, deviceSerialNumber);
        }
    }
    
    private Wind getWind(final TimePoint timePoint, String deviceSerialNumber) throws ClassNotFoundException, IOException, ParseException {
        final Wind result;
        com.sap.sse.common.Util.Pair<AWA, AWA> awaPair = getSurroundingFixes(getAwaTrack(deviceSerialNumber), timePoint);
        Bearing awa = getAWA(timePoint, awaPair);
        com.sap.sse.common.Util.Pair<AWS, AWS> awsPair = getSurroundingFixes(getAwsTrack(deviceSerialNumber), timePoint);
        Speed aws = getAWS(timePoint, awsPair);
        com.sap.sse.common.Util.Pair<GpsLatLong, GpsLatLong> gpsPair = getSurroundingFixes(getGpsTrack(deviceSerialNumber), timePoint);
        Position pos = getPosition(timePoint, gpsPair);
        if (pos != null) {
            com.sap.sse.common.Util.Pair<HDG, HDG> hdgPair = getSurroundingFixes(getHdgTrack(deviceSerialNumber), timePoint);
            com.sap.sse.common.Util.Pair<HDGM, HDGM> hdgmPair = getSurroundingFixes(getHdgmTrack(deviceSerialNumber), timePoint);
            Bearing heading = getHeading(timePoint, hdgPair, hdgmPair, pos);
            if (awa != null && aws != null && heading != null) {
                Bearing apparentWindDirection = heading.add(awa);
                SpeedWithBearing apparentWindSpeedWithDirection = new KnotSpeedWithBearingImpl(aws.getKnots(), apparentWindDirection);
                /*
                 * Hint from Brent Russell from Igtimi, at 2013-12-05 on the question whether to use GpsLatLong to
                 * improve precision of boat speed / coarse over SOG/COG measurements:
                 * 
                 * "Personally I would use COG/SOG exclusively, and if unhappy with the result add a small amount of
                 * smoothing and consider dropping samples as outliers if they cause a SOG discontinuity. The latter
                 * might happen as a satellite is dropped/acquired - and I'd expect to see a time correlated position
                 * jump as well. Probably not though a direction/speed correlation :)
                 * 
                 * All our GPS systems are using Doppler to calculate COG/SOG and this should be the most accurate
                 * measure. I don't believe that delta position really adds any more "truth" to the measurement of
                 * physical reality, if that makes sense. I'd trust d-p even less at low speeds, where you'll see the
                 * most disagreement. Also there is a significant quantisation noise error in the d-p calculations from
                 * the GPS resolution too, so you'd have to smooth it before averaging - possibly in a speed dependent
                 * way.
                 * 
                 * Remember that Doppler COG/SOG is using the same raw satellite measurements that are being used to
                 * calculate position, just the algorithm is different. I suspect that merging the two might be, in
                 * practice, just a slightly indirect way of averaging. If you like the central limit theorem in action
                 * over the set of algorithms!
                 * 
                 * So again, my personal preference would be to work with the data that should be the most accurate
                 * (COG/SOG) and consider algorithms that handle smoothing of that data best."
                 */
                com.sap.sse.common.Util.Pair<SOG, SOG> sogPair = getSurroundingFixes(getSogTrack(deviceSerialNumber), timePoint);
                Speed sog = getSOG(timePoint, sogPair);
                com.sap.sse.common.Util.Pair<COG, COG> cogPair = getSurroundingFixes(getCogTrack(deviceSerialNumber), timePoint);
                Bearing cog = getCOG(timePoint, cogPair);
                if (sog != null && cog != null) {
                    SpeedWithBearing sogCog = new KnotSpeedWithBearingImpl(sog.getKnots(), cog);
                    SpeedWithBearing trueWindSpeedAndDirection = apparentWindSpeedWithDirection.add(sogCog);
                    result = new WindImpl(pos, timePoint, trueWindSpeedAndDirection);
                } else {
                    result = null;
                }
            } else {
                result = null;
            }
        } else {
            result = null;
        }
        return result;
    }

    private Bearing getAWA(TimePoint timePoint, com.sap.sse.common.Util.Pair<AWA, AWA> awaPair) {
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
        return awaFrom == null ? null : awaFrom.reverse();
    }

    private Speed getAWS(TimePoint timePoint, com.sap.sse.common.Util.Pair<AWS, AWS> awsPair) {
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

    private Speed getSOG(TimePoint timePoint, com.sap.sse.common.Util.Pair<SOG, SOG> sogPair) {
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

    private Bearing getCOG(TimePoint timePoint, com.sap.sse.common.Util.Pair<COG, COG> cogPair) {
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

    private Position getPosition(TimePoint timePoint, com.sap.sse.common.Util.Pair<GpsLatLong, GpsLatLong> gpsPair) {
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

    private Bearing getHDG(TimePoint timePoint, com.sap.sse.common.Util.Pair<HDG, HDG> hdgPair) {
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

    private Bearing getHDGM(TimePoint timePoint, com.sap.sse.common.Util.Pair<HDGM, HDGM> hdgmPair) {
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

    private Bearing getHeading(TimePoint timePoint, com.sap.sse.common.Util.Pair<HDG, HDG> hdgPair, com.sap.sse.common.Util.Pair<HDGM, HDGM> hdgmPair, Position position) throws ClassNotFoundException, IOException, ParseException {
        final Bearing trueHeading;
        Bearing hdg = getHDG(timePoint, hdgPair);
        if (hdg != null) {
            trueHeading = hdg;
        } else {
            Bearing hdgm = getHDGM(timePoint, hdgmPair);
            if (hdgm != null) {
                if (declinationService == null) {
                    trueHeading = hdgm;
                } else {
                    try {
                        Declination declination = declinationService.getDeclination(timePoint, position, /* timeoutForOnlineFetchInMilliseconds 5s */ 5000);
                        trueHeading = hdgm.add(declination.getBearingCorrectedTo(timePoint));
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Correction of declination was requested but unsuccessful. Can't correct heading "+
                                hdgm+"@"+timePoint+" by declination. Forwarding exception.");
                        throw e;
                    }
                }
            } else {
                trueHeading = null;
            }
        }
        return trueHeading;
    }

    private <V, T> T timeBasedAgerage(TimePoint timePoint, ScalableValue<V, T> value1, TimePoint timePoint1, ScalableValue<V, T> value2, TimePoint timePoint2) {
        final T acc;
        if (timePoint1.equals(timePoint2)) {
            acc = value1.add(value2).divide(2);
        } else {
            long timeDiff1 = Math.abs(timePoint1.asMillis() - timePoint.asMillis());
            long timeDiff2 = Math.abs(timePoint2.asMillis() - timePoint.asMillis());
            acc = value1.multiply(timeDiff2).add(value2.multiply(timeDiff1)).divide(timeDiff1 + timeDiff2);
        }
        return acc;
    }

    private <T extends Timed> com.sap.sse.common.Util.Pair<T, T> getSurroundingFixes(Track<T> track, TimePoint timePoint) {
        T left = track.getLastFixAtOrBefore(timePoint);
        T right = track.getFirstFixAtOrAfter(timePoint);
        com.sap.sse.common.Util.Pair<T, T> result = new com.sap.sse.common.Util.Pair<>(left, right);
        return result;
    }

    private DynamicTrack<AWA> getAwaTrack(String deviceSerialNumber) {
        return getTrack(deviceSerialNumber, awaTrack);
    }

    private DynamicTrack<AWS> getAwsTrack(String deviceSerialNumber) {
        return getTrack(deviceSerialNumber, awsTrack);
    }

    private DynamicTrack<GpsLatLong> getGpsTrack(String deviceSerialNumber) {
        return getTrack(deviceSerialNumber, gpsTrack);
    }

    private DynamicTrack<COG> getCogTrack(String deviceSerialNumber) {
        return getTrack(deviceSerialNumber, cogTrack);
    }

    private DynamicTrack<SOG> getSogTrack(String deviceSerialNumber) {
        return getTrack(deviceSerialNumber, sogTrack);
    }

    private DynamicTrack<HDG> getHdgTrack(String deviceSerialNumber) {
        return getTrack(deviceSerialNumber, hdgTrack);
    }

    private DynamicTrack<HDGM> getHdgmTrack(String deviceSerialNumber) {
        return getTrack(deviceSerialNumber, hdgmTrack);
    }

    /**
     * Tells the set of types that this wind receiver is interested in. Can be used to subscribe for fixes from
     * resource data. See {@link IgtimiConnection#getResourceData(TimePoint, TimePoint, Iterable, Type...)}.
     */
    public Type[] getFixTypes() {
        return new Type[] { Type.AWA, Type.AWS, Type.HDG, Type.HDGM, Type.gps_latlong, Type.COG, Type.SOG };
    }
}
