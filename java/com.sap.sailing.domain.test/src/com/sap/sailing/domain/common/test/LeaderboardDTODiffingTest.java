package com.sap.sailing.domain.common.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.Cloner;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.IncrementalLeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
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
        newVersion.strip(previousVersion);
        assertNotNull(newVersion.rows);
        assertEquals(1, newVersion.rows.size()); // only wolfgang's row show show
        assertTrue(newVersion.rows.get(wolfgang).fieldsByRaceColumnName.isEmpty());
    }
}
