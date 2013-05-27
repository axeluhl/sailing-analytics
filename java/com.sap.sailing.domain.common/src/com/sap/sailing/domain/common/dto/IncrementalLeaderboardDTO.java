package com.sap.sailing.domain.common.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sailing.domain.common.Cloner;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;

/**
 * 
 */
public class IncrementalLeaderboardDTO extends LeaderboardDTO implements IncrementalOrFullLeaderboardDTO {
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
     * holds a valid object which describes for which competitors in which race columns their leaderboard entry is
     * unchanged as compared to the previous leaderboard. The representation chosen is pretty compact. It uses a bit set
     * that encodes the competitors relative to their position in the previous leaderboard version or uses <code>null</code>
     * for the entire bit set if the leaderboard entry is unchanged for all competitors in a column.
     */
    private UnchangedLeaderboardEntries unchangedLeaderboardEntries;

    private Set<String> raceColumnNamesForWhichCompetitorOrderingPerRaceUnchanged;
    
    private transient Cloner cloner;
    
    /**
     * If a {@link LegEntryDTO} is unchanged in an otherwise modified {@link LeaderboardEntryDTO}, the triple of
     * competitor index in the previous leaderboard's competitors list, the name of the race column, and the index of
     * the {@link LegEntryDTO} in the {@link LeaderboardEntryDTO#legDetails} list is recorded here.
     * <p>
     * 
     * Note that this field can be <code>null</code>, meaning that there are no such leg details.
     */
    private Set<Triple<Integer, String, Integer>> legDetailsUnchanged;
    
    static class UnchangedLegDetails implements Serializable {
        private static final long serialVersionUID = 4141249996386599220L;
        
    }
    
    static class UnchangedLeaderboardEntries implements Serializable {
        private static final long serialVersionUID = -8855577157811163915L;
        private Map<String, long[]> unchangedLeaderboardEntriesByRaceColumnNameAsBitSetOverCompetitorNumbersInPreviousVersion;
        private transient LeaderboardDTO previousLeaderboard;
        private transient int totalNumberOfCompetitorsInNewLeaderboard;
        
        UnchangedLeaderboardEntries() {} // for serialization/deserialization
        
        public UnchangedLeaderboardEntries(LeaderboardDTO previousLeaderboard, int totalNumberOfCompetitorsInNewLeaderboard) {
            this.previousLeaderboard = previousLeaderboard;
            this.totalNumberOfCompetitorsInNewLeaderboard = totalNumberOfCompetitorsInNewLeaderboard;
            unchangedLeaderboardEntriesByRaceColumnNameAsBitSetOverCompetitorNumbersInPreviousVersion = new HashMap<String, long[]>();
        }
        
        /**
         * @param previousVersion needed because after serialization/deserialization, the transient {@link #previousLeaderboard} will be <code>null</code>
         */
        public Set<Pair<CompetitorDTO, String>> getAllUnchangedCompetitorAndRaceColumnPairs(LeaderboardDTO previousVersion) {
            Set<Pair<CompetitorDTO, String>> result = new HashSet<Util.Pair<CompetitorDTO,String>>();
            for (Map.Entry<String, long[]> raceColumnNameAndBitSet : unchangedLeaderboardEntriesByRaceColumnNameAsBitSetOverCompetitorNumbersInPreviousVersion.entrySet()) {
                final long[] bitset = raceColumnNameAndBitSet.getValue();
                if (bitset == null) {
                    // this means that all entries for the column are unchanged for all competitors
                    for (CompetitorDTO competitor : previousVersion.competitors) {
                        result.add(new Pair<CompetitorDTO, String>(competitor, raceColumnNameAndBitSet.getKey()));
                    }
                } else {
                    int competitorNumber = 0;
                    for (int arrayIndex = 0; arrayIndex < bitset.length; arrayIndex++) {
                        long bitValue = 1;
                        for (int bit=0; bit<Long.SIZE; bit++) {
                            if ((bitset[arrayIndex] & bitValue) != 0) {
                                result.add(new Pair<CompetitorDTO, String>(previousVersion.competitors.get(competitorNumber), raceColumnNameAndBitSet.getKey()));
                            }
                            bitValue <<= 1;
                            competitorNumber++;
                        }
                    }
                }
            }
            return result;
        }

