package com.sap.sailing.domain.tractracadapter.impl;

import java.util.UUID;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.tractrac.clientmodule.ControlPoint;

public class ControlPointAdapter extends AbstractTracTracControlPoint {
    private final ControlPoint controlPoint;

    public ControlPointAdapter(ControlPoint controlPoint) {
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
     * Short name is not offered through the regular TTCM ControlPoint API.
     * 
     * @return <code>null</code>
     */
    @Override
    public String getShortName() {
        return null;
    }

    @Override
    public boolean getHasTwoPoints() {
        return controlPoint.getHasTwoPoints();
    }

    @Override
    public Position getMark1Position() {
        return new DegreePosition(controlPoint.getLat1(), controlPoint.getLon1());
    }

    @Override
    public Position getMark2Position() {
        Position result;
        if (getHasTwoPoints()) {
            result = new DegreePosition(controlPoint.getLat2(), controlPoint.getLon2());
        } else {
            result = null;
        }
        return result;
    }
    
}
