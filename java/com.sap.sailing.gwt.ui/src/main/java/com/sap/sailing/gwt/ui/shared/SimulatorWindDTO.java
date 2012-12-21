package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SimulatorWindDTO implements IsSerializable {
    public Boolean isTurn;
    public Double trueWindSpeedInKnots;
    public Double trueWindBearingDeg;
    public PositionDTO position;
    public Long timepoint;

    public SimulatorWindDTO() {
        this.isTurn = false;
        this.trueWindBearingDeg = 0.0;
        this.trueWindSpeedInKnots = 0.0;
        this.position = null;
        this.timepoint = 0L;
    }

    public SimulatorWindDTO(final PositionDTO position, final double windSpeedKn, final double windBearingDeg, final long timepointMsec) {
        this.position = position;
        this.trueWindBearingDeg = windBearingDeg;
        this.trueWindSpeedInKnots = windSpeedKn;
        this.timepoint = timepointMsec;
        this.isTurn = false;
    }

    public SimulatorWindDTO(final double latDeg, final double lngDeg, final double windSpeedKn, final double windBearingDeg, final long timepointMsec) {
        this.position = new PositionDTO(latDeg, lngDeg);
        this.trueWindBearingDeg = windBearingDeg;
        this.trueWindSpeedInKnots = windSpeedKn;
        this.timepoint = timepointMsec;
        this.isTurn = false;
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
}
