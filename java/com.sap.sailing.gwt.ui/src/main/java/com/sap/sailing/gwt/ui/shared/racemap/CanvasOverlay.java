package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.maps.client.MapPane;
import com.google.gwt.maps.client.MapPaneType;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Overlay;

public abstract class CanvasOverlay extends Overlay {

    protected final Canvas canvas;

    protected boolean isSelected;

    protected MapWidget map;
    
    protected MapPane pane;

    protected LatLng latLngPosition;
    
    public CanvasOverlay() {
        canvas = Canvas.createIfSupported();
    }
    
     @Override
    protected void initialize(MapWidget map) {
      this.map = map;
      pane = map.getPane(MapPaneType.MAP_PANE);
      pane.add(canvas);
    }

    @Override
    protected void remove() {
        canvas.removeFromParent();
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

    public LatLng getLatLngPosition() {
        return latLngPosition;
    }
}
