package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapHelpLinesSettings.HelpLineTypes;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

public class RaceMapSettings extends AbstractSettings {
    public static final String PARAM_SHOW_MAPCONTROLS = "showMapControls";
    public static final String PARAM_SHOW_COURSE_GEOMETRY = "showCourseGeometry";
    public static final String PARAM_MAP_ORIENTATION_WIND_UP = "windUp";
    public static final String PARAM_VIEW_SHOW_STREAMLETS = "viewShowStreamlets";
    public static final String PARAM_VIEW_SHOW_STREAMLET_COLORS = "viewShowStreamletColors";
    public static final String PARAM_VIEW_SHOW_SIMULATION = "viewShowSimulation";

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
    
    private final boolean showMapControls;
    
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
        this(
                /* showMapControls */ true,
                new RaceMapHelpLinesSettings(),
                /* windUp */ false,
                /* showWindStreamletOverlay */ false,
                /* showWindStreamletColors */ false,
                /* showSimulationOverlay */ false);
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

    private RaceMapSettings(boolean showMapControls, boolean showCourseGeometry, boolean windUp, boolean showWindStreamletOverlay, boolean showWindStreamletColors, boolean showSimulationOverlay) {
        this(showMapControls, new RaceMapHelpLinesSettings(createHelpLineSettings(showCourseGeometry)), windUp, showWindStreamletOverlay, showWindStreamletColors, showSimulationOverlay);
    }
    
    private RaceMapSettings(boolean showMapControls, RaceMapHelpLinesSettings helpLineSettings, boolean windUp, boolean showWindStreamletOverlay, boolean showWindStreamletColors, boolean showSimulationOverlay) {
        this(
                new RaceMapZoomSettings(),
                helpLineSettings,
                /* transparentHoverlines as discussed with Stefan on 2015-12-08 */ false,
                /* hoverlineStrokeWeight as discussed with Stefan on 2015-12-08 */ 15,
                /* tailLengthInMilliseconds */ 100000l,
                /* windUp */ windUp,
                /* buoyZoneRadiusInMeters */ 0.0,
                /* showOnlySelectedCompetitors */ false,
                /* showSelectedCompetitorsInfo */ true,
                /* showWindStreamletColors */ showWindStreamletColors,
                /* showWindStreamletOverlay */ showWindStreamletOverlay,
                /* showSimulationOverlay */ showSimulationOverlay,
                /* showMapControls */ showMapControls,
                /* maneuverTypesToShow */ getDefaultManeuvers(),
                /* showDouglasPeuckerPoints */ false);
    }
    
    private static Set<HelpLineTypes> createHelpLineSettings(boolean showCourseGeometry) {
        final Set<HelpLineTypes> helpLineTypes = new HashSet<>();
        Util.addAll(new RaceMapHelpLinesSettings().getVisibleHelpLineTypes(), helpLineTypes);
        if (showCourseGeometry) {
            helpLineTypes.add(HelpLineTypes.COURSEGEOMETRY);
        } else {
            helpLineTypes.remove(HelpLineTypes.COURSEGEOMETRY);
        }
        return helpLineTypes;
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

    private static HashSet<ManeuverType> getDefaultManeuvers() {
        HashSet<ManeuverType> types = new HashSet<ManeuverType>();
        types.add(ManeuverType.JIBE);
        types.add(ManeuverType.TACK);
        types.add(ManeuverType.PENALTY_CIRCLE);
        return types;
    }

    public static RaceMapSettings readSettingsFromURL(boolean defaultForShowMapControls,
            boolean defaultForShowCourseGeometry, boolean defaultForMapOrientationWindUp,
            boolean defaultForViewShowStreamlets, boolean defaultForViewShowStreamletColors,
            boolean defaultForViewShowSimulation) {
        final boolean showMapControls = GwtHttpRequestUtils.getBooleanParameter(PARAM_SHOW_MAPCONTROLS, defaultForShowMapControls /* default */);
        final boolean showCourseGeometry = GwtHttpRequestUtils.getBooleanParameter(PARAM_SHOW_COURSE_GEOMETRY, defaultForShowCourseGeometry /* default */);
        final boolean windUp = GwtHttpRequestUtils.getBooleanParameter(PARAM_MAP_ORIENTATION_WIND_UP, defaultForMapOrientationWindUp /* default */);
        final boolean showWindStreamletOverlay = GwtHttpRequestUtils.getBooleanParameter(PARAM_VIEW_SHOW_STREAMLETS, defaultForViewShowStreamlets /* default */);
        final boolean showWindStreamletColors = GwtHttpRequestUtils.getBooleanParameter(PARAM_VIEW_SHOW_STREAMLET_COLORS, defaultForViewShowStreamletColors /* default */);
        final boolean showSimulationOverlay = GwtHttpRequestUtils.getBooleanParameter(PARAM_VIEW_SHOW_SIMULATION, defaultForViewShowSimulation /* default */);
        return new RaceMapSettings(showMapControls, showCourseGeometry, windUp, showWindStreamletOverlay, showWindStreamletColors, showSimulationOverlay);
    }

    public Set<ManeuverType> getManeuverTypesToShow() {
        return maneuverTypesToShow;
    }
}