        /**
         * Declares that the leaderboard entry for the given <code>competitor</code> in the race column named <code>raceColumnName</code>
         * is unchanged when compared to the previous version.<p>
         * 
         * If this means that the entries are unchanged for <em>all</em> competitors in that column, the internal representation is
         * compacted for less bandwidth-consuming serialization.
         */
        public void unchanged(CompetitorDTO competitor, String raceColumnName) {
            long[] bitset = unchangedLeaderboardEntriesByRaceColumnNameAsBitSetOverCompetitorNumbersInPreviousVersion.get(raceColumnName);
            if (bitset == null) {
                bitset = createBitSet(competitor);
                unchangedLeaderboardEntriesByRaceColumnNameAsBitSetOverCompetitorNumbersInPreviousVersion.put(raceColumnName, bitset);
            } else {
                final int indexOfCompetitor = previousLeaderboard.competitors.indexOf(competitor);
                if (bitset.length < indexOfCompetitor/Long.SIZE) {
                    // bitset array is too short; extend
                    long[] newBitset = new long[1+indexOfCompetitor/Long.SIZE];
                    System.arraycopy(bitset, 0, newBitset, 0, bitset.length);
                    bitset = newBitset;
                    unchangedLeaderboardEntriesByRaceColumnNameAsBitSetOverCompetitorNumbersInPreviousVersion.put(raceColumnName, bitset);
                }
                bitset[indexOfCompetitor/Long.SIZE] |= 1l << (indexOfCompetitor%Long.SIZE);
            }
            if (getNumberOfSetBits(bitset) >= totalNumberOfCompetitorsInNewLeaderboard) {
                // unchanged for all; set bitset to null in map, representing this fact
                unchangedLeaderboardEntriesByRaceColumnNameAsBitSetOverCompetitorNumbersInPreviousVersion.put(raceColumnName, null);
            }
        }
        
        private int getNumberOfSetBits(long[] bitset) {
            int result = 0;
            for (int i=0; i<bitset.length; i++) {
                for (int j=0; j<Long.SIZE; j++) {
                    if ((bitset[i] & 1l << j) != 0) {
                        result++;
                    }
                }
            }
            return result;
        }

