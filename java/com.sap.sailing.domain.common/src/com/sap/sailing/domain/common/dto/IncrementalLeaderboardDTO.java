package com.sap.sailing.domain.common.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sse.common.Cloner;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

/**
 * Reduces the transmission size of a {@link LeaderboardDTO} by comparing this object to a previous version of
 * this object. For memory-consuming elements that equal the corresponding elements in the previous version,
 * only a marker about the equality is remembered, and the space-consuming structure is replaced by a <code>null</code>
 * value. On the receiving end where the previous version is also present, those <code>null</code> values can then be
 * replaced by the previous value.<p>
 * 
 * The "compression" is performed by the {@link #strip(LeaderboardDTO)} method on the sending end, and the
 * "de-compression" happens in the {@link #applyThisToPreviousVersionByUpdatingThis(LeaderboardDTO)} method
 * on the receiving end.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class IncrementalLeaderboardDTO extends LeaderboardDTO implements IncrementalOrFullLeaderboardDTO {
    private static final long serialVersionUID = -7011986430671280594L;
    private String isDiffToLeaderboardDTOWithId;
    
    private String updatedFromPreviousVersionWithId;

    private boolean commentUnchanged;
    
    /**
     * If <code>null</code>, {@link #competitors} is filled and must not be touched; otherwise, the size of the array tells the new size
     * of {@link #competitors}, and for each index it either tells the index of the {@link CompetitorWithBoatDTO} object to use in the previous
     * leaderboard's {@link #competitors} list, or holds -1, meaning that the next {@link CompetitorWithBoatDTO} object is to be used from
     * {@link #addedCompetitors} which only needs to hold a valid collection if at least one new competitor exists.
     */
    private int[] competitorIndexesInPreviousCompetitorsList;
    
    /**
     * The "uncompacted" original competitors that were added. There is no need to try to compact these because they haven't existed
     * in the previous leaderboard and therefore need to be transmitted in their entirety.
     */
    private List<CompetitorDTO> addedCompetitors;
    
    private boolean suppressedCompetitorsUnchanged;
    private boolean competitorDisplayNamesUnchanged;
    private boolean regattaNameUnchanged;
    private boolean displayNameUnchanged;
    private boolean defaultCourseAreaIdAsStringUnchanged;
    private boolean defaultCourseAreaNameUnchanged;
    private boolean boatClassUnchanged;
    
    /**
     * If a {@link LeaderboardEntryDTO} in {@link LeaderboardRowDTO#fieldsByRaceColumnName} is unchanged, this field
     * holds a valid object which describes for which competitors in which race columns their leaderboard entry is
     * unchanged as compared to the previous leaderboard. As key, the race column name is used. This field can be
     * <code>null</code>, meaning that there are no unchanged leaderboard entries.
     */
    private UnchangedWithCompetitorsInBitSet<String> unchangedLeaderboardEntries;

    private Set<String> raceColumnNamesForWhichCompetitorOrderingPerRaceUnchanged;
    
    /**
     * For each index in this object's {@link #getRaceList()}, either has <code>-1</code> meaning that the element at the respective index
     * is properly set because it has changed compared to the previous version, or it has a non-negative index pointing to the previous
     * leaderboard's {@link #getRaceList()} result which has an unchanged {@link RaceColumnDTO} at that index which is to be
     * set in this object's {@link #getRaceList()} during {@link #applyThisToPreviousVersionByUpdatingThis(LeaderboardDTO) expansion}.
     */
    private int[] indexOfUnchangedRaceColumnDTOsInPrevious;
    
    private transient Cloner cloner;
    
    /**
     * If a {@link LegEntryDTO} is unchanged in an otherwise modified {@link LeaderboardEntryDTO}, this is recorded in
     * this field. The key is the pair of race column name and the leg number, both starting with zero. If <code>null</code>
     * is used for the leg number, this means that for that competitor in that race, all leg details are unchanged as compared
     * to the previous version.
     * <p>
     * 
     * This field can be <code>null</code>, meaning that there are no unchanged leg details.
     */
    private UnchangedLegDetails legDetailsUnchanged;
    
    /**
     * The single key is <code>null</code> if the field is not <code>null</code>. The value identifies the competitors
     * for which the entire row is unchanged in {@link #rows}.
     */
    private UnchangedWithCompetitorsInBitSet<Void> rowsUnchanged;
    
    /**
     * Time stamp in "server time," updated to when this object was sent out, just before serialization
     */
    private Date currentServerTime;
    
    static class UnchangedWithCompetitorsInBitSet<K> implements Serializable {
        private static final long serialVersionUID = 504599408604780499L;
        private transient int totalNumberOfCompetitorsInNewLeaderboard;
        private transient LeaderboardDTO previousLeaderboard;
        
        /**
         * Values are bit sets where the bits encode competitors by their position in {@link #previousLeaderboard}'s
         * {@link LeaderboardDTO#competitors} list; if a competitor has its bit set, this means that its value corresponding
         * to the key of type <code>K</code> used as key to this map is unchanged between previous and current leaderboard
         * version. If the value is unchanged for <em>all</em> competitors for one key, <code>null</code> is used as the value
         * in this map instead of the bit set.
         */
        private Map<K, long[]> unchanged;
        
        UnchangedWithCompetitorsInBitSet() {} // for serialization/deserialization
        
        public UnchangedWithCompetitorsInBitSet(LeaderboardDTO previousLeaderboard, int totalNumberOfCompetitorsInNewLeaderboard) {
            this.previousLeaderboard = previousLeaderboard;
            this.totalNumberOfCompetitorsInNewLeaderboard = totalNumberOfCompetitorsInNewLeaderboard;
            unchanged = new HashMap<K, long[]>();
        }
        
        /**
         * ORs two bit sets into a new bit set; the bit sets may have different or zero lengths and can even be <code>null</code>
         */
        protected long[] or(long[] a, long[] b) {
            final int maxLength = Math.max(a==null?0:a.length, b==null?0:b.length);
            long[] result = new long[maxLength];
            for (int i=0; i<maxLength; i++) {
                if (a != null && a.length > i) {
                    result[i] |= a[i];
                }
                if (b != null && b.length > i) {
                    result[i] |= b[i];
                }
            }
            return result;
        }

        protected int getNumberOfSetBits(K key) {
            return getNumberOfBitsSet(getUnchangedBitset(key));
        }

        protected int getNumberOfBitsSet(long[] bitset) {
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

        private int getIndexOfCompetitor(CompetitorDTO competitor) {
            return previousLeaderboard.competitors.indexOf(competitor);
        }

        protected int getTotalNumberOfCompetitorsInNewLeaderboard() {
            return totalNumberOfCompetitorsInNewLeaderboard;
        }

        /**
         * @param previousVersion
         *            needed because after serialization/deserialization, the transient {@link #previousLeaderboard}
         *            will be <code>null</code>.
         * @return a map whose keys are the {@link CompetitorWithBoatDTO#getIdAsString()} of the competitors for which the value
         *         tells the set of keys that are unchanged
         */
        public Map<String, Set<K>> getAllUnchangedCompetitorIdsAsStringAndKeys(LeaderboardDTO previousVersion) {
            Map<String, Set<K>> result = new HashMap<>();
            for (Map.Entry<K, long[]> raceColumnNameAndBitSet : unchanged.entrySet()) {
                final long[] bitset = raceColumnNameAndBitSet.getValue();
                if (bitset == null) {
                    // this means that all entries for the column are unchanged for all competitors
                    for (CompetitorDTO competitor : previousVersion.competitors) {
                        Util.add(result, competitor.getIdAsString(), raceColumnNameAndBitSet.getKey());
                    }
                } else {
                    int competitorNumber = 0;
                    for (int arrayIndex = 0; arrayIndex < bitset.length; arrayIndex++) {
                        long bitValue = 1;
                        for (int bit=0; bit<Long.SIZE; bit++) {
                            if ((bitset[arrayIndex] & bitValue) != 0) {
                                Util.add(result, previousVersion.competitors.get(competitorNumber).getIdAsString(), raceColumnNameAndBitSet.getKey());
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
         * Declares that the value for the given <code>competitor</code> for key <code>key</code> is unchanged when
         * compared to the previous version.
         * <p>
         * 
         * If this means that the values are unchanged for <em>all</em> competitors for that key, the internal
         * representation is compacted for less bandwidth-consuming serialization.
         * 
         * @param competitor
         *            a competitor that already existed in the previous version of the leaderboard
         */
        public void unchanged(CompetitorDTO competitor, K key) {
            assert getIndexOfCompetitor(competitor) >= 0;
            long[] bitset = getUnchangedBitset(key);
            if (bitset == null) {
                bitset = createBitSet(competitor);
                unchanged.put(key, bitset);
            } else {
                final int indexOfCompetitor = getIndexOfCompetitor(competitor);
                if (bitset.length <= indexOfCompetitor/Long.SIZE) {
                    // bitset array is too short; extend
                    long[] newBitset = new long[1+indexOfCompetitor/Long.SIZE];
                    System.arraycopy(bitset, 0, newBitset, 0, bitset.length);
                    bitset = newBitset;
                    unchanged.put(key, bitset);
                }
                bitset[indexOfCompetitor/Long.SIZE] |= 1l << (indexOfCompetitor%Long.SIZE);
            }
            tryToCompact(key);
        }

        protected void tryToCompact(K key) {
            if (getNumberOfSetBits(key) >= getTotalNumberOfCompetitorsInNewLeaderboard()) {
                // unchanged for all; set bitset to null in map, representing this fact
                unchanged.put(key, null);
            }
        }
        
        protected long[] getUnchangedBitset(K key) {
            return unchanged.get(key);
        }
        
        protected Set<K> getAllUnchangedKeys() {
            return Collections.unmodifiableSet(unchanged.keySet());
        }
    }
    
    /**
     * A specialized, bit set-based structure for marking leg details that haven't changed. The key is a composite of
     * the race column name and the zero-based leg index. If <code>null</code> is used for the leg index in a call to
     * {@link #unchanged(CompetitorWithBoatDTO, Object)}, this means that all leg details in that race were unchanged for the
     * competitor, e.g., because they were <code>null</code> both in the old and new version.
     * 
     * @author Axel Uhl (d043530)
     * 
     */
    static class UnchangedLegDetails extends UnchangedWithCompetitorsInBitSet<com.sap.sse.common.Util.Pair<String, Integer>> {
        private static final long serialVersionUID = 8726138955651801210L;
        
        UnchangedLegDetails() {} // for (de)serialization
        
        public UnchangedLegDetails(LeaderboardDTO previousLeaderboard, int totalNumberOfCompetitorsInNewLeaderboard) {
            super(previousLeaderboard, totalNumberOfCompetitorsInNewLeaderboard);
        }
        
        /**
         * For all keys with non-<code>null</code> leg index, combine the competitor (bit)set with that for the
         * key with the same respective race column name and <code>null</code> as the leg index. The latter competitors
         * are those for which the leg details are both <code>null</code> in the old and the new version, and therefore
         * unchanged. If for any of the keys with non-<code>null</code> leg index this leads to a competitor (bit)set that
         * has at least {@link #getTotalNumberOfCompetitorsInNewLeaderboard()} entries, then this bit set can be replaced
         * by <code>null</code>, meaning the race column / leg details are unchnaged for all competitors. 
         */
        public void compact() {
            for (com.sap.sse.common.Util.Pair<String, Integer> key : getAllUnchangedKeys()) {
                if (key.getB() != null) {
                    tryToCompact(key);
                }
            }
        }
        
        /**
         * If the key's leg index is not <code>null</code>, then the bits of the entry with the leg index replaced by <code>null</code>
         * is OR-ed to the bit set found for <code>key</code>, computing the competitors for which the entry at <code>key</code> is unchanged
         * or for which all entries in the race column are unchanged.
         */
        @Override
        protected int getNumberOfSetBits(com.sap.sse.common.Util.Pair<String, Integer> key) {
            int result;
            if (key.getB() != null) {
                long[] bitset = getUnchangedBitset(key);
                long[] nullBitset = getUnchangedBitset(new com.sap.sse.common.Util.Pair<String, Integer>(key.getA(), null));
                long[] orBitset = or(bitset, nullBitset);
                result = getNumberOfBitsSet(orBitset);
            } else {
                // the key asks "which competitors have their leg details unchanged for the race named key.getA() and all legs within with legDetails==null?
                result = super.getNumberOfSetBits(key);
            }
            return result;
        }
    }
    
    @Deprecated
    IncrementalLeaderboardDTO() {} // for GWT serialization only

    public IncrementalLeaderboardDTO(String id, Cloner cloner) {
        super(id, /* boatClass */ null);
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
        // As a performance improvement for the GWT-compiled version of this class, try to avoid key types
        // other than strings when there is a map or set to be implemented that has a lot of strain on the
        // put / add / contains methods. For this, we use the competitor's getIdAsString() result that
        // identifies them reasonably well.
        final Map<String, CompetitorDTO> competitorsOfPreviousVersionByIdAsString = new HashMap<>();
        for (final CompetitorDTO c : previousVersion.competitors) {
            competitorsOfPreviousVersionByIdAsString.put(c.getIdAsString(), c);
        }
        if (this.updatedFromPreviousVersionWithId != null) {
            if (!this.updatedFromPreviousVersionWithId.equals(previousVersion.getId())) {
                throw new IllegalStateException("This incremental leaderboard DTO was already applied to a different previous version. It cannot be applied multiple times.");
            } else {
                // the previous version remains unchanged; no need to make any changes, no need to throw an exception
            }
        } else {
            if (!isDiffToLeaderboardDTOWithId.equals(previousVersion.getId())) {
                throw new IllegalArgumentException("Trying to apply leaderboard DTO diff to leaderboard DTO with ID "+previousVersion.getId()+
                        " although the diff was meant to be applied to a leaderboard DTO with ID "+isDiffToLeaderboardDTOWithId);
            }
            this.updatedFromPreviousVersionWithId = previousVersion.getId();
            if (this.commentUnchanged) {
                this.setComment(previousVersion.getComment());
            }
            if (this.defaultCourseAreaIdAsStringUnchanged) {
                this.defaultCourseAreaId = previousVersion.defaultCourseAreaId;
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
            if (this.boatClassUnchanged) {
                this.setBoatClass(previousVersion.getBoatClass());
            }
            if (this.competitorIndexesInPreviousCompetitorsList != null) {
                this.competitors = new ArrayList<>(competitorIndexesInPreviousCompetitorsList.length);
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
            List<CompetitorDTO> suppressedCompetitors = new ArrayList<>();
            if (suppressedCompetitorsUnchanged) {
                Util.addAll(previousVersion.getSuppressedCompetitors(), suppressedCompetitors);
            } else {
                for (CompetitorDTO compactSuppressedCompetitor : getSuppressedCompetitors()) {
                    suppressedCompetitors.add(compactSuppressedCompetitor.getCompetitorFromPrevious(previousVersion));
                }
            }
            setSuppressedCompetitors(suppressedCompetitors);
            if (competitorDisplayNamesUnchanged) {
                competitorDisplayNames = previousVersion.competitorDisplayNames;
            } else {
                Map<CompetitorDTO, String> expandedCompetitorDisplayNames = new HashMap<>();
                for (Map.Entry<CompetitorDTO, String> e : competitorDisplayNames.entrySet()) {
                    expandedCompetitorDisplayNames.put(e.getKey().getCompetitorFromPrevious(previousVersion), e.getValue());
                }
                competitorDisplayNames = expandedCompetitorDisplayNames;
            }
            if (indexOfUnchangedRaceColumnDTOsInPrevious != null) {
                for (int i=0; i<indexOfUnchangedRaceColumnDTOsInPrevious.length; i++) {
                    if (indexOfUnchangedRaceColumnDTOsInPrevious[i] != -1) {
                        getRaceList().set(i, previousVersion.getRaceList().get(indexOfUnchangedRaceColumnDTOsInPrevious[i]));
                    }
                }
            }
            Set<String> columnNamesForWhichToExpandCompetitorOrderingPerRace = new HashSet<String>(getCompetitorOrderingPerRaceColumnName().keySet());
            for (String raceColumnNameForWhichCompetitorOrderingPerRaceUnchanged : raceColumnNamesForWhichCompetitorOrderingPerRaceUnchanged) {
                // be on the safe side regarding the equals/hashCode implementation of RaceColumnDTO and look it up by name for old and new version
                setCompetitorsFromBestToWorst(raceColumnNameForWhichCompetitorOrderingPerRaceUnchanged, previousVersion.getCompetitorsFromBestToWorst(raceColumnNameForWhichCompetitorOrderingPerRaceUnchanged));
                columnNamesForWhichToExpandCompetitorOrderingPerRace.remove(raceColumnNameForWhichCompetitorOrderingPerRaceUnchanged);
            }
            // expand all other ordered competitor lists for all races where it changed 
            for (String columnNameForWhichToExpandCompetitorOrderingPerRace : columnNamesForWhichToExpandCompetitorOrderingPerRace) {
                List<CompetitorDTO> competitorsFromBestToWorstForRace = getCompetitorsFromBestToWorst(columnNameForWhichToExpandCompetitorOrderingPerRace);
                for (int i=competitorsFromBestToWorstForRace.size()-1; i>=0; i--) {
                    competitorsFromBestToWorstForRace.set(i, competitorsFromBestToWorstForRace.get(i).getCompetitorFromPrevious(previousVersion));
                }
            }
            if (rows == null) {
                rows = new HashMap<>();
            }
            // expand all keys and remove values from compact keys and re-enter with expanded keys; expand their competitor field
            for (CompetitorDTO compactCompetitor : new ArrayList<>(rows.keySet())) {
                final CompetitorDTO expandedCompetitor = compactCompetitor.getCompetitorFromPrevious(previousVersion);
                final LeaderboardRowDTO row = rows.remove(compactCompetitor);
                row.competitor = expandedCompetitor;
                rows.put(expandedCompetitor, row);
            }
            final Set<String> rowsUnchangedForCompetitorsWithIdAsString = new HashSet<>();
            if (rowsUnchanged != null) {
                for (Entry<String, Set<Void>> rowUnchanged : rowsUnchanged.getAllUnchangedCompetitorIdsAsStringAndKeys(previousVersion).entrySet()) {
                    rowsUnchangedForCompetitorsWithIdAsString.add(rowUnchanged.getKey());
                    final CompetitorDTO previousCompetitor = competitorsOfPreviousVersionByIdAsString.get(rowUnchanged.getKey());
                    rows.put(previousCompetitor, previousVersion.rows.get(previousCompetitor));
                }
            }
            final Map<String, Set<String>> allUnchangedLeaderboardEntriesAsCompetitorIdsAsStringAndColumnNames =
                    unchangedLeaderboardEntries == null ? new HashMap<String, Set<String>>() :
                        unchangedLeaderboardEntries.getAllUnchangedCompetitorIdsAsStringAndKeys(previousVersion);
            if (unchangedLeaderboardEntries != null) {
                for (Entry<String, Set<String>> competitorAndColumnNames : allUnchangedLeaderboardEntriesAsCompetitorIdsAsStringAndColumnNames.entrySet()) {
                    CompetitorDTO previousCompetitor = competitorsOfPreviousVersionByIdAsString.get(competitorAndColumnNames.getKey());
                    for (final String columnName : competitorAndColumnNames.getValue()) {
                        if (!rowsUnchangedForCompetitorsWithIdAsString.contains(competitorAndColumnNames.getKey())) { // only care if not the entire row was marked unchanged
                            LeaderboardEntryDTO previousEntry = previousVersion.rows.get(previousCompetitor).fieldsByRaceColumnName.get(columnName);
                            final LeaderboardRowDTO row = rows.get(previousCompetitor);
                            if (row != null) { // may be null if the competitor disappeared, e.g., because of suppression
                                row.fieldsByRaceColumnName.put(columnName, previousEntry);
                            }
                        }
                    }
                }
            }
            if (legDetailsUnchanged != null) {
                for (Entry<String, Set<Pair<String, Integer>>> competitorInPreviosAndColumnNameAndLegDetailsIndex :
                        legDetailsUnchanged.getAllUnchangedCompetitorIdsAsStringAndKeys(previousVersion).entrySet()) {
                    CompetitorDTO previousCompetitor = competitorsOfPreviousVersionByIdAsString.get(competitorInPreviosAndColumnNameAndLegDetailsIndex.getKey());
                    for (final Pair<String, Integer> columnNameAndLegDetailsIndex : competitorInPreviosAndColumnNameAndLegDetailsIndex.getValue()) {
                        final String raceColumnName = columnNameAndLegDetailsIndex.getA();
                        // if it's already clear that the entire LeaderboardEntryDTO was unchanged, the entry in legDetailsUnchanged was
                        // merely a hint for column compression within legDetailsUnchanged and doesn't need to be handled here
                        final Set<String> unchangedRaceColumnNamesForCompetitor;
                        if ((unchangedRaceColumnNamesForCompetitor=allUnchangedLeaderboardEntriesAsCompetitorIdsAsStringAndColumnNames.get(competitorInPreviosAndColumnNameAndLegDetailsIndex.getKey())) == null
                         || !unchangedRaceColumnNamesForCompetitor.contains(raceColumnName)) {
                            final LeaderboardRowDTO leaderboardRowDTO = rows.get(previousCompetitor);
                            LeaderboardEntryDTO leaderboardEntry = leaderboardRowDTO == null ? null
                                    : leaderboardRowDTO.fieldsByRaceColumnName.get(raceColumnName);
                            if (leaderboardEntry != null) {
                                final LeaderboardRowDTO previousLeaderboardRowDTO = previousVersion.rows.get(previousCompetitor);
                                final LeaderboardEntryDTO previousLeaderboardEntryDTO = previousLeaderboardRowDTO == null ? null
                                        : previousLeaderboardRowDTO.fieldsByRaceColumnName.get(raceColumnName);
                                final List<LegEntryDTO> previousLegDetails = previousLeaderboardEntryDTO == null ? null
                                        : previousLeaderboardEntryDTO.legDetails;
                                if (previousLegDetails == null) {
                                    // the leg index can only be null if the previous leg details are null
                                    leaderboardEntry.legDetails = null;
                                } else {
                                    // here, the leg index cannot be null
                                    if (leaderboardEntry.legDetails == null) {
                                        leaderboardEntry.legDetails = new ArrayList<LegEntryDTO>();
                                    }
                                    final Integer pos = columnNameAndLegDetailsIndex.getB();
                                    ensureSize(leaderboardEntry.legDetails, pos + 1);
                                    leaderboardEntry.legDetails.set(pos, previousLegDetails.get(pos));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void ensureSize(List<?> list, int size) {
        while (list.size() < size) {
            list.add(null);
        }
    }

    /**
     * TODO handle exceptions in here; log them very prominently; then continue in a fail-safe way as this is very central to leaderboard delivery
     * 
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
        if (Util.equalsWithNull(this.defaultCourseAreaId, previousVersion.defaultCourseAreaId)) {
            this.defaultCourseAreaId = null;
            this.defaultCourseAreaIdAsStringUnchanged = true;
        }
        if (Util.equalsWithNull(this.defaultCourseAreaName, previousVersion.defaultCourseAreaName)) {
            this.defaultCourseAreaName = null;
            this.defaultCourseAreaNameUnchanged = true;
        }
        if (Util.equalsWithNull(this.getBoatClass(), previousVersion.getBoatClass())) {
            this.setBoatClass(null);
            this.boatClassUnchanged = true;
        }
        competitorIndexesInPreviousCompetitorsList = new int[competitors.size()];
        // for this stripping run, remembers the mapping of real CompetitorDTO objects to the compact form that only holds an int as reference to the
        // previous version's competitors list; those will be used to replace the real CompetitorDTO objects where possible and will be replaced the other
        // way in applyThisToPreviousVersionByUpdatingThis(...).
        Map<CompetitorDTO, CompetitorDTO> compactCompetitorMap = new HashMap<>();
        int i=0;
        for (CompetitorDTO competitor : competitors) {
            int indexInPrevious = previousVersion.competitors.indexOf(competitor);
            competitorIndexesInPreviousCompetitorsList[i++] = indexInPrevious;
            if (indexInPrevious == -1) {
                if (addedCompetitors == null) {
                    addedCompetitors = new ArrayList<>();
                }
                addedCompetitors.add(competitor);
                compactCompetitorMap.put(competitor, competitor);
            } else {
                CompetitorDTO compactReplacementCompetitor = competitor.hasBoat() ?
                        new PreviousCompetitorWithBoatDTOImpl(indexInPrevious) : new PreviousCompetitorDTOImpl(indexInPrevious);
                compactCompetitorMap.put(competitor, compactReplacementCompetitor);
            }
        }
        competitors = null;
        if (Util.equalsWithNull(getSuppressedCompetitors(), previousVersion.getSuppressedCompetitors())) {
            suppressedCompetitorsUnchanged = true;
            setSuppressedCompetitors(null);
        } else {
            List<CompetitorDTO> compactSuppressedCompetitors = new ArrayList<>();
            for (CompetitorDTO suppressedCompetitor : getSuppressedCompetitors()) {
                final int indexOfSuppressedCompetitorInPreviousSuppressed = Util.indexOf(previousVersion.getSuppressedCompetitors(), suppressedCompetitor);
                if (indexOfSuppressedCompetitorInPreviousSuppressed == -1) {
                    // not found in previous version's suppressed competitors; maybe it just transitioned from non-suppressed to suppressed
                    final CompetitorDTO compactedFromPreviousCompetitors = compactCompetitorMap.get(suppressedCompetitor);
                    if (compactedFromPreviousCompetitors != null) {
                        compactSuppressedCompetitors.add(compactedFromPreviousCompetitors);
                    } else {
                        compactSuppressedCompetitors.add(suppressedCompetitor);
                    }
                } else {
                    compactSuppressedCompetitors.add(new PreviousSuppressedCompetitorDTOImpl(
                            indexOfSuppressedCompetitorInPreviousSuppressed));
                }
            }
            setSuppressedCompetitors(compactSuppressedCompetitors);
        }
        if (Util.equalsWithNull(competitorDisplayNames, previousVersion.competitorDisplayNames)) {
            competitorDisplayNamesUnchanged = true;
            competitorDisplayNames = null;
        } else {
            Map<CompetitorDTO, String> compactCompetitorDisplayNames = new HashMap<CompetitorDTO, String>();
            for (Map.Entry<CompetitorDTO, String> e : competitorDisplayNames.entrySet()) {
                compactCompetitorDisplayNames.put(compactCompetitorMap.get(e.getKey()), e.getValue());
            }
            competitorDisplayNames = compactCompetitorDisplayNames;
        }
        raceColumnNamesForWhichCompetitorOrderingPerRaceUnchanged = new HashSet<String>();
        final HashMap<String, List<CompetitorDTO>> competitorOrderingPerRaceColumnName = new HashMap<>(getCompetitorOrderingPerRaceColumnName());
        for (RaceColumnDTO raceColumn : this.getRaceList()) {
            List<CompetitorDTO> competitorsFromBestToWorstForRaceColumn = getCompetitorsFromBestToWorst(raceColumn.getName());
            List<CompetitorDTO> previousCompetitorsFromBestToWorstForRaceColumn = previousVersion.getCompetitorsFromBestToWorst(raceColumn.getName());
            if (Util.equalsWithNull(competitorsFromBestToWorstForRaceColumn, previousCompetitorsFromBestToWorstForRaceColumn)) {
                raceColumnNamesForWhichCompetitorOrderingPerRaceUnchanged.add(raceColumn.getName());
                competitorOrderingPerRaceColumnName.remove(raceColumn.getName());
            } else {
                // try at least partial compaction
                List<CompetitorDTO> compactedCompetitorsFromBestToWorstForRaceColumn = new ArrayList<>();
                for (CompetitorDTO competitor : competitorsFromBestToWorstForRaceColumn) {
                    compactedCompetitorsFromBestToWorstForRaceColumn.add(compactCompetitorMap.get(competitor));
                }
                competitorOrderingPerRaceColumnName.put(raceColumn.getName(), compactedCompetitorsFromBestToWorstForRaceColumn);
            }
        }
        setCompetitorOrderingPerRace(competitorOrderingPerRaceColumnName);
        // now clone the rows map to enable stripping the LeaderboardEntryDTOs inside
        HashMap<CompetitorDTO, LeaderboardRowDTO> newRows = new HashMap<>();
        for (Map.Entry<CompetitorDTO, LeaderboardRowDTO> competitorAndRow : rows.entrySet()) {
            LeaderboardRowDTO previousRowDTO = previousVersion.rows.get(competitorAndRow.getKey());
            if (Util.equalsWithNull(competitorAndRow.getValue(), previousRowDTO)
                    // can only compress for competitors that existed in the previous version
                    && previousVersion.competitors.contains(competitorAndRow.getKey())) {
                entireRowUnchanged(previousVersion, competitorAndRow.getKey());
                // And don't enter into newRows. Mark all entries and all leg details in this row as unchanged for compaction of those structures:
            } else {
                LeaderboardRowDTO newRowDTO = new LeaderboardRowDTO();
                cloner.clone(competitorAndRow.getValue(), newRowDTO);
                CompetitorDTO compactCompetitor = compactCompetitorMap.get(competitorAndRow.getKey());
                newRowDTO.competitor = compactCompetitor;
                newRows.put(compactCompetitor, newRowDTO);
                HashMap<String, LeaderboardEntryDTO> newFieldsByRaceColumnName = new HashMap<String, LeaderboardEntryDTO>();
                for (Map.Entry<String, LeaderboardEntryDTO> raceColumnNameAndLeaderboardEntry : newRowDTO.fieldsByRaceColumnName.entrySet()) {
                    LeaderboardEntryDTO previousEntryDTO = null;
                    if (previousRowDTO != null) {
                        previousEntryDTO = previousRowDTO.fieldsByRaceColumnName.get(raceColumnNameAndLeaderboardEntry.getKey());
                    }
                    if (previousEntryDTO != null
                            && Util.equalsWithNull(raceColumnNameAndLeaderboardEntry.getValue(), previousEntryDTO)
                            // can only compress for competitors that existed in the previous version
                            && previousVersion.competitors.contains(competitorAndRow.getKey())) {
                        getUnchangedLeaderboardEntries(previousVersion).unchanged(competitorAndRow.getKey(),
                                raceColumnNameAndLeaderboardEntry.getKey());
                        // also mark all leg details for the competitor for this column as unchanged; this will ensure
                        // that column compression can work for leg details
                        getLegDetailsUnchanged(previousVersion).unchanged(competitorAndRow.getKey(),
                                new com.sap.sse.common.Util.Pair<String, Integer>(raceColumnNameAndLeaderboardEntry.getKey(), null));
                    } else {
                        LeaderboardEntryDTO newLeaderboardEntryDTO = new LeaderboardEntryDTO();
                        cloner.clone(raceColumnNameAndLeaderboardEntry.getValue(), newLeaderboardEntryDTO);
                        // clone the leg details collection to ensure that when we need to manipulate it we don't manipulate the original
                        if (newLeaderboardEntryDTO.legDetails != null) {
                            newLeaderboardEntryDTO.legDetails = new ArrayList<LegEntryDTO>(newLeaderboardEntryDTO.legDetails);
                        }
                        newFieldsByRaceColumnName.put(raceColumnNameAndLeaderboardEntry.getKey(), newLeaderboardEntryDTO);
                        if (newLeaderboardEntryDTO.legDetails != null) {
                            for (int legDetailsIndex = 0; legDetailsIndex < newLeaderboardEntryDTO.legDetails.size(); legDetailsIndex++) {
                                LegEntryDTO legDetails = newLeaderboardEntryDTO.legDetails.get(legDetailsIndex);
                                if (previousEntryDTO != null
                                        && previousEntryDTO.legDetails != null
                                        && previousEntryDTO.legDetails.size() > legDetailsIndex
                                        && Util.equalsWithNull(legDetails,
                                                previousEntryDTO.legDetails.get(legDetailsIndex))
                                        // can only compress for competitors that existed in the previous version
                                        && previousVersion.competitors.contains(competitorAndRow.getKey())) {
                                    getLegDetailsUnchanged(previousVersion).unchanged(
                                            competitorAndRow.getKey(),
                                            new com.sap.sse.common.Util.Pair<String, Integer>(raceColumnNameAndLeaderboardEntry.getKey(),
                                                    legDetailsIndex));
                                    newLeaderboardEntryDTO.legDetails.set(legDetailsIndex, null);
                                }
                            }
                        } else {
                            if ((previousEntryDTO == null || previousEntryDTO.legDetails == null)
                                    // can only compress for competitors that existed in the previous version
                                    && previousVersion.competitors.contains(competitorAndRow.getKey())) {
                                // old and new entry are null; no need to set the legDetails in the new version to null
                                // as it already consumes no space;
                                // however, mark the legDetails as unchanged in legDetailsUnchanged so as to allow for
                                // all-column compression
                                getLegDetailsUnchanged(previousVersion).unchanged(competitorAndRow.getKey(),
                                        new com.sap.sse.common.Util.Pair<String, Integer>(raceColumnNameAndLeaderboardEntry.getKey(), null));
                            }
                        }
                    }
                }
                newRowDTO.fieldsByRaceColumnName = newFieldsByRaceColumnName;
            }
        }
        rows = newRows.isEmpty() ? null : newRows;
        if (legDetailsUnchanged != null) {
            legDetailsUnchanged.compact(); // compacts even those columns where the *last* entry was one with a null leg index
        }
        if (getRaceList() != null) {
            List<RaceColumnDTO> newRaceList = new ArrayList<RaceColumnDTO>(getRaceList().size());
            indexOfUnchangedRaceColumnDTOsInPrevious = new int[getRaceList().size()];
            boolean changed = false;
            for (int raceIndex=0; raceIndex<getRaceList().size(); raceIndex++) {
                int previousIndex = previousVersion.getRaceList().indexOf(getRaceList().get(raceIndex));
                indexOfUnchangedRaceColumnDTOsInPrevious[raceIndex] = previousIndex;
                if (previousIndex != -1) {
                    newRaceList.add(null);
                    changed = true;
                } else {
                    newRaceList.add(getRaceList().get(raceIndex));
                }
            }
            if (changed) {
                setRaceList(newRaceList);
            }
        }
        return this;
    }

    /**
     * Marks the <code>competitor</code>'s row as unchanged in the {@link #rowsUnchanged} structure as well as for all
     * races and legs in {@link #legDetailsUnchanged} and {@link #unchangedLeaderboardEntries} so that compaction of
     * those structures can happen accordingly. The races/legs can only be marked as unchanged if the row is not
     * <code>null</code>.
     * 
     * @param competitor
     *            a competitor that already exists in <code>previousVersion.competitors</code>
     */
    private void entireRowUnchanged(LeaderboardDTO previousVersion, CompetitorDTO competitor) {
        assert previousVersion.competitors.contains(competitor);
        if (rowsUnchanged == null) {
            rowsUnchanged = new UnchangedWithCompetitorsInBitSet<Void>(previousVersion, rows.size());
        }
        rowsUnchanged.unchanged(competitor, null);
        LeaderboardRowDTO row = rows.get(competitor);
        if (row != null && row.fieldsByRaceColumnName != null) {
            for (Map.Entry<String, LeaderboardEntryDTO> e : row.fieldsByRaceColumnName.entrySet()) {
                getUnchangedLeaderboardEntries(previousVersion).unchanged(competitor, e.getKey());
                getLegDetailsUnchanged(previousVersion).unchanged(competitor, new com.sap.sse.common.Util.Pair<String, Integer>(e.getKey(), /* all legs unchanged */ null));
            }
        }
    }

    private UnchangedWithCompetitorsInBitSet<String> getUnchangedLeaderboardEntries(LeaderboardDTO previousVersion) {
        if (unchangedLeaderboardEntries == null) {
            unchangedLeaderboardEntries = new UnchangedWithCompetitorsInBitSet<String>(previousVersion, rows.size());
        }
        return unchangedLeaderboardEntries;
    }

    private UnchangedLegDetails getLegDetailsUnchanged(LeaderboardDTO previousVersion) {
        if (legDetailsUnchanged == null) {
            legDetailsUnchanged = new UnchangedLegDetails(previousVersion, rows.size());
        }
        return legDetailsUnchanged;
    }

    public void setCurrentServerTime(Date currentServerTime) {
        this.currentServerTime = currentServerTime;
    }

    public Date getCurrentServerTime() {
        return currentServerTime;
    }

}
