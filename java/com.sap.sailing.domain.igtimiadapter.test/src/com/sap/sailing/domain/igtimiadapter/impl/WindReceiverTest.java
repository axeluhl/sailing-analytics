package com.sap.sailing.domain.igtimiadapter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.igtimiadapter.IgtimiWindListener;
import com.sap.sailing.domain.igtimiadapter.datatypes.AWA;
import com.sap.sailing.domain.igtimiadapter.datatypes.AWS;
import com.sap.sailing.domain.igtimiadapter.datatypes.COG;
import com.sap.sailing.domain.igtimiadapter.datatypes.GpsLatLong;
import com.sap.sailing.domain.igtimiadapter.datatypes.HDG;
import com.sap.sailing.domain.igtimiadapter.datatypes.SOG;
import com.sap.sailing.domain.igtimiadapter.shared.IgtimiWindReceiver;
import com.sap.sailing.domain.tracking.Wind;

public class WindReceiverTest {
    @Test
    public void simpleWindReceiverTest() {
        final List<Wind> windReceived = new ArrayList<>();
        final String deviceSerialNumber = "Non-Existing Test Device";
        IgtimiWindReceiver receiver = new IgtimiWindReceiver(Collections.singleton(deviceSerialNumber));
        receiver.addListener(new IgtimiWindListener() {
            @Override
            public void windDataReceived(Wind wind, String deviceSerialNumber) {
                windReceived.add(wind);
            }
        });
        TimePoint timePoint = MillisecondsTimePoint.now();
        Map<Integer, Object> awaMap = new HashMap<>(); awaMap.put(1, 123. /* degrees from */);
        Map<Integer, Object> awsMap = new HashMap<>(); awsMap.put(1, 12. /* knots */);
        Map<Integer, Object> hdgMap = new HashMap<>(); hdgMap.put(1, 20. /* true degrees */);
        Map<Integer, Object> gpsLatLongMap = new HashMap<>(); gpsLatLongMap.put(2, 49.8 /* lat */); gpsLatLongMap.put(1, 8.93 /* lng */);
        Map<Integer, Object> sogMap = new HashMap<>(); sogMap.put(1, 10. /* knots */);
        Map<Integer, Object> cogMap = new HashMap<>(); cogMap.put(1, 22. /* degrees; 2 degrees drift */);
        final SensorImpl sensor = new SensorImpl(deviceSerialNumber, 0);
        SpeedWithBearing boatSogCog = new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(22));
        SpeedWithBearing apparentWind = new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(20).add(new DegreeBearingImpl(123).reverse()));
        SpeedWithBearing expectedTrueWind = apparentWind.add(boatSogCog);
        receiver.received(Arrays.asList(new AWA(timePoint, sensor, awaMap),
                                        new AWS(timePoint, sensor, awsMap),
                                        new HDG(timePoint, sensor, hdgMap),
                                        new GpsLatLong(timePoint, sensor, gpsLatLongMap),
                                        new SOG(timePoint, sensor, sogMap),
                                        new COG(timePoint, sensor, cogMap)));
        assertFalse(windReceived.isEmpty());
        assertEquals(1, windReceived.size());
        Wind wind = windReceived.get(0);
        assertEquals(49.8, wind.getPosition().getLatDeg(), 0.00000001);
        assertEquals(8.93, wind.getPosition().getLngDeg(), 0.00000001);
        assertEquals(expectedTrueWind.getKnots(), wind.getKnots(), 0.00000001);
        assertEquals(expectedTrueWind.getBearing().getDegrees(), wind.getBearing().getDegrees(), 0.00000001);
    }
}
