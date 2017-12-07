package com.sap.sailing.domain.common.dto;

import java.io.Serializable;
import java.util.List;

public class PairingListDTO implements Serializable{
    
    private static final long serialVersionUID = 102220422437194196L;
    // TODO set Pair of competitors and boats
    private List<List<List<CompetitorDTO>>> pairingList;
    
    public PairingListDTO() { }
    
    public PairingListDTO(List<List<List<CompetitorDTO>>> competitors) {
        this.pairingList = competitors;
    }

    public List<List<List<CompetitorDTO>>> getPairingList() {
        return pairingList;
    }
}
