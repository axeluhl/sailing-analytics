package com.sap.sailing.gwt.ui.shared;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Captures the serializable properties of a leaderboard which in particular has the competitors, any optional display
 * name mappings for the competitors, races and their net / total points as well as possible reasons for maximum points
 * (DNS, DNF, DSQ).
 * 
 * @author Axel Uhl (d043530)
 *  
 */
public class LeaderboardDTO extends AbstractLeaderboardDTO implements IsSerializable {
    /**
     * The competitor list, ordered ascending by total rank
     */
    public List<CompetitorDTO> competitors;
    
    private Map<RaceColumnDTO, List<CompetitorDTO>> competitorOrderingPerRace;

    public LeaderboardDTO() {
        competitorOrderingPerRace = new HashMap<RaceColumnDTO, List<CompetitorDTO>>();
    }
    
    public void setCompetitorsFromBestToWorst(RaceColumnDTO raceColumn, List<CompetitorDTO> orderedCompetitors) {
        competitorOrderingPerRace.put(raceColumn, orderedCompetitors);
    }
    
    public List<CompetitorDTO> getCompetitorsFromBestToWorst(RaceColumnDTO raceColumn) {
        return competitorOrderingPerRace.get(raceColumn);
    }

    public int getRank(CompetitorDTO competitor) {
        return competitors.indexOf(competitor) + 1;
    };
    
    @Override
    public int hashCode() {
        return super.hashCode() ^ ((competitors == null) ? 0 : competitors.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && ((competitors == null) ?
            (((LeaderboardDTO) competitors) != null) ? false : (!competitors.equals(((LeaderboardDTO) obj).competitors)) ? false : true : true);
    }

}
