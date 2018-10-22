package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.NamedDTO;

public class RaceGroupDTO extends NamedDTO implements IsSerializable {
        
    private static final long serialVersionUID = 8432572954520767329L;
    public String courseAreaIdAsString;
    public String displayName;
    public String leaderboardGroupName;
    public String boatClass;
    private List<RaceGroupSeriesDTO> series = new ArrayList<RaceGroupSeriesDTO>();
    
    public RaceGroupDTO() {}

    public RaceGroupDTO(String name) {
        super(name);
    }

    public List<RaceGroupSeriesDTO> getSeries() {
        return series;
    }
}
