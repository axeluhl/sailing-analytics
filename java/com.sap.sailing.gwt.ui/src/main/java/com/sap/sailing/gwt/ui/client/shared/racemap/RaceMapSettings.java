package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.gwt.common.settings.DistanceSetting;
import com.sap.sailing.gwt.common.settings.converter.ManeuverTypeStringToEnumConverter;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapHelpLinesSettings.HelpLineTypes;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.BooleanSetting;
import com.sap.sse.common.settings.generic.EnumSetSetting;
import com.sap.sse.common.settings.generic.IntegerSetting;
import com.sap.sse.common.settings.generic.LongSetting;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

public class RaceMapSettings extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = 6283369783437892096L;
    
    public static final String PARAM_SHOW_MAPCONTROLS = "showMapControls";
    public static final String PARAM_SHOW_COURSE_GEOMETRY = "showCourseGeometry";
    public static final String PARAM_MAP_ORIENTATION_WIND_UP = "windUp";
    public static final String PARAM_VIEW_SHOW_STREAMLETS = "viewShowStreamlets";
    public static final String PARAM_VIEW_SHOW_STREAMLET_COLORS = "viewShowStreamletColors";
    public static final String PARAM_VIEW_SHOW_SIMULATION = "viewShowSimulation";
    public static final String PARAM_BUOY_ZONE_RADIUS_IN_METERS = "buoyZoneRadiusInMeters";

    public static final Distance DEFAULT_BUOY_ZONE_RADIUS = new MeterDistance(15);

    private BooleanSetting showDouglasPeuckerPoints;

    private EnumSetSetting<ManeuverType> maneuverTypesToShow;

    private BooleanSetting showOnlySelectedCompetitors;

    private RaceMapZoomSettings zoomSettings;

    private RaceMapHelpLinesSettings helpLinesSettings;
    
    private BooleanSetting transparentHoverlines;
    
    private IntegerSetting hoverlineStrokeWeight;

    private LongSetting tailLengthInMilliseconds;

    private DistanceSetting buoyZoneRadius;

    private BooleanSetting showSelectedCompetitorsInfo;
    
    private BooleanSetting showWindStreamletColors;
    
    private BooleanSetting showWindStreamletOverlay;

    private BooleanSetting showSimulationOverlay;
    
    private BooleanSetting showMapControls;
    
    /**
     * If <code>true</code>, all map contents will be transformed to a water-only environment, rotating all directions /
     * bearings / headings so that an assumed average wind direction for the race is coming from the top of the map
     * ("wind-up display"). The implementation hinges on
     * {@link Position#getLocalCoordinates(Position, com.sap.sailing.domain.common.Bearing)} which can transform
     * positions to any other coordinate space that is translated and rotated compared to the original
     * coordinate space.
     */
    private BooleanSetting windUp;
    
    @Override
    protected void addChildSettings() {
        showMapControls = new BooleanSetting("showMapControls", this, true);
        helpLinesSettings = new RaceMapHelpLinesSettings();
        windUp = new BooleanSetting("windUp", this, false);
        buoyZoneRadius = new DistanceSetting("buoyZoneRadius", this, DEFAULT_BUOY_ZONE_RADIUS);
        showWindStreamletOverlay = new BooleanSetting("showWindStreamletOverlay", this, false);
        showWindStreamletColors = new BooleanSetting("showWindStreamletColors", this, false);
        showSimulationOverlay = new BooleanSetting("showSimulationOverlay", this, false);
        zoomSettings = new RaceMapZoomSettings();
        transparentHoverlines = new BooleanSetting("transparentHoverlines", this, false);
        hoverlineStrokeWeight = new IntegerSetting("hoverlineStrokeWeight", this, 15);
        tailLengthInMilliseconds = new LongSetting("tailLengthInMilliseconds", this, 100000l);
        showOnlySelectedCompetitors = new BooleanSetting("showOnlySelectedCompetitors", this, false);
        showSelectedCompetitorsInfo = new BooleanSetting("showSelectedCompetitorsInfo", this, true);
        maneuverTypesToShow = new EnumSetSetting<>("maneuverTypesToShow", this, getDefaultManeuvers(), new ManeuverTypeStringToEnumConverter());
        showDouglasPeuckerPoints = new BooleanSetting("showDouglasPeuckerPoints", this, false);
    }

    public RaceMapSettings() {
        this(
                /* showMapControls */ true,
                new RaceMapHelpLinesSettings(),
                /* windUp */ false,
                /* buoyZoneRadius */ DEFAULT_BUOY_ZONE_RADIUS,
                /* showWindStreamletOverlay */ false,
                /* showWindStreamletColors */ false,
                /* showSimulationOverlay */ false);
    }

    public RaceMapSettings(RaceMapZoomSettings zoomSettings, RaceMapHelpLinesSettings helpLinesSettings,
            boolean transparentHoverlines, int hoverlineStrokeWeight, long tailLengthInMilliseconds, boolean windUp,
            Distance buoyZoneRadius, boolean showOnlySelectedCompetitors, boolean showSelectedCompetitorsInfo,
            boolean showWindStreamletColors, boolean showWindStreamletOverlay, boolean showSimulationOverlay,
            boolean showMapControls, Collection<ManeuverType> maneuverTypesToShow, boolean showDouglasPeuckerPoints) {
        this.zoomSettings = zoomSettings;
        this.helpLinesSettings = helpLinesSettings;
        this.transparentHoverlines.setValue(transparentHoverlines);
        this.hoverlineStrokeWeight.setValue(hoverlineStrokeWeight);
        this.tailLengthInMilliseconds.setValue(tailLengthInMilliseconds);
        this.windUp.setValue(windUp);
        this.buoyZoneRadius.setValue(buoyZoneRadius);
        this.showOnlySelectedCompetitors.setValue(showOnlySelectedCompetitors);
        this.showSelectedCompetitorsInfo.setValue(showSelectedCompetitorsInfo);
        this.showWindStreamletColors.setValue(showWindStreamletColors);
        this.showWindStreamletOverlay.setValue(showWindStreamletOverlay);
        this.showSimulationOverlay.setValue(showSimulationOverlay);
        this.showMapControls.setValue(showMapControls);
        this.maneuverTypesToShow.setValues(maneuverTypesToShow);
        this.showDouglasPeuckerPoints.setValue(showDouglasPeuckerPoints);
    }

    private RaceMapSettings(boolean showMapControls, boolean showCourseGeometry, boolean windUp, Distance buoyZoneRadius, boolean showWindStreamletOverlay, boolean showWindStreamletColors, boolean showSimulationOverlay) {
        this(showMapControls, new RaceMapHelpLinesSettings(createHelpLineSettings(showCourseGeometry)), windUp, buoyZoneRadius, showWindStreamletOverlay, showWindStreamletColors, showSimulationOverlay);
    }

    private RaceMapSettings(boolean showMapControls, RaceMapHelpLinesSettings helpLineSettings, boolean windUp, Distance buoyZoneRadius, boolean showWindStreamletOverlay, boolean showWindStreamletColors, boolean showSimulationOverlay) {
        this(
                new RaceMapZoomSettings(),
                helpLineSettings,
                /* transparentHoverlines as discussed with Stefan on 2015-12-08 */ false,
                /* hoverlineStrokeWeight as discussed with Stefan on 2015-12-08 */ 15,
                /* tailLengthInMilliseconds */ 100000l,
                /* windUp */ windUp,
                /* buoyZoneRadius */ buoyZoneRadius,
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
        this.buoyZoneRadius.setValue(settings.buoyZoneRadius.getValue());
        this.helpLinesSettings = new RaceMapHelpLinesSettings(settings.getHelpLinesSettings().getVisibleHelpLineTypes());
        this.transparentHoverlines.setValue(settings.transparentHoverlines.getValue());
        this.hoverlineStrokeWeight.setValue(settings.hoverlineStrokeWeight.getValue());
        this.maneuverTypesToShow.setValues(settings.maneuverTypesToShow.getValues());
        this.showDouglasPeuckerPoints.setValue(settings.showDouglasPeuckerPoints.getValue());
        this.showOnlySelectedCompetitors.setValue(settings.showOnlySelectedCompetitors.getValue());
        this.showSelectedCompetitorsInfo.setValue(settings.showSelectedCompetitorsInfo.getValue());
        this.showSimulationOverlay.setValue(settings.showSimulationOverlay.getValue());
        this.showWindStreamletOverlay.setValue(settings.showWindStreamletOverlay.getValue());
        this.showWindStreamletColors.setValue(settings.showWindStreamletColors.getValue());
        this.showMapControls.setValue(settings.showMapControls.getValue());
        this.tailLengthInMilliseconds.setValue(settings.tailLengthInMilliseconds.getValue());
        this.windUp.setValue(settings.windUp.getValue());
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
        return tailLengthInMilliseconds.getValue();
    }

    public boolean isShowDouglasPeuckerPoints() {
        return showDouglasPeuckerPoints.getValue();
    }
    
    public boolean isShowWindStreamletOverlay() {
        return showWindStreamletOverlay.getValue();
    }

    public boolean isShowWindStreamletColors() {
        return showWindStreamletColors.getValue();
    }

    public boolean isShowSimulationOverlay() {
        return showSimulationOverlay.getValue();
    }

    public boolean isShowManeuverType(ManeuverType maneuverType) {
        return Util.contains(maneuverTypesToShow.getValues(), maneuverType);
    }

    public boolean isShowOnlySelectedCompetitors() {
        return showOnlySelectedCompetitors.getValue();
    }

    public RaceMapZoomSettings getZoomSettings() {
        return zoomSettings;
    }

    public RaceMapHelpLinesSettings getHelpLinesSettings() {
        return helpLinesSettings;
    }

    public boolean getTransparentHoverlines() {
        return this.transparentHoverlines.getValue();
    }
    
    public int getHoverlineStrokeWeight() {
        return this.hoverlineStrokeWeight.getValue();
    }
    
    public boolean isShowSelectedCompetitorsInfo() {
        return showSelectedCompetitorsInfo.getValue();
    }

    public Distance getBuoyZoneRadius() {
        return buoyZoneRadius.getValue();
    }

    public boolean isWindUp() {
        return windUp.getValue();
    }

    public boolean isShowMapControls() {
        return showMapControls.getValue();
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
        final double buoyZoneRadiusInMeters = GwtHttpRequestUtils.getDoubleParameter(PARAM_BUOY_ZONE_RADIUS_IN_METERS, DEFAULT_BUOY_ZONE_RADIUS.getMeters() /* default */);
        return new RaceMapSettings(showMapControls, showCourseGeometry, windUp, new MeterDistance(buoyZoneRadiusInMeters), showWindStreamletOverlay, showWindStreamletColors, showSimulationOverlay);
    }

    public Set<ManeuverType> getManeuverTypesToShow() {
        return Util.createSet(maneuverTypesToShow.getValues());
    }
}
