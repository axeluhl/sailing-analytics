package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapHelpLinesSettings.HelpLineTypes;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapZoomSettings.ZoomTypes;

public class RaceMapSettingsTypeFormatter {
    public static String formatZoomType(ZoomTypes zoomType, StringMessages stringConstants) {
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

    public static String formatHelpLineType(HelpLineTypes helpLineType, StringMessages stringConstants) {
        switch (helpLineType) {
        case STARTLINE:
            return stringConstants.startLine();
        case FINISHLINE:
            return stringConstants.finishLine();
        case ADVANTAGELINE:
            return stringConstants.advantageLine();
        case COURSEMIDDLELINE:            
            return stringConstants.courseMiddleLine();
        case BUOYZONE:            
            return stringConstants.buoyZone();
        case BOATTAILS:
            return stringConstants.boatTails();
        case STARTLINETOFIRSTMARKTRIANGLE:
            return stringConstants.startFirstMarkTriangle();
        case COURSEGEOMETRY:
            return stringConstants.courseGeometry();
        }
        return null;
    }
}
