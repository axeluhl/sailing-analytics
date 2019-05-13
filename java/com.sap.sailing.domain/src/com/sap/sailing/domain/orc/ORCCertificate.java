package com.sap.sailing.domain.orc;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.orc.impl.ORCPerformanceCurveImpl;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.impl.DegreeBearingImpl;

/**
 * For a {@link Competitor} 
 * 
 * 
 * @author Daniel Lisunkin (I505543)
 *
 */

// TODO Improve public API in regards of semantic connection with real ORC Certificate (paper)
// TODO COMMENTS!

public class ORCCertificate {

    private static final String BEAT = "Beat";
    private Map<String, String> general;
    private Map<String, Number> hull;
    private Map<String, Number> sails;
    private Map<String, Number> scoring;
    private Map<String, Map<Speed, Duration>> twaCourses;
    private Map<String, Map<Speed, Duration>> predefinedCourses;
    private Map<Speed, Bearing> beatAngles;
    private Map<Speed, Bearing> gybeAngles;
    private ORCPerformanceCurve performanceCurve;

    /**
     * 
     * @param general
     * @param hull
     * @param sails
     * @param scoring
     * @param allowancesByTwa
     * @param predefinedCourses
     * @param beatAngles
     * @param gybeAngles
     */
    public ORCCertificate(Map<String, String> general, Map<String, Number> hull, Map<String, Number> sails,
            Map<String, Number> scoring, Map<String, Map<Speed, Duration>> allowancesByTwa,
            Map<String, Map<Speed, Duration>> predefinedCourses, Map<Speed, Bearing> beatAngles,
            Map<Speed, Bearing> gybeAngles) {
        this.general = general;
        this.hull = hull;
        this.sails = sails;
        this.scoring = scoring;
        this.twaCourses = allowancesByTwa;
        this.predefinedCourses = predefinedCourses;
        this.beatAngles = beatAngles;
        this.gybeAngles = gybeAngles;

        if (((String) getValue("C_Type")).equals("INTL")) {
            initializePerformanceCurve();
        } else {
            initializePerformanceCurve();
        }
        
    }

    private void initializePerformanceCurve() {
        Map<Speed, Map<Bearing, Duration>> map = new HashMap<>();
        // TODO Get Speed Deltas from RMS, create SubBug in Bugzilla, extract Deltas as static final variable
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
        for (Speed tws : map.keySet()) {
            // TODO Define constant "Beat"/"Run" as static final literals 
            map.get(tws).put(beatAngles.get(tws), predefinedCourses.get(BEAT).get(tws));
            map.get(tws).put(gybeAngles.get(tws), predefinedCourses.get("Run").get(tws));
        }
        performanceCurve = new ORCPerformanceCurveImpl(map, beatAngles, gybeAngles);
        performanceCurve.hashCode(); // Only to resolve "Warning"
    }

    public Object getValue(String key) {
        Object result = null;
        if (general.containsKey(key)) {
            result = general.get(key);
        } else if (hull.containsKey(key)) {
            result = hull.get(key);
        } else if (sails.containsKey(key)) {
            result = sails.get(key);
        } else if (scoring.containsKey(key)) {
            result = scoring.get(key);
        } else if (twaCourses.containsKey(key)) {
            result = twaCourses.get(key);
        } else if (predefinedCourses.containsKey(key)) {
            result = predefinedCourses.get(key);
        }
        return result;
    }
    
    public String getValueString(String key) {
        return getValue(key).toString();
    }
    
    ORCPerformanceCurve getPerformanceCurve() {
        return performanceCurve;
    }
}
