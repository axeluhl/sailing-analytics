package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Tack;

public class ManeuverDAO implements IsSerializable {
    public ManeuverType type;
    
    public Tack newTack;
    
    public PositionDAO position;
    
    public Date timepoint;
    
    public SpeedWithBearingDAO speedWithBearingBefore;
    
    public SpeedWithBearingDAO speedWithBearingAfter;
    
    public double directionChangeInDegrees;

    public ManeuverDAO() {}
    
    public ManeuverDAO(ManeuverType type, Tack newTack, PositionDAO position, Date timepoint, SpeedWithBearingDAO speedWithBearingBefore,
            SpeedWithBearingDAO speedWithBearingAfter, double directionChangeInDegrees) {
        super();
        this.type = type;
        this.newTack = newTack;
        this.position = position;
        this.timepoint = timepoint;
        this.speedWithBearingBefore = speedWithBearingBefore;
        this.speedWithBearingAfter = speedWithBearingAfter;
        this.directionChangeInDegrees = directionChangeInDegrees;
    }
    
}
