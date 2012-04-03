package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.maps.client.MapPane;
import com.google.gwt.maps.client.MapPaneType;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Projection;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;

public class BoatCanvasOverlay extends Overlay {

    private final CompetitorDTO competitorDTO;

    private final RaceMapResources raceMapResources;
    
    private final Canvas canvas;

    private boolean isSelected;

    private MapWidget map;
    
    private MapPane pane;

    private GPSFixDTO boatFix;
    
    public BoatCanvasOverlay(CompetitorDTO competitorDTO, RaceMapResources raceMapResources) {
        this.competitorDTO = competitorDTO;
        this.raceMapResources = raceMapResources;
        canvas = Canvas.createIfSupported();
    }
    
    @Override
    protected Overlay copy() {
      return new BoatCanvasOverlay(competitorDTO, raceMapResources);
    }

    @Override
    protected void initialize(MapWidget map) {
      this.map = map;
      pane = map.getPane(MapPaneType.MAP_PANE);
      pane.add(canvas);
    }

    @Override
    protected void redraw(boolean force) {
      // Only set the rectangle's size if the map's size has changed
      if (boatFix != null) {
        ImageTransformer boatImageTransformer = raceMapResources.getBoatImageTransformer(boatFix, isSelected);
        double realBoatSizeScaleFactor = raceMapResources.getRealBoatSizeScaleFactor(boatImageTransformer.getImageSize());        
        ImageData imageData = boatImageTransformer.getTransformedImageData(boatFix.speedWithBearing.bearingInDegrees, realBoatSizeScaleFactor);
        
        int imageWidth = imageData.getWidth();
        int imageHeigth = imageData.getHeight();
        canvas.setWidth(String.valueOf(imageWidth));
        canvas.setHeight(String.valueOf(imageHeigth));
        canvas.setCoordinateSpaceWidth(imageWidth);
        canvas.setCoordinateSpaceHeight(imageHeigth);
        
        LatLng latLngPosition = LatLng.newInstance(boatFix.position.latDeg, boatFix.position.lngDeg);
        
        Point distanceInPx = calculateLocationDistanceInPx(map.getCenter(), latLngPosition);
        Size size = map.getSize();
        
        int x = size.getWidth() / 2 - distanceInPx.getX() - imageWidth / 2;
        int y = size.getHeight() / 2 - distanceInPx.getY() - imageHeigth / 2;
        
        pane.setWidgetPosition(canvas, x, y);

        canvas.getContext2d().putImageData(imageData, 0, 0);
      }
    }

    @Override
    protected void remove() {
        canvas.removeFromParent();
    }

    private Point calculateLocationDistanceInPx(LatLng location1, LatLng location2) {
        int zoomLevel = map.getZoomLevel();
        Projection projection = map.getCurrentMapType().getProjection();
        Point latLngToPixel1 = projection.fromLatLngToPixel(location1, zoomLevel);
        Point latLngToPixel2 = projection.fromLatLngToPixel(location2, zoomLevel);

        Point result = Point.newInstance(latLngToPixel1.getX() - latLngToPixel2.getX(), latLngToPixel1.getY() - latLngToPixel2.getY());
        return result;
    }

    public GPSFixDTO getBoatFix() {
        return boatFix;
    }

    public void setBoatFix(GPSFixDTO boatFix) {
        this.boatFix = boatFix;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public boolean isVisible() {
        if(canvas == null)
            return false;
        
        return canvas.isVisible();
    }

    public void setVisible(boolean isVisible) {
        if(canvas != null)
            canvas.setVisible(isVisible);
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}
