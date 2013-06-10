package com.sap.sailing.domain.common.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Captures the serializable properties of a leaderboard which in particular has the competitors, any optional display
 * name mappings for the competitors, races and their net / total points as well as possible reasons for maximum points
 * (DNS, DNF, DSQ).
 * 
 * @author Axel Uhl (d043530)
 *  
 */
public class LeaderboardDTO extends AbstractLeaderboardDTO implements Serializable {
    private static final long serialVersionUID = -520930809792750648L;
    
    public interface UUIDGenerator {
        String generateRandomUUID();
    }
    
    /**
     * A unique ID, obtained by applying <code>toString</code> to an object of type <code>java.util.UUID</code>.
     * When {@link #clone} is called on this object, a new ID is generated for the result.
     */
    private String id;
    
    /**
     * The competitor list, ordered ascending by total rank
     */
    public List<CompetitorDTO> competitors;

    private Set<CompetitorDTO> suppressedCompetitors;

    private Map<RaceColumnDTO, List<CompetitorDTO>> competitorOrderingPerRace;

    private Date timePointOfLastCorrectionsValidity;

    private String comment;
    
    /**
     * Taken from the scoring scheme. Shall be used by the race columns to control their initial sort order.
     */
    private boolean higherScoresIsBetter;

    LeaderboardDTO() {} // for serialization

    /**
     * @param uuidGenerator used to provide the {@link #id ID} for this object (see also {@link #getId()}) and for any clones produced
     * from it by the {@link #clone()} operation.
     */
    public LeaderboardDTO(Date timePointOfLastCorrectionsValidity, String comment, boolean higherScoreIsBetter, UUIDGenerator uuidGenerator) {
        initCollections();
        id = uuidGenerator.generateRandomUUID();
        this.timePointOfLastCorrectionsValidity = timePointOfLastCorrectionsValidity;
        this.comment = comment;
        this.higherScoresIsBetter = higherScoreIsBetter;
    }

    private void initCollections() {
        competitorOrderingPerRace = new HashMap<RaceColumnDTO, List<CompetitorDTO>>();
        this.suppressedCompetitors = new HashSet<CompetitorDTO>();
    }
    
    public LeaderboardDTO(String id) {
        initCollections();
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Iterable<CompetitorDTO> getSuppressedCompetitors() {
        return suppressedCompetitors;
    }
    
    public void setSuppressedCompetitors(Set<CompetitorDTO> suppressedCompetitors) {
        this.suppressedCompetitors = suppressedCompetitors;
    }

    public void setSuppressed(CompetitorDTO competitor, boolean suppressed) {
        if (suppressed) {
            suppressedCompetitors.add(competitor);
        } else {
            suppressedCompetitors.remove(competitor);
        }
    }

    public boolean isHigherScoreBetter() {
        return higherScoresIsBetter;
    }

    public void setCompetitorsFromBestToWorst(RaceColumnDTO raceColumn, List<CompetitorDTO> orderedCompetitors) {
        competitorOrderingPerRace.put(raceColumn, orderedCompetitors);
    }
    
    public void setCompetitorOrderingPerRace(Map<RaceColumnDTO, List<CompetitorDTO>> competitorOrderingPerRace) {
        this.competitorOrderingPerRace = competitorOrderingPerRace;
    }

    public Map<RaceColumnDTO, List<CompetitorDTO>> getCompetitorOrderingPerRace() {
        return competitorOrderingPerRace;
    }
    
    public List<CompetitorDTO> getCompetitorsFromBestToWorst(RaceColumnDTO raceColumn) {
        return competitorOrderingPerRace.get(raceColumn);
    }

    public int getRank(CompetitorDTO competitor) {
        return competitors.indexOf(competitor) + 1;
    };

    /**
     * A free-form comment to display to the viewers of the leaderboard that has these score corrections. It should make
     * crystal clear if the scores are preliminary or not yet jury-finalized. If <code>null</code> is returned, this
     * always has to be interpreted as "preliminary" because then no comment as to the correctness have been made.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Tells when the score correction was last updated. This should usually be the "validity time" and not the
     * "transaction time." In other words, if scores provided by the race committee are updated to this score correction
     * at time X, and the race committee's scores are tagged with time Y, then this method should return Y, not X. If
     * Y is not available for some reason, X may be used as a default.
     */
    public Date getTimePointOfLastCorrectionsValidity() {
        return timePointOfLastCorrectionsValidity;
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

    public void setTimePointOfLastCorrectionsValidity(Date timePointOfLastCorrectionsValidity) {
        this.timePointOfLastCorrectionsValidity = timePointOfLastCorrectionsValidity;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
