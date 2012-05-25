package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    public LeaderboardDTO() {}

    public int getRank(CompetitorDTO competitor) {
        return competitors.indexOf(competitor) + 1;
    };
    
    /**
     * Determines the competitor's rank in the race with name <code>raceName</code>. If a race with that name does not exist or the
     * competitor has no score for that race, <code>null</code> is returned; the 1-based rank otherwise.
     */
    public Integer getRank(CompetitorDTO competitor, String raceName) {
        Integer result = null;
        if (rows.get(competitor) != null) {
            LeaderboardEntryDTO fields = rows.get(competitor).fieldsByRaceName.get(raceName);
            if (fields != null) {
                List<CompetitorDTO> competitorsOrderedByNetPoints = new ArrayList<CompetitorDTO>(competitors);
                Collections.sort(competitorsOrderedByNetPoints, new Comparator<CompetitorDTO>() {
                    @Override
                    public int compare(CompetitorDTO o1, CompetitorDTO o2) {
                        return getTotalRankingComparator().compare(rows.get(o1), rows.get(o2));
                    }
                });
                result = competitorsOrderedByNetPoints.indexOf(competitor) + 1;
            }
        }
        return result;
    }

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
