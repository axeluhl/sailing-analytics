package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.security.SecuredDomainType.TrackedRaceActions;
import com.sap.sailing.gwt.settings.client.settingtypes.DistanceSetting;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapHelpLinesSettings.HelpLineTypes;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.BooleanSetting;
import com.sap.sse.common.settings.generic.DoubleSetting;
import com.sap.sse.common.settings.generic.EnumSetSetting;
import com.sap.sse.common.settings.generic.IntegerSetting;
import com.sap.sse.common.settings.generic.LongSetting;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;
import com.sap.sse.security.ui.client.premium.PaywallResolver;
import com.sap.sse.security.ui.client.premium.SecuredDTOProxy;
import com.sap.sse.security.ui.client.premium.settings.SecuredBooleanSetting;

public class RaceMapSettings extends AbstractGenericSerializableSettings {

    private static final long serialVersionUID = 6283369783437892096L;
    
    private static final String SHOW_MANEUVER_LOSS_VISUALIZATION = "showManeuverLossVisualization";
    private static final String START_COUNT_DOWN_FONT_SIZE_SCALING = "startCountDownFontSizeScaling";
    private static final String SHOW_ESTIMATED_DURATION = "showEstimatedDuration";
    private static final String SHOW_DOUGLAS_PEUCKER_POINTS = "showDouglasPeuckerPoints";
    private static final String MANEUVER_TYPES_TO_SHOW = "maneuverTypesToShow";
    private static final String SHOW_SELECTED_COMPETITORS_INFO = "showSelectedCompetitorsInfo";
    private static final String SHOW_ONLY_SELECTED_COMPETITORS = "showOnlySelectedCompetitors";
    public static final String TAIL_LENGTH_IN_MILLISECONDS = "tailLengthInMilliseconds";
    private static final String HOVERLINE_STROKE_WEIGHT = "hoverlineStrokeWeight";
    private static final String TRANSPARENT_HOVERLINES = "transparentHoverlines";
    private static final String HELP_LINES_SETTINGS = "helpLinesSettings";
    private static final String ZOOM_SETTINGS = "zoomSettings";
    private static final String PARAM_SHOW_SATELLITE_LAYER = "showSatelliteLayer";
    public static final String PARAM_SHOW_MAPCONTROLS = "showMapControls";
    public static final String PARAM_SHOW_COURSE_GEOMETRY = "showCourseGeometry";
    public static final String PARAM_MAP_ORIENTATION_WIND_UP = "windUp";
    private static final String PARAM_VIEW_SHOW_STREAMLETS = "viewShowStreamlets";
    private static final String PARAM_VIEW_SHOW_STREAMLET_COLORS = "viewShowStreamletColors";
    private static final String PARAM_VIEW_SHOW_SIMULATION = "viewShowSimulation";
    private static final String PARAM_BUOY_ZONE_RADIUS_IN_METERS = "buoyZoneRadiusInMeters";
    private static final String PARAM_SHOW_WIND_LADDER = "showWindLadder";
    public static final String PARAM_TAIL_LENGTH_IN_MILLISECONDS = "tailLengthInMilliseconds";
    
    public static final Distance DEFAULT_BUOY_ZONE_RADIUS = new MeterDistance(15);
    
    private static final long DEFAULT_TAIL_LENGTH_IN_MILLISECONDS = Duration.ONE_SECOND.times(100l).asMillis();

    private BooleanSetting showSatelliteLayer;

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
    
    private SecuredBooleanSetting showWindStreamletColors;
    
    private SecuredBooleanSetting showWindStreamletOverlay;

    private BooleanSetting showSimulationOverlay;
    
    private BooleanSetting showMapControls;
    
    private BooleanSetting showManeuverLossVisualization;
    
    /**
     * If <code>true</code>, all map contents will be transformed to a water-only environment, rotating all directions /
     * bearings / headings so that an assumed average wind direction for the race is coming from the top of the map
     * ("wind-up display"). The implementation hinges on
     * {@link Position#getLocalCoordinates(Position, com.sap.sailing.domain.common.Bearing)} which can transform
     * positions to any other coordinate space that is translated and rotated compared to the original
     * coordinate space.
     */
    private BooleanSetting windUp;
    
    private BooleanSetting showEstimatedDuration;

    private BooleanSetting showWindLadder;

