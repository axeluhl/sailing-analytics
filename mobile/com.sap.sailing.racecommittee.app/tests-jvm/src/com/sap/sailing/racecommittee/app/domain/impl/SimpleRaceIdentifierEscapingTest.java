package com.sap.sailing.racecommittee.app.domain.impl;

import com.sap.sse.common.Util.Triple;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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
        test("Leaderboard with trailing backslash and dot\\.", "empty", "fleet", "raceColumn");
        test("Leaderboard\\.with\\.two dots", "empty", "fleet", "raceColumn");
        test("Leaderboard with double backslash\\\\", "empty", "fleet", "raceColumn");
        test("Leaderboard with double backslash and dot\\\\.", "empty", "fleet", "raceColumn");
        test("\\Leaderboard with leading backslash", "empty", "fleet", "raceColumn");
        test("\\.Leaderboard with leading backslash and dot", "empty", "fleet", "raceColumn");
        test("\\\\.Leaderboard with leading double backslash and dot", "empty", "fleet", "raceColumn");
        test("\\\\.Leaderboard with leading double backslash and two dots", "empty", "fleet", "raceColumn");
    }
    
    private void test(String leaderboardName, String empty, String fleetName, String raceColumnName) {
        final String marshalled = build(leaderboardName, empty, fleetName, raceColumnName);
        final Triple<String, String, String> parsed = FleetIdentifierImpl.unescape(marshalled);
        assertEquals(leaderboardName, parsed.getA());
        assertEquals(raceColumnName, parsed.getB());
        assertEquals(fleetName, parsed.getC());
    }
    
    private String build(String leaderboardName, String empty, String fleetName, String raceColumnName) {
        return String.format("%s.%s.%s.%s",
                escape(leaderboardName),
                escape(empty),
                escape(fleetName),
                escape(raceColumnName));
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\");
    }
}
