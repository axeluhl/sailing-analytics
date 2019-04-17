package com.sap.sailing.domain.orc;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class ORCCertificate {

    private Map<String, String> general;
    private Map<String, Number> hull;
    private Map<String, Number> sails;
    private Map<String, Number> scoring;
    private Map<String, Map<Speed, Duration>> twaCourses;
    private Map<String, Map<Speed, Duration>> predefinedCourses;
    private Map<Speed, Bearing> beatAngles;
    private Map<Speed, Bearing> gybeAngles;
    private ORCCertificatePerformanceCurve performanceCurve;

    public ORCCertificate(Map<String, String> general, Map<String, Number> hull, Map<String, Number> sails,
            Map<String, Number> scoring, Map<String, Map<Speed, Duration>> twaCourses,
            Map<String, Map<Speed, Duration>> predefinedCourses, Map<Speed, Bearing> beatAngles,
            Map<Speed, Bearing> gybeAngles) {
        this.general = general;
        this.hull = hull;
        this.sails = sails;
        this.scoring = scoring;
        this.twaCourses = twaCourses;
        this.predefinedCourses = predefinedCourses;
        this.beatAngles = beatAngles;
        this.gybeAngles = gybeAngles;

        if ((String) getValue("C_Type") == "INTL") {
            initializePerformanceCurve();
        }
    }

    private void initializePerformanceCurve() {
        Map<Speed, Map<Bearing, Duration>> map = new HashMap<>();

        map.put(new KnotSpeedImpl( 6), new HashMap<Bearing, Duration>());
        map.put(new KnotSpeedImpl( 8), new HashMap<Bearing, Duration>());
        map.put(new KnotSpeedImpl(10), new HashMap<Bearing, Duration>());
        map.put(new KnotSpeedImpl(12), new HashMap<Bearing, Duration>());
        map.put(new KnotSpeedImpl(14), new HashMap<Bearing, Duration>());
        map.put(new KnotSpeedImpl(16), new HashMap<Bearing, Duration>());
        map.put(new KnotSpeedImpl(20), new HashMap<Bearing, Duration>());

        for (String keyTWA : twaCourses.keySet()) {
            int twa = Integer.parseInt(keyTWA.substring(1));

            for (Speed keyTWS : twaCourses.get(keyTWA).keySet()) {
                map.get(keyTWS).put(new DegreeBearingImpl(twa), twaCourses.get(keyTWA).get(keyTWS));
            }
        }
        ;

        for (Speed tws : map.keySet()) {
            map.get(tws).put(beatAngles.get(tws), predefinedCourses.get("Beat").get(tws));
            map.get(tws).put(gybeAngles.get(tws), predefinedCourses.get("Gybe").get(tws));
        }

        performanceCurve = new ORCCertificatePerformanceCurve(map, beatAngles, gybeAngles);
        performanceCurve.hashCode(); // Only to resolve "Warning"
    }

    public String getValue(String key) {
        String result = null;

        if (general.containsKey(key)) {
            result = general.get(key);
        } else if (hull.containsKey(key)) {
            result = hull.get(key).toString();
        } else if (sails.containsKey(key)) {
            result = sails.get(key).toString();
        } else if (scoring.containsKey(key)) {
            result = scoring.get(key).toString();
        } else if (twaCourses.containsKey(key)) {
            result = twaCourses.get(key).toString();
        } else if (predefinedCourses.containsKey(key)) {
            result = predefinedCourses.get(key).toString();
        }

        return result;
    }
}