    /**
     * The factor by which the start count down shown at one side of the start line shall be scaled compared to the
     * other small info overlays such as the course geometry. Defaults to 1.0.
     */
    private DoubleSetting startCountDownFontSizeScaling;

    private SecuredDTOProxy securedDTO;

    private PaywallResolver paywallResolver;
    
    @Override
    protected void addChildSettings() {
        showSatelliteLayer = new BooleanSetting(PARAM_SHOW_SATELLITE_LAYER, this, false);
        showMapControls = new BooleanSetting(PARAM_SHOW_MAPCONTROLS, this, true);
        helpLinesSettings = new RaceMapHelpLinesSettings(HELP_LINES_SETTINGS, this);
        windUp = new BooleanSetting(PARAM_MAP_ORIENTATION_WIND_UP, this, false);
        buoyZoneRadius = new DistanceSetting(PARAM_BUOY_ZONE_RADIUS_IN_METERS, this, DEFAULT_BUOY_ZONE_RADIUS);
        showWindStreamletOverlay = new SecuredBooleanSetting(PARAM_VIEW_SHOW_STREAMLETS, this, false, paywallResolver, TrackedRaceActions.VIEWSTREAMLETS, getSecuredDTO());
        showWindStreamletColors = new SecuredBooleanSetting(PARAM_VIEW_SHOW_STREAMLET_COLORS, this, false, paywallResolver, TrackedRaceActions.VIEWSTREAMLETS, getSecuredDTO());
        showSimulationOverlay = new BooleanSetting(PARAM_VIEW_SHOW_SIMULATION, this, false);
        showWindLadder = new BooleanSetting(PARAM_SHOW_WIND_LADDER, this, false);
        zoomSettings = new RaceMapZoomSettings(ZOOM_SETTINGS, this);
        transparentHoverlines = new BooleanSetting(TRANSPARENT_HOVERLINES, this, false);
        hoverlineStrokeWeight = new IntegerSetting(HOVERLINE_STROKE_WEIGHT, this, 15);
        tailLengthInMilliseconds = new LongSetting(TAIL_LENGTH_IN_MILLISECONDS, this, DEFAULT_TAIL_LENGTH_IN_MILLISECONDS);
        showOnlySelectedCompetitors = new BooleanSetting(SHOW_ONLY_SELECTED_COMPETITORS, this, false);
        showSelectedCompetitorsInfo = new BooleanSetting(SHOW_SELECTED_COMPETITORS_INFO, this, true);
        maneuverTypesToShow = new EnumSetSetting<>(MANEUVER_TYPES_TO_SHOW, this, getDefaultManeuvers(), ManeuverType::valueOf);
        showDouglasPeuckerPoints = new BooleanSetting(SHOW_DOUGLAS_PEUCKER_POINTS, this, false);
        showEstimatedDuration = new BooleanSetting(SHOW_ESTIMATED_DURATION, this, false); 
        startCountDownFontSizeScaling = new DoubleSetting(START_COUNT_DOWN_FONT_SIZE_SCALING, this, 1.0);
        showManeuverLossVisualization = new BooleanSetting(SHOW_MANEUVER_LOSS_VISUALIZATION, this, false);
    }
    
    public RaceMapSettings() {
    }

    public RaceMapSettings(RaceMapZoomSettings zoomSettings, RaceMapHelpLinesSettings helpLinesSettings,
            Boolean transparentHoverlines, Integer hoverlineStrokeWeight, Long tailLengthInMilliseconds, Boolean windUp,
            Distance buoyZoneRadius, Boolean showOnlySelectedCompetitors, Boolean showSelectedCompetitorsInfo,
            Boolean showWindStreamletColors, Boolean showWindStreamletOverlay, Boolean showSimulationOverlay,
            Boolean showMapControls, Collection<ManeuverType> maneuverTypesToShow, Boolean showDouglasPeuckerPoints,
            Boolean showEstimatedDuration, Double startCountDownFontSizeScaling, Boolean showManeuverLossVisualization,
            Boolean showSatelliteLayer, Boolean showWindLadder, PaywallResolver paywallResolver, SecuredDTOProxy securedDTO) {
        this.zoomSettings.init(zoomSettings);
        this.helpLinesSettings.init(helpLinesSettings);
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
        this.showEstimatedDuration.setValue(showEstimatedDuration);
        this.startCountDownFontSizeScaling.setValue(startCountDownFontSizeScaling);
        this.showManeuverLossVisualization.setValue(showManeuverLossVisualization);
        this.showSatelliteLayer.setValue(showSatelliteLayer);
        this.showWindLadder.setValue(showWindLadder);
        this.paywallResolver = paywallResolver;
        this.securedDTO = securedDTO;
    }
    
