package com.sap.sailing.gwt.ui.shared.race;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RaceProgressDTO implements IsSerializable {
    private Integer currentLeg;
    private Integer totalLegs;
    
    @SuppressWarnings("unused")
    private RaceProgressDTO() {
    }

    public RaceProgressDTO(Integer currentLeg, Integer totalLegs) {
        super();
        this.currentLeg = currentLeg;
        this.totalLegs = totalLegs;
    }

    public Integer getCurrentLeg() {
        return currentLeg;
    }

    public Integer getTotalLegs() {
        return totalLegs;
    }
}
