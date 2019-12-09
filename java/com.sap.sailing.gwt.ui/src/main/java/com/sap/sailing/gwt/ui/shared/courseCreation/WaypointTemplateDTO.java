package com.sap.sailing.gwt.ui.shared.courseCreation;

import java.io.Serializable;
import java.util.ArrayList;

import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sse.common.Util;

public class WaypointTemplateDTO implements Serializable {
    private static final long serialVersionUID = -3137039437059719498L;

    // using concrete type ArrayList because of GWT serialization
    private ArrayList<MarkTemplateDTO> markTemplatesForControlPoint = new ArrayList<>();

    private String passingInstruction;
    private String name;
    private String shortName;

    public WaypointTemplateDTO() {
    }

    public WaypointTemplateDTO(String name, String shortName, Iterable<MarkTemplateDTO> markTemplatesForControlPoint,
            PassingInstruction passingInstruction) {
        super();
        this.shortName = shortName;
        Util.addAll(markTemplatesForControlPoint, this.markTemplatesForControlPoint);
        this.passingInstruction = passingInstruction.name();
        this.name = name;
    }

    public ArrayList<MarkTemplateDTO> getMarkTemplatesForControlPoint() {
        return markTemplatesForControlPoint;
    }

    public PassingInstruction getPassingInstruction() {
        return passingInstruction != null ? PassingInstruction.valueOf(passingInstruction) : null;
    }

    public void setPassingInstruction(String passingInstruction) {
        this.passingInstruction = passingInstruction;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

}
