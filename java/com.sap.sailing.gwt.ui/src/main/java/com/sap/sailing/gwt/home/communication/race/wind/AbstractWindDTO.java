package com.sap.sailing.gwt.home.communication.race.wind;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class AbstractWindDTO implements IsSerializable {

    private Double trueWindFromDeg;

    protected AbstractWindDTO() {
    }

    protected AbstractWindDTO(Double trueWindFromDeg) {
        this.trueWindFromDeg = trueWindFromDeg;
    }

    public Double getTrueWindFromDeg() {
        return trueWindFromDeg;
    }
}
