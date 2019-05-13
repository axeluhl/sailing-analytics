package com.sap.sailing.domain.orc.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.orc.ORCCertificate;
import com.sap.sailing.domain.orc.ORCPerformanceCurve;
import com.sap.sailing.domain.orc.ORCPerformanceCurveCourse;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.impl.DegreeBearingImpl;

/**
 * For a {@link Competitor} 
 * https://orc.org/index.asp?id=23
 * 
 * @author Daniel Lisunkin (I505543)
 *
 */

// TODO Improve public API in regards of semantic connection with real ORC Certificate (paper)
// TODO COMMENTS!

public class ORCCertificateImpl implements ORCCertificate {

    /*
     * Equals the column heading of the allowances table of an ORC certificate. The speeds are set by the offshore racing congress.
     */
    public static final Speed[] ALLOWANCES_TRUE_WIND_SPEEDS = {new KnotSpeedImpl( 6),
                                                               new KnotSpeedImpl( 8),
                                                               new KnotSpeedImpl(10),
                                                               new KnotSpeedImpl(12),
                                                               new KnotSpeedImpl(14),
                                                               new KnotSpeedImpl(16),
                                                               new KnotSpeedImpl(20) };
    
    /*
     * 
     */
    public static final Bearing[] ALLOWANCES_TRUE_WIND_ANGLES = {new DegreeBearingImpl( 52),
                                                                 new DegreeBearingImpl( 60),
                                                                 new DegreeBearingImpl( 75),
                                                                 new DegreeBearingImpl( 90),
                                                                 new DegreeBearingImpl(110),
                                                                 new DegreeBearingImpl(120),
                                                                 new DegreeBearingImpl(135),
                                                                 new DegreeBearingImpl(150) };
    
    /*
     * 
     */
    private static final String RUN = "Run";
    private static final String BEAT = "Beat";
    
    private Map<String, String> general;
    private Map<String, Number> hull;
    private Map<String, Number> sails;
    private Map<String, Number> scoring;
    private Map<String, Map<Speed, Duration>> twaCourses;
    private Map<String, Map<Speed, Duration>> predefinedCourses;
    private final Map<Speed, Map<Bearing, Duration>> timeAllowancesPerTrueWindSpeedAndAngle;
    private final Map<Speed, Bearing> beatAngles;
    private final Map<Speed, Bearing> gybeAngles;

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
    public ORCCertificateImpl(Map<String, String> general, Map<String, Number> hull, Map<String, Number> sails,
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
        this.timeAllowancesPerTrueWindSpeedAndAngle = initializeTimeAllowances();
    }

    private Map<Speed, Map<Bearing, Duration>> initializeTimeAllowances() {
        Map<Speed, Map<Bearing, Duration>> result = new HashMap<>();
        for (Speed tws : ALLOWANCES_TRUE_WIND_SPEEDS) {
            result.put(tws, new HashMap<Bearing, Duration>());
        }
        for (String keyTWA : twaCourses.keySet()) {
            int twa = Integer.parseInt(keyTWA.substring(1));

            for (Speed keyTWS : twaCourses.get(keyTWA).keySet()) {
                result.get(keyTWS).put(new DegreeBearingImpl(twa), twaCourses.get(keyTWA).get(keyTWS));
            }
        }
        for (Speed tws : result.keySet()) {
            result.get(tws).put(beatAngles.get(tws), predefinedCourses.get(BEAT).get(tws));
            result.get(tws).put(gybeAngles.get(tws), predefinedCourses.get(RUN).get(tws));
        }
        
        return result;
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
    
    @Override
    public ORCPerformanceCurve getPerformanceCurve(ORCPerformanceCurveCourse course) {
        return new ORCPerformanceCurveImpl(timeAllowancesPerTrueWindSpeedAndAngle, beatAngles, gybeAngles, course);
    }
}
