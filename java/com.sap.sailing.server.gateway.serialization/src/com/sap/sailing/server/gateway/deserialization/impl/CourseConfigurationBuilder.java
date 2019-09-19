package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.FreestyleMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkPairWithConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkPropertiesBasedMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.MarkTemplateBasedMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.Positioning;
import com.sap.sailing.domain.coursetemplate.RegattaMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.impl.CourseConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.FreestyleMarkConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkPairWithConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkPropertiesBasedMarkConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkTemplateBasedMarkConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.RegattaMarkConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.WaypointWithMarkConfigurationImpl;
import com.sap.sailing.domain.sharedsailingdata.SharedSailingData;

/**
 * Builder API for constructing a {@link CourseConfiguration}. Various domain objects like {@link MarkTemplate}s,
 * {@link Mark}s of {@link MarkProperties} need to get resolved when constructing a {@link CourseConfiguration} using
 * the information given by the UI or API. This builder helps to resolve those domain objects and validate the
 * configuration.
 */
public class CourseConfigurationBuilder {

    private final SharedSailingData sharedSailingData;
    private final Regatta optionalRegatta;
    private final CourseTemplate optionalCourseTemplate;
    // TODO decide if we should combine markConfigurations and roleMapping to one field
    private Set<MarkConfiguration> markConfigurations;
    private Map<MarkConfiguration, String> associatedRoles;
    private List<WaypointWithMarkConfiguration> waypoints;
    private RepeatablePart optionalRepeatablePart;
    private Integer numberOfLaps;
    private Map<MarkPairWithConfiguration, MarkPairWithConfiguration> markPairCache;

    public CourseConfigurationBuilder(SharedSailingData sharedSailingData, Regatta optionalRegatta,
            CourseTemplate optionalCourseTemplate) {
        this.sharedSailingData = sharedSailingData;
        this.optionalRegatta = optionalRegatta;
        this.optionalCourseTemplate = optionalCourseTemplate;
    }

    public MarkConfiguration addMarkConfiguration(UUID optionalMarkTemplateID, UUID optionalMarkPropertiesID,
            UUID optionalMarkID, CommonMarkProperties commonMarkProperties, Positioning optionalPositioning) {
        final MarkConfiguration result;
        if (commonMarkProperties != null) {
            if (optionalMarkID != null) {
                throw new IllegalArgumentException(
                        "Freestyle mark configurations may not reference an existing regatta mark");
            }
            result = addFreestyleMarkConfiguration(optionalMarkTemplateID, optionalMarkPropertiesID,
                    commonMarkProperties, optionalPositioning);
        } else if (optionalMarkID != null) {
            result = addRegattaMarkConfiguration(optionalMarkID, optionalPositioning);
        } else if (optionalMarkPropertiesID != null) {
            result = addMarkPropertiesConfiguration(optionalMarkPropertiesID, optionalPositioning);
        } else if (optionalMarkTemplateID != null) {
            result = addMarkTemplateConfiguration(optionalMarkTemplateID);
        } else {
            throw new IllegalArgumentException(
                    "Mark configuration could not be constructed due to missing specification");
        }
        return result;
    }

    // TODO handle positioning information in all possible cases
    public MarkTemplateBasedMarkConfiguration addMarkTemplateConfiguration(UUID markTemplateID) {
        final MarkTemplate resolvedMarkTemplate = resolveMarkTemplateByID(markTemplateID);
        if (resolvedMarkTemplate == null) {
            throw new IllegalStateException("Mark template with ID " + markTemplateID + " could not be resolved");
        }
        return new MarkTemplateBasedMarkConfigurationImpl(resolvedMarkTemplate);
    }

    /**
     * First tries to resolve from the course template and then by ID using sharedSailingData. This allows users having
     * access to a course template to use all mark templates being included even if they don't have explicit read
     * permissions for those.
     */
    private MarkTemplate resolveMarkTemplateByID(UUID markTemplateID) {
        MarkTemplate resolvedMarkTemplate = null;
        if (optionalCourseTemplate != null) {
            for (MarkTemplate markTemplate : optionalCourseTemplate.getMarkTemplates()) {
                if (markTemplate.getId().equals(markTemplateID)) {
                    resolvedMarkTemplate = markTemplate;
                    break;
                }
            }
        }
        if (resolvedMarkTemplate == null) {
            resolvedMarkTemplate = sharedSailingData.getMarkTemplateById(markTemplateID);
        }
        return resolvedMarkTemplate;
    }

