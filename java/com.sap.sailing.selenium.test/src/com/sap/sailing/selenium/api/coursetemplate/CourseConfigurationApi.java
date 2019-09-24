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

    public CourseConfiguration createCourseConfigurationFromCourseTemplate(final ApiContext ctx,
            final UUID courseTemplateId, final String optionalRegattaName, final Iterable<String> tags) {
        final String url = COURSE_CONFIGURATION + FROM_COURSE_TEMPLATE + courseTemplateId.toString()
                + (optionalRegattaName != null ? "?regattaName=" + optionalRegattaName : "");
        final Map<String, String> queryParams = new TreeMap<>();
        if (tags != null) {
            for (String tag : tags) {
                queryParams.put(PARAM_TAG, tag);
            }
        }
        final JSONObject result = ctx.get(url, queryParams);
        return new CourseConfiguration(result);
    }

    public CourseConfiguration createCourseConfigurationFromCourse(final ApiContext ctx, final String regattaName,
            final String raceName, final Iterable<String> tags) {
        final String url = COURSE_CONFIGURATION + FROM_COURSE + regattaName + "/" + raceName;
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
        final String url = COURSE_CONFIGURATION + TO_COURSE_TEMPLATE
                + (optionalRegattaName != null ? "?regattaName=" + optionalRegattaName : "");
        final JSONObject result = ctx.post(url, null, courseConfiguration.getJson());
        return new CourseConfiguration(result);
    }

    public JSONObject createCourse(final ApiContext ctx, final CourseConfiguration courseConfiguration,
            final String regattaName) {
        final JSONObject result = ctx.post(COURSE_CONFIGURATION + TO_COURSE + regattaName, null,
                courseConfiguration.getJson());
        return result;
    }
}
