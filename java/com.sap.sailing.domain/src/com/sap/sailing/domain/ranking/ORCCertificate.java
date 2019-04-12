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
}
