package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.events.mouseout.MouseOutMapEvent;
import com.google.gwt.maps.client.events.mouseout.MouseOutMapHandler;
import com.google.gwt.maps.client.events.mouseover.MouseOverMapEvent;
import com.google.gwt.maps.client.events.mouseover.MouseOverMapHandler;
import com.google.gwt.maps.client.mvc.MVCArray;
import com.google.gwt.maps.client.overlays.Polyline;
import com.google.gwt.maps.client.overlays.PolylineOptions;

class Hoverline {
    private static final double TRANSPARENT = 0;
    private static final double VISIBLE = 0.2d;
    
    private static boolean transparent = true;
    private static int weight = 10;
    
    private Polyline hoverline;
    private PolylineOptions options;
    
    public Hoverline(final Polyline polyline, PolylineOptions polylineOptions) {        
        this.options = PolylineOptions.newInstance();
        
        this.options.setClickable(polylineOptions.getClickable());
        this.options.setGeodesic(polylineOptions.getGeodesic());
        this.options.setMap(polylineOptions.getMap());
        this.options.setPath(polylineOptions.getPath_JsArray());
        this.options.setStrokeColor(polylineOptions.getStrokeColor());
        this.options.setStrokeOpacity(Hoverline.transparent ? TRANSPARENT : VISIBLE);
        this.options.setStrokeWeight(weight);
        this.options.setVisible(polylineOptions.getVisible());
        this.options.setZindex(polylineOptions.getZindex());
        
        hoverline = Polyline.newInstance(this.options);
        
        this.hoverline.setVisible(false);
        
        polyline.addMouseOverHandler(new MouseOverMapHandler() {
            @Override
            public void onEvent(MouseOverMapEvent event) {
                options.setStrokeOpacity(Hoverline.getOpacity());
                options.setStrokeWeight(weight);
                options.setMap(polyline.getMap());
                options.setPath(polyline.getPath());
                hoverline.setOptions(options);
                
                hoverline.setVisible(true);
            }
        });
        
        hoverline.addMouseOutMoveHandler(new MouseOutMapHandler() {
            @Override
            public void onEvent(MouseOutMapEvent event) {
                hoverline.setVisible(false);
            }
        });
    }
    
    public Hoverline setMap(MapWidget mapWidget) {
        this.hoverline.setMap(mapWidget);
        return this;
    }
    
    public static void setTransparent(boolean transparent) {
        Hoverline.transparent = transparent;
    }
    
    public static void setStrokeWeight(int weight) {
        Hoverline.weight = weight;
    }
    
    public MVCArray<LatLng> getPath() {
        return this.hoverline.getPath();
    }
    
    public Hoverline setPath(MVCArray<LatLng> path) {
        this.hoverline.setPath(path);
        return this;
    }
    
    public HandlerRegistration addClickHandler(ClickMapHandler handler) {
        return this.hoverline.addClickHandler(handler);
    }
    
    public HandlerRegistration addMouseOutMoveHandler(MouseOutMapHandler handler) {
        return this.hoverline.addMouseOutMoveHandler(handler);
    }
    
    public HandlerRegistration addMouseOverHandler(MouseOverMapHandler handler) {
        return this.hoverline.addMouseOverHandler(handler);
    }
    
    private static double getOpacity() {
        return Hoverline.transparent ? TRANSPARENT : VISIBLE;
    }
}