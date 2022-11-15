package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.dto.CourseAreaDTO;
import com.sap.sse.security.shared.dto.NamedDTO;

public class VenueDTO extends NamedDTO {
    private static final long serialVersionUID = 2182920890078828150L;
    private List<CourseAreaDTO> courseAreas;

    @Deprecated
    VenueDTO() {} // for GWT RPC serialization only

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
