package com.sap.sailing.selenium.api.coursetemplate;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;

public class CourseConfigurationApi {

    private static final String COURSE_CONFIGURATION = "/api/v1/courseconfiguration";
    private static final String FROM_COURSE = "/getFromCourse/";
    private static final String FROM_COURSE_TEMPLATE = "/getFromCourseTemplate/";
    private static final String TO_COURSE_TEMPLATE = "/createCourseTemplate";
    private static final String TO_COURSE = "/createCourse/";
    private static final String PARAM_TAG = "tag";
    private static final String PARAM_REGATTA_NAME = "regattaName";

    public CourseConfiguration createCourseConfigurationFromCourseTemplate(final ApiContext ctx,
            final UUID courseTemplateId, final String optionalRegattaName, final Iterable<String> tags) {
        final String url = COURSE_CONFIGURATION + FROM_COURSE_TEMPLATE + courseTemplateId.toString();
        final Map<String, String> queryParams = new TreeMap<>();
        if (tags != null) {
            for (String tag : tags) {
                queryParams.put(PARAM_TAG, tag);
            }
        }
        if (optionalRegattaName != null) {
            queryParams.put("regattaName", optionalRegattaName);
        }
        final JSONObject result = ctx.get(url, queryParams);
        return new CourseConfiguration(result);
    }

    public CourseConfiguration createCourseConfigurationFromCourse(final ApiContext ctx, final String regattaName,
            final String raceColumn, String fleet, final Iterable<String> tags) {
        final String url = COURSE_CONFIGURATION + FROM_COURSE + regattaName + "/" + raceColumn + "/" + fleet;
        final Map<String, String> queryParams = new TreeMap<>();
        if (tags != null) {
            for (String tag : tags) {
                queryParams.put(PARAM_TAG, tag);
            }
        }
        final JSONObject result = ctx.get(url, queryParams);
        return new CourseConfiguration(result);
    }

    public CourseConfiguration createCourseTemplate(final ApiContext ctx, final CourseConfiguration courseConfiguration,
            final String optionalRegattaName) {
        final String url = COURSE_CONFIGURATION + TO_COURSE_TEMPLATE;
        final Map<String, String> queryParams = new TreeMap<>();
        if (optionalRegattaName != null) {
            queryParams.put(PARAM_REGATTA_NAME, optionalRegattaName);
        }
        final JSONObject result = ctx.post(url, queryParams, courseConfiguration.getJson());
        return new CourseConfiguration(result);
    }

    public CourseConfiguration createCourse(final ApiContext ctx, final CourseConfiguration courseConfiguration,
            final String regattaName, final String raceColumn, String fleet) {
        final JSONObject result = ctx.post(
                COURSE_CONFIGURATION + TO_COURSE + regattaName + "/" + raceColumn + "/" + fleet, null,
                courseConfiguration.getJson());
        return new CourseConfiguration(result);
    }
}
