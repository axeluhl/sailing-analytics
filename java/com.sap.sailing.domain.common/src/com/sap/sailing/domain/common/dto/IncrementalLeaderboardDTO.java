package com.sap.sailing.domain.common.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
    
    /**
     * If <code>null</code>, {@link #competitors} is filled and must not be touched; otherwise, the size of the array tells the new size
     * of {@link #competitors}, and for each index it either tells the index of the {@link CompetitorDTO} object to use in the previous
     * leaderboard's {@link #competitors} list, or holds -1, meaning that the next {@link CompetitorDTO} object is to be used from
     * {@link #addedCompetitors} which only needs to hold a valid collection if at least one new competitor exists.
     */
    private int[] competitorIndexesInPreviousCompetitorsList;
    
    private List<CompetitorDTO> addedCompetitors;
    
    private boolean suppressedCompetitorsUnchanged;
    private boolean competitorDisplayNamesUnchanged;
    private boolean regattaNameUnchanged;
    private boolean displayNameUnchanged;
    private boolean defaultCourseAreaIdAsStringUnchanged;
    private boolean defaultCourseAreaNameUnchanged;

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
            if (this.defaultCourseAreaIdAsStringUnchanged) {
                this.defaultCourseAreaIdAsString = previousVersion.defaultCourseAreaIdAsString;
            }
            if (this.defaultCourseAreaNameUnchanged) {
                this.defaultCourseAreaName = previousVersion.defaultCourseAreaName;
            }
            if (this.displayNameUnchanged) {
                this.displayName = previousVersion.displayName;
            }
            if (this.regattaNameUnchanged) {
                this.regattaName = previousVersion.regattaName;
            }
            if (this.competitorIndexesInPreviousCompetitorsList != null) {
                this.competitors = new ArrayList<CompetitorDTO>(competitorIndexesInPreviousCompetitorsList.length);
                Iterator<CompetitorDTO> addedCompetitorsIter = null;
                for (int i=0; i<competitorIndexesInPreviousCompetitorsList.length; i++) {
                    if (competitorIndexesInPreviousCompetitorsList[i] == -1) {
                        if (addedCompetitorsIter==null) {
                            addedCompetitorsIter = addedCompetitors.iterator();
                        }
                        this.competitors.add(addedCompetitorsIter.next());
                    } else {
                        this.competitors.add(previousVersion.competitors.get(competitorIndexesInPreviousCompetitorsList[i]));
                    }
                }
            }
            if (suppressedCompetitorsUnchanged) {
                Set<CompetitorDTO> suppressedCompetitors = new HashSet<CompetitorDTO>();
                Util.addAll(previousVersion.getSuppressedCompetitors(), suppressedCompetitors);
                setSuppressedCompetitors(suppressedCompetitors);
            }
            if (competitorDisplayNamesUnchanged) {
                competitorDisplayNames = previousVersion.competitorDisplayNames;
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
        if (Util.equalsWithNull(this.regattaName, previousVersion.regattaName)) {
            this.regattaName = null;
            this.regattaNameUnchanged = true;
        }
        if (Util.equalsWithNull(this.displayName, previousVersion.displayName)) {
            this.displayName = null;
            this.displayNameUnchanged = true;
        }
        if (Util.equalsWithNull(this.defaultCourseAreaIdAsString, previousVersion.defaultCourseAreaIdAsString)) {
            this.defaultCourseAreaIdAsString = null;
            this.defaultCourseAreaIdAsStringUnchanged = true;
        }
        if (Util.equalsWithNull(this.defaultCourseAreaName, previousVersion.defaultCourseAreaName)) {
            this.defaultCourseAreaName = null;
            this.defaultCourseAreaNameUnchanged = true;
        }
        competitorIndexesInPreviousCompetitorsList = new int[competitors.size()];
        int i=0;
        for (CompetitorDTO competitor : competitors) {
            int indexInPrevious = previousVersion.competitors.indexOf(competitor);
            competitorIndexesInPreviousCompetitorsList[i++] = indexInPrevious;
            if (indexInPrevious == -1) {
                if (addedCompetitors == null) {
                    addedCompetitors = new ArrayList<CompetitorDTO>();
                }
                addedCompetitors.add(competitor);
            }
        }
        competitors = null;
        if (Util.equalsWithNull(getSuppressedCompetitors(), previousVersion.getSuppressedCompetitors())) {
            suppressedCompetitorsUnchanged = true;
            setSuppressedCompetitors(null);
        }
        if (Util.equalsWithNull(competitorDisplayNames, previousVersion.competitorDisplayNames)) {
            competitorDisplayNamesUnchanged = true;
            competitorDisplayNames = null;
        }
        raceColumnNamesForWhichCompetitorOrderingPerRaceUnchanged = new HashSet<String>();
        final HashMap<RaceColumnDTO, List<CompetitorDTO>> competitorOrderingPerRace = new HashMap<RaceColumnDTO, List<CompetitorDTO>>(getCompetitorOrderingPerRace());
        for (RaceColumnDTO raceColumn : this.getRaceList()) {
            List<CompetitorDTO> competitorsFromBestToWorstForRaceColumn = getCompetitorsFromBestToWorst(raceColumn);
            List<CompetitorDTO> previousCompetitorsFrombestToWorstForRaceColumn = previousVersion.getCompetitorsFromBestToWorst(raceColumn);
            if (Util.equalsWithNull(competitorsFromBestToWorstForRaceColumn, previousCompetitorsFrombestToWorstForRaceColumn)) {
                raceColumnNamesForWhichCompetitorOrderingPerRaceUnchanged.add(raceColumn.name);
                competitorOrderingPerRace.remove(raceColumn);
            }
        }
        setCompetitorOrderingPerRace(competitorOrderingPerRace);
        // TODO remove those field values from this which are equal to previousVersion, set ...Unchanged flags accordingly
        return this;
    }
    
}
