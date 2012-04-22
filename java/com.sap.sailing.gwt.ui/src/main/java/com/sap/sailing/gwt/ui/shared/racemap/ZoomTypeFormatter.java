package com.sap.sailing.gwt.ui.shared.racemap;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.racemap.RaceMapZoomSettings.ZoomTypes;

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
