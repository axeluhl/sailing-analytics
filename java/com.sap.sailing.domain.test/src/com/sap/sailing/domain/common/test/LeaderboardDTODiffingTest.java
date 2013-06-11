package com.sap.sailing.domain.common.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.Cloner;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.IncrementalLeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardEntryDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.LegEntryDTO;
import com.sap.sailing.domain.test.StoredTrackBasedTest;
import com.sap.sailing.util.ClonerImpl;

/**
 * Tests the compressing / de-compressing functionality of {@link LeaderboardDTO} and {@link IncrementalLeaderboardDTO}.
 * See also bug 1417.<p>
 * 
 * The data of a meaningful and non-trivial {@link LeaderboardDTO} is obtained by using an instrumented version of
 * <code>SailingServiceImpl.getLeaderboardByName(...)</code> which serializes the leaderboard at the end of the method
 * to a file used by this test. The leaderboard that this test wants to use is that of the 505 Worlds 2013, obtained
 * for an expanded Race R9 at time 2013-05-03T17:21:40Z.  
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class LeaderboardDTODiffingTest {
    private LeaderboardDTO previousVersion;
    private IncrementalLeaderboardDTO newVersion;
    private final Cloner cloner = new ClonerImpl();
    
    @Before
    public void setUp() throws FileNotFoundException, IOException, ClassNotFoundException {
        ObjectInputStream ois = StoredTrackBasedTest.getObjectInputStream("IncrementalLeaderboardDTO.ser");
        previousVersion = (LeaderboardDTO) ois.readObject();
        ois.close();
        newVersion = new IncrementalLeaderboardDTO("12345", cloner);
        cloner.clone(previousVersion, newVersion);
    }
    
    private CompetitorDTO getPreviousCompetitorByName(String name) {
        for (CompetitorDTO competitor : previousVersion.competitors) {
            if (competitor.name.equals(name)) {
                return competitor;
            }
        }
        return null;
    }
    
    @Test
    public void testLeaderboardSuccessfullyRead() {
        assertNotNull(previousVersion);
        assertNotNull(newVersion);
    }

    @Test
    public void testPreviousAndNewVersionHaveEqualRows() {
        assertEquals(previousVersion.rows, newVersion.rows);
    }
    
    @Test
    public void testTotalStripping() {
        newVersion.strip(previousVersion);
        assertNull(newVersion.rows);
    }

    @Test
    public void testMajorStripping() {
        newVersion.rows = new HashMap<CompetitorDTO, LeaderboardRowDTO>(newVersion.rows);
        CompetitorDTO wolfgang = getPreviousCompetitorByName("HUNGER +JESS");
        assertNotNull(wolfgang);
        LeaderboardRowDTO wolfgangsRow = new LeaderboardRowDTO();
        cloner.clone(newVersion.rows.get(wolfgang), wolfgangsRow);
        newVersion.rows.put(wolfgang, wolfgangsRow);
        wolfgangsRow.totalDistanceTraveledInMeters += 1;
        Map<CompetitorDTO, LeaderboardRowDTO> rowsBeforeStripping = newVersion.rows;
        newVersion.strip(previousVersion);
        assertNotNull(newVersion.rows);
        assertEquals(1, newVersion.rows.size()); // only wolfgang's row show show
        assertTrue(newVersion.rows.get(wolfgang).fieldsByRaceColumnName.isEmpty());
        LeaderboardDTO applied = newVersion.getLeaderboardDTO(previousVersion);
        assertEquals(rowsBeforeStripping, applied.rows);
    }

    @Test
    public void testStrippingExceptOneColumnInOneRace() {
        HashMap<CompetitorDTO, LeaderboardRowDTO> newRows = new HashMap<CompetitorDTO, LeaderboardRowDTO>(newVersion.rows);
        double newDistance = 1234;
        String nameOfRaceColumnToChange = "R9";
        int indexOfLegToChange = 7; // ( zero based: ) 8th leg is the last leg
        for (Map.Entry<CompetitorDTO, LeaderboardRowDTO> e : newVersion.rows.entrySet()) {
            LeaderboardRowDTO newRow = new LeaderboardRowDTO();
            cloner.clone(e.getValue(), newRow);
            newRows.put(e.getKey(), newRow);
            newRow.fieldsByRaceColumnName = new HashMap<String, LeaderboardEntryDTO>(newRow.fieldsByRaceColumnName); // clone entry map
            LeaderboardEntryDTO newEntry = new LeaderboardEntryDTO();
            cloner.clone(newRow.fieldsByRaceColumnName.get(nameOfRaceColumnToChange), newEntry);
            newRow.fieldsByRaceColumnName.put(nameOfRaceColumnToChange, newEntry);
            if (newEntry.legDetails != null) {
                newEntry.legDetails = new ArrayList<LegEntryDTO>(newEntry.legDetails); // clone leg details list
                if (newEntry.legDetails.get(indexOfLegToChange) != null) {
                    LegEntryDTO newLegDetail = new LegEntryDTO();
                    cloner.clone(newEntry.legDetails.get(indexOfLegToChange), newLegDetail);
                    newEntry.legDetails.set(indexOfLegToChange, newLegDetail);
                    newLegDetail.distanceTraveledInMeters = newDistance;
                    newDistance += 1;
                }
            }
        }
        newVersion.rows = newRows;
        Map<CompetitorDTO, LeaderboardRowDTO> rowsBeforeStripping = newVersion.rows;
        newVersion.strip(previousVersion);
        assertNotNull(newVersion.rows);
        assertEquals(previousVersion.rows.size()-17, newVersion.rows.size()); // all rows have changed except for 17 that have no leg details in leg 8
        // now assert that for all rows there is no leaderboard entry for all races but R9 and
        // for R9 there either are no leg details or all leg details for all legs other than L8 are null
        for (Map.Entry<CompetitorDTO, LeaderboardRowDTO> e : newVersion.rows.entrySet()) {
            if (e.getValue().fieldsByRaceColumnName != null) {
                assertTrue(e.getValue().fieldsByRaceColumnName.size() <= 1);
                for (Map.Entry<String, LeaderboardEntryDTO> e2 : e.getValue().fieldsByRaceColumnName.entrySet()) {
                    if (e2.getKey().equals(nameOfRaceColumnToChange)) {
                        List<LegEntryDTO> r9LegDetails = e2.getValue().legDetails;
                        if (r9LegDetails != null) {
                            for (int i=0; i<r9LegDetails.size(); i++) {
                                if (i != indexOfLegToChange) {
                                    assertNull(r9LegDetails.get(i));
                                } else {
                                    assertNotNull(r9LegDetails.get(i));
                                }
                            }
                        }
                    } else {
                        // if there is an entry for any column other than R9 (which is not really expected) then the entry is expected to be null
                        assertNull(e2.getValue());
                    }
                }
            }
        }
        LeaderboardDTO applied = newVersion.getLeaderboardDTO(previousVersion);
        assertEquals(rowsBeforeStripping, applied.rows);
    }
    
    @Test
    public void testCompetitorListChange() {
        newVersion.competitors = new ArrayList<CompetitorDTO>(newVersion.competitors); // clone competitor list so it's not identical to that of previous version
        CompetitorDTO somebodyNew = new CompetitorDTO("Someone New", "DE", "GER", "Germany", "GER 1234", "912p09871203987",
                new BoatClassDTO("505", 5.05));
        newVersion.competitors.add(13, somebodyNew); // insert a competitor; this should mess up all others' indexes; check if this works
        CompetitorDTO wolfgang = getPreviousCompetitorByName("HUNGER +JESS");
        newVersion.competitors.remove(wolfgang);
        newVersion.rows.remove(wolfgang); // remove another competitor
        List<CompetitorDTO> newCompetitorsBeforeStripping = new ArrayList<CompetitorDTO>(newVersion.competitors);
        newVersion.strip(previousVersion);
        assertNull(newVersion.competitors); // but there should be an added competitor that we can't see through the public interface
        LeaderboardDTO applied = newVersion.getLeaderboardDTO(previousVersion);
        assertEquals(newCompetitorsBeforeStripping, applied.competitors);
    }
}
