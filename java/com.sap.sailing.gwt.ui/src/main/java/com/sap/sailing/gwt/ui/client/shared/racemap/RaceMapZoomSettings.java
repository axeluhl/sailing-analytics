package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.maps.client.base.LatLngBounds;
import com.sap.sse.common.Util;

/**
 * @author Lennart Hensler (D054527)
 */
public class RaceMapZoomSettings {
    
    /**
     * The auto-zoom types for a {@link RaceMap}.<br />
     * Each zoom type has a {@link LatLngBoundsCalculator}, which calculates the new bounds for a map.
     */
    public enum ZoomTypes {
        NONE(null), WINDSENSORS(new RaceMap.WindSensorsBoundsCalculator()), BOATS(new RaceMap.BoatsBoundsCalculator()), 
        TAILS(new RaceMap.TailsBoundsCalculator()), BUOYS(new RaceMap.CourseMarksBoundsCalculator());

        private LatLngBoundsCalculator calculator;

        private ZoomTypes(LatLngBoundsCalculator calculator) {
            this.calculator = calculator;
        }

        public LatLngBounds calculateNewBounds(RaceMap forMap) {
            return calculator == null ? null : calculator.calculateNewBounds(forMap);
        }
    };

    private Iterable<ZoomTypes> typesToConsiderOnZoom;
    private boolean zoomToSelectedCompetitors;

    /**
     * Creates new RaceMapZoomSettings with the {@link ZoomTypes} <code>BOATS</code> and <code>TAILS</code>.<br />
     * The attribute <code>zoomToSelectedCompetitors</code> will be <code>false</code>.
     */
    public RaceMapZoomSettings() {
        final List<ZoomTypes> myTypesToConsiderOnZoom = new ArrayList<>();
        typesToConsiderOnZoom = myTypesToConsiderOnZoom;
        // Other zoom types such as BOATS, TAILS or WINDSENSORS are not currently used as default zoom types.
        myTypesToConsiderOnZoom.add(ZoomTypes.BUOYS);
        zoomToSelectedCompetitors = false;
    }
    
    public RaceMapZoomSettings(Iterable<ZoomTypes> typesToConsiderOnZoom, boolean zoomToSelected) {
        this.typesToConsiderOnZoom = typesToConsiderOnZoom;
        this.zoomToSelectedCompetitors = zoomToSelected;
    }

    public LatLngBounds getNewBounds(RaceMap forMap) {
        LatLngBounds newBounds = null;
        if (typesToConsiderOnZoom != null) {
            for (ZoomTypes type : typesToConsiderOnZoom) {
                //Calculate the new bounds and extend the result
                LatLngBounds calculatedBounds = type.calculateNewBounds(forMap);
                if (calculatedBounds != null) {
                    if (newBounds == null) {
                        newBounds = calculatedBounds;
                    } else {
                        newBounds.extend(calculatedBounds.getNorthEast());
                        newBounds.extend(calculatedBounds.getSouthWest());
                    }
                }
            }
        }
        return newBounds;
    }
    
    public Iterable<ZoomTypes> getTypesToConsiderOnZoom() {
        return typesToConsiderOnZoom;
    }
    
    public void setTypesToConsiderOnZoom(Iterable<ZoomTypes> typesToConsiderOnZoom) {
        List<ZoomTypes> newTypesToConsiderOnZoom = new ArrayList<>();
        Util.addAll(typesToConsiderOnZoom, newTypesToConsiderOnZoom);
        this.typesToConsiderOnZoom = newTypesToConsiderOnZoom;
    }
    
    public void setZoomToSelectedCompetitors(boolean zoomToSelectedCompetitors) {
        this.zoomToSelectedCompetitors = zoomToSelectedCompetitors;
    }
    
    public boolean isZoomToSelectedCompetitors() {
        return zoomToSelectedCompetitors;
    }

    public boolean containsZoomType(ZoomTypes zoomType) {
        return Util.contains(typesToConsiderOnZoom, zoomType);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((typesToConsiderOnZoom == null) ? 0 : typesToConsiderOnZoom.hashCode());
        result = prime * result + (zoomToSelectedCompetitors ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RaceMapZoomSettings other = (RaceMapZoomSettings) obj;
        if (typesToConsiderOnZoom == null) {
            if (other.typesToConsiderOnZoom != null)
                return false;
        } else if (!typesToConsiderOnZoom.equals(other.typesToConsiderOnZoom))
            return false;
        if (zoomToSelectedCompetitors != other.zoomToSelectedCompetitors)
            return false;
        return true;
    }
}