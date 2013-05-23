package com.sap.sailing.domain.common.dto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.common.impl.Util;

/**
 * 
 */
public class IncrementalLeaderboardDTO extends LeaderboardDTO implements Cloneable, IncrementalOrFullLeaderboardDTO {
    private static final long serialVersionUID = -7011986430671280594L;
    private String isDiffToLeaderboardDTOWithId;
    private LeaderboardDTO updatedFromPreviousVersion;

    private boolean commentUnchanged;
    private boolean competitorsUnchanged;
    private boolean suppressedCompetitorsUnchanged;
    private Set<String> raceColumnNamesForWhichCompetitorOrderingPerRaceUnchanged;
    
    IncrementalLeaderboardDTO() {}

    public IncrementalLeaderboardDTO(String id) {
        super(id);
    }

    @Override
    public LeaderboardDTO getLeaderboardDTO(LeaderboardDTO previousVersion) {
        if (previousVersion == null) {
            throw new IllegalArgumentException("Must provide a valid previous leaderboard version to reconstruct full LeaderboardDTO from differential leaderboard DTO");
        }
        applyThisToPreviousVersionByUpdatingThis(previousVersion);
        return this;
    }

    private void applyThisToPreviousVersionByUpdatingThis(LeaderboardDTO previousVersion) {
        if (this.updatedFromPreviousVersion != null) {
            if (this.updatedFromPreviousVersion != previousVersion) {
                throw new IllegalStateException("This incremental leaderboard DTO was already applied to a different previous version. It cannot be applied multiple times.");
            } else {
                // the previous version remains unchanged; no need to make any changes, no need to throw an exception
            }
        } else {
            if (!isDiffToLeaderboardDTOWithId.equals(previousVersion.getId())) {
                throw new IllegalArgumentException("Trying to apply leaderboard DTO diff to leaderboard DTO with ID "+previousVersion.getId()+
                        " although the diff was meant to be applied to a leaderboard DTO with ID "+isDiffToLeaderboardDTOWithId);
            }
            this.updatedFromPreviousVersion = previousVersion;
            if (this.commentUnchanged) {
                this.setComment(previousVersion.getComment());
            }
            if (this.competitorsUnchanged) {
                this.competitors = previousVersion.competitors;
            }
            if (suppressedCompetitorsUnchanged) {
                Set<CompetitorDTO> suppressedCompetitors = new HashSet<CompetitorDTO>();
                Util.addAll(previousVersion.getSuppressedCompetitors(), suppressedCompetitors);
                setSuppressedCompetitors(suppressedCompetitors);
            }
            // TODO ensure that the races collection has all the necessary RaceColumnDTO objects before looking them up by name
            for (String raceColumnNameForWhichCompetitorOrderingPerRaceUnchanged : raceColumnNamesForWhichCompetitorOrderingPerRaceUnchanged) {
                RaceColumnDTO raceColumn = getRaceColumnByName(raceColumnNameForWhichCompetitorOrderingPerRaceUnchanged);
                // be on the safe side regarding the equals/hashCode implementation of RaceColumnDTO and look it up by name for old and new version
                RaceColumnDTO previousRaceColumn = previousVersion.getRaceColumnByName(raceColumnNameForWhichCompetitorOrderingPerRaceUnchanged);
                setCompetitorsFromBestToWorst(raceColumn, previousVersion.getCompetitorsFromBestToWorst(previousRaceColumn));
            }
            // TODO copy all elements marked as UNCHANGED from the previousVersion to this object
        }
    }

    /**
     * @return for easy chaining, <code>this</code> object is returned
     */
    public IncrementalLeaderboardDTO strip(LeaderboardDTO previousVersion) {
        isDiffToLeaderboardDTOWithId = previousVersion.getId();
        if (Util.equalsWithNull(this.getComment(), previousVersion.getComment())) {
            this.setComment(null);
            this.commentUnchanged = true;
        }
        if (Util.equalsWithNull(competitors, previousVersion.competitors)) {
            competitorsUnchanged = true;
            competitors = null;
        }
        if (Util.equalsWithNull(getSuppressedCompetitors(), previousVersion.getSuppressedCompetitors())) {
            suppressedCompetitorsUnchanged = true;
            setSuppressedCompetitors(null);
        }
        for (RaceColumnDTO raceColumn : this.getRaceList()) {
            List<CompetitorDTO> competitorsFromBestToWorstForRaceColumn = getCompetitorsFromBestToWorst(raceColumn);
            List<CompetitorDTO> previousCompetitorsFrombestToWorstForRaceColumn = previousVersion.getCompetitorsFromBestToWorst(raceColumn);
            if (Util.equalsWithNull(competitorsFromBestToWorstForRaceColumn, previousCompetitorsFrombestToWorstForRaceColumn)) {
                raceColumnNamesForWhichCompetitorOrderingPerRaceUnchanged.add(raceColumn.name);
                removeCompetitorsFromBestToWorst(raceColumn);
            }
        }
        // TODO remove those field values from this which are equal to previousVersion, set ...Unchanged flags accordingly
        return this;
    }
    
}
