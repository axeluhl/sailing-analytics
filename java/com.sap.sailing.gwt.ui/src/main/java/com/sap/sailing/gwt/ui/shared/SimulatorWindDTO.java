package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SimulatorWindDTO implements IsSerializable {
    private boolean isTurn;
    private Double trueWindSpeedInKnots;
    private Double trueWindBearingDeg;
    private PositionDTO position;
    private Long timepoint;

    public SimulatorWindDTO() {
    }

    public SimulatorWindDTO(final double latDeg, final double lngDeg, final double windSpeedKn, final double windBearingDeg, final long timepointMsec,
            final boolean isTurn) {
        this.position = new PositionDTO(latDeg, lngDeg);
        this.trueWindBearingDeg = windBearingDeg;
        this.trueWindSpeedInKnots = windSpeedKn;
        this.timepoint = timepointMsec;
        this.isTurn = isTurn;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;

        long temp = 0;

        temp = this.trueWindSpeedInKnots.intValue();
        result = prime * result + (int) (temp ^ (temp >>> 32));

        temp = this.trueWindBearingDeg.intValue();
        result = prime * result + (int) (temp ^ (temp >>> 32));

        temp = this.timepoint.intValue();
        result = prime * result + (int) (temp ^ (temp >>> 32));

        temp = this.position.hashCode();
        result = prime * result + (int) (temp ^ (temp >>> 32));

        return result;

    }

    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        } else {
            if (o instanceof SimulatorWindDTO) {
                final SimulatorWindDTO other = (SimulatorWindDTO) o;

                return this.position.equals(other.position) && (Math.abs(this.timepoint - other.timepoint) < 9999);
            }
            return false;
        }
    }

    public boolean isTurn() {
        return this.isTurn;
    }

    public void setTurn(final boolean isTurn) {
        this.isTurn = isTurn;
    }

    public Double getTrueWindSpeedInKnots() {
        return this.trueWindSpeedInKnots;
    }

    public void setTrueWindSpeedInKnots(final Double trueWindSpeedInKnots) {
        this.trueWindSpeedInKnots = trueWindSpeedInKnots;
    }

    public Double getTrueWindBearingDeg() {
        return this.trueWindBearingDeg;
    }

    public void setTrueWindBearingDeg(final Double trueWindBearingDeg) {
        this.trueWindBearingDeg = trueWindBearingDeg;
    }

    public PositionDTO getPosition() {
        return this.position;
    }

    public void setPosition(final PositionDTO position) {
        this.position = position;
    }

    public Long getTimepoint() {
        return this.timepoint;
    }

    public void setTimepoint(final Long timepoint) {
        this.timepoint = timepoint;
    }
}
