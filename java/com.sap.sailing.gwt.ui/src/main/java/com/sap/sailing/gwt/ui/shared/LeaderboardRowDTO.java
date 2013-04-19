package com.sap.sailing.gwt.ui.shared;

import java.util.Date;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Holds data about one competitor and all races represented by the owning {@link LeaderboardDTO leaderboard}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class LeaderboardRowDTO implements IsSerializable {
    public CompetitorDTO competitor;
    public Map<String, LeaderboardEntryDTO> fieldsByRaceColumnName;
    public Double carriedPoints;
    public Double totalTimeSailedInSeconds;
    public Double totalTimeSailedDownwindInSeconds;
    public Double maximumSpeedOverGroundInKnots;
    public Date whenMaximumSpeedOverGroundWasAchieved;
    public Double totalTimeSailedUpwindInSeconds;
    public Double totalTimeSailedReachingInSeconds;
    public Double totalDistanceTraveledInMeters;
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((carriedPoints == null) ? 0 : carriedPoints.hashCode());
        result = prime * result + ((competitor == null) ? 0 : competitor.hashCode());
        result = prime * result + ((fieldsByRaceColumnName == null) ? 0 : fieldsByRaceColumnName.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LeaderboardRowDTO other = (LeaderboardRowDTO) obj;
        if (carriedPoints == null) {
            if (other.carriedPoints != null)
                return false;
        } else if (!carriedPoints.equals(other.carriedPoints))
            return false;
        if (competitor == null) {
            if (other.competitor != null)
                return false;
        } else if (!competitor.equals(other.competitor))
            return false;
        if (fieldsByRaceColumnName == null) {
            if (other.fieldsByRaceColumnName != null)
                return false;
        } else if (!fieldsByRaceColumnName.equals(other.fieldsByRaceColumnName))
            return false;
        return true;
    }
}
