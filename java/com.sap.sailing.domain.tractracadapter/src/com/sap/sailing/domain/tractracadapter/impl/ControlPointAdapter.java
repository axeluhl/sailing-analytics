package com.sap.sailing.domain.tractracadapter.impl;

import java.util.UUID;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.tractracadapter.TracTracControlPoint;
import com.tractrac.model.lib.api.route.IControl;

public class ControlPointAdapter extends AbstractWithID implements TracTracControlPoint {
    private final IControl controlPoint;

    public ControlPointAdapter(IControl controlPoint) {
        super();
        this.controlPoint = controlPoint;
    }

    @Override
    public UUID getId() {
        return controlPoint.getId();
    }

    @Override
    public String getName() {
        return controlPoint.getName();
    }

    @Override
    public String getMetadata() {
        String result;
        if (controlPoint.getMetadata() != null && !controlPoint.getMetadata().isEmpty()) {
            result = controlPoint.getMetadata().getText();
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Short name is not offered through the regular TTCM IControl API.
     * 
     * @return <code>null</code>
     */
    @Override
    public String getShortName() {
        return null;
    }

    @Override
    public boolean getHasTwoPoints() {
        return controlPoint.isMultiple();
    }

    @Override
    public Position getMark1Position() {
        return new DegreePosition(controlPoint.getControlPoints().get(0).getPosition().getLatitude(), controlPoint.getControlPoints().get(0).getPosition().getLongitude());
    }

    @Override
    public Position getMark2Position() {
        Position result;
        if (getHasTwoPoints()) {
            result = new DegreePosition(controlPoint.getControlPoints().get(1).getPosition().getLatitude(), controlPoint.getControlPoints().get(1).getPosition().getLongitude());
        } else {
            result = null;
        }
        return result;
    }
    
}
