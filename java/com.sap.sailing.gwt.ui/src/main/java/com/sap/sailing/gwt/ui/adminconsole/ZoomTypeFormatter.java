package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.gwt.ui.adminconsole.RaceMapZoomSettings.ZoomTypes;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class ZoomTypeFormatter {
    public static String format(ZoomTypes zoomType, StringMessages stringConstants) {
        switch (zoomType) {
        case NONE:
            return stringConstants.autoZoomOff();
        case BOATS:
            return stringConstants.autoZoomToBoats();
        case TAILS:
            return stringConstants.autoZoomToTails();
        case BUOYS:
            return stringConstants.autoZoomToBuoys();
        case WINDSENSORS:
            return stringConstants.autoZoomToWindSensors();
        }
        return null;
    }
}
