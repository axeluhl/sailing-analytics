package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;

public class SimulatorWindDTO implements IsSerializable {
    public Boolean isTurn;
    public Double trueWindSpeedInKnots;
    public Double trueWindBearingDeg;
    public Position position;
    public Long timepoint;

    public SimulatorWindDTO() {
        this.isTurn = false;
        this.trueWindBearingDeg = 0.0;
        this.trueWindSpeedInKnots = 0.0;
        this.position = null;
        this.timepoint = 0L;
    }

    public SimulatorWindDTO(Position position, double windSpeedKn, double windBearingDeg, long timepointMsec) {
        this.position = position;
        this.trueWindBearingDeg = windBearingDeg;
        this.trueWindSpeedInKnots = windSpeedKn;
        this.timepoint = timepointMsec;
        this.isTurn = false;
    }

    public SimulatorWindDTO(double latDeg, double lngDeg, double windSpeedKn, double windBearingDeg, long timepointMsec) {
        this.position = new DegreePosition(latDeg, lngDeg);
        this.trueWindBearingDeg = windBearingDeg;
        this.trueWindSpeedInKnots = windSpeedKn;
        this.timepoint = timepointMsec;
        this.isTurn = false;
    }

    public SimulatorWindDTO(double latDeg, double lngDeg, double windSpeedKn, double windBearingDeg, long timepointMsec, boolean isTurn) {
        this.position = new DegreePosition(latDeg, lngDeg);
        this.trueWindBearingDeg = windBearingDeg;
        this.trueWindSpeedInKnots = windSpeedKn;
        this.timepoint = timepointMsec;
        this.isTurn = isTurn;
    }

    @Override
    public int hashCode() {

        int prime = 31;
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
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else {
            if (o instanceof SimulatorWindDTO) {
                SimulatorWindDTO other = (SimulatorWindDTO) o;

                return this.position.equals(other.position) && (Math.abs(this.timepoint - other.timepoint) < 9999);
            }
            return false;
        }
    }
}
