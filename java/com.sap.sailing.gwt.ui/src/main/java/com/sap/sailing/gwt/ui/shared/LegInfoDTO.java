package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.LegType;

public class LegInfoDTO extends NamedDTO implements IsSerializable {
    public int legNumber;
    
    public LegType legType;
    
    public double legBearingInDegrees;

    public LegInfoDTO() {}

    public LegInfoDTO(int legNumber) {
        this.legNumber = legNumber;
    }
}
