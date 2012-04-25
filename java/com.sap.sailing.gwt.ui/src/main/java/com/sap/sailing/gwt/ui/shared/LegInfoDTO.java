package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.LegType;

public class LegInfoDTO extends NamedDTO implements IsSerializable {
    /**
     * The time point when the leg was first entered by a competitor; for the first leg the semantics
     * are slightly different: if the official race start time is known it is used instead of the first
     * passing event.
     */
    public Date firstPassingDate;

    public int legNumber;
    
    public LegType legType;
    
    public double legBearingInDegrees;

    public LegInfoDTO() {}

    public LegInfoDTO(int legNumber) {
        this.legNumber = legNumber;
    }
}
