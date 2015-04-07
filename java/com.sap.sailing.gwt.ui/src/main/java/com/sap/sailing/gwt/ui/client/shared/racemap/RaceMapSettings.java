package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapHelpLinesSettings.HelpLineTypes;

public class RaceMapSettings {

    private boolean showDouglasPeuckerPoints = false;

    private final Set<ManeuverType> maneuverTypesToShow;

    private boolean showOnlySelectedCompetitors = false;

    private RaceMapZoomSettings zoomSettings;

    private RaceMapHelpLinesSettings helpLinesSettings;

    private long tailLengthInMilliseconds = 100000l;

    private double buoyZoneRadiusInMeters = 0.0;

    private boolean showSelectedCompetitorsInfo = true;
    
    private boolean showWindStreamletOverlay = false;

    private boolean showSimulationOverlay = false;
    
    /**
     * If <code>true</code>, all map contents will be transformed to a water-only environment, rotating all directions /
     * bearings / headings so that an assumed average wind direction for the race is coming from the top of the map
     * ("wind-up display"). The implementation hinges on
     * {@link Position#getLocalCoordinates(Position, com.sap.sailing.domain.common.Bearing)} which can transform
     * positions to any other coordinate space that is translated and rotated compared to the original
     * coordinate space.
     */
    private boolean windUp = false;

    public RaceMapSettings() {
        // empty default settings; don't show maneuvers by default
        maneuverTypesToShow = new HashSet<ManeuverType>();
        this.zoomSettings = new RaceMapZoomSettings();
        this.helpLinesSettings = new RaceMapHelpLinesSettings();
        
        // FIXME remove again
        int i;
        this.windUp = true;
    }

    /**
     * @return 0 if the tails are not visible {@link #getTailLengthInMilliseconds()} otherwise
     */
    public long getEffectiveTailLengthInMilliseconds() {
        return helpLinesSettings.isVisible(HelpLineTypes.BOATTAILS) ? getTailLengthInMilliseconds() : 0;
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

    public boolean isShowWindStreamletOverlay() {
        return showWindStreamletOverlay;
    }

    public void setShowWindStreamletOverlay(boolean showWindStreamletOverlay) {
        this.showWindStreamletOverlay = showWindStreamletOverlay;
    }

    public boolean isShowSimulationOverlay() {
        return showSimulationOverlay;
    }

    public void setShowSimulationOverlay(boolean showSimulationOverlay) {
        this.showSimulationOverlay = showSimulationOverlay;
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

    public boolean isShowSelectedCompetitorsInfo() {
        return showSelectedCompetitorsInfo;
    }

    public void setShowSelectedCompetitorsInfo(boolean showSelectedCompetitorsInfo) {
        this.showSelectedCompetitorsInfo = showSelectedCompetitorsInfo;
    }

    public double getBuoyZoneRadiusInMeters() {
        return buoyZoneRadiusInMeters;
    }

    public void setBuoyZoneRadiusInMeters(double buoyZoneRadiusInMeters) {
        this.buoyZoneRadiusInMeters = buoyZoneRadiusInMeters;
    }

    public boolean isWindUp() {
        return windUp;
    }

    public void setWindUp(boolean windUp) {
        this.windUp = windUp;
    }
}
