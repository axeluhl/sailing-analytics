package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;

public class BoatCanvasOverlay extends CanvasOverlay {

    private final CompetitorDTO competitorDTO;

    private final RaceMapImageManager raceMapImageManager;

    private GPSFixDTO boatFix;

    public BoatCanvasOverlay(CompetitorDTO competitorDTO, RaceMapImageManager raceMapImageManager) {
        super();
        this.competitorDTO = competitorDTO;
        this.raceMapImageManager = raceMapImageManager;
    }

    @Override
    protected Overlay copy() {
        return new BoatCanvasOverlay(competitorDTO, raceMapImageManager);
    }

    @Override
    protected void redraw(boolean force) {
        if (boatFix != null) {
            ImageTransformer boatImageTransformer = raceMapImageManager.getBoatImageTransformer(boatFix, isSelected());
            double realBoatSizeScaleFactor = raceMapImageManager.getRealBoatSizeScaleFactor(boatImageTransformer
                    .getImageSize());
            boatImageTransformer.drawToCanvas(getCanvas(), boatFix.speedWithBearing.bearingInDegrees, realBoatSizeScaleFactor);
            LatLng latLngPosition = LatLng.newInstance(boatFix.position.latDeg, boatFix.position.lngDeg);
            Point boatPositionInPx = getMap().convertLatLngToDivPixel(latLngPosition);
            getPane().setWidgetPosition(getCanvas(), boatPositionInPx.getX() - getCanvas().getCoordinateSpaceWidth() / 2, boatPositionInPx.getY()
                    - getCanvas().getCoordinateSpaceHeight() / 2);
        }
    }

    public GPSFixDTO getBoatFix() {
        return boatFix;
    }

    public void setBoatFix(GPSFixDTO boatFix) {
        this.boatFix = boatFix;
    }
}