    public MarkPropertiesBasedMarkConfiguration addMarkPropertiesConfiguration(UUID markPropertiesID, Positioning optionalPositioning) {
        final MarkProperties resolvedMarkProperties = sharedSailingData.getMarkPropertiesById(markPropertiesID);
        if (resolvedMarkProperties == null) {
            throw new IllegalArgumentException(
                    "Mark properties with ID " + markPropertiesID + " could not be resolved");
        }
        return new MarkPropertiesBasedMarkConfigurationImpl(resolvedMarkProperties, optionalPositioning);
    }

    public FreestyleMarkConfiguration addFreestyleMarkConfiguration(UUID optionalMarkTemplateID,
            UUID optionalMarkPropertiesID, CommonMarkProperties commonMarkProperties, Positioning optionalPositioning) {
        final MarkTemplate resolvedMarkTemplate = optionalMarkTemplateID == null ? null
                : resolveMarkTemplateByID(optionalMarkTemplateID);
        final MarkProperties resolvedMarkProperties = sharedSailingData.getMarkPropertiesById(optionalMarkPropertiesID);
        // TODO decide if it is fine if we can't resolve a MarkTemplate or MarkProperties here because all appearance
        // properties are available. This vcould potentially cause a lack of tracking information if the MarkProperties
        // isn't available.
        return new FreestyleMarkConfigurationImpl(resolvedMarkTemplate, resolvedMarkProperties, commonMarkProperties, optionalPositioning);
    }

    public RegattaMarkConfiguration addRegattaMarkConfiguration(UUID markID, Positioning optionalPositioning) {
        if (optionalRegatta == null) {
            throw new IllegalStateException();
        }
        for (RaceColumn raceColumn : optionalRegatta.getRaceColumns()) {
            for (Mark mark : raceColumn.getAvailableMarks()) {
                if (mark.getId().equals(markID)) {
                    return new RegattaMarkConfigurationImpl(mark, optionalPositioning, /* TODO optionalMarkTemplate */ null);
                }
            }
        }
        throw new IllegalArgumentException(
                "Mark " + markID + " could not be found in regatta " + optionalRegatta.getName());
    }

    public void addWaypoint(MarkConfiguration markConfiguration, PassingInstruction passingInstruction) {
        if (!markConfigurations.contains(markConfiguration)) {
            throw new IllegalArgumentException();
        }
        waypoints.add(new WaypointWithMarkConfigurationImpl(markConfiguration, passingInstruction));
    }

    public void addWaypoint(MarkConfiguration leftMark, MarkConfiguration rightMark, String name,
            PassingInstruction passingInstruction) {
        if (!markConfigurations.contains(leftMark) || !markConfigurations.contains(rightMark)) {
            throw new IllegalArgumentException();
        }
        final MarkPairWithConfiguration markPair = markPairCache
                .computeIfAbsent(new MarkPairWithConfigurationImpl(name, rightMark, leftMark), mp -> mp);
        waypoints.add(new WaypointWithMarkConfigurationImpl(markPair, passingInstruction));
    }

    public void setRole(MarkConfiguration markConfiguration, String roleName) {
        if (!markConfigurations.contains(markConfiguration)) {
            throw new IllegalArgumentException();
        }
        associatedRoles.forEach((mc, existingRole) -> {
            if (roleName.equals(existingRole) && !mc.equals(markConfiguration)) {
                throw new IllegalArgumentException(
                        "Role name '" + roleName + "' is already used for another mark configuration");
            }
        });
        associatedRoles.put(markConfiguration, roleName);
    }

    public void setOptionalRepeatablePart(RepeatablePart optionalRepeatablePart) {
        this.optionalRepeatablePart = optionalRepeatablePart;
    }

    public void setNumberOfLaps(Integer numberOfLaps) {
        this.numberOfLaps = numberOfLaps;
    }

    public CourseConfiguration build() {
        return new CourseConfigurationImpl(optionalCourseTemplate, markConfigurations, associatedRoles, waypoints,
                optionalRepeatablePart, numberOfLaps);
    }
}
