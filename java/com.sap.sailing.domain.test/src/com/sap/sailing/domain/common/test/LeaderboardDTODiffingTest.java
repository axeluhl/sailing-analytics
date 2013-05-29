package com.sap.sailing.domain.common.test;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.dto.IncrementalLeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.test.StoredTrackBasedTest;

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
    
    @Before
    public void setUp() throws FileNotFoundException, IOException, ClassNotFoundException {
        ObjectInputStream ois = StoredTrackBasedTest.getObjectInputStream("IncrementalLeaderboardDTO.ser");
        previousVersion = (LeaderboardDTO) ois.readObject();
    }
    
    @Test
    public void testLeaderboardSuccessfullyRead() {
        assertNotNull(previousVersion);
    }
}
