package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapHelpLinesSettings.HelpLineTypes;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapZoomSettings.ZoomTypes;

public class RaceMapSettingsTypeFormatter {
    public static String formatZoomType(ZoomTypes zoomType, StringMessages stringMessages) {
        switch (zoomType) {
        case NONE:
            return stringMessages.autoZoomOff();
        case BOATS:
            return stringMessages.autoZoomToBoats();
        case TAILS:
            return stringMessages.autoZoomToTails();
        case BUOYS:
            return stringMessages.autoZoomToBuoys();
        case WINDSENSORS:
            return stringMessages.autoZoomToWindSensors();
        }
        return null;
    }

    public static String formatHelpLineType(HelpLineTypes helpLineType, StringMessages stringMessages) {
        switch (helpLineType) {
        case STARTLINE:
            return stringMessages.startLine();
        case FINISHLINE:
            return stringMessages.finishLine();
        case ADVANTAGELINE:
            return stringMessages.advantageLine();
        case COURSEMIDDLELINE:            
            return stringMessages.courseMiddleLine();
        case BUOYZONE:            
            return stringMessages.buoyZone();
        case BOATTAILS:
            return stringMessages.boatTails();
        case STARTLINETOFIRSTMARKTRIANGLE:
            return stringMessages.startFirstMarkTriangle();
        case COURSEGEOMETRY:
            return stringMessages.courseGeometry();
        case COURSEAREACIRCLES:
            return stringMessages.courseAreas();
        }
        return null;
    }
}
