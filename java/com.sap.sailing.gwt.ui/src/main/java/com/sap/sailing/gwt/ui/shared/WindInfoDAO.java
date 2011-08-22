package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class WindInfoDAO implements IsSerializable {
    public List<WindInfoForRaceDAO> windInfoForRaces;
    
    public WindInfoDAO() {}

    public WindInfoDAO(List<WindInfoForRaceDAO> windInfoForRaces) {
        super();
        this.windInfoForRaces = windInfoForRaces;
    }
}
