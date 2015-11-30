package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.HashSet;
import java.util.Set;

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
    private static final Set<Hoverline> hoverlines = new HashSet<>();
    
    private Polyline hoverline;
    private PolylineOptions options;
    
    public Hoverline(final Polyline polyline, PolylineOptions options) {
        hoverlines.add(this);
        
        this.options = PolylineOptions.newInstance();
        
        this.options.setClickable(options.getClickable());
        this.options.setGeodesic(options.getGeodesic());
        this.options.setMap(options.getMap());
        this.options.setPath(options.getPath_JsArray());
        this.options.setStrokeColor(options.getStrokeColor());
        this.options.setStrokeOpacity(TRANSPARENT);
        this.options.setStrokeWeight(10);
        this.options.setVisible(options.getVisible());
        this.options.setZindex(options.getZindex());
        
        hoverline = Polyline.newInstance(this.options);
        
        this.hoverline.setVisible(false);
        
        polyline.addMouseOverHandler(new MouseOverMapHandler() {
            @Override
            public void onEvent(MouseOverMapEvent event) {
                hoverline.setMap(polyline.getMap());
                hoverline.setPath(polyline.getPath());
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
        for (Hoverline hoverline : hoverlines) {
            hoverline.options.setStrokeOpacity(transparent ? TRANSPARENT : VISIBLE);
            hoverline.hoverline.setOptions(hoverline.options);
        }
    }
    
    public static void setStrokeWeight(int weight) {
        for (Hoverline hoverline : hoverlines) {
            hoverline.options.setStrokeWeight(weight);
            hoverline.hoverline.setOptions(hoverline.options);
        }
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
}