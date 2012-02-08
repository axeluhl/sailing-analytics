package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.ManeuverType;

public class RaceMapSettings {
    private boolean showDouglasPeuckerPoints = false;
    
    private final Set<ManeuverType> maneuverTypesToShow;

    private boolean showOnlySelectedCompetitors = true;
    
    private boolean autoZoomToBoats = false;
    private boolean autoZoomToBuoys = false;
    
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

    public boolean isAutoZoomToBoats() {
        return autoZoomToBoats;
    }

    public void setAutoZoomToBoats(boolean autoZoomToBoats) {
        this.autoZoomToBoats = autoZoomToBoats;
        if (autoZoomToBoats) {
            this.autoZoomToBuoys = false;
        }
    }

    public boolean isAutoZoomToBuoys() {
        return autoZoomToBuoys;
    }

    public void setAutoZoomToBuoys(boolean autoZoomToBuoys) {
        this.autoZoomToBuoys = autoZoomToBuoys;
        if (autoZoomToBuoys) {
            this.autoZoomToBoats = false;
        }
    }

}
