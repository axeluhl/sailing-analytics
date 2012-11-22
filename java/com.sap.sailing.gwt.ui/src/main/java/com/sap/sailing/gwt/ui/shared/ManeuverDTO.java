package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Tack;

public class ManeuverDTO implements IsSerializable {
    public ManeuverType type;
    
    public Tack newTack;
    
    public PositionDTO position;
    
    public Date timepoint;
    
    public SpeedWithBearingDTO speedWithBearingBefore;
    
    public SpeedWithBearingDTO speedWithBearingAfter;
    
    public double directionChangeInDegrees;
    
    public Double maneuverLossInMeters;

    public ManeuverDTO() {}
    
    public ManeuverDTO(ManeuverType type, Tack newTack, PositionDTO position, Date timepoint, SpeedWithBearingDTO speedWithBearingBefore,
            SpeedWithBearingDTO speedWithBearingAfter, double directionChangeInDegrees, Double maneuverLossInMeters) {
        super();
        this.type = type;
        this.newTack = newTack;
        this.position = position;
        this.timepoint = timepoint;
        this.speedWithBearingBefore = speedWithBearingBefore;
        this.speedWithBearingAfter = speedWithBearingAfter;
        this.directionChangeInDegrees = directionChangeInDegrees;
        this.maneuverLossInMeters = maneuverLossInMeters;
    }
    
}
