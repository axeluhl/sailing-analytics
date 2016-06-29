package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapHelpLinesSettings.HelpLineTypes;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

public class RaceMapSettings extends AbstractSettings {
    public static final String PARAM_SHOW_MAPCONTROLS = "showMapControls";
    public static final String PARAM_SHOW_COURSE_GEOMETRY = "showCourseGeometry";
    public static final String PARAM_MAP_ORIENTATION_WIND_UP = "windUp";

    private final boolean showDouglasPeuckerPoints;

    private final Set<ManeuverType> maneuverTypesToShow;

    private final boolean showOnlySelectedCompetitors;

    private final RaceMapZoomSettings zoomSettings;

    private final RaceMapHelpLinesSettings helpLinesSettings;
    
    private final boolean transparentHoverlines;
    
    private final int hoverlineStrokeWeight;

    private final long tailLengthInMilliseconds;

    private final double buoyZoneRadiusInMeters;

    private final boolean showSelectedCompetitorsInfo;
    
    private final boolean showWindStreamletColors;
    
    private final boolean showWindStreamletOverlay;

    private final boolean showSimulationOverlay;
    
    private boolean showMapControls;
    
    /**
     * If <code>true</code>, all map contents will be transformed to a water-only environment, rotating all directions /
     * bearings / headings so that an assumed average wind direction for the race is coming from the top of the map
     * ("wind-up display"). The implementation hinges on
     * {@link Position#getLocalCoordinates(Position, com.sap.sailing.domain.common.Bearing)} which can transform
     * positions to any other coordinate space that is translated and rotated compared to the original
     * coordinate space.
     */
    private final boolean windUp;

    public RaceMapSettings() {
        this.maneuverTypesToShow = getDefaultManeuvers();
        this.zoomSettings = new RaceMapZoomSettings();
        this.helpLinesSettings = new RaceMapHelpLinesSettings();
        
        this.showDouglasPeuckerPoints = false;
        this.showOnlySelectedCompetitors = false;
        this.transparentHoverlines = false; // as discussed with Stefan on 2015-12-08
        this.hoverlineStrokeWeight = 15; // as discussed with Stefan on 2015-12-08
        this.tailLengthInMilliseconds = 100000l;
        this.buoyZoneRadiusInMeters = 0.0;
        this.showSelectedCompetitorsInfo = true;
        this.showWindStreamletColors = false;
        this.showWindStreamletOverlay = false;
        this.showSimulationOverlay = false;
        this.showMapControls = true;
        this.windUp = false;
    }

    public RaceMapSettings(RaceMapZoomSettings zoomSettings, RaceMapHelpLinesSettings helpLinesSettings,
            boolean transparentHoverlines, int hoverlineStrokeWeight, long tailLengthInMilliseconds, boolean windUp,
            double buoyZoneRadiusInMeters, boolean showOnlySelectedCompetitors, boolean showSelectedCompetitorsInfo,
            boolean showWindStreamletColors, boolean showWindStreamletOverlay, boolean showSimulationOverlay,
            boolean showMapControls, Set<ManeuverType> maneuverTypesToShow, boolean showDouglasPeuckerPoints) {
        this.zoomSettings = zoomSettings;
        this.helpLinesSettings = helpLinesSettings;
        this.transparentHoverlines = transparentHoverlines;
        this.hoverlineStrokeWeight = hoverlineStrokeWeight;
        this.tailLengthInMilliseconds = tailLengthInMilliseconds;
        this.windUp = windUp;
        this.buoyZoneRadiusInMeters = buoyZoneRadiusInMeters;
        this.showOnlySelectedCompetitors = showOnlySelectedCompetitors;
        this.showSelectedCompetitorsInfo = showSelectedCompetitorsInfo;
        this.showWindStreamletColors = showWindStreamletColors;
        this.showWindStreamletOverlay = showWindStreamletOverlay;
        this.showSimulationOverlay = showSimulationOverlay;
        this.showMapControls = showMapControls;
        this.maneuverTypesToShow = maneuverTypesToShow;
        this.showDouglasPeuckerPoints = showDouglasPeuckerPoints;
    }

