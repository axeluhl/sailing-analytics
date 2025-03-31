package com.sap.sailing.racecommittee.app.domain.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;
import com.sap.sailing.racecommittee.app.domain.FleetIdentifier;
import com.sap.sse.common.Util.Triple;

public class FleetIdentifierImpl implements FleetIdentifier {

    private Fleet fleet;
    private SeriesWithRows series;
    private RaceGroup raceGroup;

    public FleetIdentifierImpl(Fleet fleet, SeriesWithRows series, RaceGroup raceGroup) {
        this.fleet = fleet;
        this.series = series;
        this.raceGroup = raceGroup;
    }

    public Fleet getFleet() {
        return fleet;
    }

    public SeriesWithRows getSeries() {
        return series;
    }

    public RaceGroup getRaceGroup() {
        return raceGroup;
    }

    public String getId() {
        return String.format("%s.%s.%s", escapeIdentifierFragment(getRaceGroup().getName()),
                escapeIdentifierFragment(getSeries().getName()), escapeIdentifierFragment(getFleet().getName()));
    }

    protected String escapeIdentifierFragment(String fragment) {
        return fragment.replace("\\", "\\\\").replace(".", "\\.");
    }

    /**
     * @return a triple consisting of regattaLikeName/raceColumnName/fleetName; note that the series name which is
     *         encoded in the <code>escapedId</code> in second place (zero-based index 1) is silently ignored
     */
    public static Triple<String, String, String> unescape(String escapedId) {
        int arrayIndex = 0;
        StringBuilder[] split = new StringBuilder[4];
        for (int i = 0; i < split.length; i++) {
            split[i] = new StringBuilder();
        }
        boolean escaped = false;
        for (int i = 0; i < escapedId.length(); i++) {
            if (escaped) {
                split[arrayIndex].append(escapedId.charAt(i));
                escaped = false;
            } else if (escapedId.charAt(i) == '\\') {
                escaped = true; // next character is escaped
            } else if (escapedId.charAt(i) == '.') {
                // an unescaped dot
                arrayIndex++;
            } else {
                // unescaped non-escape, non-dot character
                split[arrayIndex].append(escapedId.charAt(i));
            }
        }
        String leaderboardName = split[0].toString();
        String raceColumnName = split[3].toString();
        String fleetName = split[2].toString();
        return new Triple<>(leaderboardName, raceColumnName, fleetName);
    }

}
