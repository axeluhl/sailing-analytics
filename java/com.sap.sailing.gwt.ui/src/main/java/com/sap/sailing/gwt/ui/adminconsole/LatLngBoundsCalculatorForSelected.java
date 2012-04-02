package com.sap.sailing.gwt.ui.adminconsole;

public abstract class LatLngBoundsCalculatorForSelected implements LatLngBoundsCalculator {

    public boolean isZoomOnlyToSelectedCompetitors(RaceMap forMap) {
        return forMap.getSettings().getZoomSettings().isZoomToSelectedCompetitors();
    }

}
