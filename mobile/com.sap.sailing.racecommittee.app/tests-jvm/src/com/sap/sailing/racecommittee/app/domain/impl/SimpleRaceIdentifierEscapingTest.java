package com.sap.sailing.racecommittee.app.domain.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;
import com.sap.sailing.domain.base.racegroup.impl.RaceGroupImpl;
import com.sap.sailing.racecommittee.app.domain.ManagedRaceIdentifier;
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
        test("Leaderboard with trailing backslash and dot\\.", "series", "fleet", "raceColumn");
        test("Leaderboard\\.with\\.two dots", "series", "fleet", "raceColumn");
        test("Leaderboard with double backslash\\\\", "series", "fleet", "raceColumn");
        test("Leaderboard with double backslash and dot\\\\.", "series", "fleet", "raceColumn");
        test("\\Leaderboard with leading backslash", "series", "fleet", "raceColumn");
        test("\\.Leaderboard with leading backslash and dot", "series", "fleet", "raceColumn");
        test("\\\\.Leaderboard with leading double backslash and dot", "series", "fleet", "raceColumn");
        test("\\\\.Leaderboard with leading double backslash and two dots", "series", "fleet", "raceColumn");
    }

    private void test(String leaderboardName, String seriesName, String fleetName, String raceColumnName) {
        final String marshalled = build(leaderboardName, seriesName, fleetName, raceColumnName);
        final Triple<String, String, String> parsed = FleetIdentifierImpl.unescape(marshalled);
        assertEquals(leaderboardName, parsed.getA());
        assertEquals(raceColumnName, parsed.getB());
        assertEquals(fleetName, parsed.getC());
    }

    private String build(final String leaderboardName, final String seriesName, final String fleetName,
            final String raceColumnName) {
        Fleet fleet = mock(Fleet.class);
        SeriesWithRows series = mock(SeriesWithRows.class);
        RaceGroup raceGroup = mock(RaceGroupImpl.class);

        when(fleet.getName()).thenReturn(fleetName);
        when(series.getName()).thenReturn(seriesName);
        when(raceGroup.getName()).thenReturn(leaderboardName);

        ManagedRaceIdentifier identifier = new ManagedRaceIdentifierImpl(raceColumnName, fleet, series, raceGroup);
        return identifier.getId();
    }
}
