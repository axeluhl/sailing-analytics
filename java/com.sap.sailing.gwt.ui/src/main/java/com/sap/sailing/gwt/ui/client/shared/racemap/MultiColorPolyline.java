package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.events.mouseover.MouseOverMapHandler;
import com.google.gwt.maps.client.mvc.MVCArray;
import com.google.gwt.maps.client.overlays.Polyline;

/**
 * 
 * @author Tim Hessenmüller (D062243)
 */
public class MultiColorPolyline {
    private MultiColorPolylineColorMode colorMode = MultiColorPolylineColorMode.POLYCHROMATIC;
    private MultiColorPolylineOptions options;
    
    private List<Polyline> polylines;
    
    private MapWidget map;
    private List<ClickMapHandler> clickMapHandler;
    private List<MouseOverMapHandler> mouseOverMapHandler;
    
    public MultiColorPolyline(MultiColorPolylineColorProvider colorProvider) {
        this(new MultiColorPolylineOptions(colorProvider));
    }
    public MultiColorPolyline(MultiColorPolylineOptions options) {
        this.options = options;
        polylines = new ArrayList<Polyline>(); //TODO impl?
        
        clickMapHandler = new LinkedList<ClickMapHandler>();
        mouseOverMapHandler = new LinkedList<MouseOverMapHandler>();
        log("Constructor");
    }
    
    private void log(String msg) {
        GWT.log(this.hashCode() + " " + msg);
    }
    
    public MultiColorPolylineColorMode getColorMode() {
        return colorMode;
    }
    
    public void setColorMode(MultiColorPolylineColorMode colorMode) {
        if (this.colorMode != colorMode) {
            MVCArray<LatLng> path = MVCArray.newInstance(getPath().toArray(new LatLng[0]));
            this.colorMode = colorMode;
            setPath(path);
        }
    }
    
    /*public MultiColorPolylineOptions getOptions() {
        return options;
    }*/
    public void setOptions(MultiColorPolylineOptions options) {
        this.options = options;
        for (Polyline line : polylines) {
            String color = options.getColorProvider().getColor(line.getPath().get(0));
            line.setOptions(options.newPolylineOptionsInstance(color));
        }
    }
    
    
    
    public List<LatLng> getPath() {
        int cap = getLength();
        List<LatLng> path = new ArrayList<>(cap);
        if (cap > 0) {
            path.add(polylines.get(0).getPath().get(0));
            for (Polyline line : polylines) {
                for (int i = 1; i < line.getPath().getLength(); i++) {
                    path.add(line.getPath().get(i));
                }
            }
        }
        return path;
    }
    
    public void setPath(MVCArray<LatLng> path) {
        polylines.clear(); //TODO maybe not?
        switch (colorMode) {
        case MONOCHROMATIC:
            polylines.add(createPolyline(path));
            break;
        case POLYCHROMATIC:
            for (int i = 0; i < path.getLength() - 1; i++) {
                MVCArray<LatLng> subPath = MVCArray.newInstance();
                subPath.push(path.get(i));
                subPath.push(path.get(i + 1));
                polylines.add(createPolyline(subPath));
            }
            break;
        }
    }
    /*public void setPath(List<GPSFixDTO> path) {
        polylines.clear();
        fixes = path; //TODO Force specific impl?
        //polylines.ensureCapacity(fixes.size() - 1);
        for (int i = 0; i < fixes.size() - 2; i++) {
            polylines.add(createPolyline(i));
        }
    }*/
    
