package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.events.mouseover.MouseOverMapHandler;
import com.google.gwt.maps.client.mvc.MVCArray;
import com.google.gwt.maps.client.overlays.Polyline;
import com.google.gwt.maps.client.overlays.PolylineOptions;
import com.sap.sailing.gwt.ui.shared.CompetitorRaceDataDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.ValueRangeFlexibleBoundaries;

/**
 * A Polyline with multi color support. The line's individual segments are each made of a {@link Polyline} which can have a different color than the other segments.
 * A segment is created in between two adjacent {@link GPSFixDTO}s stored in {@link #fixes}.
 * 
 * @author Tim Hessenmüller (D062243)
 */
public class MultiColorPolyline {
    private MultiColorPolylineOptions options;
    private CoordinateSystem coordinateSystem;
    
    /**
     * Contains {@link GPSFixDTO}s marking the start and/or end of individual {@link Polyline}s stored in {@link #polylines}.
     */
    private List<GPSFixDTO> fixes; //TODO Decide on what list impl to use
    
    // Contains data points at which new polylines might be created
    private CompetitorRaceDataDTO data;
    
    /**
     * Stores the individual segments which together make up a multi color {@link Polyline} following the positions in {@link #fixes}.
     * There will always be one {@link Polyline} less than there are {@link GPSFixDTO}s in {@link #fixes} (except when both are empty).
     * The nth {@link Polyline} will connect the n and n+1 {@link GPSFixDTO}s.
     */
    private List<Polyline> polylines;
    
    private MapWidget map;
    private List<ClickMapHandler> clickMapHandler;
    private List<MouseOverMapHandler> mouseOverMapHandler;
    
    public MultiColorPolyline(CoordinateSystem coordinateSystem) {
        options = new MultiColorPolylineOptions(new ValueRangeFlexibleBoundaries(0, 100, 0.5));
        fixes = new LinkedList<GPSFixDTO>();
        polylines = new ArrayList<Polyline>(); //TODO impl?
        this.coordinateSystem = coordinateSystem;
        
        clickMapHandler = new LinkedList<ClickMapHandler>();
        mouseOverMapHandler = new LinkedList<MouseOverMapHandler>();
    }
    
    
    public MultiColorPolylineOptions getOptions() {
        return options;
    }
    
    public void setOptions(PolylineOptions options) {
        //TODO valueRange
        this.options = new MultiColorPolylineOptions(options, new ValueRangeFlexibleBoundaries(0, 100, 0.5));
        this.options.applyTo(polylines);
    }
    public void setOptions(MultiColorPolylineOptions options) {
        this.options = options;
        this.options.applyTo(polylines);
    }
    
    private Polyline createPolyline(int start) {
        //TODO Color
        Polyline line;
        if (data != null) {
            Date time = new Date((fixes.get(start).timepoint.getTime() + fixes.get(start + 1).timepoint.getTime()) / 2);
            line = options.newPolylineInstance(interpolateData(time));
        } else {
            line = options.newPolylineInstance(options.getColorMapper().getColor(new Random().nextInt(101)));
        }
        MVCArray<LatLng> path = MVCArray.newInstance();
        for (GPSFixDTO fix : fixes.subList(start, start + 2)) { // subList(inclusive, exclusive)
            path.push(coordinateSystem.toLatLng(fix.position));
        }
        line.setPath(path);
        if (map != null) {
            line.setMap(map);
        }
        for (ClickMapHandler h : clickMapHandler) {
            line.addClickHandler(h);
        }
        for (MouseOverMapHandler h : mouseOverMapHandler) {
            line.addMouseOverHandler(h);
        }
        return line;
    }
    
    public List<GPSFixDTO> getPath() {
        return fixes;
    }
    
    public void setPath(List<GPSFixDTO> path) {
        polylines.clear();
        fixes = path; //TODO Force specific impl?
        //polylines.ensureCapacity(fixes.size() - 1);
        for (int i = 0; i < fixes.size() - 2; i++) {
            polylines.add(createPolyline(i));
        }
    }
    
    public void insertAt(int index, GPSFixDTO fix) {
        fixes.add(index, fix);
        // Split what was until now a single Polyline into two that incorporate the new fix
        if (fixes.size() >= 2) {
            // Polylines can only be created between two fixes
            if (index == 0) {
                // We only need to prepend a single Polyline
                polylines.add(0, createPolyline(0));
            } else if (index == fixes.size() - 1) {
                // We only need to append a single Polyline
                polylines.add(createPolyline(index - 1));
            } else {
                // We need to split an existing Polyline into two
                polylines.get(index - 1).getPath().setAt(1, coordinateSystem.toLatLng(fix.position));
                polylines.add(index, createPolyline(index));
            }
        }
    }
    
    public GPSFixDTO removeAt(int index) {
        GPSFixDTO fix = fixes.remove(index);
        // The two Polylines meeting in fix need to be combined
        if (index == 0) {
            // Remove the Polyline connecting the first to the second fix
            polylines.remove(0);
        } else if (index == fixes.size()) { // The last fix has already been removed
            // Remove the Polyline connecting the last two fixes
            polylines.remove(index - 1);
        } else {
            // Remove the Polyline starting from fix
            polylines.remove(index);
            // and update the preceding Polyline to end at what is now fixes.get(index)
            polylines.get(index - 1).getPath().setAt(1, coordinateSystem.toLatLng(fixes.get(index).position));
        }
        return fix;
    }
    
    public void setAt(int index, GPSFixDTO fix) {
        fixes.set(index, fix);
        if (index == 0) {
            polylines.get(index).getPath().setAt(0, coordinateSystem.toLatLng(fix.position));
        } else if (index == fixes.size() - 1) {
            polylines.get(index - 1).getPath().setAt(1, coordinateSystem.toLatLng(fix.position));
        } else {
            polylines.get(index - 1).getPath().setAt(1, coordinateSystem.toLatLng(fix.position));
            polylines.get(index).getPath().setAt(0, coordinateSystem.toLatLng(fix.position));
        }
    }
    
    public void setMap(MapWidget map) {
        this.map = map;
        for (Polyline line : polylines) {
            line.setMap(map);
        }
    }
    
    private double interpolateData(Date time) {
        Iterator<Pair<Date, Double>> iter = data.getRaceData().iterator();
        //TODO Binary search? Map? Mostly called when a new Polyline gets created so guess its for the newest data?
        Pair<Date, Double> current = null;
        Pair<Date, Double> previous = null;
        while (iter.hasNext()) {
            current = iter.next();
            if (current.getA().after(time)) {
                break;
            }
            previous = current;
        }
        if (current == null) return 0.0;
        if (previous == null) return current.getB();
        double perc = Math.abs(time.getTime() - previous.getA().getTime() / (double) (current.getA().getTime() - previous.getA().getTime()));
        return (previous.getB() * (1.0 - perc)) + (current.getB() * perc);
    }
    
    public void addClickHandler(ClickMapHandler handler) {
        clickMapHandler.add(handler);
        // Add to already existing Polylines
        for (Polyline line : polylines) {
            line.addClickHandler(handler);
        }
    }
    
    public void addMouseOverHandler(MouseOverMapHandler handler) {
        mouseOverMapHandler.add(handler);
        // Add to already existing Polylines
        for (Polyline line : polylines) {
            line.addMouseOverHandler(handler);
        }
    }
}