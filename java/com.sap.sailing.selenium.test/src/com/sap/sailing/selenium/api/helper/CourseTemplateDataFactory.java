package com.sap.sailing.selenium.api.helper;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.coursetemplate.CourseTemplate;
import com.sap.sailing.selenium.api.coursetemplate.MarkRole;
import com.sap.sailing.selenium.api.coursetemplate.MarkTemplate;
import com.sap.sailing.selenium.api.coursetemplate.MarkTemplateApi;
import com.sap.sailing.selenium.api.coursetemplate.WaypointTemplate;
import com.sap.sse.common.Util.Pair;

public class CourseTemplateDataFactory {
    public final MarkTemplate sb;
    public final MarkTemplate pe;
    public final MarkTemplate b1;
    public final MarkTemplate b4s;
    public final MarkTemplate b4p;
    public final MarkTemplate spare;
    public final List<WaypointTemplate> waypointSequence;

    public CourseTemplateDataFactory(ApiContext ctx) {
        final MarkTemplateApi markTemplateApi = new MarkTemplateApi();

        sb = markTemplateApi.createMarkTemplate(ctx, "Startboat", "sb", null, null, null, MarkType.STARTBOAT.name());
        pe = markTemplateApi.createMarkTemplate(ctx, "Pinend", "pe", "#000000", null, null, MarkType.BUOY.name());
        b1 = markTemplateApi.createMarkTemplate(ctx, "blue", "blue", "#0000FF", null, null, MarkType.BUOY.name());
        b4s = markTemplateApi.createMarkTemplate(ctx, "red", "red", "#FF0000", null, null, MarkType.BUOY.name());
        b4p = markTemplateApi.createMarkTemplate(ctx, "green", "green", "#00FF00", null, null, MarkType.BUOY.name());
        spare = markTemplateApi.createMarkTemplate(ctx, "spare", "spare", "#00FFFF", null, null, MarkType.BUOY.name());

        waypointSequence = Arrays.asList(
                new WaypointTemplate("Start/End", PassingInstruction.Line, Arrays.asList(sb, pe)),
                new WaypointTemplate(null, PassingInstruction.Port, Arrays.asList(b1)),
                new WaypointTemplate(null, PassingInstruction.Gate, Arrays.asList(b4p, b4s)),
                new WaypointTemplate(null, PassingInstruction.Port, Arrays.asList(b1)),
                new WaypointTemplate("Start/End", PassingInstruction.Line, Arrays.asList(sb, pe)));
    }

    public CourseTemplate constructCourseTemplate() {
        return constructCourseTemplate(null, null);
    }

    public CourseTemplate constructCourseTemplate(Pair<Integer, Integer> optionalRepeatablePart,
            Integer defaultNumberOfLaps) {
        return this.constructCourseTemplate(optionalRepeatablePart, defaultNumberOfLaps, Collections.emptyMap());
    }

    public CourseTemplate constructCourseTemplate(Pair<Integer, Integer> optionalRepeatablePart,
            Integer defaultNumberOfLaps, Map<MarkTemplate, MarkRole> associatedRoles) {
        return new CourseTemplate("my-special-course-template", Arrays.asList(sb, pe, b1, b4s, b4p, spare),
                extractMarkRoleIds(associatedRoles), waypointSequence, optionalRepeatablePart, Collections.emptySet(),
                null, defaultNumberOfLaps);
    }

    private Map<MarkTemplate, String> extractMarkRoleIds(Map<MarkTemplate, MarkRole> associatedRoles) {
        final Map<MarkTemplate, String> result = new HashMap<>();
        for (Entry<MarkTemplate, MarkRole> associatedRole : associatedRoles.entrySet()) {
            result.put(associatedRole.getKey(), associatedRole.getValue().getId().toString());
        }
        return result;
    }
}
