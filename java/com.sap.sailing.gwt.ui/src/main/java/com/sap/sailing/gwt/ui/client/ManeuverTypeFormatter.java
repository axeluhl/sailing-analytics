package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.ManeuverType;

public class ManeuverTypeFormatter {
    public static String format(ManeuverType maneuverType, StringMessages stringMessages) {
        switch (maneuverType) {
        case BEAR_AWAY:
            return stringMessages.bearAway();
        case HEAD_UP:
            return stringMessages.headUp();
        case JIBE:
            return stringMessages.jibe();
        case PENALTY_CIRCLE:
            return stringMessages.penaltyCircle();
        case TACK:
            return stringMessages.tack();
        case UNKNOWN:
            return stringMessages.unknownManeuver();
        }
        return null;

    }
}
