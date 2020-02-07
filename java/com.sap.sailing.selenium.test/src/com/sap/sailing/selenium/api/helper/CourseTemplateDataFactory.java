package com.sap.sailing.selenium.api.helper;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.coursetemplate.CourseTemplate;
import com.sap.sailing.selenium.api.coursetemplate.MarkRole;
import com.sap.sailing.selenium.api.coursetemplate.MarkRoleApi;
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
    public final MarkRole sbRole, peRole, b1Role, b4sRole, b4pRole;
    public final List<WaypointTemplate> waypointSequence;

    public CourseTemplateDataFactory(ApiContext ctx) {
        // mark templates
        final MarkTemplateApi markTemplateApi = new MarkTemplateApi();
        sb = markTemplateApi.createMarkTemplate(ctx, "Startboat", "sb", null, null, null, MarkType.STARTBOAT.name());
        pe = markTemplateApi.createMarkTemplate(ctx, "Pinend", "pe", "#000000", null, null, MarkType.BUOY.name());
        b1 = markTemplateApi.createMarkTemplate(ctx, "blue", "blue", "#0000FF", null, null, MarkType.BUOY.name());
        b4s = markTemplateApi.createMarkTemplate(ctx, "red", "red", "#FF0000", null, null, MarkType.BUOY.name());
        b4p = markTemplateApi.createMarkTemplate(ctx, "green", "green", "#00FF00", null, null, MarkType.BUOY.name());
        spare = markTemplateApi.createMarkTemplate(ctx, "spare", "spare", "#00FFFF", null, null, MarkType.BUOY.name());
        // mark roles
        final MarkRoleApi markRoleApi = new MarkRoleApi();
        sbRole = markRoleApi.createMarkRole(ctx, "Startboat", "sb");
        peRole = markRoleApi.createMarkRole(ctx, "Pinend", "pe");
        b1Role = markRoleApi.createMarkRole(ctx, "blue", "blue");
        b4sRole = markRoleApi.createMarkRole(ctx, "red", "red");
        b4pRole = markRoleApi.createMarkRole(ctx, "green", "green");
        // default waypoint sequence
        waypointSequence = Arrays.asList(
                new WaypointTemplate("Start/End", PassingInstruction.Line, Arrays.asList(sbRole, peRole)),
                new WaypointTemplate(null, PassingInstruction.Port, Arrays.asList(b1Role)),
                new WaypointTemplate(null, PassingInstruction.Gate, Arrays.asList(b4pRole, b4sRole)),
                new WaypointTemplate(null, PassingInstruction.Port, Arrays.asList(b1Role)),
                new WaypointTemplate("Start/End", PassingInstruction.Line, Arrays.asList(sbRole, peRole)));
    }

    public CourseTemplate constructCourseTemplate() {
        return constructCourseTemplate(null, null);
    }

    public CourseTemplate constructCourseTemplate(Pair<Integer, Integer> optionalRepeatablePart, Integer defaultNumberOfLaps) {
        final Map<MarkRole, MarkTemplate> defaultAssociatedRoles = new HashMap<>();
        defaultAssociatedRoles.put(sbRole, sb);
        defaultAssociatedRoles.put(peRole, pe);
        defaultAssociatedRoles.put(b1Role, b1);
        defaultAssociatedRoles.put(b4sRole, b4s);
        defaultAssociatedRoles.put(b4pRole, b4p);
        return this.constructCourseTemplate(optionalRepeatablePart, defaultNumberOfLaps, defaultAssociatedRoles);
    }

    public CourseTemplate constructCourseTemplate(Pair<Integer, Integer> optionalRepeatablePart,
            Integer defaultNumberOfLaps, Map<MarkRole, MarkTemplate> associatedRoles) {
        return new CourseTemplate("my-special-course-template", "msct", Arrays.asList(sb, pe, b1, b4s, b4p, spare),
                associatedRoles, waypointSequence, optionalRepeatablePart, Collections.emptySet(),
                null, defaultNumberOfLaps);
    }
}
