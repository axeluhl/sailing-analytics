package com.sap.sailing.gwt.ui.shared.courseCreation;

import java.io.Serializable;
import java.util.ArrayList;

import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sse.common.Util;

public class WaypointTemplateDTO implements Serializable{
    private static final long serialVersionUID = -3137039437059719498L;

    // using concrete type ArrayList because of GWT serialization
    private ArrayList<MarkTemplateDTO> markTemplatesForControlPoint = new ArrayList<>();

    private String passingInstruction;
    private String name;

    public WaypointTemplateDTO() {

    }

    public WaypointTemplateDTO(String name, Iterable<MarkTemplateDTO> markTemplatesForControlPoint,
            PassingInstruction passingInstruction) {
        super();
        Util.addAll(markTemplatesForControlPoint, this.markTemplatesForControlPoint);
        this.passingInstruction = passingInstruction.name();
        this.name = name;
    }

    public ArrayList<MarkTemplateDTO> getMarkTemplatesForControlPoint() {
        return markTemplatesForControlPoint;
    }

    public PassingInstruction getPassingInstruction() {
        return PassingInstruction.valueOf(passingInstruction);
    }

    public String getName() {
        return name;
    }

}
