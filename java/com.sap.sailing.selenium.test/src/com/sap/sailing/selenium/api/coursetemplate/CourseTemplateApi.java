package com.sap.sailing.selenium.api.coursetemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;

public class CourseTemplateApi {

    private static final String COURSE_TEMPLATES = "/api/v1/coursetemplates";
    private static final String PARAM_TAG = "tags";

    public CourseTemplate createCourseTemplate(final ApiContext ctx, final CourseTemplate courseTemplate) {
        JSONObject result = ctx.post(COURSE_TEMPLATES, null, courseTemplate.getJson());
        return new CourseTemplate(result);
    }

    public CourseTemplate getCourseTemplate(final ApiContext ctx, final UUID id) {
        JSONObject result = ctx.get(COURSE_TEMPLATES + "/" + id.toString());
        return new CourseTemplate(result);
    }
    
    public void deleteCourseTemplate(final ApiContext ctx, final UUID id) {
        ctx.delete(COURSE_TEMPLATES + "/" + id.toString());
    }

    public Iterable<CourseTemplate> getAllCourseTemplates(final ApiContext ctx) {
        return getAllCourseTemplates(ctx, Collections.emptySet());
    }
    
    public Iterable<CourseTemplate> getAllCourseTemplates(final ApiContext ctx, final Iterable<String> tags) {
        // FIXME: multiple query parameters with the same key sould be passed but cannot be put into Map<String,
        // String>. Should use Map<String, Iterator<String>>. Will be fixed in bug4942.
        final Map<String, String> queryParams = new TreeMap<>();
        for (String tag : tags) {
            queryParams.put(PARAM_TAG, tag);
        }
        JSONArray markPropertiesArray = ctx.get(COURSE_TEMPLATES, queryParams);
        List<CourseTemplate> result = new ArrayList<>();
        markPropertiesArray.stream().map(o -> (JSONObject) o).map(CourseTemplate::new).forEach(result::add);
        return result;
    }
}
