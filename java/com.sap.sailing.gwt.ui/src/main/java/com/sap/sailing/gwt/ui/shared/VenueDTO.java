package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.List;

import com.sap.sse.security.shared.NamedDTO;

public class VenueDTO extends NamedDTO {
    private static final long serialVersionUID = 2182920890078828150L;
    private List<CourseAreaDTO> courseAreas;

    public VenueDTO() {
    }

    public VenueDTO(String name) {
        super(name);
        this.courseAreas = new ArrayList<CourseAreaDTO>();
    }
    public VenueDTO(String name, List<CourseAreaDTO> courseAreas) {
        super(name);
        this.courseAreas = courseAreas;
    }

    public List<CourseAreaDTO> getCourseAreas() {
        return courseAreas;
    }

    public void setCourseAreas(List<CourseAreaDTO> courseAreas) {
        this.courseAreas = courseAreas;
    }
}
