package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.sap.sailing.gwt.ui.shared.ManeuverDTO;

public enum ManeuverColor {
    RED, GREEN;

    public static ManeuverColor getManeuverColor(ManeuverDTO maneuver) {
        if (maneuver.directionChangeInDegrees < 0) {
            return RED;
        }
        return GREEN;
    }

}
