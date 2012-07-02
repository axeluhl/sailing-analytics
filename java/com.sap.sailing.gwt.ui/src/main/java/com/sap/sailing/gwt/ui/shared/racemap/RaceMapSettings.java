package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.ManeuverType;

public class RaceMapSettings {
    
    private boolean showDouglasPeuckerPoints = false;
    
    private final Set<ManeuverType> maneuverTypesToShow;

    private boolean showOnlySelectedCompetitors = false;
    
    private RaceMapZoomSettings zoomSettings;

    private RaceMapHelpLinesSettings helpLinesSettings;

    private long tailLengthInMilliseconds = 100000l;

    public RaceMapSettings() {
        // empty default settings; don't show maneuvers by default
        maneuverTypesToShow = new HashSet<ManeuverType>();
        this.zoomSettings = new RaceMapZoomSettings();
        this.helpLinesSettings = new RaceMapHelpLinesSettings();
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

    public RaceMapZoomSettings getZoomSettings() {
        return zoomSettings;
    }
    
    public void setZoomSettings(RaceMapZoomSettings zoomSettings) {
        this.zoomSettings = zoomSettings;
    }

    public RaceMapHelpLinesSettings getHelpLinesSettings() {
        return helpLinesSettings;
    }

    public void setHelpLinesSettings(RaceMapHelpLinesSettings helpLinesSettings) {
        this.helpLinesSettings = helpLinesSettings;
    }

}
