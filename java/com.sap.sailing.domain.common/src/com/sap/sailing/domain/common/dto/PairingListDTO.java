package com.sap.sailing.domain.common.dto;

import java.io.Serializable;
import java.util.List;

import com.sap.sse.common.Util.Pair;

public class PairingListDTO implements Serializable{
    
    private static final long serialVersionUID = 102220422437194196L;
    // TODO set Pair of competitors and boats
    private List<List<List<Pair<CompetitorDTO, BoatDTO>>>> pairingList;
    
    //private int maxCompetitorsPerFleet;
    
    public PairingListDTO() { }
    
    public PairingListDTO(List<List<List<Pair<CompetitorDTO, BoatDTO>>>> result) {
        this.pairingList = result;
    }

    public List<List<List<Pair<CompetitorDTO, BoatDTO>>>> getPairingList() {
        return pairingList;
    }
    
    /*
    public int getMaxCompetitorsPerFleet() {
        return maxCompetitorsPerFleet;
    }
    */
}
