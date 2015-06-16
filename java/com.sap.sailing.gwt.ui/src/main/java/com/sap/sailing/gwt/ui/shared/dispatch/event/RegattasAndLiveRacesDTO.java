package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sse.common.Util.Pair;

public class RegattasAndLiveRacesDTO implements DTO {
    
    private ArrayList<Pair<RegattaMetadataDTO, TreeSet<LiveRaceDTO>>> regattasWithRaces;
    
    @SuppressWarnings("unused")
    private RegattasAndLiveRacesDTO() {
    }

    public RegattasAndLiveRacesDTO(ArrayList<Pair<RegattaMetadataDTO, TreeSet<LiveRaceDTO>>> regattasWithRaces) {
        super();
        this.regattasWithRaces = regattasWithRaces;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<Pair<RegattaMetadataDTO, Set<LiveRaceDTO>>> getRegattasWithRaces() {
        return (List)regattasWithRaces;
    }
}
