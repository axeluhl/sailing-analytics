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
    
    private final Polyline hoverline;
    private final PolylineOptions options;
    private final RaceMap map;
    
    public Hoverline(final Polyline polyline, PolylineOptions polylineOptions, RaceMap map) {     
        this.map = map;
        this.options = PolylineOptions.newInstance();
        this.options.setClickable(polylineOptions.getClickable());
        this.options.setGeodesic(polylineOptions.getGeodesic());
        this.options.setMap(polylineOptions.getMap());
        this.options.setStrokeColor(polylineOptions.getStrokeColor());
        this.options.setVisible(polylineOptions.getVisible());
        this.options.setZindex(polylineOptions.getZindex());
        this.hoverline = Polyline.newInstance(this.options);
        this.hoverline.setVisible(false);
        polyline.addMouseOverHandler(new MouseOverMapHandler() {
            @Override
            public void onEvent(MouseOverMapEvent event) {
                Hoverline.this.options.setStrokeOpacity(Hoverline.this.map.getSettings().getTransparentHoverlines() ? TRANSPARENT : VISIBLE);
                options.setStrokeWeight(Hoverline.this.map.getSettings().getHoverlineStrokeWeight());
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