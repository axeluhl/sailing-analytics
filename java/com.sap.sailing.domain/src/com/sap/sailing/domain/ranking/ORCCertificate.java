package com.sap.sailing.domain.ranking;

import java.util.Map;

public class ORCCertificate {

    private Map<String, String> general;
    private Map<String, Number> hull;
    private Map<String, Number> sails;
    private Map<String, Number> scoring;
    private Map<String, Map<Integer, Number>> twaCourses;
    private Map<String, Map<Integer, Number>> predefinedCourses;
    private ORCCertificatePerformanceCurve performanceCurve;

    public ORCCertificate(Map<String, String> general, Map<String, Number> hull, Map<String, Number> sails,
        Map<String, Number> scoring, Map<String, Map<Integer, Number>> twaCourses,
        Map<String, Map<Integer, Number>> predefinedCourses) {
        this.general = general;
        this.hull = hull;
        this.sails = sails;
        this.scoring = scoring;
        this.twaCourses = twaCourses;
        this.predefinedCourses = predefinedCourses;
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
        };
        
        return result;
    } 
}
