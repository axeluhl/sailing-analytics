package com.sap.sailing.domain.common.dto;

import java.util.ArrayList;

public class MetaLeaderboardRaceColumnDTO extends RaceColumnDTO {

    private static final long serialVersionUID = 6152752963316150432L;
    
    private ArrayList<BasicRaceDTO> raceList = new ArrayList<BasicRaceDTO>();
    
    public boolean isLive(FleetDTO fleet, long serverTimePointAsMillis) {
        for (BasicRaceDTO race : raceList) {
            if (race.isLive(serverTimePointAsMillis)) {
                return true;
            }
        }
        return false;
    }
    
    public void addRace(BasicRaceDTO race) {
        raceList.add(race);
    }
    
    public ArrayList<BasicRaceDTO> getRaceList() {
        return raceList;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((raceList == null) ? 0 : raceList.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        MetaLeaderboardRaceColumnDTO other = (MetaLeaderboardRaceColumnDTO) obj;
        if (raceList == null) {
            if (other.raceList != null)
                return false;
        } else if (!raceList.equals(other.raceList))
            return false;
        return true;
    }

}
