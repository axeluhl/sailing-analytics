package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.Position;
import com.sap.sse.common.Duration;

public class ManeuverLossDTO implements IsSerializable {
    public Position maneuverStartPosition;
    public Position maneuverEndPosition;
    public SpeedWithBearingDTO speedWithBearingbefore;
    public Double middleManeuverAngle;
    public Duration maneuverDuration;
    
    public ManeuverLossDTO() {
        
    }

    public ManeuverLossDTO(Position maneuverStartPosition, Position maneuverEndPosition,
            SpeedWithBearingDTO speedWithBearingbefore, Double middleManeuverAngle, Duration maneuverDuration) {
        this.maneuverStartPosition = maneuverStartPosition;
        this.maneuverEndPosition = maneuverEndPosition;
        this.speedWithBearingbefore = speedWithBearingbefore;
        this.middleManeuverAngle = middleManeuverAngle;
        this.maneuverDuration = maneuverDuration;
    }
    
    

}
