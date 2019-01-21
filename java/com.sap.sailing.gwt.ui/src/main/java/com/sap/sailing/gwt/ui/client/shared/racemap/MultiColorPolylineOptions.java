package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.List;
import java.util.Random;

import com.google.gwt.maps.client.overlays.Polyline;
import com.google.gwt.maps.client.overlays.PolylineOptions;
import com.sap.sse.common.ColorMapper;
import com.sap.sse.common.ValueRangeFlexibleBoundaries;

public class MultiColorPolylineOptions {
    private boolean clickable = true;
    private boolean draggable = false;
    private boolean editable  = false;
    private boolean geodesic  = true;
    private boolean visible   = true;
    
    private int strokeWeight;
    private double strokeOpacity;
    
    private int zIndex;
    
    private ColorMapper colorMapper;
    
    public MultiColorPolylineOptions(ValueRangeFlexibleBoundaries valueRange) {
        colorMapper = new ColorMapper(valueRange, false);
    }
    public MultiColorPolylineOptions(PolylineOptions options, ValueRangeFlexibleBoundaries valueRange) {
        clickable = options.getClickable();
        geodesic = options.getGeodesic();
        visible = options.getVisible();
        strokeWeight = options.getStrokeWeight();
        strokeOpacity = options.getStrokeOpacity();
        zIndex = options.getZindex();
        colorMapper = new ColorMapper(valueRange, false);
    }
    
    public void applyTo(Polyline line) {
        PolylineOptions opt = PolylineOptions.newInstance();
        opt.setStrokeColor(colorMapper.getColor(new Random().nextInt(101))); //TODO
        opt.setClickable(clickable);
        opt.setGeodesic(geodesic);
        opt.setVisible(visible);
        opt.setStrokeWeight(strokeWeight);
        opt.setStrokeOpacity(strokeOpacity);
        opt.setZindex(zIndex);
        line.setOptions(opt);
        line.setEditable(editable);
    }
    public void applyTo(List<Polyline> lines) {
        for (Polyline line : lines) {
            applyTo(line);
        }
    }
    
    public Polyline newPolylineInstance(double value) {
        return newPolylineInstance(colorMapper.getColor(value));
    }
    public Polyline newPolylineInstance(String strokeColor) {
        PolylineOptions opt = PolylineOptions.newInstance();
        opt.setStrokeColor(strokeColor);
        opt.setClickable(clickable);
        opt.setGeodesic(geodesic);
        opt.setVisible(visible);
        opt.setZindex(zIndex);
        Polyline line = Polyline.newInstance(opt);
        line.setEditable(editable);
        return line;
    }
    
    public PolylineOptions toPolylineOptions() {
        PolylineOptions opt = PolylineOptions.newInstance();
        opt.setClickable(clickable);
        opt.setGeodesic(geodesic);
        opt.setVisible(visible);
        opt.setStrokeWeight(strokeWeight);
        opt.setStrokeOpacity(strokeOpacity);
        opt.setZindex(zIndex);
        return opt;
    }
    
    public ColorMapper getColorMapper() {
        return this.colorMapper;
    }
    public void setColorMapper(ColorMapper colorMapper) {
        this.colorMapper = colorMapper;
        //TODO Update polylines
    }
    
    public boolean getClickable() {
        return clickable;
    }
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }
    public boolean getDraggable() {
        return draggable;
    }
    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }
    public boolean getEditable() {
        return editable;
    }
    public void setEditable(boolean editable) {
        this.editable = editable;
    }
    public boolean getGeodesic() {
        return geodesic;
    }
    public void setGeodesic(boolean geodesic) {
        this.geodesic = geodesic;
    }
    public boolean getVisible() {
        return visible;
    }
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    public int getZIndex() {
        return zIndex;
    }
    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
    }
}