    public void insertAt(int index, LatLng position) {
        switch (colorMode) {
        case MONOCHROMATIC:
            if (polylines.isEmpty()) {
                polylines.add(createPolyline(MVCArray.newInstance()));   
            }
            polylines.get(0).getPath().insertAt(index, position);
            break;
        case POLYCHROMATIC:
            if (index == 0) {
                // Prepend a new Polyline
                MVCArray<LatLng> path = MVCArray.newInstance();
                path.push(position);
                if (!polylines.isEmpty()) {
                    path.push(polylines.get(0).getPath().get(0));
                }
                polylines.add(0, createPolyline(path));
            } else if (index == getLength()) {
                if (index == 1 && polylines.get(0).getPath().getLength() == 1) {
                    // Finish first polyline
                    polylines.get(0).getPath().push(position);
                } else {
                    // Append a new Polyline
                    MVCArray<LatLng> path = MVCArray.newInstance();
                    path.push(polylines.get(index - 2).getPath().get(1));
                    path.push(position);
                    polylines.add(index - 1, createPolyline(path));
                }
            } else {
                // Split an existing Polyline into two
                LatLng end = polylines.get(index - 1).getPath().get(1);
                polylines.get(index - 1).getPath().setAt(1, position);
                MVCArray<LatLng> path = MVCArray.newInstance();
                path.push(position);
                path.push(end);
                polylines.add(index, createPolyline(path));
            }
            break;
        }
    }
    /*public void insertAt(int index, GPSFixDTO fix) {
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
    }*/
    
    public LatLng removeAt(int index) {
        switch (colorMode) {
        case MONOCHROMATIC:
            return polylines.get(0).getPath().removeAt(index);
        case POLYCHROMATIC: //TODO Not working
            LatLng removed;
            if (index == 0) {
                // Remove the Polyline connecting the first to the second fix
                removed = polylines.get(0).getPath().get(0);
                polylines.get(0).setMap(null);
                polylines.remove(0);
            } else if (index == getLength() - 1) {
                // Remove the Polyline connecting the last two fixes
                removed = polylines.get(index - 1).getPath().get(1);
                polylines.get(index - 1).setMap(null);
                polylines.remove(index - 1);
            } else {
                // Remove the Polyline ending at fix
                removed = polylines.get(index - 1).getPath().get(1);
                LatLng start = polylines.get(index - 1).getPath().get(0);
                polylines.get(index - 1).setMap(null);
                polylines.remove(index - 1);
                // and update the following Polyline to fill the gap
                polylines.get(index - 1).getPath().setAt(0, start);
            }
            return removed;
        }
        return null;
    }
    /*public GPSFixDTO removeAt(int index) {
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
    }*/
    
    public void setAt(int index, LatLng position) {
        switch (colorMode) {
        case MONOCHROMATIC:
            polylines.get(0).getPath().setAt(index, position);
            break;
        case POLYCHROMATIC:
            if (index == 0) {
                polylines.get(0).getPath().setAt(0, position);
            } else if (index == getLength() - 1) {
                polylines.get(index - 1).getPath().setAt(1, position);
            } else {
                polylines.get(index - 1).getPath().setAt(1, position);
                polylines.get(index).getPath().setAt(0, position);
            }
        }
    }
    /*public void setAt(int index, GPSFixDTO fix) {
        fixes.set(index, fix);
        if (index == 0) {
            polylines.get(index).getPath().setAt(0, coordinateSystem.toLatLng(fix.position));
        } else if (index == fixes.size() - 1) {
            polylines.get(index - 1).getPath().setAt(1, coordinateSystem.toLatLng(fix.position));
        } else {
            polylines.get(index - 1).getPath().setAt(1, coordinateSystem.toLatLng(fix.position));
            polylines.get(index).getPath().setAt(0, coordinateSystem.toLatLng(fix.position));
        }
    }*/
    
    public void setMap(MapWidget map) {
        this.map = map;
        for (int i = 0; i < polylines.size(); i++) {
            polylines.get(i).setMap(map);
        }
    }
    
    public void clear() {
        polylines.clear();
    }
    
    public int getLength() {
        switch (colorMode) {
        case MONOCHROMATIC:
            return polylines.isEmpty() ? 0 : polylines.get(0).getPath().getLength();
        case POLYCHROMATIC:
            switch (polylines.size()) {
            case 0:
                return 0;
            case 1:
                return polylines.get(0).getPath().getLength();
            default:
                log(Integer.toString(1 + polylines.size()));
                return 1 + polylines.size();
            }
        }
        return -1;
    }
    
    private Polyline createPolyline(MVCArray<LatLng> path) {
        Polyline line = options.newPolylineInstance(path.get(0)); //TODO Get color from different point?
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