    public RaceMapSettings(boolean showMapCcontrol) {
        this();
        this.showMapControls = showMapCcontrol;
    }

    /**
     * copy constructor that produces a new settings object that equals the one passed as argument
     */
    public RaceMapSettings(RaceMapSettings settings) {
        this(settings, new RaceMapZoomSettings(settings.zoomSettings.getTypesToConsiderOnZoom(), settings.zoomSettings.isZoomToSelectedCompetitors()));
    }

    /**
     * copy constructor that produces a new settings object that equals the one passed as argument but takes the zoom settings from the second parameter
     */
    public RaceMapSettings(RaceMapSettings settings, RaceMapZoomSettings zoomSettings) {
        this.buoyZoneRadiusInMeters = settings.buoyZoneRadiusInMeters;
        this.helpLinesSettings = new RaceMapHelpLinesSettings(settings.getHelpLinesSettings().getVisibleHelpLineTypes());
        this.transparentHoverlines = settings.transparentHoverlines;
        this.hoverlineStrokeWeight = settings.hoverlineStrokeWeight;
        this.maneuverTypesToShow = settings.maneuverTypesToShow;
        this.showDouglasPeuckerPoints = settings.showDouglasPeuckerPoints;
        this.showOnlySelectedCompetitors = settings.showOnlySelectedCompetitors;
        this.showSelectedCompetitorsInfo = settings.showSelectedCompetitorsInfo;
        this.showSimulationOverlay = settings.showSimulationOverlay;
        this.showWindStreamletOverlay = settings.showWindStreamletOverlay;
        this.showWindStreamletColors = settings.showWindStreamletColors;
        this.showMapControls = settings.showMapControls;
        this.tailLengthInMilliseconds = settings.tailLengthInMilliseconds;
        this.windUp = settings.windUp;
        this.zoomSettings = zoomSettings;
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

    public boolean isShowDouglasPeuckerPoints() {
        return showDouglasPeuckerPoints;
    }
    
    public boolean isShowWindStreamletOverlay() {
        return showWindStreamletOverlay;
    }

    public boolean isShowWindStreamletColors() {
        return showWindStreamletColors;
    }

    public boolean isShowSimulationOverlay() {
        return showSimulationOverlay;
    }

    public boolean isShowManeuverType(ManeuverType maneuverType) {
        return maneuverTypesToShow.contains(maneuverType);
    }

    public boolean isShowOnlySelectedCompetitors() {
        return showOnlySelectedCompetitors;
    }

    public RaceMapZoomSettings getZoomSettings() {
        return zoomSettings;
    }

    public RaceMapHelpLinesSettings getHelpLinesSettings() {
        return helpLinesSettings;
    }

    public boolean getTransparentHoverlines() {
        return this.transparentHoverlines;
    }
    
    public int getHoverlineStrokeWeight() {
        return this.hoverlineStrokeWeight;
    }
    
    public boolean isShowSelectedCompetitorsInfo() {
        return showSelectedCompetitorsInfo;
    }

    public double getBuoyZoneRadiusInMeters() {
        return buoyZoneRadiusInMeters;
    }

    public boolean isWindUp() {
        return windUp;
    }

    public boolean isShowMapControls() {
        return showMapControls;
    }

    private HashSet<ManeuverType> getDefaultManeuvers() {
        HashSet<ManeuverType> types = new HashSet<ManeuverType>();
        types.add(ManeuverType.JIBE);
        types.add(ManeuverType.TACK);
        types.add(ManeuverType.PENALTY_CIRCLE);
        return types;
    }

    public static RaceMapSettings readSettingsFromURL() {
        final boolean showMapControls = GwtHttpRequestUtils.getBooleanParameter(PARAM_SHOW_MAPCONTROLS, true /* default */);
        return new RaceMapSettings(showMapControls);
    }

    public Set<ManeuverType> getManeuverTypesToShow() {
        return maneuverTypesToShow;
    }
}
