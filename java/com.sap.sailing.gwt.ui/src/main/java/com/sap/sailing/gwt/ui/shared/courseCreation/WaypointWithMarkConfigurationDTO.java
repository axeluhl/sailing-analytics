package com.sap.sailing.gwt.ui.shared.courseCreation;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.PassingInstruction;

public class WaypointWithMarkConfigurationDTO implements IsSerializable {
    private ControlPointWithMarkConfigurationDTO controlPoint;
    private PassingInstruction passingInstruction;

    public ControlPointWithMarkConfigurationDTO getControlPoint() {
        return controlPoint;
    }

    public void setControlPoint(ControlPointWithMarkConfigurationDTO controlPoint) {
        this.controlPoint = controlPoint;
    }

    public PassingInstruction getPassingInstruction() {
        return passingInstruction;
    }

    public void setPassingInstruction(PassingInstruction passingInstruction) {
        this.passingInstruction = passingInstruction;
    }
}
