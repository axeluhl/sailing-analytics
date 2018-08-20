package com.sap.sailing.domain.tractracadapter.impl;

import java.util.UUID;

import com.sap.sailing.domain.tractracadapter.TracTracControlPoint;
import com.tractrac.model.lib.api.route.IControl;

public class ControlPointAdapter extends AbstractWithID implements TracTracControlPoint {
    private static final long serialVersionUID = 1012632574166553433L;
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
    public UUID getFirstMarkId() {
        return controlPoint.getControlPoints().get(0).getId();
    }

    @Override
    public UUID getSecondMarkId() {
        return controlPoint.getControlPoints().get(1).getId();
    }
}
