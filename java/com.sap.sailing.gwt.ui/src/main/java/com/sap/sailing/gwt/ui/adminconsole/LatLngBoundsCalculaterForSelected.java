package com.sap.sailing.gwt.ui.adminconsole;

public abstract class LatLngBoundsCalculaterForSelected implements LatLngBoundsCalculator {
    
    private boolean zoomOnlyToSelectedCompetitors;

    public void setZoomOnlyToSelectedCompetitors(boolean zoomOnlyToSelectedCompetitors) {
        this.zoomOnlyToSelectedCompetitors = zoomOnlyToSelectedCompetitors;
    }

    public boolean isZoomOnlyToSelectedCompetitors() {
        return zoomOnlyToSelectedCompetitors;
    }

}
