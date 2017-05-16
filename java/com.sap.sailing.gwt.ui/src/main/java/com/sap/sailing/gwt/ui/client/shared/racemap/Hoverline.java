package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.events.mousemove.MouseMoveMapEvent;
import com.google.gwt.maps.client.events.mousemove.MouseMoveMapHandler;
import com.google.gwt.maps.client.events.mouseout.MouseOutMapEvent;
import com.google.gwt.maps.client.events.mouseout.MouseOutMapHandler;
import com.google.gwt.maps.client.events.mouseover.MouseOverMapEvent;
import com.google.gwt.maps.client.events.mouseover.MouseOverMapHandler;
import com.google.gwt.maps.client.overlays.Polyline;
import com.google.gwt.maps.client.overlays.PolylineOptions;

public class Hoverline {
    private static final double TRANSPARENT = 0;
    private static final double VISIBLE = 0.2d;
    
    private final Polyline hoverline;
    private final PolylineOptions options;
    
    public Hoverline(final Polyline polyline, PolylineOptions polylineOptions, final RaceMap map) {     
        this.options = PolylineOptions.newInstance();
        this.options.setClickable(polylineOptions.getClickable());
        this.options.setGeodesic(polylineOptions.getGeodesic());
        this.options.setMap(polyline.getMap());
        this.options.setPath(polyline.getPath());
        this.options.setStrokeColor(polylineOptions.getStrokeColor());
        try {
            this.options.setZindex(polylineOptions.getZindex());  // if the zindex is not set, this line throws an exception in dev mode
        } catch (Exception e) {
            // the Z-index of polylineOptions most likely was undefined and therefore cannot be copied (GWT DevMode problem, mostly)
        }
        this.hoverline = Polyline.newInstance(this.options);
        this.hoverline.setVisible(false);
        polyline.addMouseOverHandler(new MouseOverMapHandler() {
            @Override
            public void onEvent(MouseOverMapEvent event) {
                options.setStrokeOpacity(map.getSettings().getTransparentHoverlines() ? TRANSPARENT : VISIBLE);
                options.setStrokeWeight(map.getSettings().getHoverlineStrokeWeight());
                options.setVisible(true);
                hoverline.setOptions(options);
            }
        });
        hoverline.addMouseOutMoveHandler(new MouseOutMapHandler() {
            @Override
            public void onEvent(MouseOutMapEvent event) {
                hoverline.setVisible(false);
            }
        });
        map.getMap().addMouseMoveHandler(new MouseMoveMapHandler() {
            @Override
            public void onEvent(MouseMoveMapEvent event) {
                hoverline.setVisible(false);
            }
        });
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