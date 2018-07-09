package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.Position;
import com.sap.sse.common.Duration;

public class ManeuverLossDTO implements IsSerializable {
    public Position maneuverStartPosition;
    public Position maneuverEndPosition;
    public SpeedWithBearingDTO speedWithBearingBefore;
    public Double middleManeuverAngle;
    public Duration maneuverDuration;
    public Double projectedDistanceLostInMeters;
    
    public ManeuverLossDTO() {
    }

    public ManeuverLossDTO(Position maneuverStartPosition, Position maneuverEndPosition,
            SpeedWithBearingDTO speedWithBearingBefore, Double middleManeuverAngle, Duration maneuverDuration, Double projectedDistanceLostInMeters) {
        this.maneuverStartPosition = maneuverStartPosition;
        this.maneuverEndPosition = maneuverEndPosition;
        this.speedWithBearingBefore = speedWithBearingBefore;
        this.middleManeuverAngle = middleManeuverAngle;
        this.maneuverDuration = maneuverDuration;
        this.projectedDistanceLostInMeters = projectedDistanceLostInMeters;
    }
    
    public Duration getManeuverDuration() {
        return maneuverDuration;
    }
    public Position getManeuverStartPosition() {
        return maneuverStartPosition;
    }
    public Position getManeuverEndPosition() {
        return maneuverEndPosition;
    }
    public Double getMiddleManeuverAngle() {
        return middleManeuverAngle;
    }
    public SpeedWithBearingDTO getSpeedWithBearingBefore() {
        return speedWithBearingBefore;
    }
    public Double getDistanceLostInMeters() {
        return projectedDistanceLostInMeters;
    }
}