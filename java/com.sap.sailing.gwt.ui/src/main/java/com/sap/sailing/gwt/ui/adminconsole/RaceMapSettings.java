package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.ManeuverType;

public class RaceMapSettings {
    
    private boolean showDouglasPeuckerPoints = false;
    
    private final Set<ManeuverType> maneuverTypesToShow;

    private boolean showOnlySelectedCompetitors = false;
    
    private RaceMapZoomSettings zoomSettings;
    
    private long tailLengthInMilliseconds = 30000l;

    public RaceMapSettings() {
        maneuverTypesToShow = new HashSet<ManeuverType>();
        maneuverTypesToShow.add(ManeuverType.TACK);
        maneuverTypesToShow.add(ManeuverType.JIBE);
        maneuverTypesToShow.add(ManeuverType.PENALTY_CIRCLE);
        maneuverTypesToShow.add(ManeuverType.MARK_PASSING);
        
        this.zoomSettings = new RaceMapZoomSettings();
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

}
