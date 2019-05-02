package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sse.security.shared.dto.NamedDTO;

public class RaceGroupSeriesDTO extends NamedDTO implements IsSerializable {

    private static final long serialVersionUID = 8432572954520767329L;
    private List<FleetDTO> fleets = new ArrayList<FleetDTO>();
    private List<RaceColumnDTO> raceColumns = new ArrayList<RaceColumnDTO>();
    
    protected RaceGroupSeriesDTO() {}

    public RaceGroupSeriesDTO(String name) {
        super(name);
    }

    public List<FleetDTO> getFleets() {
        return fleets;
    }

    public List<RaceColumnDTO> getRaceColumns() {
        return raceColumns;
    }

    public boolean hasOrderedFleets() {
        boolean result = false;
        int firstOrderNo = fleets.get(0).getOrderNo();
        for (FleetDTO fleet : fleets) {
            if (fleet.getOrderNo() != firstOrderNo) {
                result = true;
                break;
            }
        }
        return result;
    }
}
