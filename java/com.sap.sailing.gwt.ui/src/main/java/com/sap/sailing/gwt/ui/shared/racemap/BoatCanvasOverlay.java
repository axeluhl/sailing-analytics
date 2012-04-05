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
        ImageTransformer boatImageTransformer = raceMapImageManager.getBoatImageTransformer(boatFix, isSelected);
        double realBoatSizeScaleFactor = raceMapImageManager.getRealBoatSizeScaleFactor(boatImageTransformer.getImageSize());        
        ImageData imageData = boatImageTransformer.getTransformedImageData(boatFix.speedWithBearing.bearingInDegrees, realBoatSizeScaleFactor);
        
        int imageWidth = imageData.getWidth();
        int imageHeigth = imageData.getHeight();
        canvas.setWidth(String.valueOf(imageWidth));
        canvas.setHeight(String.valueOf(imageHeigth));
        canvas.setCoordinateSpaceWidth(imageWidth);
        canvas.setCoordinateSpaceHeight(imageHeigth);
        
        LatLng latLngPosition = LatLng.newInstance(boatFix.position.latDeg, boatFix.position.lngDeg);
      
        Point boatPositionInPx = map.convertLatLngToDivPixel(latLngPosition);
        
        pane.setWidgetPosition(canvas, boatPositionInPx.getX() - imageWidth / 2, boatPositionInPx.getY() - imageHeigth / 2);

        canvas.getContext2d().putImageData(imageData, 0, 0);
      }
    }

    public GPSFixDTO getBoatFix() {
        return boatFix;
    }

    public void setBoatFix(GPSFixDTO boatFix) {
        this.boatFix = boatFix;
    }
}
