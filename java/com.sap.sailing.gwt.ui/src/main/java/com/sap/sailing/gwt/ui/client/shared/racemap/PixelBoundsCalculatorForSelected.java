package com.sap.sailing.gwt.ui.client.shared.racemap;


public abstract class PixelBoundsCalculatorForSelected implements PixelBoundsCalculator {

    public boolean isZoomOnlyToSelectedCompetitors(RaceMap forMap) {
        return forMap.getSettings().getZoomSettings().isZoomToSelectedCompetitors();
    }

}
