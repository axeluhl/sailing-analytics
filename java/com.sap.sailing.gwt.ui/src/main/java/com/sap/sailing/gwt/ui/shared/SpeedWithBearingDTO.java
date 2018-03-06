package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;

public class SpeedWithBearingDTO extends DegreeBearingImpl implements IsSerializable {
    private static final long serialVersionUID = 6612188266415659733L;
    public double speedInKnots;

    /**GWT only**/
    public SpeedWithBearingDTO() {
        super(0);
    }
    
    public SpeedWithBearingDTO(double speedInKnots, double bearingInDegrees) {
        super(bearingInDegrees);
        this.speedInKnots = speedInKnots;
    }

    @Override
    public String toString() {
        return "SpeedWithBearingDTO [speedInKnots=" + speedInKnots + ", getDegrees()=" + getDegrees() + "]";
    }

}
