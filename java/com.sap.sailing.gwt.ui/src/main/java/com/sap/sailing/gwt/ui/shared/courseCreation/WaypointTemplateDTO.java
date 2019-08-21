package com.sap.sailing.gwt.ui.shared.courseCreation;

import java.util.ArrayList;

import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sse.common.Util;

public class WaypointTemplateDTO {
    // using concrete type ArrayList because of GWT serialization
    private ArrayList<MarkTemplateDTO> markTemplatesForControlPoint = new ArrayList<>();

    private PassingInstruction passingInstruction;

    public WaypointTemplateDTO(Iterable<MarkTemplateDTO> markTemplatesForControlPoint,
            PassingInstruction passingInstruction) {
        super();
        Util.addAll(markTemplatesForControlPoint, this.markTemplatesForControlPoint);
        this.passingInstruction = passingInstruction;
    }

    public ArrayList<MarkTemplateDTO> getMarkTemplatesForControlPoint() {
        return markTemplatesForControlPoint;
    }

    public PassingInstruction getPassingInstruction() {
        return passingInstruction;
    }

}
