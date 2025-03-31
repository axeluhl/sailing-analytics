package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.NauticalSide;

public class NauticalSideFormatter {
    public static String format(NauticalSide nauticalSide, StringMessages stringMessages) {
        final String result;
        switch (nauticalSide) {
        case PORT:
            result = stringMessages.portSide();
            break;
        case STARBOARD:
            result = stringMessages.starboardSide();
            break;
        default:
            result = null;
        }
        return result;
    }
}
