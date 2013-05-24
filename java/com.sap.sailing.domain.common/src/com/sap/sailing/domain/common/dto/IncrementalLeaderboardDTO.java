package com.sap.sailing.domain.common.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.common.Cloner;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;

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
    
    /**
     * If a {@link LeaderboardEntryDTO} in {@link LeaderboardRowDTO#fieldsByRaceColumnName} is unchanged, this field
     * holds a valid set which contains the position of the competitor in the previous leaderboard's
     * {@link LeaderboardDTO#competitors} list and the race column name for which the entry is unchanged. If the
     * {@link LeaderboardEntryDTO}s are unchanged for <em>all</em> competitors in a column and the previous version has
     * entries for the same set of competitors as the new version, a single entry with <code>null</code> as the
     * {@link Integer} is provided. In this case, the {@link #applyThisToPreviousVersionByUpdatingThis(LeaderboardDTO)} method
     * can simply copy all {@link LeaderboardEntryDTO}s for the entire column from the previous version.
     */
    private Set<Pair<Integer, String>> unchangedLeaderboardEntryDTOsByCompetitorIndexInPreviousLeaderboardAndRaceColumnName;

    private Set<String> raceColumnNamesForWhichCompetitorOrderingPerRaceUnchanged;
    
    private transient Cloner cloner;
    
    /**
     * If a {@link LegEntryDTO} is unchanged in an otherwise modified {@link LeaderboardEntryDTO}, the triple of
     * competitor index in the previous leaderboard's competitors list, the name of the race column, and the
     * index of the {@link LegEntryDTO} in the {@link LeaderboardEntryDTO#legDetails} list is recorded here.
     * Note that this field can be <code>null</code>, meaning that there are no such leg details.
     */
    private Set<Triple<Integer, String, Integer>> legDetailsUnchanged;
    
    IncrementalLeaderboardDTO() {}

    public IncrementalLeaderboardDTO(String id, Cloner cloner) {
        super(id);
        this.cloner = cloner;
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
            for (Pair<Integer, String> competitorIndexAndColumnName : unchangedLeaderboardEntryDTOsByCompetitorIndexInPreviousLeaderboardAndRaceColumnName) {
                if (competitorIndexAndColumnName.getA() == null) {
                    // this means that all entries remained unchanged compared to the previous version for this column
                    for (Map.Entry<CompetitorDTO, LeaderboardRowDTO> e : rows.entrySet()) {
                        LeaderboardEntryDTO previousEntry = previousVersion.rows.get(e.getKey()).fieldsByRaceColumnName.get(competitorIndexAndColumnName.getB());
                        e.getValue().fieldsByRaceColumnName.put(competitorIndexAndColumnName.getB(), previousEntry);
                    }
                } else {
                    CompetitorDTO previousCompetitor = previousVersion.competitors.get(competitorIndexAndColumnName.getA());
                    LeaderboardEntryDTO previousEntry = previousVersion.rows.get(previousCompetitor).fieldsByRaceColumnName
                            .get(competitorIndexAndColumnName.getB());
                    rows.get(previousCompetitor).fieldsByRaceColumnName.put(competitorIndexAndColumnName.getB(),previousEntry);
                }
            }
            if (legDetailsUnchanged != null) {
                for (Triple<Integer, String, Integer> competitorIndexInPreviosAndColumnNameAndLegDetailsIndex : legDetailsUnchanged) {
                    if (competitorIndexInPreviosAndColumnNameAndLegDetailsIndex.getA() == null) {
                        // use all leg details for the column / leg index for all competitors from previous version
                        for (Map.Entry<CompetitorDTO, LeaderboardRowDTO> e : rows.entrySet()) {
                            LegEntryDTO previousEntry = previousVersion.rows.get(e.getKey()).fieldsByRaceColumnName
                                    .get(competitorIndexInPreviosAndColumnNameAndLegDetailsIndex.getB()).legDetails
                                    .get(competitorIndexInPreviosAndColumnNameAndLegDetailsIndex.getC());
                            LeaderboardEntryDTO leaderboardEntry = rows.get(e.getKey()).fieldsByRaceColumnName.get(competitorIndexInPreviosAndColumnNameAndLegDetailsIndex.getB());
                            leaderboardEntry.legDetails.set(competitorIndexInPreviosAndColumnNameAndLegDetailsIndex.getC(), previousEntry);
                        }
                    } else {
                        CompetitorDTO previousCompetitor = previousVersion.competitors.get(competitorIndexInPreviosAndColumnNameAndLegDetailsIndex.getA());
                        LeaderboardEntryDTO leaderboardEntry = rows.get(previousCompetitor).fieldsByRaceColumnName.get(competitorIndexInPreviosAndColumnNameAndLegDetailsIndex.getB());
                        leaderboardEntry.legDetails.set(competitorIndexInPreviosAndColumnNameAndLegDetailsIndex.getC(),
                                previousVersion.rows.get(previousCompetitor).fieldsByRaceColumnName
                                        .get(competitorIndexInPreviosAndColumnNameAndLegDetailsIndex.getB()).legDetails
                                        .get(competitorIndexInPreviosAndColumnNameAndLegDetailsIndex.getC()));
                    }
                }
            }
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
        // now clone the rows map to enable stripping the LeaderboardEntryDTOs inside
        HashMap<CompetitorDTO, LeaderboardRowDTO> newRows = new HashMap<CompetitorDTO, LeaderboardRowDTO>(rows);
        Map<String, Set<CompetitorDTO>> unchangedLeaderboardEntriesForCompetitorsByColumnName = new HashMap<String, Set<CompetitorDTO>>();
        Map<Pair<String, Integer>, Set<CompetitorDTO>> unchangedLegDetailsForCompetitorsByColumnNameAndLegDetailsIndex = new HashMap<Util.Pair<String,Integer>, Set<CompetitorDTO>>();
        for (Map.Entry<CompetitorDTO, LeaderboardRowDTO> competitorAndRow : rows.entrySet()) {
            final int competitorIndexInPrevious = previousVersion.competitors.indexOf(competitorAndRow.getKey());
            LeaderboardRowDTO previousRowDTO = previousVersion.rows.get(competitorAndRow.getKey());
            LeaderboardRowDTO newRowDTO = new LeaderboardRowDTO();
            cloner.clone(competitorAndRow.getValue(), newRowDTO);
            newRows.put(competitorAndRow.getKey(), newRowDTO);
            HashMap<String, LeaderboardEntryDTO> newFieldsByRaceColumnName = new HashMap<String, LeaderboardEntryDTO>();
            for (Map.Entry<String, LeaderboardEntryDTO> raceColumnNameAndLeaderboardEntry : newRowDTO.fieldsByRaceColumnName.entrySet()) {
                LeaderboardEntryDTO previousEntryDTO = null;
                if (previousRowDTO != null) {
                    previousEntryDTO = previousRowDTO.fieldsByRaceColumnName.get(raceColumnNameAndLeaderboardEntry.getKey());
                }
                if (previousEntryDTO != null && Util.equalsWithNull(raceColumnNameAndLeaderboardEntry.getValue(), previousEntryDTO)) {
                    Set<CompetitorDTO> competitorsForWhichEntryForColumnIsUnchanged = unchangedLeaderboardEntriesForCompetitorsByColumnName.get(raceColumnNameAndLeaderboardEntry.getKey());
                    if (competitorsForWhichEntryForColumnIsUnchanged == null) {
                        competitorsForWhichEntryForColumnIsUnchanged = new HashSet<CompetitorDTO>();
                        unchangedLeaderboardEntriesForCompetitorsByColumnName.put(raceColumnNameAndLeaderboardEntry.getKey(), competitorsForWhichEntryForColumnIsUnchanged);
                    }
                    competitorsForWhichEntryForColumnIsUnchanged.add(competitorAndRow.getKey());
                    if (competitorsForWhichEntryForColumnIsUnchanged.equals(rows.keySet())) {
                        // entries for current column unchanged for all competitors; replace the many individual entries by a global one with null as competitor index
                        for (Iterator<Pair<Integer, String>> iter=unchangedLeaderboardEntryDTOsByCompetitorIndexInPreviousLeaderboardAndRaceColumnName.iterator();
                                iter.hasNext(); ) {
                            Pair<Integer, String> entry = iter.next();
                            if (entry.getB().equals(raceColumnNameAndLeaderboardEntry.getKey())) {
                                iter.remove();
                            }
                        }
                        unchangedLeaderboardEntryDTOsByCompetitorIndexInPreviousLeaderboardAndRaceColumnName.add(new Pair<Integer, String>(null, raceColumnNameAndLeaderboardEntry.getKey()));
                    } else {
                        // add a single entry for the competitor/column where the leaderboard entry remained unchanged
                        if (unchangedLeaderboardEntryDTOsByCompetitorIndexInPreviousLeaderboardAndRaceColumnName == null) {
                            unchangedLeaderboardEntryDTOsByCompetitorIndexInPreviousLeaderboardAndRaceColumnName = new HashSet<Util.Pair<Integer, String>>();
                        }
                        unchangedLeaderboardEntryDTOsByCompetitorIndexInPreviousLeaderboardAndRaceColumnName
                                .add(new Pair<Integer, String>(competitorIndexInPrevious,
                                        raceColumnNameAndLeaderboardEntry.getKey()));
                    }
                } else {
                    LeaderboardEntryDTO newLeaderboardEntryDTO = new LeaderboardEntryDTO();
                    cloner.clone(raceColumnNameAndLeaderboardEntry.getValue(), newLeaderboardEntryDTO);
                    newFieldsByRaceColumnName.put(raceColumnNameAndLeaderboardEntry.getKey(), newLeaderboardEntryDTO);
                    for (int legDetailsIndex=0; legDetailsIndex<newLeaderboardEntryDTO.legDetails.size(); legDetailsIndex++) {
                        LegEntryDTO legDetails = newLeaderboardEntryDTO.legDetails.get(legDetailsIndex);
                        if (previousEntryDTO != null && previousEntryDTO.legDetails != null && Util.equalsWithNull(legDetails, previousEntryDTO.legDetails.get(legDetailsIndex))) {
                            if (legDetailsUnchanged == null) {
                                legDetailsUnchanged = new HashSet<Triple<Integer, String, Integer>>();
                            }
                            final Pair<String, Integer> key = new Pair<String, Integer>(raceColumnNameAndLeaderboardEntry.getKey(), legDetailsIndex);
                            Set<CompetitorDTO> legDetailsUnchangedForCompetitors = unchangedLegDetailsForCompetitorsByColumnNameAndLegDetailsIndex.get(key);
                            legDetailsUnchanged.add(new Triple<Integer, String, Integer>(competitorIndexInPrevious, raceColumnNameAndLeaderboardEntry.getKey(), legDetailsIndex));
                            if (legDetailsUnchangedForCompetitors == null) {
                                legDetailsUnchangedForCompetitors = new HashSet<CompetitorDTO>();
                                unchangedLegDetailsForCompetitorsByColumnNameAndLegDetailsIndex.put(key, legDetailsUnchangedForCompetitors);
                            }
                            legDetailsUnchangedForCompetitors.add(competitorAndRow.getKey());
                            if (legDetailsUnchangedForCompetitors.equals(rows.keySet())) {
                                // leg details for the current leg unchanged for all competitors; replace individual entries by one entry with null as the competitor index
                                for (Iterator<Triple<Integer, String, Integer>> legDetailsUnchangedIter=legDetailsUnchanged.iterator(); legDetailsUnchangedIter.hasNext(); ) {
                                    Triple<Integer, String, Integer> triple = legDetailsUnchangedIter.next();
                                    if (triple.getB().equals(raceColumnNameAndLeaderboardEntry.getKey()) && triple.getC() == legDetailsIndex) {
                                        legDetailsUnchangedIter.remove();
                                    }
                                }
                                legDetailsUnchanged.add(new Triple<Integer, String, Integer>(null, raceColumnNameAndLeaderboardEntry.getKey(), legDetailsIndex));
                            }
                            newLeaderboardEntryDTO.legDetails.set(legDetailsIndex, null);
                        }
                    }
                }
            }
            newRowDTO.fieldsByRaceColumnName = newFieldsByRaceColumnName;
        }
        rows = newRows;
        return this;
    }
    
}
