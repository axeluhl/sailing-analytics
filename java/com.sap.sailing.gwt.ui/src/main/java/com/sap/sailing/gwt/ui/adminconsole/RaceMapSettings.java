package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.ManeuverType;

public class RaceMapSettings {
    private boolean showDouglasPeuckerPoints = false;
    
    private final Set<ManeuverType> maneuverTypesToShow;

    private boolean showOnlySelectedCompetitors = true;
    
    private long tailLengthInMilliseconds = 30000l;

    public RaceMapSettings() {
        maneuverTypesToShow = new HashSet<ManeuverType>();
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

}
