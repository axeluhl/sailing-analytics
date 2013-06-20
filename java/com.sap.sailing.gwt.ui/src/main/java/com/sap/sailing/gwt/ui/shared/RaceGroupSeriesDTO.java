package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.NamedDTO;

public class RaceGroupSeriesDTO extends NamedDTO implements IsSerializable {
        
    private static final long serialVersionUID = 8432572954520767329L;
    private List<FleetDTO> fleets = new ArrayList<FleetDTO>();
    
    public RaceGroupSeriesDTO() {}

    public RaceGroupSeriesDTO(String name) {
        super(name);
    }

    public List<FleetDTO> getFleets() {
        return fleets;
    }

}
