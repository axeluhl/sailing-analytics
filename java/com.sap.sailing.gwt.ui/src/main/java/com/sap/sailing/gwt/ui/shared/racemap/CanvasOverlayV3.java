package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.overlays.MapCanvasProjection;
import com.google.gwt.maps.client.overlays.OverlayView;
import com.google.gwt.maps.client.overlays.overlayhandlers.OverlayViewMethods;
import com.google.gwt.maps.client.overlays.overlayhandlers.OverlayViewOnAddHandler;
import com.google.gwt.maps.client.overlays.overlayhandlers.OverlayViewOnDrawHandler;
import com.google.gwt.maps.client.overlays.overlayhandlers.OverlayViewOnRemoveHandler;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.MeterDistance;

/**
 * The abstract base class for all canvas overlays.
 * @author Frank
 */
public abstract class CanvasOverlayV3 {
    
    private OverlayView customOverlayView;
    
    /**
     * The HTML5 canvas which can be used to draw arbitrary shapes on a google map.
     */
    protected Canvas canvas;

    /**
     * Indicates whether the canvas has been selected or not.
     */
    protected boolean isSelected;

    /**
     * The reference to the actual Google map.
     */
    protected MapWidget map;

    /**
     * The position of the canvas as a Latitude/Longitude position
     */
    protected LatLng latLngPosition;
    
    /**
     * the z-Index of the canvas
     */
    protected int zIndex;

    protected MapCanvasProjection mapProjection;

    public CanvasOverlayV3(MapWidget map, int zIndex) {
        this.map = map;
        this.mapProjection = null;
        
        canvas = Canvas.createIfSupported();
        customOverlayView = OverlayView.newInstance(map, getOnDrawHandler(), getOnAddHandler(), getOnRemoveHandler());
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

    public void addToMap() {
        customOverlayView.setMap(map);
    }
    
    public void removeFromMap() {
        customOverlayView.setMap(null);
    }
    
    protected void setLatLngPosition(LatLng latLngPosition) {
        this.latLngPosition = latLngPosition;
    }

    protected MapWidget getMap() {
        return map;
    }

    protected abstract void draw();

    protected OverlayViewOnAddHandler getOnAddHandler() {
        OverlayViewOnAddHandler result = new OverlayViewOnAddHandler() {
            @Override
            public void onAdd(OverlayViewMethods methods) {
                methods.getPanes().getMapPane().appendChild(canvas.getElement());
                methods.getPanes().getOverlayLayer().getStyle().setZIndex(zIndex);
            }
        };
        return result;
    }
     
    protected OverlayViewOnDrawHandler getOnDrawHandler() {
        return new OverlayViewOnDrawHandler() {
            @Override
            public void onDraw(OverlayViewMethods methods) {
                mapProjection = methods.getProjection();
                draw();
            }
        };
    }
    
    protected OverlayViewOnRemoveHandler getOnRemoveHandler() {
        OverlayViewOnRemoveHandler result = new OverlayViewOnRemoveHandler() {
            @Override
            public void onRemove(OverlayViewMethods methods) {
                // remove the canvas from the parent widget
                canvas.getElement().removeFromParent();
            }
        };
        return result;
    }
    
    protected void setCanvasPosition(double x, double y) {
        canvas.getElement().getStyle().setPosition(com.google.gwt.dom.client.Style.Position.ABSOLUTE);
        canvas.getElement().getStyle().setLeft(x, Unit.PX);
        canvas.getElement().getStyle().setTop(y, Unit.PX);
    }
    
    protected double calculateRadiusOfBoundingBox(MapCanvasProjection projection, LatLng centerPosition, double lengthInMeter) {
        Position centerPos = new DegreePosition(centerPosition.getLatitude(), centerPosition.getLongitude());
        Position translateRhumbX = centerPos.translateRhumb(new DegreeBearingImpl(90), new MeterDistance(lengthInMeter));
        Position translateRhumbY = centerPos.translateRhumb(new DegreeBearingImpl(0), new MeterDistance(lengthInMeter));

        LatLng posWithDistanceX = LatLng.newInstance(translateRhumbX.getLatDeg(), translateRhumbX.getLngDeg());
        LatLng posWithDistanceY = LatLng.newInstance(translateRhumbY.getLatDeg(), translateRhumbY.getLngDeg());
        
        Point pointCenter = projection.fromLatLngToDivPixel(centerPosition);
        Point pointX =  projection.fromLatLngToDivPixel(posWithDistanceX);
        Point pointY =  projection.fromLatLngToDivPixel(posWithDistanceY);
        
        double diffX = Math.abs(pointX.getX() - pointCenter.getX());
        double diffY = Math.abs(pointY.getY() - pointCenter.getY());
        
        return Math.min(diffX, diffY);  
    }
    
    protected void setCanvasSize(int newWidthInPx, int newHeightInPx) {
        if (getCanvas() != null) {
            getCanvas().setWidth(newWidthInPx + "px");
            getCanvas().setHeight(newHeightInPx + "px");
            getCanvas().setCoordinateSpaceWidth(newWidthInPx);
            getCanvas().setCoordinateSpaceHeight(newHeightInPx);
        }
    }
}
