package com.sap.sailing.domain.ranking;

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
    private ORCCertificatePerformanceCurve performanceCurve;

    public ORCCertificate(Map<String, String> general, Map<String, Number> hull, Map<String, Number> sails,
        Map<String, Number> scoring, Map<String, Map<Speed, Duration>> twaCourses,
        Map<String, Map<Speed, Duration>> predefinedCourses) {
        this.general = general;
        this.hull = hull;
        this.sails = sails;
        this.scoring = scoring;
        this.twaCourses = twaCourses;
        this.predefinedCourses = predefinedCourses;
        
        if ((String) getValue("C_Type") == "INTL") {
            initializePerformanceCurve();
        }
    }

    private void initializePerformanceCurve() {
        Map<Speed, Map<Bearing, Duration>> PCAllowancesTws = new HashMap<>();
        
        PCAllowancesTws.put(new KnotSpeedImpl( 6), new HashMap<Bearing, Duration>());
        PCAllowancesTws.put(new KnotSpeedImpl( 8), new HashMap<Bearing, Duration>());
        PCAllowancesTws.put(new KnotSpeedImpl(10), new HashMap<Bearing, Duration>());
        PCAllowancesTws.put(new KnotSpeedImpl(12), new HashMap<Bearing, Duration>());
        PCAllowancesTws.put(new KnotSpeedImpl(14), new HashMap<Bearing, Duration>());
        PCAllowancesTws.put(new KnotSpeedImpl(16), new HashMap<Bearing, Duration>());
        PCAllowancesTws.put(new KnotSpeedImpl(20), new HashMap<Bearing, Duration>());
        
        for (String keyTWA : twaCourses.keySet()) {
            int twa = Integer.parseInt(keyTWA.substring(1));
            
            for (Speed keyTWS : twaCourses.get(keyTWA).keySet()) {
                PCAllowancesTws.get(keyTWS).put(new DegreeBearingImpl(twa), twaCourses.get(keyTWA).get(keyTWS));
            }
        };
        
        performanceCurve = new ORCCertificatePerformanceCurve(  PCAllowancesTws.get(new KnotSpeedImpl( 6)),
                                                                PCAllowancesTws.get(new KnotSpeedImpl( 8)),
                                                                PCAllowancesTws.get(new KnotSpeedImpl(10)),
                                                                PCAllowancesTws.get(new KnotSpeedImpl(12)),
                                                                PCAllowancesTws.get(new KnotSpeedImpl(14)),
                                                                PCAllowancesTws.get(new KnotSpeedImpl(16)),
                                                                PCAllowancesTws.get(new KnotSpeedImpl(20)));
        
        if (performanceCurve != null) {System.out.println("Everything fine!");} //TODO Bookmark for the next day.
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
        ;

        return result;
    }
}
