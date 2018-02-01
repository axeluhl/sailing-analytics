package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class WindInfoDTO implements IsSerializable {
    public List<WindInfoForRaceDTO> windInfoForRaces;
    
    public WindInfoDTO() {}

    public WindInfoDTO(List<WindInfoForRaceDTO> windInfoForRaces) {
        super();
        this.windInfoForRaces = windInfoForRaces;
    }
}
