package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sap.sse.common.Util.Triple;

/**
 * This test verifies the algorithm used to escape and unescape SimpleRaceIdentifier components as used in
 * <code>FleetIfentifierImpl</code>.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class SimpleRaceIdentifierEscapingTest {
    @Test
    public void testUnescapingSimpleRaceIdentifier() {
        test("Leaderboard with trailing backslash and dot\\.", "raceColumn", "fleet"); 
        test("Leaderboard\\.with\\.two dots", "raceColumn", "fleet"); 
        test("Leaderboard with double backslash\\\\", "raceColumn", "fleet"); 
        test("Leaderboard with double backslash and dot\\\\.", "raceColumn", "fleet"); 
    }
    
    private void test(String leaderboardName, String raceColumnName, String fleetName) {
        final String marshalled = build(leaderboardName, raceColumnName, fleetName);
        final Triple<String, String, String> parsed = parse(marshalled);
        assertEquals(leaderboardName, parsed.getA());
        assertEquals(raceColumnName, parsed.getB());
        assertEquals(fleetName, parsed.getC());
    }

    private Triple<String, String, String> parse(String s) {
        final String id = s.replace("\\\\", "\\").replace("\\.", ".");
        String[] split = id.split("\\.");
        String leaderboardName = split[0];
        String raceColumnName = split[3];
        String fleetName = split[2];
        return new Triple<>(leaderboardName, raceColumnName, fleetName);
    }
    
    private String build(String leaderboardName, String raceColumnName, String fleetName) {
        return String.format("%s.%s.%s", 
                escape(leaderboardName), 
                escape(raceColumnName), 
                escape(fleetName));
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace(".", "\\.");
    }
}
