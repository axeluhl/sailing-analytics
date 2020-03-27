package com.sap.sailing.shared.server.operations;

import java.net.URL;
import java.util.Map;
import java.util.UUID;

import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;
import com.sap.sailing.shared.server.impl.ReplicatingSharedSailingData;

public class CreateCourseTemplateOperation implements SharedSailingDataOperation<Void> {
    private static final long serialVersionUID = 5028364832943967084L;
    protected final UUID idOfNewCourseTemplate;
    protected final String courseTemplateName;
    protected final String courseTemplateShortName;
    protected final Iterable<MarkTemplate> marks;
    protected final Iterable<WaypointTemplate> waypoints;
    protected final Map<MarkTemplate, MarkRole> associatedRoles;
    protected final Map<MarkRole, MarkTemplate> defaultMarkTemplatesForMarkRoles;
    protected final RepeatablePart optionalRepeatablePart;
    protected final Iterable<String> tags;
    protected final URL optionalImageURL;
    protected final Integer defaultNumberOfLaps;

    public CreateCourseTemplateOperation(UUID idOfNewCourseTemplate, String courseTemplateName,
            String courseTemplateShortName, Iterable<MarkTemplate> marks, Iterable<WaypointTemplate> waypoints,
            Map<MarkTemplate, MarkRole> associatedRoles, Map<MarkRole, MarkTemplate> defaultMarkTemplatesForMarkRoles,
            RepeatablePart optionalRepeatablePart, Iterable<String> tags, URL optionalImageURL,
            Integer defaultNumberOfLaps) {
        this.idOfNewCourseTemplate = idOfNewCourseTemplate;
        this.courseTemplateName = courseTemplateName;
        this.courseTemplateShortName = courseTemplateShortName;
        this.marks = marks;
        this.waypoints = waypoints;
        this.associatedRoles = associatedRoles;
        this.defaultMarkTemplatesForMarkRoles = defaultMarkTemplatesForMarkRoles;
        this.optionalRepeatablePart = optionalRepeatablePart;
        this.tags = tags;
        this.optionalImageURL = optionalImageURL;
        this.defaultNumberOfLaps = defaultNumberOfLaps;
    }

    @Override
    public Void internalApplyTo(ReplicatingSharedSailingData toState) throws Exception {
        toState.internalCreateCourseTemplate(idOfNewCourseTemplate, courseTemplateName, courseTemplateShortName, marks,
                waypoints, associatedRoles, defaultMarkTemplatesForMarkRoles, optionalRepeatablePart, tags,
                optionalImageURL, defaultNumberOfLaps);
        return null;
    }
}