    public static RaceMapSettings getDefaultWithShowMapControls(boolean showMapControlls) {
        RaceMapSettings raceMapSetting = new RaceMapSettings();
        raceMapSetting.showMapControls.setValue(showMapControlls);
        return raceMapSetting;
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
    
    public boolean isBuoyZoneRadiusDefaultValue() {
        return buoyZoneRadius.isDefaultValue();
    }
    
    public double getStartCountDownFontSizeScaling() {
        return startCountDownFontSizeScaling.getValue();
    }
    
    public boolean isShowManeuverLossVisualization() {
        return showManeuverLossVisualization.getValue();
    }
    
    public boolean isWindUp() {
        return windUp.getValue();
    }

    public boolean isShowMapControls() {
        return showMapControls.getValue();
    }

    public boolean isShowSatelliteLayer() {
        return showSatelliteLayer.getValue();
    }

    public boolean isShowWindLadder() {
        return showWindLadder.getValue();
    }

    public static HashSet<ManeuverType> getDefaultManeuvers() {
        HashSet<ManeuverType> types = new HashSet<ManeuverType>();
        types.add(ManeuverType.JIBE);
        types.add(ManeuverType.TACK);
        types.add(ManeuverType.PENALTY_CIRCLE);
        return types;
    }

    public static RaceMapSettings readSettingsFromURL(boolean defaultForShowMapControls,
            boolean defaultForShowCourseGeometry, boolean defaultForMapOrientationWindUp,
            boolean defaultForViewShowStreamlets, boolean defaultForViewShowStreamletColors,
            boolean defaultForViewShowSimulation, Long defaultForTailLengthInMilliseconds, PaywallResolver paywallResolver, SecuredDTOProxy securedDTO) {
        final boolean showSatelliteLayer = GwtHttpRequestUtils.getBooleanParameter(PARAM_SHOW_SATELLITE_LAYER, false /* default */);
        final boolean showMapControls = GwtHttpRequestUtils.getBooleanParameter(PARAM_SHOW_MAPCONTROLS, defaultForShowMapControls /* default */);
        final boolean showCourseGeometry = GwtHttpRequestUtils.getBooleanParameter(PARAM_SHOW_COURSE_GEOMETRY, defaultForShowCourseGeometry /* default */);
        final RaceMapHelpLinesSettings raceMapHelpLinesSettings = new RaceMapHelpLinesSettings(createHelpLineSettings(showCourseGeometry));
        final boolean windUp = GwtHttpRequestUtils.getBooleanParameter(PARAM_MAP_ORIENTATION_WIND_UP, defaultForMapOrientationWindUp /* default */);
        final boolean showWindStreamletOverlay = GwtHttpRequestUtils.getBooleanParameter(PARAM_VIEW_SHOW_STREAMLETS, defaultForViewShowStreamlets /* default */);
        final boolean showWindStreamletColors = GwtHttpRequestUtils.getBooleanParameter(PARAM_VIEW_SHOW_STREAMLET_COLORS, defaultForViewShowStreamletColors /* default */);
        final boolean showSimulationOverlay = GwtHttpRequestUtils.getBooleanParameter(PARAM_VIEW_SHOW_SIMULATION, defaultForViewShowSimulation /* default */);
        final Long tailLengthInMilliseconds = GwtHttpRequestUtils.getLongParameter(PARAM_TAIL_LENGTH_IN_MILLISECONDS, defaultForTailLengthInMilliseconds);
        final double buoyZoneRadiusInMeters = GwtHttpRequestUtils.getDoubleParameter(PARAM_BUOY_ZONE_RADIUS_IN_METERS,
                DEFAULT_BUOY_ZONE_RADIUS.getMeters() /* default */);
        final MeterDistance meterDistance = new MeterDistance(buoyZoneRadiusInMeters);
        return new RaceMapSettingsBuilder().withShowSatelliteLayer(showSatelliteLayer).withShowMapControls(showMapControls)
                .withHelpLinesSettings(raceMapHelpLinesSettings).withWindUp(windUp).withBuoyZoneRadius(meterDistance)
                .withShowWindStreamletOverlay(showWindStreamletOverlay)
                .withShowWindStreamletColors(showWindStreamletColors).withShowSimulationOverlay(showSimulationOverlay)
                .withTailLengthInMilliseconds(tailLengthInMilliseconds).withPaywallResolver(paywallResolver)
                .withSecuredDTO(securedDTO).build();
    }

    public Set<ManeuverType> getManeuverTypesToShow() {
        return Util.asSet(maneuverTypesToShow.getValues());
    }

    public boolean isShowEstimatedDuration() {
        return showEstimatedDuration.getValue();
    }

    protected PaywallResolver getPaywallResolver() {
        return this.paywallResolver;
    }
    
    protected SecuredDTOProxy getSecuredDTO() {
        return securedDTO;
    }
    
    public static class RaceMapSettingsBuilder {
        private Boolean showSatelliteLayer;
        private Boolean showDouglasPeuckerPoints;
        private Set<ManeuverType> maneuverTypesToShow;
        private Boolean showOnlySelectedCompetitors;
        private RaceMapZoomSettings zoomSettings;
        private RaceMapHelpLinesSettings helpLinesSettings;
        private Boolean transparentHoverlines;
        private Integer hoverlineStrokeWeight;
        private Long tailLengthInMilliseconds;
        private Distance buoyZoneRadius;
        private Boolean showSelectedCompetitorsInfo;
        private Boolean showWindStreamletColors;
        private Boolean showWindStreamletOverlay;
        private Boolean showSimulationOverlay;
        private Boolean showMapControls;
        private Boolean showManeuverLossVisualization;
        private Boolean windUp;
        private Boolean showEstimatedDuration;
        private Boolean showWindLadder;
        private Double startCountDownFontSizeScaling;
        private SecuredDTOProxy securedDTO;
        private PaywallResolver paywallResolver;
        
        public RaceMapSettingsBuilder() {
        }
        
        public RaceMapSettingsBuilder(RaceMapSettings settings) {
            copyValues(settings);
        }
        
        private void copyValues(RaceMapSettings settings) {
            this.showSatelliteLayer = settings.isShowSatelliteLayer();
            this.showDouglasPeuckerPoints = settings.isShowDouglasPeuckerPoints();
            this.maneuverTypesToShow = settings.getManeuverTypesToShow();
            this.showOnlySelectedCompetitors = settings.isShowOnlySelectedCompetitors();
            this.zoomSettings = settings.getZoomSettings();
            this.helpLinesSettings = settings.getHelpLinesSettings();
            this.transparentHoverlines = settings.getTransparentHoverlines();
            this.hoverlineStrokeWeight = settings.getHoverlineStrokeWeight();
            this.tailLengthInMilliseconds = settings.getTailLengthInMilliseconds();
            this.buoyZoneRadius = settings.getBuoyZoneRadius();
            this.showSelectedCompetitorsInfo = settings.isShowSelectedCompetitorsInfo();
            this.showWindStreamletColors = settings.isShowWindStreamletColors();
            this.showWindStreamletOverlay = settings.isShowWindStreamletOverlay();
            this.showSimulationOverlay = settings.isShowSimulationOverlay();
            this.showMapControls = settings.isShowMapControls();
            this.showManeuverLossVisualization = settings.isShowManeuverLossVisualization();
            this.windUp = settings.isWindUp();
            this.showEstimatedDuration = settings.isShowEstimatedDuration();
            this.showWindLadder = settings.isShowWindLadder();
            this.startCountDownFontSizeScaling = settings.getStartCountDownFontSizeScaling();
            this.securedDTO = settings.getSecuredDTO();
            this.paywallResolver = settings.getPaywallResolver();
        }

        public RaceMapSettings build() {
            return new RaceMapSettings(zoomSettings, helpLinesSettings, transparentHoverlines, hoverlineStrokeWeight,
                    tailLengthInMilliseconds, windUp, buoyZoneRadius, showOnlySelectedCompetitors,
                    showSelectedCompetitorsInfo, showWindStreamletColors, showWindStreamletOverlay,
                    showSimulationOverlay, showMapControls, maneuverTypesToShow, showDouglasPeuckerPoints,
                    showEstimatedDuration, startCountDownFontSizeScaling, showManeuverLossVisualization,
                    showSatelliteLayer, showWindLadder, paywallResolver, securedDTO);
        }

        public RaceMapSettingsBuilder witholdSettings(RaceMapSettings settings) {
            this.copyValues(settings);
            return this;
        }

        public RaceMapSettingsBuilder withZoomSettings(RaceMapZoomSettings zoomSettings) {
            this.zoomSettings = zoomSettings;
            return this;
        }

        public RaceMapSettingsBuilder withHelpLinesSettings(RaceMapHelpLinesSettings helpLinesSettings) {
            this.helpLinesSettings = helpLinesSettings;
            return this;
        }

        public RaceMapSettingsBuilder withTransparentHoverlines(Boolean transparentHoverlines) {
            this.transparentHoverlines = transparentHoverlines;
            return this;
        }

        public RaceMapSettingsBuilder withHoverlineStrokeWeight(Integer hoverlineStrokeWeight) {
            this.hoverlineStrokeWeight = hoverlineStrokeWeight;
            return this;
        }

        public RaceMapSettingsBuilder withTailLengthInMilliseconds(Long tailLengthInMilliseconds) {
            this.tailLengthInMilliseconds = tailLengthInMilliseconds;
            return this;
        }

        public RaceMapSettingsBuilder withWindUp(Boolean windUp) {
            this.windUp = windUp;
            return this;
        }

        public RaceMapSettingsBuilder withBuoyZoneRadius(Distance buoyZoneRadius) {
            this.buoyZoneRadius = buoyZoneRadius;
            return this;
        }

        public RaceMapSettingsBuilder withShowOnlySelectedCompetitors(Boolean showOnlySelectedCompetitors) {
            this.showOnlySelectedCompetitors = showOnlySelectedCompetitors;
            return this;
        }

        public RaceMapSettingsBuilder withShowSelectedCompetitorsInfo(Boolean showSelectedCompetitorsInfo) {
            this.showSelectedCompetitorsInfo = showSelectedCompetitorsInfo;
            return this;
        }

        public RaceMapSettingsBuilder withShowWindStreamletColors(Boolean showWindStreamletColors) {
            this.showWindStreamletColors = showWindStreamletColors;
            return this;
        }

        public RaceMapSettingsBuilder withShowWindStreamletOverlay(Boolean showWindStreamletOverlay) {
            this.showWindStreamletOverlay = showWindStreamletOverlay;
            return this;
        }

        public RaceMapSettingsBuilder withShowSimulationOverlay(Boolean showSimulationOverlay) {
            this.showSimulationOverlay = showSimulationOverlay;
            return this;
        }

        public RaceMapSettingsBuilder withShowMapControls(Boolean showMapControls) {
            this.showMapControls = showMapControls;
            return this;
        }

        public RaceMapSettingsBuilder withManeuverTypesToShow(Set<ManeuverType> maneuverTypesToShow) {
            this.maneuverTypesToShow = maneuverTypesToShow;
            return this;
        }

        public RaceMapSettingsBuilder withShowDouglasPeuckerPoints(Boolean showDouglasPeuckerPoints) {
            this.showDouglasPeuckerPoints = showDouglasPeuckerPoints;
            return this;
        }

        public RaceMapSettingsBuilder withShowEstimatedDuration(Boolean showEstimatedDuration) {
            this.showEstimatedDuration = showEstimatedDuration;
            return this;
        }

        public RaceMapSettingsBuilder withStartCountDownFontSizeScaling(Double startCountDownFontSizeScaling) {
            this.startCountDownFontSizeScaling = startCountDownFontSizeScaling;
            return this;
        }

        public RaceMapSettingsBuilder withShowManeuverLossVisualization(Boolean showManeuverLossVisualization) {
            this.showManeuverLossVisualization = showManeuverLossVisualization;
            return this;
        }

        public RaceMapSettingsBuilder withShowSatelliteLayer(Boolean showSatelliteLayer) {
            this.showSatelliteLayer = showSatelliteLayer;
            return this;
        }

        public RaceMapSettingsBuilder withShowWindLadder(Boolean showWindLadder) {
            this.showWindLadder = showWindLadder;
            return this;
        }

        public RaceMapSettingsBuilder withPaywallResolver(PaywallResolver paywallResolver) {
            this.paywallResolver = paywallResolver;
            return this;
        }

        public RaceMapSettingsBuilder withSecuredDTO(SecuredDTOProxy securedDTO) {
            this.securedDTO = securedDTO;
            return this;
        }
    }
}
