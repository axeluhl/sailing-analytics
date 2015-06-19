package com.sap.sailing.racecommittee.app.domain.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
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
    
    private String build(final String leaderboardName, final String seriesName, final String fleetName, final String raceColumnName) {
        final Fleet fleet = new FleetImpl(fleetName);
        final SeriesBase series = new SeriesBase() {
            private static final long serialVersionUID = 1139740910228343375L;

            @Override
            public String getName() {
                return seriesName;
            }
            
            @Override
            public void setName(String newName) {
            }
            
            @Override
            public boolean isMedal() {
                return false;
            }
            
            @Override
            public Iterable<? extends Fleet> getFleets() {
                return null;
            }
        };
        RaceGroup raceGroup = new RaceGroupImpl(leaderboardName, /* displayName */ null, /* boatClass */ null, /* courseArea */ null, /* series */ null, /* regattaConfiguration */ null);
        ManagedRaceIdentifier identifier = new ManagedRaceIdentifierImpl(raceColumnName, fleet, series, raceGroup);
        return identifier.getId();
    }
}
