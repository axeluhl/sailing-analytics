package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SpeedBearingPositionDTO implements IsSerializable {

    private PositionDTO position;
    private SpeedWithBearingDTO speedWithBearing;

    public SpeedBearingPositionDTO() {
    }

    public SpeedBearingPositionDTO(final PositionDTO position, final SpeedWithBearingDTO speedWithBearing) {
        this.position = position;
        this.speedWithBearing = speedWithBearing;
    }

    public SpeedWithBearingDTO getSpeedWithBearing() {
        return this.speedWithBearing;
    }

    public void setSpeedWithBearing(final SpeedWithBearingDTO speedWithBearing) {
        this.speedWithBearing = speedWithBearing;
    }

    public PositionDTO getPosition() {
        return this.position;
    }

    public void setPosition(final PositionDTO position) {
        this.position = position;
    }

}
