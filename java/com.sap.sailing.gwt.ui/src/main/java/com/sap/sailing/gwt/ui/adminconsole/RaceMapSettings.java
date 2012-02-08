package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.ManeuverType;

public class RaceMapSettings {
    public enum ZoomSettings{MANUAL, ZOOM_TO_BOATS, ZOOM_TO_BUOYS, ZOOM_TO_BOATS_AND_BUOYS}
    
    private boolean showDouglasPeuckerPoints = false;
    
    private final Set<ManeuverType> maneuverTypesToShow;

    private boolean showOnlySelectedCompetitors = true;
    
    private ZoomSettings zoomSetting = ZoomSettings.MANUAL;
    private boolean includeTailsToAutoZoom = false;
    
    private long tailLengthInMilliseconds = 30000l;

    public RaceMapSettings() {
        maneuverTypesToShow = new HashSet<ManeuverType>();
        maneuverTypesToShow.add(ManeuverType.TACK);
        maneuverTypesToShow.add(ManeuverType.JIBE);
        maneuverTypesToShow.add(ManeuverType.PENALTY_CIRCLE);
        maneuverTypesToShow.add(ManeuverType.MARK_PASSING);
    }

    public long getTailLengthInMilliseconds() {
        return tailLengthInMilliseconds;
    }

    public void setTailLengthInMilliseconds(long tailLengthInMilliseconds) {
        this.tailLengthInMilliseconds = tailLengthInMilliseconds;
    }

    public boolean isShowDouglasPeuckerPoints() {
        return showDouglasPeuckerPoints;
    }

    public void setShowDouglasPeuckerPoints(boolean showDouglasPeuckerPoints) {
        this.showDouglasPeuckerPoints = showDouglasPeuckerPoints;
    }

    public void showManeuverType(ManeuverType maneuverType, boolean show) {
        if (show) {
            maneuverTypesToShow.add(maneuverType);
        } else {
            maneuverTypesToShow.remove(maneuverType);
        }
    }
    
    public boolean isShowManeuverType(ManeuverType maneuverType) {
        return maneuverTypesToShow.contains(maneuverType);
    }

    public boolean isShowOnlySelectedCompetitors() {
        return showOnlySelectedCompetitors;
    }

    public void setShowOnlySelectedCompetitors(boolean showOnlySelectedCompetitors) {
        this.showOnlySelectedCompetitors = showOnlySelectedCompetitors;
    }

    public ZoomSettings getZoomSetting() {
        return zoomSetting;
    }
    
    /**
     * Sets the zoom settings. If <code>zoomSetting</code> is a setting which doesn't include the boats (e.g.
     * <code>MANUAL_ZOOM</code> or <code>ZOOM_TO_BUOYS</code>), is the <code>includeTailsToAutoZoom</code> value set to
     * <code>false</code>.
     * 
     * @param zoomSetting The new zoom settings
     */
    public void setZoomSetting(ZoomSettings zoomSetting) {
        this.zoomSetting = zoomSetting;
        if (zoomSetting == ZoomSettings.MANUAL || zoomSetting == ZoomSettings.ZOOM_TO_BUOYS) {
            setIncludeTailsToAutoZoom(false);
        }
    }

    public boolean isIncludeTailsToAutoZoom() {
        return includeTailsToAutoZoom;
    }
    
    public void setIncludeTailsToAutoZoom(boolean includeTailsToAutoZoom) {
        this.includeTailsToAutoZoom = includeTailsToAutoZoom;
    }

}
