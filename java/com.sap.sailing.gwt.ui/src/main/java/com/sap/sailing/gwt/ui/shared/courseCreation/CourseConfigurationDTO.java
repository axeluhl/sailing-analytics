package com.sap.sailing.gwt.ui.shared.courseCreation;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sse.common.RepeatablePart;

public class CourseConfigurationDTO implements IsSerializable {
    private String shortName;
    private CourseTemplateDTO optionalCourseTemplate;
    // TODO decide if we should combine markConfigurations and roleMapping to one field
    private ArrayList<MarkConfigurationDTO> markConfigurations;
    private HashMap<MarkConfigurationDTO, MarkRoleDTO> associatedRoles;

    /**
     * The waypoints sequence, with at least a single occurrence of any {@link #getRepeatablePart() repeatable part}, in
     * order to obtain a sequence for more than only one lap (meaning zero occurrences of the repeatable part). There
     * may be more occurrences of the repeatable part than only one, in particular to allow subsequent repetitions to
     * use different mark configurations in their lap. See also {@link #getWaypoints()} and {@link #getWaypoints(int)}.
     * So, if {@link #numberOfLaps} is less than or equal to {@code 1}, an occurrence of the repeatable part will be in
     * this sequence although it isn't used in the current configuration. This is, however, required in order to
     * interpret the index-based repeatable part in case one or more occurrences of the repeatable part will be
     * requested.
     * <p>
     * 
     * This sequence may contain more occurrences of the repeatable part than requested through {@link #numberOfLaps}
     * even if one or more occurrences of the repeatable part are requested this way. These extra occurrences will then
     * be used if {@link #getWaypoints(int)} is called with more laps than {@link #numberOfLaps}. This way, a course
     * configuration can prepare for more laps being sailed and prepare a configuration for extra laps that differ from
     * the configurations used for the laps currently selected.
     */
    private ArrayList<WaypointWithMarkConfigurationDTO> waypoints;

    /**
     * Refers to a repeatable part in {@link #waypoints}. Other than in a {@link CourseTemplate} that always has exactly
     * one occurrence of a repeatable part in its waypoint template sequence, here the {@link #waypoints} sequence must
     * have at least one but may have multiple occurrences of the repeatable part. The special handling that happens in
     * this class is that when more than one occurrence already exists in {@link #waypoints}, upon adding more
     * repetitions the <em>last</em> occurrence will be repeated. See also {@link #getWaypoints()}.
     */
    private RepeatablePart optionalRepeatablePart;

    /**
     * The number of laps this course configuration has, in case there is a {@link #getRepeatablePart() repeatable part}
     * defined for a valid {@link #optionalCourseTemplate}. In this case, {@link #getWaypoints()} will consider this
     * number of laps and will return
     */
    private Integer numberOfLaps;

    private String name;
    private URL optionalImageURL;

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public CourseTemplateDTO getOptionalCourseTemplate() {
        return optionalCourseTemplate;
    }

    public void setOptionalCourseTemplate(CourseTemplateDTO optionalCourseTemplate) {
        this.optionalCourseTemplate = optionalCourseTemplate;
    }

    public ArrayList<MarkConfigurationDTO> getAllMarks() {
        return markConfigurations;
    }

    public void setAllMarks(ArrayList<MarkConfigurationDTO> markConfigurations) {
        this.markConfigurations = markConfigurations;
    }

    public HashMap<MarkConfigurationDTO, MarkRoleDTO> getAssociatedRoles() {
        return associatedRoles;
    }

    public void setAssociatedRoles(HashMap<MarkConfigurationDTO, MarkRoleDTO> associatedRoles) {
        this.associatedRoles = associatedRoles;
    }

    public ArrayList<WaypointWithMarkConfigurationDTO> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(ArrayList<WaypointWithMarkConfigurationDTO> waypoints) {
        this.waypoints = waypoints;
    }

    public RepeatablePart getOptionalRepeatablePart() {
        return optionalRepeatablePart;
    }

    public void setOptionalRepeatablePart(RepeatablePart optionalRepeatablePart) {
        this.optionalRepeatablePart = optionalRepeatablePart;
    }

    public Integer getNumberOfLaps() {
        return numberOfLaps;
    }

    public void setNumberOfLaps(Integer numberOfLaps) {
        this.numberOfLaps = numberOfLaps;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URL getOptionalImageURL() {
        return optionalImageURL;
    }

    public void setOptionalImageURL(URL optionalImageURL) {
        this.optionalImageURL = optionalImageURL;
    }
}
