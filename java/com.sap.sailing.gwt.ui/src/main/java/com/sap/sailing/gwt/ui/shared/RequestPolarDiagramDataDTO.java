package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RequestPolarDiagramDataDTO implements IsSerializable {

    private int boatClass;
    private List<PositionDTO> positions;
    private SpeedWithBearingDTO windSpeed;

    public RequestPolarDiagramDataDTO() {

    }

    public RequestPolarDiagramDataDTO(final int boatClass, final List<PositionDTO> positions, final SpeedWithBearingDTO windSpeed) {
        this.boatClass = boatClass;
        this.positions = positions;
        this.windSpeed = windSpeed;
    }

    public int getBoatClass() {
        return this.boatClass;
    }

    public void setBoatClass(final int boatClass) {
        this.boatClass = boatClass;
    }

    public List<PositionDTO> getPositions() {
        return this.positions;
    }

    public void setPositions(final List<PositionDTO> positions) {
        this.positions = positions;
    }

    public SpeedWithBearingDTO getWindSpeed() {
        return this.windSpeed;
    }

    public void setWindSpeed(final SpeedWithBearingDTO windSpeed) {
        this.windSpeed = windSpeed;
    }
}
