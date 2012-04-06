package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.ImageData;
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
            ImageData imageData = boatImageTransformer.getTransformedImageData(
                    boatFix.speedWithBearing.bearingInDegrees, realBoatSizeScaleFactor);
            int imageWidth = imageData.getWidth();
            int imageHeigth = imageData.getHeight();
            getCanvas().setWidth(String.valueOf(imageWidth));
            getCanvas().setHeight(String.valueOf(imageHeigth));
            getCanvas().setCoordinateSpaceWidth(imageWidth);
            getCanvas().setCoordinateSpaceHeight(imageHeigth);
            LatLng latLngPosition = LatLng.newInstance(boatFix.position.latDeg, boatFix.position.lngDeg);
            Point boatPositionInPx = getMap().convertLatLngToDivPixel(latLngPosition);
            getPane().setWidgetPosition(getCanvas(), boatPositionInPx.getX() - imageWidth / 2, boatPositionInPx.getY()
                    - imageHeigth / 2);
            getCanvas().getContext2d().putImageData(imageData, 0, 0);
        }
    }

    public GPSFixDTO getBoatFix() {
        return boatFix;
    }

    public void setBoatFix(GPSFixDTO boatFix) {
        this.boatFix = boatFix;
    }
}
