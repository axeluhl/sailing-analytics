package com.sap.sailing.domain.common.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sap.sse.common.Util.Pair;

public class PairingListDTO implements Serializable {
    
    private static final long serialVersionUID = 102220422437194196L;
    private List<List<List<Pair<CompetitorDTO, BoatDTO>>>> pairingList;
    private List<String> raceColumnNames;
    
    public PairingListDTO() { }
    
    public PairingListDTO(List<List<List<Pair<CompetitorDTO, BoatDTO>>>> result) {
        this(result, null);
    }
    
    public PairingListDTO(List<List<List<Pair<CompetitorDTO, BoatDTO>>>> result, List<String> raceColumnNames) {
        this.pairingList = result;
        this.raceColumnNames = raceColumnNames;
    }

    public List<List<List<Pair<CompetitorDTO, BoatDTO>>>> getPairingList() {
        return pairingList;
    }
    
    public List<BoatDTO> getBoats() {
        List<BoatDTO> boats = new ArrayList<>();
        for (List<Pair<CompetitorDTO, BoatDTO>> fleet : this.pairingList.get(0)) {
            for (Pair<CompetitorDTO, BoatDTO> competitorAndBoatPair : fleet) {
                if (!boats.contains(competitorAndBoatPair.getB())) {
                    boats.add(competitorAndBoatPair.getB());
                }
            }
        }
        return boats;
    }
    
    public List<String> getRaceColumnNames() {
        return raceColumnNames;
    }
}