        /**
         * Creates a bit set that is large enough to hold the bit for the index of <code>competitor</code> in the previous leaderboard's competitors
         * list and sets the bit for that competitor.
         */
        private long[] createBitSet(CompetitorDTO competitor) {
            final int indexOfCompetitor = previousLeaderboard.competitors.indexOf(competitor);
            long[] result = new long[1+indexOfCompetitor/Long.SIZE];
            result[indexOfCompetitor/Long.SIZE] = 1l << (indexOfCompetitor%Long.SIZE);
            return result;
        }
    }
    
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
            if (unchangedLeaderboardEntries != null) {
                for (Pair<CompetitorDTO, String> competitorAndColumnName : unchangedLeaderboardEntries
                        .getAllUnchangedCompetitorAndRaceColumnPairs(previousVersion)) {
                    CompetitorDTO previousCompetitor = competitorAndColumnName.getA();
                    LeaderboardEntryDTO previousEntry = previousVersion.rows.get(previousCompetitor).fieldsByRaceColumnName.get(competitorAndColumnName.getB());
                    rows.get(previousCompetitor).fieldsByRaceColumnName.put(competitorAndColumnName.getB(), previousEntry);
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
        // key is either column name + leg number, or column name + null if entry is unchanged for all legs in that race column;
        // values are the sets of competitors for which the leg details identified by the key are unchanged
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
                    if (unchangedLeaderboardEntries == null) {
                        unchangedLeaderboardEntries = new UnchangedLeaderboardEntries(previousVersion, rows.size());
                    }
                    unchangedLeaderboardEntries.unchanged(competitorAndRow.getKey(), raceColumnNameAndLeaderboardEntry.getKey());
                } else {
                    LeaderboardEntryDTO newLeaderboardEntryDTO = new LeaderboardEntryDTO();
                    cloner.clone(raceColumnNameAndLeaderboardEntry.getValue(), newLeaderboardEntryDTO);
                    newFieldsByRaceColumnName.put(raceColumnNameAndLeaderboardEntry.getKey(), newLeaderboardEntryDTO);
                    final Pair<String, Integer> keyForAllLegsUnchanged = new Pair<String, Integer>(raceColumnNameAndLeaderboardEntry.getKey(), null);
                    if (newLeaderboardEntryDTO.legDetails != null) {
                        for (int legDetailsIndex = 0; legDetailsIndex < newLeaderboardEntryDTO.legDetails.size(); legDetailsIndex++) {
                            LegEntryDTO legDetails = newLeaderboardEntryDTO.legDetails.get(legDetailsIndex);
                            if (previousEntryDTO != null
                                    && previousEntryDTO.legDetails != null
                                    && Util.equalsWithNull(legDetails, previousEntryDTO.legDetails.get(legDetailsIndex))) {
                                if (legDetailsUnchanged == null) {
                                    legDetailsUnchanged = new HashSet<Triple<Integer, String, Integer>>();
                                }
                                legDetailsUnchanged.add(new Triple<Integer, String, Integer>(competitorIndexInPrevious,
                                        raceColumnNameAndLeaderboardEntry.getKey(), legDetailsIndex));
                                final Pair<String, Integer> key = new Pair<String, Integer>(raceColumnNameAndLeaderboardEntry.getKey(), legDetailsIndex);
                                Set<CompetitorDTO> legDetailsUnchangedForCompetitors = unchangedLegDetailsForCompetitorsByColumnNameAndLegDetailsIndex
                                        .get(key);
                                if (legDetailsUnchangedForCompetitors == null) {
                                    legDetailsUnchangedForCompetitors = new HashSet<CompetitorDTO>();
                                    unchangedLegDetailsForCompetitorsByColumnNameAndLegDetailsIndex.put(key,
                                            legDetailsUnchangedForCompetitors);
                                }
                                legDetailsUnchangedForCompetitors.add(competitorAndRow.getKey());
                                // include all competitors for which in the current race column the leg details for *all* legs are unchanged:
                                Set<CompetitorDTO> competitorsWithCurrentLegUnchangedAndCompetitorsWithAllLegsUnchanged = new HashSet<CompetitorDTO>(
                                        legDetailsUnchangedForCompetitors);
                                Set<CompetitorDTO> competitorsWithAllLegsUnchanged = unchangedLegDetailsForCompetitorsByColumnNameAndLegDetailsIndex.
                                        get(keyForAllLegsUnchanged);
                                if (competitorsWithAllLegsUnchanged != null) {
                                    competitorsWithCurrentLegUnchangedAndCompetitorsWithAllLegsUnchanged.addAll(competitorsWithAllLegsUnchanged);
                                }
                                if (competitorsWithCurrentLegUnchangedAndCompetitorsWithAllLegsUnchanged.equals(rows.keySet())) {
                                    // leg details for the current leg unchanged for all competitors; replace individual
                                    // entries by one entry with null as the competitor index to save space in legDetailsUnchanged
                                    compactLegDetailsUnchanged(raceColumnNameAndLeaderboardEntry.getKey(), legDetailsIndex);
                                }
                                newLeaderboardEntryDTO.legDetails.set(legDetailsIndex, null);
                            }
                        }
                    } else {
                        if (previousEntryDTO.legDetails == null) {
                            // legDetails are null for both, previous and current version; leave as is in current version, but mark
                            // as unchanged in unchangedLegDetailsForCompetitorsByColumnNameAndLegDetailsIndex and check for the possibility
                            // to compact legDetailsUnchanged if the leg details are unchanged for an entire column
                            Set<CompetitorDTO> competitorsWithAllLegsUnchanged = unchangedLegDetailsForCompetitorsByColumnNameAndLegDetailsIndex.
                                    get(keyForAllLegsUnchanged);
                            if (competitorsWithAllLegsUnchanged == null) {
                                competitorsWithAllLegsUnchanged = new HashSet<CompetitorDTO>();
                                unchangedLegDetailsForCompetitorsByColumnNameAndLegDetailsIndex.put(keyForAllLegsUnchanged, competitorsWithAllLegsUnchanged);
                            }
                            competitorsWithAllLegsUnchanged.add(competitorAndRow.getKey());
                            if (competitorsWithAllLegsUnchanged.equals(rows.keySet())) {
                                // no competitor has a change for any leg detail in this column:
                                // TODO compact all legs in this column
                            }
                            String currentRaceColumnName = raceColumnNameAndLeaderboardEntry.getKey();
                            for (Entry<Pair<String, Integer>, Set<CompetitorDTO>> e : unchangedLegDetailsForCompetitorsByColumnNameAndLegDetailsIndex.entrySet()) {
                                if (/* same race column */ raceColumnNameAndLeaderboardEntry.getKey().equals(e.getKey().getA()) &&
                                        e.getKey().getB() != null) {
                                    // no "all leg details" entry; combine with the "all let details" entries, then check if this spans all competitors
                                    Set<CompetitorDTO> competitorsWithUnchangedLegDetailsForCurrentColumnAndOneLegPlusThoseUnchangedForAllLegs =
                                            new HashSet<CompetitorDTO>(e.getValue());
                                    Set<CompetitorDTO> allLegDetailsUnchanged = unchangedLegDetailsForCompetitorsByColumnNameAndLegDetailsIndex.get(
                                            new Pair<String, Integer>(currentRaceColumnName, null));
                                    if (allLegDetailsUnchanged != null) {
                                        competitorsWithUnchangedLegDetailsForCurrentColumnAndOneLegPlusThoseUnchangedForAllLegs.addAll(allLegDetailsUnchanged);
                                    }
                                    if (competitorsWithUnchangedLegDetailsForCurrentColumnAndOneLegPlusThoseUnchangedForAllLegs.equals(rows.keySet())) {
                                        compactLegDetailsUnchanged(raceColumnNameAndLeaderboardEntry.getKey(), e.getKey().getB());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            newRowDTO.fieldsByRaceColumnName = newFieldsByRaceColumnName;
        }
        rows = newRows;
        return this;
    }

    /**
     * leg details for the current leg unchanged for all competitors; replace individual entries by one entry with null
     * as the competitor index to save space in legDetailsUnchanged
     */
    private void compactLegDetailsUnchanged(String raceColumnName, int legDetailsIndex) {
        for (Iterator<Triple<Integer, String, Integer>> legDetailsUnchangedIter = legDetailsUnchanged
                .iterator(); legDetailsUnchangedIter.hasNext();) {
            Triple<Integer, String, Integer> triple = legDetailsUnchangedIter.next();
            if (triple.getB().equals(raceColumnName) && triple.getC() == legDetailsIndex) {
                legDetailsUnchangedIter.remove();
            }
        }
        legDetailsUnchanged.add(new Triple<Integer, String, Integer>(null, raceColumnName, legDetailsIndex));
    }
    
}
