package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.maps.client.MapPane;
import com.google.gwt.maps.client.MapPaneType;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.MeterDistance;

/**
 * This class provides an google map overlay based on a HTML5 canvas.
 * See {@link com.google.gwt.maps.client.overlay.Overlay} how to implement an overlay.
 */
public abstract class CanvasOverlay extends Overlay {

    /**
     * The HTML5 canvas which can be used to draw arbitrary shapes on a google map.
     */
    protected final Canvas canvas;

    /**
     * Indicates whether the canvas has been selected or not.
     */
    protected boolean isSelected;

    /**
     * The reference to the actual Google map.
     */
    protected MapWidget map;

    /**
     * The pane of the Google map containing the HTML DIV element of the canvas. 
     */
    protected MapPane pane;

    /**
     * The position of the canvas as a Latitude/Longitude position
     */
    protected LatLng latLngPosition;

    public CanvasOverlay() {
        canvas = Canvas.createIfSupported();
    }

    protected void setCanvasSize(int newWidthInPx, int newHeightInPx) {
        if (getCanvas() != null) {
            getCanvas().setWidth(newWidthInPx + "px");
            getCanvas().setHeight(newHeightInPx + "px");
            getCanvas().setCoordinateSpaceWidth(newWidthInPx);
            getCanvas().setCoordinateSpaceHeight(newHeightInPx);
        }
    }

    protected int calculateRadiusOfBoundingBox(LatLng centerPosition, double lengthInMeter) {
        Position centerPos = new DegreePosition(centerPosition.getLatitude(), centerPosition.getLongitude());
        Position translateRhumbX = centerPos.translateRhumb(new DegreeBearingImpl(90), new MeterDistance(lengthInMeter));
        Position translateRhumbY = centerPos.translateRhumb(new DegreeBearingImpl(0), new MeterDistance(lengthInMeter));

        LatLng posWithDistanceX = LatLng.newInstance(translateRhumbX.getLatDeg(), translateRhumbX.getLngDeg());
        LatLng posWithDistanceY = LatLng.newInstance(translateRhumbY.getLatDeg(), translateRhumbY.getLngDeg());

        Point pointCenter = map.convertLatLngToDivPixel(centerPosition);
        Point pointX = map.convertLatLngToDivPixel(posWithDistanceX);
        Point pointY = map.convertLatLngToDivPixel(posWithDistanceY);
        
        int diffX = Math.abs(pointX.getX() - pointCenter.getX());
        int diffY = Math.abs(pointY.getY() - pointCenter.getY());
        
        return Math.min(diffX, diffY);  
    }

    @Override
    protected void initialize(MapWidget map) {
        this.map = map;
        this.pane = map.getPane(MapPaneType.MAP_PANE);
        getPane().add(getCanvas());
    }

    @Override
    protected void remove() {
        getCanvas().removeFromParent();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public boolean isVisible() {
        return getCanvas() != null && getCanvas().isVisible();
    }

    public void setVisible(boolean isVisible) {
        if (getCanvas() != null) {
            getCanvas().setVisible(isVisible);
        }
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public LatLng getLatLngPosition() {
        return latLngPosition;
    }

    protected void setLatLngPosition(LatLng latLngPosition) {
        this.latLngPosition = latLngPosition;
    }

    protected MapWidget getMap() {
        return map;
    }

    protected MapPane getPane() {
        return pane;
    }
}
