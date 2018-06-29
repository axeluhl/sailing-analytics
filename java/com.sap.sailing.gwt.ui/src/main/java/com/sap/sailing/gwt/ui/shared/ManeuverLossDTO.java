package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.Position;
import com.sap.sse.common.Duration;

public class ManeuverLossDTO implements IsSerializable {
    private Position maneuverStartPosition;
    private Position maneuverEndPosition;
    private SpeedWithBearingDTO speedWithBearingBefore;
    private Double middleManeuverAngle;
    private Duration maneuverDuration;
    
    public ManeuverLossDTO() {
    }

    public ManeuverLossDTO(Position maneuverStartPosition, Position maneuverEndPosition,
            SpeedWithBearingDTO speedWithBearingBefore, Double middleManeuverAngle, Duration maneuverDuration) {
        this.maneuverStartPosition = maneuverStartPosition;
        this.maneuverEndPosition = maneuverEndPosition;
        this.speedWithBearingBefore = speedWithBearingBefore;
        this.middleManeuverAngle = middleManeuverAngle;
        this.maneuverDuration = maneuverDuration;
    }

    public Position getManeuverStartPosition() {
        return maneuverStartPosition;
    }

    public Position getManeuverEndPosition() {
        return maneuverEndPosition;
    }

    public SpeedWithBearingDTO getSpeedWithBearingBefore() {
        return speedWithBearingBefore;
    }

    public Double getMiddleManeuverAngle() {
        return middleManeuverAngle;
    }

    public Duration getManeuverDuration() {
        return maneuverDuration;
    }
}