package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.ManeuverType;

public class RaceMapSettings {
    
    private boolean showDouglasPeuckerPoints = false;
    
    private final Set<ManeuverType> maneuverTypesToShow;

    private boolean showOnlySelectedCompetitors = false;

    private boolean showTails = true;

    private boolean showAllCompetitors = false;

    private RaceMapZoomSettings zoomSettings;

    private RaceMapHelpLinesSettings helpLinesSettings;

    private long tailLengthInMilliseconds = 100000l;

    private int maxVisibleCompetitorsCount = 50;
    
    public RaceMapSettings() {
        // empty default settings; don't show maneuvers by default
        maneuverTypesToShow = new HashSet<ManeuverType>();
        this.zoomSettings = new RaceMapZoomSettings();
        this.helpLinesSettings = new RaceMapHelpLinesSettings();
    }
    
    /**
     * @return 0 if {@link #isShowTails()} returns <code>false</code>; {@link #getTailLengthInMilliseconds()} otherwise
     */
    public long getEffectiveTailLengthInMilliseconds() {
        return isShowTails() ? getTailLengthInMilliseconds() : 0;
    }

    /**
     * The tail length as set in the dialog; feeds into {@link #getEffectiveTailLengthInMilliseconds()}, but only if
     * {@link #isShowTails()} is <code>true</code>.
     */
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

    public int getMaxVisibleCompetitorsCount() {
        return maxVisibleCompetitorsCount;
    }

    public void setMaxVisibleCompetitorsCount(int maxVisibleCompetitorsCount) {
        this.maxVisibleCompetitorsCount = maxVisibleCompetitorsCount;
    }

    public boolean isShowTails() {
        return showTails;
    }

    public void setShowTails(boolean showTails) {
        this.showTails = showTails;
    }

    public boolean isShowAllCompetitors() {
        return showAllCompetitors;
    }

    public void setShowAllCompetitors(boolean showAllCompetitors) {
        this.showAllCompetitors = showAllCompetitors;
    }

}
