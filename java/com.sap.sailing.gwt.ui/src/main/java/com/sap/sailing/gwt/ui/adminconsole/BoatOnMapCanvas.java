package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Projection;
import com.google.gwt.maps.client.geom.Size;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;

public class BoatOnMapCanvas extends BoatCanvas {

    private MapWidget map;
    
    private LatLng latLngPosition;

    private GPSFixDTO boatFix;
    
    public BoatOnMapCanvas(CompetitorDTO competitorDTO, RaceMapResources raceMapResources, MapWidget map) {
        super(competitorDTO, raceMapResources);
        
        this.map = map;
    }

    public void draw() {
        ImageTransformer boatImageTransformer = raceMapResources.getBoatImageTransformer(boatFix, isSelected);
        double realBoatSizeScaleFactor = raceMapResources.getRealBoatSizeScaleFactor(boatImageTransformer.getImageSize());        
        ImageData imageData = boatImageTransformer.getTransformedImageData(boatFix.speedWithBearing.bearingInDegrees, realBoatSizeScaleFactor);
        
        int imageWidth = imageData.getWidth();
        int imageHeigth = imageData.getHeight();
        canvas.setWidth(String.valueOf(imageWidth));
        canvas.setHeight(String.valueOf(imageHeigth));
        canvas.setCoordinateSpaceWidth(imageWidth);
        canvas.setCoordinateSpaceHeight(imageHeigth);
        
        latLngPosition = LatLng.newInstance(boatFix.position.latDeg, boatFix.position.lngDeg);
        
        Point distanceInPx = calculateLocationDistanceInPx(map.getCenter(), latLngPosition);
        Size size = map.getSize();
        
        int x = size.getWidth() / 2 - distanceInPx.getX() - imageWidth / 2;
        int y = size.getHeight() / 2 - distanceInPx.getY() - imageHeigth / 2;
        
        setPosition(x, y);

        canvas.getContext2d().putImageData(imageData, 0, 0);
    }

    protected Point calculateLocationDistanceInPx(LatLng location1, LatLng location2) {
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

}
