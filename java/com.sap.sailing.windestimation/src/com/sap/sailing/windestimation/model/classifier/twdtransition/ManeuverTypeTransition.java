package com.sap.sailing.windestimation.model.classifier.twdtransition;

import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;

public enum ManeuverTypeTransition {
    TACK_TACK, TACK_JIBE, TACK_OTHER, JIBE_JIBE, JIBE_OTHER, OTHER_OTHER;

    public static ManeuverTypeTransition valueOf(ManeuverTypeForClassification fromManeuverType,
            ManeuverTypeForClassification toManeuverType) {
        switch (fromManeuverType) {
        case TACK:
            switch (toManeuverType) {
            case TACK:
                return TACK_TACK;
            case JIBE:
                return ManeuverTypeTransition.TACK_JIBE;
            case BEAR_AWAY:
            case HEAD_UP:
                return ManeuverTypeTransition.TACK_OTHER;
            }
        case JIBE:
            switch (toManeuverType) {
            case TACK:
                return TACK_JIBE;
            case JIBE:
                return ManeuverTypeTransition.JIBE_JIBE;
            case BEAR_AWAY:
            case HEAD_UP:
                return ManeuverTypeTransition.JIBE_OTHER;
            }
        case BEAR_AWAY:
        case HEAD_UP:
            switch (toManeuverType) {
            case TACK:
                return ManeuverTypeTransition.TACK_OTHER;
            case JIBE:
                return ManeuverTypeTransition.JIBE_OTHER;
            case BEAR_AWAY:
            case HEAD_UP:
                return ManeuverTypeTransition.OTHER_OTHER;
            }
        }
        throw new IllegalStateException("Unsupported transition from " + fromManeuverType + " to " + toManeuverType);
    }
}