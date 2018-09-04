package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.maps.client.LoadApi;
import com.google.gwt.maps.client.LoadApi.LoadLibrary;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.google.gwt.maps.client.controls.ControlPosition;
import com.google.gwt.maps.client.controls.MapTypeControlOptions;
import com.google.gwt.maps.client.controls.MapTypeStyle;
import com.google.gwt.maps.client.controls.PanControlOptions;
import com.google.gwt.maps.client.controls.ZoomControlOptions;
import com.google.gwt.maps.client.events.bounds.BoundsChangeMapEvent;
import com.google.gwt.maps.client.events.bounds.BoundsChangeMapHandler;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.events.dragend.DragEndMapEvent;
import com.google.gwt.maps.client.events.dragend.DragEndMapHandler;
import com.google.gwt.maps.client.events.idle.IdleMapEvent;
import com.google.gwt.maps.client.events.idle.IdleMapHandler;
import com.google.gwt.maps.client.events.mouseout.MouseOutMapEvent;
import com.google.gwt.maps.client.events.mouseout.MouseOutMapHandler;
import com.google.gwt.maps.client.events.mouseover.MouseOverMapEvent;
import com.google.gwt.maps.client.events.mouseover.MouseOverMapHandler;
import com.google.gwt.maps.client.events.zoom.ZoomChangeMapEvent;
import com.google.gwt.maps.client.events.zoom.ZoomChangeMapHandler;
import com.google.gwt.maps.client.maptypes.MapTypeStyleFeatureType;
import com.google.gwt.maps.client.mvc.MVCArray;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.client.overlays.MarkerOptions;
import com.google.gwt.maps.client.overlays.Polygon;
import com.google.gwt.maps.client.overlays.PolygonOptions;
import com.google.gwt.maps.client.overlays.Polyline;
import com.google.gwt.maps.client.overlays.PolylineOptions;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.Bounds;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalableBearing;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalablePosition;
import com.sap.sailing.domain.common.windfinder.SpotDTO;
import com.sap.sailing.gwt.ui.actions.GetBoatPositionsAction;
import com.sap.sailing.gwt.ui.actions.GetPolarAction;
import com.sap.sailing.gwt.ui.actions.GetRaceMapDataAction;
import com.sap.sailing.gwt.ui.actions.GetWindInfoAction;
import com.sap.sailing.gwt.ui.client.ClientResources;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.NumberFormatterFactory;
import com.sap.sailing.gwt.ui.client.RaceCompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.RequiresDataInitialization;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.WindSourceTypeFormatter;
import com.sap.sailing.gwt.ui.client.shared.filter.QuickRankProvider;
import com.sap.sailing.gwt.ui.client.shared.racemap.BoatOverlay.DisplayMode;
import com.sap.sailing.gwt.ui.client.shared.racemap.QuickRanksDTOProvider.QuickRanksListener;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceCompetitorSet.CompetitorsForRaceDefinedListener;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapHelpLinesSettings.HelpLineTypes;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapZoomSettings.ZoomTypes;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.CompactBoatPositionsDTO;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sailing.gwt.ui.shared.CoursePositionsDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTOWithSpeedWindTackAndLegType;
import com.sap.sailing.gwt.ui.shared.LegInfoDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;
import com.sap.sailing.gwt.ui.shared.RaceCourseDTO;
import com.sap.sailing.gwt.ui.shared.RaceMapDataDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.SidelineDTO;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
import com.sap.sailing.gwt.ui.shared.WaypointDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sailing.gwt.ui.shared.racemap.CanvasOverlayV3;
import com.sap.sailing.gwt.ui.shared.racemap.GoogleMapAPIKey;
import com.sap.sailing.gwt.ui.shared.racemap.GoogleMapStyleHelper;
import com.sap.sailing.gwt.ui.shared.racemap.RaceSimulationOverlay;
import com.sap.sailing.gwt.ui.shared.racemap.WindStreamletsRaceboardOverlay;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Color;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.RGBColor;
import com.sap.sse.common.impl.TimeRangeImpl;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;
import com.sap.sse.gwt.client.shared.components.AbstractCompositeComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class RaceMap extends AbstractCompositeComponent<RaceMapSettings> implements TimeListener, CompetitorSelectionChangeListener,
        RaceTimesInfoProviderListener, TailFactory, RequiresDataInitialization, RequiresResize, QuickRankProvider {
    /* Line colors */
    static final String ADVANTAGE_LINE_COLOR = "#ff9900"; // orange
    static final String START_LINE_COLOR = "#ffffff";
    static final String FINISH_LINE_COLOR = "#000000";
    static final Color LOWLIGHTED_TAIL_COLOR = new RGBColor(200, 200, 200);
    /* Line opacities and stroke weights */
    static final double LOWLIGHTED_TAIL_OPACITY = 0.3;
    static final double STANDARD_LINE_OPACITY = 1.0;
    static final int STANDARD_LINE_STROKEWEIGHT = 1;
    
    public static final String GET_RACE_MAP_DATA_CATEGORY = "getRaceMapData";
    public static final String GET_WIND_DATA_CATEGORY = "getWindData";
    private static final String COMPACT_HEADER_STYLE = "compactHeader";
    public static final Color WATER_COLOR = new RGBColor(0, 67, 125);
    
    private AbsolutePanel rootPanel = new AbsolutePanel();
    
    private MapWidget map;
    
    /**
     * Always valid, non-<code>null</code>. Must be used to map all coordinates, headings, bearings, and directions
     * displayed on the map, including the orientations of any canvases such as boat icons, wind displays etc. that are
     * embedded in the map. The coordinate systems facilitates the possibility of transformed displays such as
     * rotated and translated versions of the map, implementing the "wind-up" view.
     */
    private DelegateCoordinateSystem coordinateSystem;
    
    private FlowPanel headerPanel;
    private AbsolutePanel panelForLeftHeaderLabels;
    private AbsolutePanel panelForRightHeaderLabels;

    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;

    private final static ClientResources resources = GWT.create(ClientResources.class);

    /**
     * Polyline for the start line (connecting two marks representing the start gate).
     */
    private Polyline startLine;

    /**
     * Polyline for the finish line (connecting two marks representing the finish gate).
     */
    private Polyline finishLine;

    /**
     * Polyline for the advantage line (the leading line for the boats, orthogonal to the wind direction; touching the leading boat).
     */
    private Polyline advantageLine;
    
    private AdvantageLineAnimator advantageTimer;
    
    /**
     * The windward of two Polylines representing a triangle between startline and first mark.
     */
    private Polyline windwardStartLineMarkToFirstMarkLine;
    
    /**
     * The leeward of two Polylines representing a triangle between startline and first mark.
     */
    private Polyline leewardStartLineMarkToFirstMarkLine;

    private class AdvantageLineMouseOverMapHandler implements MouseOverMapHandler {
        private double trueWindAngle;
        private Date date;
        
        public AdvantageLineMouseOverMapHandler(double trueWindAngle, Date date) {
            this.trueWindAngle = trueWindAngle;
            this.date = date;
        }
        
        public void setTrueWindBearing(double trueWindAngle) {
            this.trueWindAngle = trueWindAngle;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        @Override
        public void onEvent(MouseOverMapEvent event) {
            map.setTitle(stringMessages.advantageLine()+" (from "+new DegreeBearingImpl(Math.round(trueWindAngle)).reverse().getDegrees()+"deg"+
                    (date == null ? ")" : ", "+ date) + ")");
        }
    };
    
    private AdvantageLineMouseOverMapHandler advantageLineMouseOverHandler;
    
    /**
     * Polylines for the course middle lines; keys are the two control points delimiting the leg for which the
     * {@link Polyline} value shows the course middle line. As only one course middle line is shown even if there
     * are multiple legs using the same control points in different directions, using a {@link Set} makes this
     * independent of the order of the two control points. If no course middle line is currently being shown for
     * a pair of control points, the map will not contain a value for this pair.
     */
    private final Map<Set<ControlPointDTO>, Polyline> courseMiddleLines;

    private final Map<SidelineDTO, Polygon> courseSidelines;
    
    /**
     * When the {@link HelpLineTypes#COURSEGEOMETRY} option is selected, little markers will be displayed on the
     * lines that show the tooltip text in a little info box linked to the line. When the line is removed by
     * {@link #showOrRemoveOrUpdateLine(Polyline, boolean, Position, Position, LineInfoProvider, String)}, these
     * overlays need to be removed as well. Also, when the {@link HelpLineTypes#COURSEGEOMETRY} setting is
     * deactivated, all these overlays need to go.
     */
    private final Map<Polyline, SmallTransparentInfoOverlay> infoOverlaysForLinesForCourseGeometry;
    
    /**
     * Wind data used to display the advantage line. Retrieved by a {@link GetWindInfoAction} execution and used in
     * {@link #showAdvantageLine(Iterable, Date)}.
     */
    private WindInfoForRaceDTO lastCombinedWindTrackInfoDTO;
    
    /**
     * Manages the cached set of {@link GPSFixDTOWithSpeedWindTackAndLegType}s for the boat positions as well as their graphical counterpart in the
     * form of {@link Polyline}s.
     */
    private final FixesAndTails fixesAndTails;

    /**
     * html5 canvases used as boat display on the map
     */
    private final Map<CompetitorDTO, BoatOverlay> boatOverlays;

    /**
     * html5 canvases used for competitor info display on the map
     */
    private final CompetitorInfoOverlays competitorInfoOverlays;
    
    private SmallTransparentInfoOverlay countDownOverlay;

    /**
     * Map overlays with html5 canvas used to display wind sensors
     */
    private final Map<WindSource, WindSensorOverlay> windSensorOverlays;

    /**
     * Map from the {@link MarkDTO#getIdAsString() mark's ID converted to a string} to the corresponding overlays with
     * html5 canvas used to display course marks including buoy zones
     */
    private final Map<String, CourseMarkOverlay> courseMarkOverlays;
    
    private final Map<String, HandlerRegistration> courseMarkClickHandlers;

    /**
     * Maps from the {@link MarkDTO#getIdAsString() mark's ID converted to a string} to the corresponding {@link MarkDTO}
     */
    private final Map<String, MarkDTO> markDTOs;

    /**
     * markers displayed in response to
     * {@link SailingServiceAsync#getDouglasPoints(String, String, Map, Map, double, AsyncCallback)}
     */
    protected Set<Marker> douglasMarkers;

    private Map<CompetitorDTO, List<GPSFixDTOWithSpeedWindTackAndLegType>> lastDouglasPeuckerResult;
    
    private final RaceCompetitorSelectionProvider competitorSelection;
    
    private final RaceCompetitorSet raceCompetitorSet;

    /**
     * Used to check if the first initial zoom to the mark markers was already done.
     */
    private boolean mapFirstZoomDone = false;

    private final Timer timer;

    private RaceTimesInfoDTO lastRaceTimesInfo;
    
    /**
     * RPC calls may receive responses out of order if there are multiple calls in-flight at the same time. If the time
     * slider is moved quickly it generates many requests for boat positions quickly after each other. Sometimes,
     * responses for requests send later may return before the responses to all earlier requests have been received and
     * processed. This counter is used to number the requests. When processing of a response for a later request has
     * already begun, responses to earlier requests will be ignored.
     */
    private int boatPositionRequestIDCounter;

    /**
     * Corresponds to {@link #boatPositionRequestIDCounter}. As soon as the processing of a response for a request ID
     * begins, this attribute is set to the ID. A response won't be processed if a later response is already being
     * processed.
     */
    private int startedProcessingRequestID;

    private final RaceMapImageManager raceMapImageManager; 

    private RaceMapSettings settings;
    private final RaceMapLifecycle raceMapLifecycle;
    
    private final StringMessages stringMessages;
    
    private boolean isMapInitialized;

    private Date lastTimeChangeBeforeInitialization;
    
    private int lastLegNumber;

    /**
     * The strategy for maintaining and delivering the "quick ranks" information. The provider will be informed about
     * quick ranks received from a {@link RaceMapDataDTO#quickRanks} field but may choose to ignore this information, e.g.,
     * if it can assume that more current information about ranks and leg numbers is available from a {@link LeaderboardDTO}.
     */
    private QuickRanksDTOProvider quickRanksDTOProvider;
    
    private final CombinedWindPanel combinedWindPanel;
    private final TrueNorthIndicatorPanel trueNorthIndicatorPanel;
    private final FlowPanel topLeftControlsWrapperPanel;
    
    private final AsyncActionsExecutor asyncActionsExecutor;

    /**
     * The map bounds as last received by map callbacks; used to determine whether to suppress the boat animation during zoom/pan
     */
    private LatLngBounds currentMapBounds; // bounds to which bounds-changed-handler compares
    private int currentZoomLevel;          // zoom-level to which bounds-changed-handler compares
    
    private boolean autoZoomIn = false;  // flags auto-zoom-in in progress
    private boolean autoZoomOut = false; // flags auto-zoom-out in progress
    private int autoZoomLevel;           // zoom-level to which auto-zoom-in/-out is zooming
    LatLngBounds autoZoomLatLngBounds;   // bounds to which auto-zoom-in/-out is panning&zooming
    
    private RaceSimulationOverlay simulationOverlay;
    private WindStreamletsRaceboardOverlay streamletOverlay;
    
    private final boolean isSimulationEnabled;
    
    private static final String GET_POLAR_CATEGORY = "getPolar";
    
    /**
     * Tells about the availability of polar / VPP data for this race. If available, the simulation feature can be
     * offered to the user.
     */
    private boolean hasPolar;
    
    private final RegattaAndRaceIdentifier raceIdentifier;
    
    /**
     * When the user requests wind-up display this may happen at a point where no mark positions are known or when
     * no wind direction is known yet. In this case, this flag will be set, and when wind information or course mark
     * positions are received later, this flag is checked, and if set, a {@link #updateCoordinateSystemFromSettings()}
     * call is issued to make sure that the user's request for a new coordinate system is honored.
     */
    private boolean requiresCoordinateSystemUpdateWhenCoursePositionAndWindDirectionIsKnown;
    
    /**
     * Tells whether currently an auto-zoom is in progress; this is used particularly to keep the smooth CSS boat transitions
     * active while auto-zooming whereas stopping them seems the better option for manual zooms.
     */
    private boolean autoZoomInProgress;
    
    /**
     * Tells whether currently an orientation change is in progress; this is required handle map events during the configuration of the map
     * during an orientation change.
     */
    private boolean orientationChangeInProgress;
    
    private final NumberFormat numberFormatOneDecimal = NumberFormatterFactory.getDecimalFormat(1);
    private final NumberFormat numberFormatNoDecimal = NumberFormatterFactory.getDecimalFormat(0);
    
    /**
     * The competitor for which the advantage line is currently showing. Should this competitor's quick rank change, or
     * should ranks be received while this field is {@code null}, the advantage line
     * {@link #showAdvantageLine(Iterable, Date, long)} drawing procedure} needs to be triggered.
     */
    private CompetitorDTO advantageLineCompetitor;
    protected Label estimatedDurationOverlay;
    private RaceMapStyle raceMapStyle;
    private final boolean showHeaderPanel;
    
    /** Callback to set the visibility of the wind chart. */
    private final Consumer<WindSource> showWindChartForProvider;
    private ManagedInfoWindow managedInfoWindow;
    
    private final ManeuverMarkersAndLossIndicators maneuverMarkersAndLossIndicators;

    private final MultiHashSet<Date> remoteCallsInExecution = new MultiHashSet<>();
    private final MultiHashSet<Date> remoteCallsToSkipInExecution = new MultiHashSet<>();
    private boolean currentlyDragging = false;

    private int zoomingAnimationsInProgress = 0;

    static class MultiHashSet<T> {
        private HashMap<T, List<T>> map = new HashMap<>();

        /** @return true if value already in set */
        public boolean add(T t) {
            List<T> l = map.get(t);
            if (l != null) {
                l.add(t);
                return true;
            } else {
                l = new ArrayList<>();
                l.add(t);
                map.put(t, l);
                return false;
            }
        }

        public void addAll(MultiHashSet<T> col) {
            if (col != null) {
                col.map.entrySet().forEach(e -> e.getValue().forEach(v -> add(v)));
            }
        }

        public boolean remove(T t) {
            List<T> l = map.get(t);
            if (l != null) {
                l.remove(t);
                if (l.size() == 0) {
                    map.remove(t);
                }
                return true;
            } else {
                return false;
            }
        }

        public boolean contains(T t) {
            return (map.containsKey(t));
        }
    }

    private class AdvantageLineUpdater implements QuickRanksListener {
        @Override
        public void rankChanged(String competitorIdAsString, QuickRankDTO oldQuickRank, QuickRankDTO quickRank) {
            if (advantageLineCompetitor == null ||
                    (oldQuickRank != null && advantageLineCompetitor.getIdAsString().equals(oldQuickRank.competitor.getIdAsString())) ||
                    (quickRank != null && advantageLineCompetitor.getIdAsString().equals(quickRank.competitor.getIdAsString()))) {
                showAdvantageLine(getCompetitorsToShow(), getTimer().getTime(), /* timeForPositionTransitionMillis */ -1 /* (no transition) */);
            }
        }
    }
    
    public RaceMap(Component<?> parent, ComponentContext<?> context, RaceMapLifecycle raceMapLifecycle,
            RaceMapSettings raceMapSettings,
            SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            ErrorReporter errorReporter, Timer timer, RaceCompetitorSelectionProvider competitorSelection,
            RaceCompetitorSet raceCompetitorSet, StringMessages stringMessages, RegattaAndRaceIdentifier raceIdentifier, 
            RaceMapResources raceMapResources, boolean showHeaderPanel, QuickRanksDTOProvider quickRanksDTOProvider) {
        this(parent, context, raceMapLifecycle, raceMapSettings, sailingService, asyncActionsExecutor, errorReporter,
                timer, competitorSelection, raceCompetitorSet, stringMessages, raceIdentifier, raceMapResources,
                showHeaderPanel, quickRanksDTOProvider, visible -> {});
    }
    
    public RaceMap(Component<?> parent, ComponentContext<?> context, RaceMapLifecycle raceMapLifecycle,
            RaceMapSettings raceMapSettings,
            SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            ErrorReporter errorReporter, Timer timer, RaceCompetitorSelectionProvider competitorSelection, RaceCompetitorSet raceCompetitorSet,
            StringMessages stringMessages, RegattaAndRaceIdentifier raceIdentifier, 
            RaceMapResources raceMapResources, boolean showHeaderPanel, QuickRanksDTOProvider quickRanksDTOProvider, Consumer<WindSource> showWindChartForProvider) {
        super(parent, context);
        this.maneuverMarkersAndLossIndicators = new ManeuverMarkersAndLossIndicators(this, sailingService, errorReporter, stringMessages);
        this.showHeaderPanel = showHeaderPanel;
        this.quickRanksDTOProvider = quickRanksDTOProvider;
        this.raceMapLifecycle = raceMapLifecycle;
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.errorReporter = errorReporter;
        this.timer = timer;
        this.isSimulationEnabled = true;
        this.showWindChartForProvider = showWindChartForProvider;
        timer.addTimeListener(this);
        raceMapImageManager = new RaceMapImageManager(raceMapResources);
        markDTOs = new HashMap<String, MarkDTO>();
        courseSidelines = new HashMap<>();
        courseMiddleLines = new HashMap<>();
        infoOverlaysForLinesForCourseGeometry = new HashMap<>();
        boatOverlays = new HashMap<>();
        competitorInfoOverlays = new CompetitorInfoOverlays(this, stringMessages);
        quickRanksDTOProvider.addQuickRanksListener(competitorInfoOverlays);
        quickRanksDTOProvider.addQuickRanksListener(new AdvantageLineUpdater());
        windSensorOverlays = new HashMap<WindSource, WindSensorOverlay>();
        courseMarkOverlays = new HashMap<String, CourseMarkOverlay>();
        courseMarkClickHandlers = new HashMap<String, HandlerRegistration>();
        this.competitorSelection = competitorSelection;
        this.raceCompetitorSet = raceCompetitorSet;
        competitorSelection.addCompetitorSelectionChangeListener(this);
        settings = raceMapSettings;
        coordinateSystem = new DelegateCoordinateSystem(new IdentityCoordinateSystem());
        fixesAndTails = new FixesAndTails(coordinateSystem);
        updateCoordinateSystemFromSettings();
        lastTimeChangeBeforeInitialization = null;
        isMapInitialized = false;
        this.hasPolar = false;
        headerPanel = new FlowPanel();
        headerPanel.setStyleName("RaceMap-HeaderPanel");
        panelForLeftHeaderLabels = new AbsolutePanel();
        panelForRightHeaderLabels = new AbsolutePanel();
        initializeData(settings.isShowMapControls(), showHeaderPanel);
        raceMapStyle = raceMapResources.raceMapStyle();
        raceMapStyle.ensureInjected();
        combinedWindPanel = new CombinedWindPanel(this, raceMapImageManager, raceMapStyle, stringMessages, coordinateSystem);
        combinedWindPanel.setVisible(false);
        trueNorthIndicatorPanel = new TrueNorthIndicatorPanel(this, raceMapImageManager, raceMapStyle, stringMessages, coordinateSystem);
        trueNorthIndicatorPanel.setVisible(true);
        topLeftControlsWrapperPanel = new FlowPanel();
        topLeftControlsWrapperPanel.add(combinedWindPanel);
        topLeftControlsWrapperPanel.add(trueNorthIndicatorPanel);
        orientationChangeInProgress = false;
        mapFirstZoomDone = false;
        // TODO bug 494: reset zoom settings to user preferences
        initWidget(rootPanel);
        this.setSize("100%", "100%");
    }
    
    ManagedInfoWindow getManagedInfoWindow() {
        return managedInfoWindow;
    }

    RaceMapImageManager getRaceMapImageManager() {
        return raceMapImageManager;
    }

    public void setQuickRanksDTOProvider(QuickRanksDTOProvider newQuickRanksDTOProvider) {
        if (this.quickRanksDTOProvider != null) {
            this.quickRanksDTOProvider.moveListernersTo(newQuickRanksDTOProvider);
        }
        this.quickRanksDTOProvider = newQuickRanksDTOProvider;
    }
    /**
     * The {@link WindDTO#dampenedTrueWindFromDeg} direction if {@link #lastCombinedWindTrackInfoDTO} has a
     * {@link WindSourceType#COMBINED} source which has at least one fix recorded; <code>null</code> otherwise.
     */
    private Bearing getLastCombinedTrueWindFromDirection() {
        if (lastCombinedWindTrackInfoDTO != null) {
            for (Entry<WindSource, WindTrackInfoDTO> e : lastCombinedWindTrackInfoDTO.windTrackInfoByWindSource.entrySet()) {
                if (e.getKey().getType() == WindSourceType.COMBINED) {
                    final List<WindDTO> windFixes = e.getValue().windFixes;
                    if (!windFixes.isEmpty()) {
                        return new DegreeBearingImpl(windFixes.get(0).dampenedTrueWindFromDeg);
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * @return {@code true} if the map was redrawn by the call to this method
     */
    private boolean updateCoordinateSystemFromSettings() {
        boolean redrawn = false;
        final MapOptions mapOptions;
        orientationChangeInProgress = true;
        if (getSettings().isWindUp()) {
            final Position centerOfCourse = getCenterOfCourse();
            if (centerOfCourse != null) {
                final Bearing lastCombinedTrueWindFromDirection = getLastCombinedTrueWindFromDirection();
                if (lastCombinedTrueWindFromDirection != null) {
                    // new equator shall point 90deg right of the "from" wind direction to make wind come from top of map
                    coordinateSystem.setCoordinateSystem(new RotateAndTranslateCoordinateSystem(centerOfCourse,
                            lastCombinedTrueWindFromDirection.add(new DegreeBearingImpl(90))));
                    if (map != null) {
                        mapOptions = getMapOptions(settings.isShowMapControls(), /* wind-up */ true);
                    } else {
                        mapOptions = null;
                    }
                    requiresCoordinateSystemUpdateWhenCoursePositionAndWindDirectionIsKnown = false;
                } else {
                    // register callback in case center of course and wind info becomes known
                    requiresCoordinateSystemUpdateWhenCoursePositionAndWindDirectionIsKnown = true;
                    mapOptions = null;
                }
            } else {
                // register callback in case center of course and wind info becomes known
                requiresCoordinateSystemUpdateWhenCoursePositionAndWindDirectionIsKnown = true;
                mapOptions = null;
            }
        } else {
            if (map != null) {
                mapOptions = getMapOptions(settings.isShowMapControls(), /* wind-up */ false);
            } else {
                mapOptions = null;
            }
            coordinateSystem.setCoordinateSystem(new IdentityCoordinateSystem());
        }
        if (mapOptions != null) { // if no coordinate system change happened that affects an existing map, don't redraw 
            fixesAndTails.clearTails();
            redraw();
            redrawn = true;
            // zooming and setting options while the event loop is still working doesn't work reliably; defer until event loop returns
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    if (map != null) {
                        map.setOptions(mapOptions);
                        // ensure zooming to what the settings tell, or defaults if what the settings tell isn't possible right now
                        mapFirstZoomDone = false;
                        trueNorthIndicatorPanel.redraw();
                        orientationChangeInProgress = false;
                    }
                }
            });
        }
        return redrawn;
    }

    private void loadMapsAPIV3(final boolean showMapControls, final boolean showHeaderPanel) {
        // load all the libs for use in the maps
        ArrayList<LoadLibrary> loadLibraries = new ArrayList<LoadApi.LoadLibrary>();
        loadLibraries.add(LoadLibrary.DRAWING);
        loadLibraries.add(LoadLibrary.GEOMETRY);

        Runnable onLoad = new Runnable() {
          @Override
          public void run() {
              MapOptions mapOptions = getMapOptions(showMapControls, /* wind up */ false);
              map = new MapWidget(mapOptions);
              rootPanel.add(map, 0, 0);
              if (showHeaderPanel) {
                  Image sapLogo = createSAPLogo();
                  rootPanel.add(sapLogo);
              }
              
              map.setControls(ControlPosition.LEFT_TOP, topLeftControlsWrapperPanel);
              adjustLeftControlsIndent();

              RaceMap.this.raceMapImageManager.loadMapIcons(map);
              map.setSize("100%", "100%");
                map.addZoomChangeHandler(new ZoomChangeMapHandler() {
                    @Override
                    public void onEvent(ZoomChangeMapEvent event) {
                        remoteCallsToSkipInExecution.addAll(remoteCallsInExecution);
                        if (!autoZoomIn && !autoZoomOut && !orientationChangeInProgress) {
                            // stop automatic zoom after a manual zoom event; automatic zoom in zoomMapToNewBounds will
                            // restore old settings
                            final List<RaceMapZoomSettings.ZoomTypes> emptyList = Collections.emptyList();
                            RaceMapZoomSettings clearedZoomSettings = new RaceMapZoomSettings(emptyList,
                                    settings.getZoomSettings().isZoomToSelectedCompetitors());
                            settings = new RaceMapSettings(settings, clearedZoomSettings);
                            simulationOverlay.setVisible(false);
                            if (zoomingAnimationsInProgress == 0) {
                                showLayoutsAfterAnimationFinishes();
                            } else {
                                showLayoutsAfterAnimationFinishes();
                            }
                        }

                        if ((streamletOverlay != null) && !map.getBounds().equals(currentMapBounds)
                                && settings.isShowWindStreamletOverlay()) {
                            streamletOverlay.onZoomChange();
                        }
                    }

                    private void showLayoutsAfterAnimationFinishes() {
                        zoomingAnimationsInProgress++;
                        new com.google.gwt.user.client.Timer() {
                            @Override
                            public void run() {
                                if (zoomingAnimationsInProgress == 1) {
                                    simulationOverlay.setVisible(settings.isShowSimulationOverlay());
                                }
                                zoomingAnimationsInProgress--;
                            }

                        }.schedule(500);
                    }
                });
              map.addDragEndHandler(new DragEndMapHandler() {
                  @Override
                  public void onEvent(DragEndMapEvent event) {
                        // stop automatic zoom after a manual drag event
                        autoZoomIn = false;
                        autoZoomOut = false;
                        final List<RaceMapZoomSettings.ZoomTypes> emptyList = Collections.emptyList();
                        RaceMapZoomSettings clearedZoomSettings = new RaceMapZoomSettings(emptyList,
                                settings.getZoomSettings().isZoomToSelectedCompetitors());
                        settings = new RaceMapSettings(settings, clearedZoomSettings);
                        currentlyDragging = false;
                        refreshMapWithoutAnimation();
                    }
              });

                map.addDragStartHandler(event -> {
                    currentlyDragging = true;
                });

              map.addIdleHandler(new IdleMapHandler() {
                  @Override
                  public void onEvent(IdleMapEvent event) {
                      // the "idle"-event is raised at the end of map-animations
                      if (autoZoomIn) {
                          // finalize zoom-in that was started with panTo() in zoomMapToNewBounds()
                          map.setZoom(autoZoomLevel);
                          autoZoomIn = false;
                      }
                      if (autoZoomOut) {
                          // finalize zoom-out that was started with setZoom() in zoomMapToNewBounds()
                          map.panTo(autoZoomLatLngBounds.getCenter());
                          autoZoomOut = false;
                      }
                  }
              });
                map.addBoundsChangeHandler(new BoundsChangeMapHandler() {
                    @Override
                    public void onEvent(BoundsChangeMapEvent event) {
                        int newZoomLevel = map.getZoom();
                        if (!isAutoZoomInProgress() && (newZoomLevel != currentZoomLevel)) {
                            removeTransitions();
                        }

                        currentMapBounds = map.getBounds();
                        currentZoomLevel = newZoomLevel;
                        headerPanel.getElement().getStyle().setWidth(map.getOffsetWidth(), Unit.PX);
                        refreshMapWithoutAnimation();
                    }
                });
                map.addDragHandler(event -> {
                        if (streamletOverlay != null && settings.isShowWindStreamletOverlay()) {
                            streamletOverlay.onBoundsChanged();
                    }
                });

              // If there was a time change before the API was loaded, reset the time
              if (lastTimeChangeBeforeInitialization != null) {
                  timeChanged(lastTimeChangeBeforeInitialization, null);
                  lastTimeChangeBeforeInitialization = null;
              }
              // Initialize streamlet canvas for wind visualization; it shouldn't be doing anything unless it's visible
              streamletOverlay = new WindStreamletsRaceboardOverlay(getMap(), /* zIndex */ 0,
                      timer, raceIdentifier, sailingService, asyncActionsExecutor, stringMessages, coordinateSystem);
              streamletOverlay.addToMap();
                streamletOverlay.setColors(settings.isShowWindStreamletColors());
              if (settings.isShowWindStreamletOverlay()) {
                  streamletOverlay.setVisible(true);
              }

              if (isSimulationEnabled) {
                  // determine availability of polar diagram
                  setHasPolar();
                  // initialize simulation canvas
                  simulationOverlay = new RaceSimulationOverlay(getMap(), /* zIndex */ 0, raceIdentifier, sailingService, stringMessages, asyncActionsExecutor, coordinateSystem);
                  simulationOverlay.addToMap();
                  showSimulationOverlay(settings.isShowSimulationOverlay());
              }
              if (showHeaderPanel) {
                  createHeaderPanel(map);
              }
              if (showMapControls) {
                  createSettingsButton(map);
              }
              // Data has been initialized
              RaceMap.this.isMapInitialized = true;
              RaceMap.this.redraw();
              trueNorthIndicatorPanel.redraw();
              showAdditionalControls(map);
              RaceMap.this.managedInfoWindow = new ManagedInfoWindow(map);
          }
        };
        LoadApi.go(onLoad, loadLibraries, GoogleMapAPIKey.V3_PARAMS); 
    }

    /**
     * Subclasses may define additional stuff to be shown on the map.
     */
    protected void showAdditionalControls(MapWidget map) {
    }

    private void setHasPolar() {
        GetPolarAction getPolar = new GetPolarAction(sailingService, raceIdentifier);
        asyncActionsExecutor.execute(getPolar, GET_POLAR_CATEGORY,
                new MarkedAsyncCallback<>(new AsyncCallback<Boolean>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.errorDeterminingPolarAvailability(
                                raceIdentifier.getRaceName(), caught.getMessage()), /* silent */ true);
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        // store results
                        hasPolar = result.booleanValue();
                    }
                }));

    }

    /**
     * Creates a header panel where additional information can be displayed by using 
     * {@link #getLeftHeaderPanel()} or {@link #getRightHeaderPanel()}. 
     * 
     * This panel is transparent and configured in such a way that it moves other controls
     * down by its height. To achieve the goal of not having added widgets transparent
     * this widget consists of two parts: First one is the transparent panel and the
     * second one is the panel for the controls. The controls then need to moved onto
     * the panel by using CSS.
     */
    private void createHeaderPanel(MapWidget map) {
        // we need a panel that does not have any transparency to have the
        // labels shown in the right color. This panel also needs to have
        // a higher z-index than other elements on the map
        map.setControls(ControlPosition.TOP_LEFT, panelForLeftHeaderLabels);
        panelForLeftHeaderLabels.getElement().getParentElement().getStyle().setProperty("zIndex", "1");
        panelForLeftHeaderLabels.getElement().getStyle().setProperty("overflow", "visible");
        rootPanel.add(panelForRightHeaderLabels);
        panelForRightHeaderLabels.getElement().getStyle().setProperty("zIndex", "1");
        panelForRightHeaderLabels.getElement().getStyle().setProperty("overflow", "visible");
        // need to initialize size before css kicks in to make sure
        // that controls get positioned right
        headerPanel.getElement().getStyle().setHeight(60, Unit.PX);
        headerPanel.getElement().getStyle().setWidth(map.getOffsetWidth(), Unit.PX);
        // some sort of hack: not positioning TOP_LEFT because then the
        // controls at RIGHT would not get the correct top setting
        map.setControls(ControlPosition.TOP_RIGHT, headerPanel);
    }
    
    private void createSettingsButton(MapWidget map) {
        final Component<RaceMapSettings> component = this;
        Button settingsButton = new Button();
        settingsButton.setStyleName("gwt-MapSettingsButton");
        settingsButton.ensureDebugId("raceMapSettingsButton");
        settingsButton.setTitle(stringMessages.settings());
        settingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                SettingsDialog<RaceMapSettings> dialog = new SettingsDialog<RaceMapSettings>(component, stringMessages);
                dialog.ensureDebugId("raceMapSettings");
                dialog.show();
            }
        });
        map.setControls(ControlPosition.RIGHT_TOP, settingsButton);
    }

    private void removeTransitions() {
        // remove the canvas animations for boats
        for (CanvasOverlayV3 boatOverlay : RaceMap.this.getBoatOverlays().values()) {
            boatOverlay.removeCanvasPositionAndRotationTransition();
        }
        // remove the canvas animations for the info overlays of the selected boats
        competitorInfoOverlays.removeTransitions();
        for (CourseMarkOverlay markOverlay : courseMarkOverlays.values()) {
            markOverlay.removeCanvasPositionAndRotationTransition();
        }
        // remove the advantage line animation
        if (advantageTimer != null) {
            advantageTimer.removeAnimation();
        }
    }

    public void redraw() {
        timeChanged(timer.getTime(), null);
    }
    
    Map<CompetitorDTO, BoatOverlay> getBoatOverlays() {
        return Collections.unmodifiableMap(boatOverlays);
    }
    
    protected RaceCompetitorSelectionProvider getCompetitorSelection() {
        return competitorSelection;
    }

    protected Timer getTimer() {
        return timer;
    }

    protected RegattaAndRaceIdentifier getRaceIdentifier() {
        return raceIdentifier;
    }

    public MapWidget getMap() {
        return map;
    }
    
    public RaceSimulationOverlay getSimulationOverlay() {
        return simulationOverlay;
    }
    
    /**
     * @return the Panel where labels or other controls for the header can be positioned
     */
    public AbsolutePanel getLeftHeaderPanel() {
        return panelForLeftHeaderLabels;
    }
    
    public AbsolutePanel getRightHeaderPanel() {
        return panelForRightHeaderLabels;
    }
    
    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfos, long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest, long clientTimeWhenResponseWasReceived) {
        timer.adjustClientServerOffset(clientTimeWhenRequestWasSent, serverTimeDuringRequest, clientTimeWhenResponseWasReceived);
        this.lastRaceTimesInfo = raceTimesInfos.get(raceIdentifier);
    }

    /**
     * In {@link PlayModes#Live live mode}, when {@link #loadCompleteLeaderboard(Date) loading the leaderboard contents}, <code>null</code>
     * is used as time point. The condition for this is encapsulated in this method so others can find out. For example, when a time change
     * is signaled due to local offset / delay adjustments, no additional call to {@link #loadCompleteLeaderboard(Date)} would be required
     * as <code>null</code> will be passed in any case, not being affected by local time offsets.
     */
    private boolean useNullAsTimePoint() {
        return timer.getPlayMode() == PlayModes.Live;
    }

    private void refreshMapWithoutAnimation() {
        removeTransitions();
        remoteCallsToSkipInExecution.addAll(remoteCallsInExecution);
    }

    private void updateMapWithWindInfo(final Date newTime, final long transitionTimeInMillis,
            final Iterable<CompetitorDTO> competitorsToShow, final WindInfoForRaceDTO windInfo,
            final List<com.sap.sse.common.Util.Pair<WindSource, WindTrackInfoDTO>> windSourcesToShow) {
        showAdvantageLine(competitorsToShow, newTime, transitionTimeInMillis);
        for (WindSource windSource : windInfo.windTrackInfoByWindSource.keySet()) {
            WindTrackInfoDTO windTrackInfoDTO = windInfo.windTrackInfoByWindSource.get(windSource);
            switch (windSource.getType()) {
            case EXPEDITION:
            case WINDFINDER:
                // we filter out measured wind sources with very low confidence
                if (windTrackInfoDTO.minWindConfidence > 0.0001) {
                    windSourcesToShow.add(new com.sap.sse.common.Util.Pair<WindSource, WindTrackInfoDTO>(windSource,
                            windTrackInfoDTO));
                }
                break;
            case COMBINED:
                showCombinedWindOnMap(windSource, windTrackInfoDTO);
                if (requiresCoordinateSystemUpdateWhenCoursePositionAndWindDirectionIsKnown) {
                    updateCoordinateSystemFromSettings();
                }
                break;
            default:
                // Which wind sources are requested is defined in a list above this
                // action. So we throw here an exception to notice a missing source.
                throw new UnsupportedOperationException("There is currently no support for the enum value '"
                        + windSource.getType() + "' in this method.");
            }
        }
    }

    private void refreshMap(final Date newTime, final long transitionTimeInMillis, boolean isRedraw) {
        final Iterable<CompetitorDTO> competitorsToShow = getCompetitorsToShow();
        final com.sap.sse.common.Util.Triple<Map<CompetitorDTO, Date>, Map<CompetitorDTO, Date>, Map<CompetitorDTO, Boolean>> fromAndToAndOverlap = fixesAndTails
                .computeFromAndTo(newTime, competitorsToShow, settings.getEffectiveTailLengthInMilliseconds());
        // Request map data update, possibly in two calls; see method details
        callGetRaceMapDataForAllOverlappingAndTipsOfNonOverlappingAndGetBoatPositionsForAllOthers(fromAndToAndOverlap,
                raceIdentifier, newTime, transitionTimeInMillis, competitorsToShow, isRedraw);
        // draw the wind into the map, get the combined wind
        List<String> windSourceTypeNames = new ArrayList<String>();
        windSourceTypeNames.add(WindSourceType.EXPEDITION.name());
        windSourceTypeNames.add(WindSourceType.WINDFINDER.name());
        windSourceTypeNames.add(WindSourceType.COMBINED.name());
        if (remoteCallsInExecution.add(newTime)) {
            if (currentlyDragging || zoomingAnimationsInProgress > 0) {
                remoteCallsToSkipInExecution.add(newTime);
            }
            GetWindInfoAction getWindInfoAction = new GetWindInfoAction(sailingService, raceIdentifier, newTime, 1000L,
                    1, windSourceTypeNames,
                    /* onlyUpToNewestEvent==false means get us any data we can get by a best effort */ false);
            asyncActionsExecutor.execute(getWindInfoAction, GET_WIND_DATA_CATEGORY,
                    new AsyncCallback<WindInfoForRaceDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            remoteCallsInExecution.remove(newTime);
                            errorReporter.reportError("Error obtaining wind information: " + caught.getMessage(),
                                    true /* silentMode */);
                        }

                        @Override
                        public void onSuccess(WindInfoForRaceDTO windInfo) {
                            remoteCallsInExecution.remove(newTime);
                            if (windInfo != null && !remoteCallsToSkipInExecution.remove(newTime)) {
                                List<com.sap.sse.common.Util.Pair<WindSource, WindTrackInfoDTO>> windSourcesToShow = new ArrayList<com.sap.sse.common.Util.Pair<WindSource, WindTrackInfoDTO>>();
                                lastCombinedWindTrackInfoDTO = windInfo;
                                updateMapWithWindInfo(newTime, transitionTimeInMillis, competitorsToShow, windInfo,
                                        windSourcesToShow);
                                showWindSensorsOnMap(windSourcesToShow);
                            }
                        }
                    });
        }
        else {
            GWT.log("added identical call to skip");
            remoteCallsToSkipInExecution.add(newTime);
        }
    }

    @Override
    public void timeChanged(final Date newTime, final Date oldTime) {
        boolean isRedraw = oldTime == null;
        if (newTime != null && isMapInitialized) {
            if (raceIdentifier != null) {
                if (raceIdentifier != null) {
                    final long transitionTimeInMillis = calculateTimeForPositionTransitionInMillis(newTime, oldTime);
                    refreshMap(newTime, transitionTimeInMillis, isRedraw);
                }
            }
        }
    }

    public WindInfoForRaceDTO getLastCombinedWindTrackInfoDTO() {
        return lastCombinedWindTrackInfoDTO;
    }
    /**
     * Requests updates for map data and, when received, updates the map structures accordingly.
     * <p>
     * 
     * The update can happen in one or two round trips. We assume that overlapping segments usually don't require a lot
     * of loading time as the most typical case will be to update a longer tail with a few new fixes that were received
     * since the last time tick. These supposedly fast requests are handled by a {@link GetRaceMapDataAction} which also
     * requests updates for mark positions, sidelines and quick ranks. The same request also loads boat positions for
     * the zero-length interval at <code>newTime</code> for the non-overlapping tails assuming that this will work
     * fairly fast and in particular in O(1) time regardless of tail length, compared to fetching the entire tail for
     * all competitors. This will at least provide quick feedback about those competitors' positions even if loading
     * their entire tail may take a little longer. The {@link GetRaceMapDataAction} therefore is intended to be a rather
     * quick call.
     * <p>
     * 
     * Non-overlapping position requests typically occur for the first request when no fix at all is known for the
     * competitor yet, or when the user has radically moved the time slider to some other time such that given the
     * current tail length setting the new tail segment does not overlap with the old one, requiring a full load of the
     * entire tail data for that competitor. For these non-overlapping requests, this method creates a
     * {@link GetBoatPositionsAction} request loading the entire tail required, but not quick ranks, sidelines and mark
     * positions. Updating the results of this call is done in {@link #updateBoatPositions(Date, long, Map, Iterable, Map, boolean)}.
     * <p>
     */
    private void callGetRaceMapDataForAllOverlappingAndTipsOfNonOverlappingAndGetBoatPositionsForAllOthers(
            final Triple<Map<CompetitorDTO, Date>, Map<CompetitorDTO, Date>, Map<CompetitorDTO, Boolean>> fromAndToAndOverlap,
            RegattaAndRaceIdentifier race, final Date newTime, final long transitionTimeInMillis,
            final Iterable<CompetitorDTO> competitorsToShow, boolean isRedraw) {
        final Map<CompetitorDTO, Date> fromTimesForQuickCall = new HashMap<>();
        final Map<CompetitorDTO, Date> toTimesForQuickCall = new HashMap<>();
        final Map<CompetitorDTO, Date> fromTimesForNonOverlappingTailsCall = new HashMap<>();
        final Map<CompetitorDTO, Date> toTimesForNonOverlappingTailsCall = new HashMap<>();
        for (Map.Entry<CompetitorDTO, Boolean> e : fromAndToAndOverlap.getC().entrySet()) {
            if (e.getValue()) {
                // overlap: expect a quick response; add original request interval for the competitor
                fromTimesForQuickCall.put(e.getKey(), fromAndToAndOverlap.getA().get(e.getKey()));
                toTimesForQuickCall.put(e.getKey(), fromAndToAndOverlap.getB().get(e.getKey()));
            } else {
                // no overlap; add competitor to request with a zero-length interval asking only position at newTime, not the entire tail
                fromTimesForQuickCall.put(e.getKey(), newTime);
                toTimesForQuickCall.put(e.getKey(), newTime);
                fromTimesForNonOverlappingTailsCall.put(e.getKey(), fromAndToAndOverlap.getA().get(e.getKey()));
                toTimesForNonOverlappingTailsCall.put(e.getKey(), fromAndToAndOverlap.getB().get(e.getKey()));
            }
        }
        final Map<String, CompetitorDTO> competitorsByIdAsString = new HashMap<>();
        for (CompetitorDTO competitor : competitorSelection.getAllCompetitors()) {
            competitorsByIdAsString.put(competitor.getIdAsString(), competitor);
        }

        // only update the tails for these competitors
        // Note: the fromAndToAndOverlap.getC() map will be UPDATED by the call to updateBoatPositions happening inside
        // the callback provided by getRaceMapDataCallback(...) for those
        // entries that are considered not overlapping; subsequently, fromAndToOverlap.getC() will contain true for
        // all its entries so that the other response received for GetBoatPositionsAction will consider this an
        // overlap if it happens after this update.
        asyncActionsExecutor.execute(new GetRaceMapDataAction(sailingService, competitorsByIdAsString,
            race, useNullAsTimePoint() ? null : newTime, fromTimesForQuickCall, toTimesForQuickCall, /* extrapolate */true,
                    (settings.isShowSimulationOverlay() ? simulationOverlay.getLegIdentifier() : null),
                    raceCompetitorSet.getMd5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID(), newTime, settings.isShowEstimatedDuration()),
            GET_RACE_MAP_DATA_CATEGORY,
                getRaceMapDataCallback(newTime, transitionTimeInMillis, fromAndToAndOverlap.getC(), competitorsToShow,
                        ++boatPositionRequestIDCounter, isRedraw));
        // next, if necessary, do the full thing; the two calls have different action classes, so throttling should not drop one for the other
        if (!fromTimesForNonOverlappingTailsCall.keySet().isEmpty()) {
            asyncActionsExecutor.execute(new GetBoatPositionsAction(sailingService, race, fromTimesForNonOverlappingTailsCall, toTimesForNonOverlappingTailsCall,
                    /* extrapolate */ true), GET_RACE_MAP_DATA_CATEGORY,
                    new MarkedAsyncCallback<>(new AsyncCallback<CompactBoatPositionsDTO>() {
                        @Override
                        public void onFailure(Throwable t) {
                            errorReporter.reportError("Error obtaining racemap data: " + t.getMessage(), true /*silentMode */);
                        }
                        
                        @Override
                        public void onSuccess(CompactBoatPositionsDTO result) {
                            // Note: the fromAndToAndOverlap.getC() map will be UPDATED by the call to updateBoatPositions for those
                            // entries that are considered not overlapping; subsequently, fromAndToOverlap.getC() will contain true for
                            // all its entries so that the other response received for GetRaceMapDataAction will consider this an
                            // overlap if it happens after this update.
                            updateBoatPositions(newTime, transitionTimeInMillis, fromAndToAndOverlap.getC(),
                                    competitorsToShow, result.getBoatPositionsForCompetitors(
                                            competitorsByIdAsString), /* updateTailsOnly */
                                    true);
                        }
                    }));
        }
        else {
        }
    }

    private AsyncCallback<RaceMapDataDTO> getRaceMapDataCallback(
            final Date newTime,
            final long transitionTimeInMillis,
            final Map<CompetitorDTO, Boolean> hasTailOverlapForCompetitor,
            final Iterable<CompetitorDTO> competitorsToShow, final int requestID, boolean isRedraw) {
        remoteCallsInExecution.add(newTime);
        return new MarkedAsyncCallback<>(new AsyncCallback<RaceMapDataDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error obtaining racemap data: " + caught.getMessage(), true /*silentMode */);
            }
            
            @Override
            public void onSuccess(RaceMapDataDTO raceMapDataDTO) {
                remoteCallsInExecution.remove(newTime);
                if (map != null && raceMapDataDTO != null && !remoteCallsToSkipInExecution.remove(newTime)) {
                    // process response only if not received out of order
                    if (startedProcessingRequestID < requestID) {
                        startedProcessingRequestID = requestID;
                        if (raceMapDataDTO.raceCompetitorIdsAsStrings != null) {
                            try {
                                raceCompetitorSet.setIdsAsStringsOfCompetitorsInRace(raceMapDataDTO.raceCompetitorIdsAsStrings);
                            } catch (Exception e) {
                                GWT.log("Error trying to update competitor set for race "+raceIdentifier.getRaceName()+
                                        " in regatta "+raceIdentifier.getRegattaName(), e);
                            }
                        }
                        quickRanksDTOProvider.quickRanksReceivedFromServer(raceMapDataDTO.quickRanks);
                        if (isSimulationEnabled && settings.isShowSimulationOverlay()) {
                            lastLegNumber = raceMapDataDTO.coursePositions.currentLegNumber;
                                simulationOverlay.updateLeg(Math.max(lastLegNumber, 1), /* clearCanvas */ false, raceMapDataDTO.simulationResultVersion);
                        }
                        // Do boat specific actions
                        Map<CompetitorDTO, List<GPSFixDTOWithSpeedWindTackAndLegType>> boatData = raceMapDataDTO.boatPositions;
                        updateBoatPositions(newTime, transitionTimeInMillis, hasTailOverlapForCompetitor,
                                competitorsToShow, boatData, /* updateTailsOnly */ false);
                        
                        if (!isRedraw) {
                            // only remove markers if the time is actually changed
                            if (douglasMarkers != null) {
                                removeAllMarkDouglasPeuckerpoints();
                            }
                            maneuverMarkersAndLossIndicators.clearAllManeuverMarkers();
                        }
                        if (requiresCoordinateSystemUpdateWhenCoursePositionAndWindDirectionIsKnown) {
                            updateCoordinateSystemFromSettings();
                        }
                        // Do mark specific actions
                        showCourseMarksOnMap(raceMapDataDTO.coursePositions, transitionTimeInMillis);
                        showCourseSidelinesOnMap(raceMapDataDTO.courseSidelines);
                        showStartAndFinishAndCourseMiddleLines(raceMapDataDTO.coursePositions);
                        showStartLineToFirstMarkTriangle(raceMapDataDTO.coursePositions);
                            
                        // Rezoom the map
                        LatLngBounds zoomToBounds = null;
                        if (!settings.getZoomSettings().containsZoomType(ZoomTypes.NONE)) { // Auto zoom if setting is not manual
                            zoomToBounds = settings.getZoomSettings().getNewBounds(RaceMap.this);
                            if (zoomToBounds == null && !mapFirstZoomDone) {
                                zoomToBounds = getDefaultZoomBounds(); // the user-specified zoom couldn't find what it was looking for; try defaults once
                            }
                        } else if (!mapFirstZoomDone) { // Zoom once to the marks if marks exist
                            zoomToBounds = new CourseMarksBoundsCalculator().calculateNewBounds(RaceMap.this);
                            if (zoomToBounds == null) {
                                zoomToBounds = getDefaultZoomBounds(); // use default zoom, e.g., 
                            }
                            /*
                             * Reset the mapZoomedOrPannedSinceLastRaceSelection: In spite of the fact that
                             * the map was just zoomed to the bounds of the marks, it was not a zoom or pan
                             * triggered by the user. As a consequence the
                             * mapZoomedOrPannedSinceLastRaceSelection option has to reset again.
                             */
                        }
                        zoomMapToNewBounds(zoomToBounds);
                        mapFirstZoomDone = true;
                        updateEstimatedDuration(raceMapDataDTO.estimatedDuration);
                    }
                } else {
                    lastTimeChangeBeforeInitialization = newTime;
                }
            }
        });
    }

    protected void updateEstimatedDuration(Duration estimatedDuration) {
        if (estimatedDuration == null) {
            return;
        }
        if (estimatedDurationOverlay == null) {
            estimatedDurationOverlay = new Label("");
            estimatedDurationOverlay.setStyleName(raceMapStyle.estimatedDuration());
            if(showHeaderPanel) {
                estimatedDurationOverlay.addStyleName(raceMapStyle.estimatedDurationWithHeader());
            }
            map.setControls(ControlPosition.TOP_CENTER, estimatedDurationOverlay);
        }
        estimatedDurationOverlay.setText(stringMessages.estimatedDuration()
                + " " + DateAndTimeFormatterUtil.formatElapsedTime(estimatedDuration.asMillis()));

    }

    /**
     * @param hasTailOverlapForCompetitor
     *            if for a competitor whose fixes are provided in <code>fixesForCompetitors</code> this holds
     *            <code>false</code>, any fixes previously stored for that competitor are removed, and the tail is
     *            deleted from the map (see {@link #removeTail(CompetitorWithBoatDTO)}); the new fixes are then added to the
     *            {@link #fixes} map, and a new tail will have to be constructed as needed (does not happen here). If
     *            this map holds <code>true</code>, {@link #mergeFixes(CompetitorWithBoatDTO, List, long)} is used to merge the
     *            new fixes from <code>fixesForCompetitors</code> into the {@link #fixes} collection, and the tail is
     *            left unchanged. <b>NOTE:</b> When a non-overlapping set of fixes is updated (<code>false</code>), this
     *            map's record for the competitor is <b>UPDATED</b> to <code>true</code> after the tail deletion and
     *            {@link #fixes} replacement has taken place. This helps in cases where this update is only one of two
     *            into which an original request was split (one quick update of the tail's head and another one for the
     *            longer tail itself), such that the second request that uses the <em>same</em> map will be considered
     *            having an overlap now, not leading to a replacement of the previous update originating from the same
     *            request. See also {@link FixesAndTails#updateFixes(Map, Map, TailFactory, long)}.
     * @param updateTailsOnly
     *            if <code>true</code>, only the tails are updated according to <code>boatData</code> and
     *            <code>hasTailOverlapForCompetitor</code>, but the advantage line is not updated, and neither are the
     *            competitor info bubbles moved. This assumes that the tips of these tails are loaded in a separate call
     *            which <em>does</em> update those structures. In particular, tails that do not appear in
     *            <code>boatData</code> are not removed from the map in case <code>updateTailsOnly</code> is
     *            <code>true</code>.
     */
    private void updateBoatPositions(final Date newTime, final long transitionTimeInMillis,
            final Map<CompetitorDTO, Boolean> hasTailOverlapForCompetitor,
            final Iterable<CompetitorDTO> competitorsToShow, Map<CompetitorDTO, List<GPSFixDTOWithSpeedWindTackAndLegType>> boatData, boolean updateTailsOnly) {
        if (zoomingAnimationsInProgress == 0 && !currentlyDragging) {
            fixesAndTails.updateFixes(boatData, hasTailOverlapForCompetitor, RaceMap.this, transitionTimeInMillis);
            showBoatsOnMap(newTime, transitionTimeInMillis,
                    /* re-calculate; it could have changed since the asynchronous request was made: */
                    getCompetitorsToShow(), updateTailsOnly);
            if (!updateTailsOnly) {
                showCompetitorInfoOnMap(newTime, transitionTimeInMillis,
                        competitorSelection.getSelectedFilteredCompetitors());

                // even though the wind data is retrieved by a separate call, re-draw the advantage line because it
                // needs to adjust to new boat positions
                showAdvantageLine(competitorsToShow, newTime, transitionTimeInMillis);
            }
        }
    }

    private void showCourseSidelinesOnMap(List<SidelineDTO> sidelinesDTOs) {
        if (map != null && sidelinesDTOs != null ) {
            Map<SidelineDTO, Polygon> toRemoveSidelines = new HashMap<SidelineDTO, Polygon>(courseSidelines);
            for (SidelineDTO sidelineDTO : sidelinesDTOs) {
                if (sidelineDTO.getMarks().size() == 2) { // right now we only support sidelines with 2 marks
                    Polygon sideline = courseSidelines.get(sidelineDTO);
                    LatLng[] sidelinePoints = new LatLng[sidelineDTO.getMarks().size()];
                    int i=0;
                    for (MarkDTO sidelineMark : sidelineDTO.getMarks()) {
                        sidelinePoints[i] = coordinateSystem.toLatLng(sidelineMark.position);
                        i++;
                    }
                    if (sideline == null) {
                        PolygonOptions options = PolygonOptions.newInstance();
                        options.setClickable(true);
                        options.setStrokeColor("#0000FF");
                        options.setStrokeWeight(1);
                        options.setStrokeOpacity(1.0);
                        options.setFillColor(null);
                        options.setFillOpacity(1.0);
                        
                        sideline = Polygon.newInstance(options);
                        MVCArray<LatLng> pointsAsArray = MVCArray.newInstance(sidelinePoints);
                        sideline.setPath(pointsAsArray);

                        sideline.addMouseOverHandler(new MouseOverMapHandler() {
                            @Override
                            public void onEvent(MouseOverMapEvent event) {
                                map.setTitle(stringMessages.sideline());
                            }
                        });
                        sideline.addMouseOutMoveHandler(new MouseOutMapHandler() {
                            @Override
                            public void onEvent(MouseOutMapEvent event) {
                                map.setTitle("");
                            }
                        });
                        courseSidelines.put(sidelineDTO, sideline);
                        sideline.setMap(map);
                    } else {
                        sideline.getPath().removeAt(1);
                        sideline.getPath().removeAt(0);
                        sideline.getPath().insertAt(0, sidelinePoints[0]);
                        sideline.getPath().insertAt(1, sidelinePoints[1]);
                        toRemoveSidelines.remove(sidelineDTO);
                    }
                }
            }
            for (SidelineDTO toRemoveSideline : toRemoveSidelines.keySet()) {
                Polygon sideline = courseSidelines.remove(toRemoveSideline);
                sideline.setMap(null);
            }
        }
    }
       
    private void showCourseMarksOnMap(CoursePositionsDTO courseDTO, long transitionTimeInMillis) {
        if (zoomingAnimationsInProgress == 0 && !currentlyDragging) {
            if (map != null && courseDTO != null) {
                WaypointDTO endWaypointForCurrentLegNumber = null;
                if (courseDTO.currentLegNumber > 0 && courseDTO.currentLegNumber <= courseDTO.totalLegsCount) {
                    endWaypointForCurrentLegNumber = courseDTO.getEndWaypointForLegNumber(courseDTO.currentLegNumber);
                }

                Map<String, CourseMarkOverlay> toRemoveCourseMarks = new HashMap<String, CourseMarkOverlay>(courseMarkOverlays);
                if (courseDTO.marks != null) {
                    for (MarkDTO markDTO : courseDTO.marks) {
                        boolean isSelected = false;
                        if (endWaypointForCurrentLegNumber != null && Util.contains(endWaypointForCurrentLegNumber.controlPoint.getMarks(), markDTO)) {
                            isSelected = true;
                        }
                        CourseMarkOverlay courseMarkOverlay = courseMarkOverlays.get(markDTO.getIdAsString());
                        if (courseMarkOverlay == null) {
                            courseMarkOverlay = new CourseMarkOverlay(map, RaceMapOverlaysZIndexes.COURSEMARK_ZINDEX, markDTO, coordinateSystem, courseDTO);
                            courseMarkOverlay.setShowBuoyZone(settings.getHelpLinesSettings().isVisible(HelpLineTypes.BUOYZONE));
                            courseMarkOverlay.setBuoyZoneRadius(settings.getBuoyZoneRadius());
                            courseMarkOverlay.setSelected(isSelected);
                            courseMarkOverlays.put(markDTO.getIdAsString(), courseMarkOverlay);
                            markDTOs.put(markDTO.getIdAsString(), markDTO);
                            registerCourseMarkInfoWindowClickHandler(markDTO.getIdAsString());
                            courseMarkOverlay.addToMap();
                        } else {
                            courseMarkOverlay.setMarkPosition(markDTO.position, transitionTimeInMillis);
                            courseMarkOverlay.setShowBuoyZone(settings.getHelpLinesSettings().isVisible(HelpLineTypes.BUOYZONE));
                            courseMarkOverlay.setBuoyZoneRadius(settings.getBuoyZoneRadius());
                            courseMarkOverlay.setSelected(isSelected);
                            courseMarkOverlay.setCourse(courseDTO);
                            courseMarkOverlay.draw();
                            toRemoveCourseMarks.remove(markDTO.getIdAsString());
                        }
                    }
                }
                for (String toRemoveMarkIdAsString : toRemoveCourseMarks.keySet()) {
                    CourseMarkOverlay removedOverlay = courseMarkOverlays.remove(toRemoveMarkIdAsString);
                    if (removedOverlay != null) {
                        removedOverlay.removeFromMap();
                    }
                }
            }
        }
    }
    
    /**
     * Based on the mark positions in {@link #courseMarkOverlays}' values this method determines the center of gravity of these marks'
     * {@link CourseMarkOverlay#getPosition() positions}.
     */
    private Position getCenterOfCourse() {
        ScalablePosition center = null;
        int count = 0;
        for (CourseMarkOverlay markOverlay : courseMarkOverlays.values()) {
            ScalablePosition markPosition = new ScalablePosition(markOverlay.getPosition());
            if (center == null) {
                center = markPosition;
            } else {
                center.add(markPosition);
            }
            count++;
        }
        return center == null ? null : center.divide(count);
    }

    private void showCombinedWindOnMap(WindSource windSource, WindTrackInfoDTO windTrackInfoDTO) {
        if (map != null) {
            combinedWindPanel.setWindInfo(windTrackInfoDTO, windSource);
            combinedWindPanel.redraw();
        }
    }

    private void showWindSensorsOnMap(List<com.sap.sse.common.Util.Pair<WindSource, WindTrackInfoDTO>> windSensorsList) {
        if (map != null) {
            Set<WindSource> toRemoveWindSources = new HashSet<WindSource>(windSensorOverlays.keySet());
            for (com.sap.sse.common.Util.Pair<WindSource, WindTrackInfoDTO> windSourcePair : windSensorsList) {
                WindSource windSource = windSourcePair.getA(); 
                WindTrackInfoDTO windTrackInfoDTO = windSourcePair.getB();

                WindSensorOverlay windSensorOverlay = windSensorOverlays.get(windSource);
                if (windSensorOverlay == null) {
                    windSensorOverlay = createWindSensorOverlay(RaceMapOverlaysZIndexes.WINDSENSOR_ZINDEX, windSource, windTrackInfoDTO);
                    windSensorOverlays.put(windSource, windSensorOverlay);
                    windSensorOverlay.addToMap();
                } else {
                    windSensorOverlay.setWindInfo(windTrackInfoDTO, windSource);
                    windSensorOverlay.draw();
                    toRemoveWindSources.remove(windSource);
                }
            }
            for (WindSource toRemoveWindSource : toRemoveWindSources) {
                WindSensorOverlay removedWindSensorOverlay = windSensorOverlays.remove(toRemoveWindSource);
                if (removedWindSensorOverlay != null) {
                    removedWindSensorOverlay.removeFromMap();
                }
            }
        }
    }

    private void showCompetitorInfoOnMap(final Date newTime, final long timeForPositionTransitionMillis, final Iterable<CompetitorDTO> competitorsToShow) {
        if (map != null) {
            if (settings.isShowSelectedCompetitorsInfo()) {
                Set<String> toRemoveCompetorInfoOverlaysIdsAsStrings = new HashSet<>();
                Util.addAll(competitorInfoOverlays.getCompetitorIdsAsStrings(), toRemoveCompetorInfoOverlaysIdsAsStrings);
                for (CompetitorDTO competitorDTO : competitorsToShow) {
                    if (fixesAndTails.hasFixesFor(competitorDTO)) {
                        GPSFixDTOWithSpeedWindTackAndLegType lastBoatFix = getBoatFix(competitorDTO, newTime);
                        if (lastBoatFix != null) {
                            CompetitorInfoOverlay competitorInfoOverlay = competitorInfoOverlays.get(competitorDTO);
                            final Integer rank = getRank(competitorDTO);
                            if (competitorInfoOverlay == null) {
                                competitorInfoOverlay = competitorInfoOverlays.createCompetitorInfoOverlay(RaceMapOverlaysZIndexes.INFO_OVERLAY_ZINDEX, competitorDTO,
                                        lastBoatFix, rank, timeForPositionTransitionMillis);
                                competitorInfoOverlay.addToMap();
                            } else {
                                competitorInfoOverlays.updatePosition(competitorDTO, lastBoatFix, timeForPositionTransitionMillis);
                            }
                            toRemoveCompetorInfoOverlaysIdsAsStrings.remove(competitorDTO.getIdAsString());
                        }
                    }
                }
                for (String toRemoveCompetitorOverlayIdAsString : toRemoveCompetorInfoOverlaysIdsAsStrings) {
                    competitorInfoOverlays.remove(toRemoveCompetitorOverlayIdAsString);
                }
            } else {
                // remove all overlays
                competitorInfoOverlays.clear();
            }
        }
    }

    private long calculateTimeForPositionTransitionInMillis(final Date newTime, final Date oldTime) {
        final long timeForPositionTransitionMillisSmoothed;
        final long timeForPositionTransitionMillis;
        if (newTime != null && oldTime != null) {
            timeForPositionTransitionMillis = newTime.getTime() - oldTime.getTime();
        } else {
            timeForPositionTransitionMillis = -1;
        }
        if (timer.getPlayState() == PlayStates.Playing) {
            // choose 130% of the refresh interval as transition period to make it unlikely that the transition
            // stops before the next update has been received
            long smoothIntervall = 1300 * timer.getRefreshInterval() / 1000;
            if (timeForPositionTransitionMillis > 0 && timeForPositionTransitionMillis < smoothIntervall) {
                timeForPositionTransitionMillisSmoothed = smoothIntervall;
            } else {
                // either a large transition positive transition happend or any negative one, do not use the smooth
                // value
                if (timeForPositionTransitionMillis > 0) {
                    timeForPositionTransitionMillisSmoothed = timeForPositionTransitionMillis;
                } else {
                    timeForPositionTransitionMillisSmoothed = -1;
                }
            }

        } else {
            // do not animate in non live modus
            timeForPositionTransitionMillisSmoothed = -1; // -1 means 'no transition
        }
        return timeForPositionTransitionMillisSmoothed;
    }
    
    /**
     * @param updateTailsOnly
     *            if <code>false</code>, tails of competitors not in <code>competitorsToShow</code> are removed from the
     *            map
     */
    private void showBoatsOnMap(final Date newTime, final long timeForPositionTransitionMillis,
            final Iterable<CompetitorDTO> competitorsToShow, boolean updateTailsOnly) {
        if (map != null) {
            Date tailsFromTime = new Date(newTime.getTime() - settings.getEffectiveTailLengthInMilliseconds());
            Date tailsToTime = newTime;
            Set<CompetitorDTO> competitorDTOsOfUnusedTails = new HashSet<>();
            Set<CompetitorDTO> competitorDTOsOfUnusedBoatCanvases = new HashSet<>();
            if (!updateTailsOnly) {
                competitorDTOsOfUnusedTails.addAll(fixesAndTails.getCompetitorsWithTails());
                competitorDTOsOfUnusedBoatCanvases.addAll(boatOverlays.keySet());
            }
            for (CompetitorDTO competitorDTO : competitorsToShow) {
                boolean hasTimeJumped = timeForPositionTransitionMillis > 3 * timer.getRefreshInterval();
                if (hasTimeJumped) {
                    fixesAndTails.clearTails();
                }
                if (fixesAndTails.hasFixesFor(competitorDTO)) {
                    if (!fixesAndTails.hasTail(competitorDTO)) {
                        fixesAndTails.createTailAndUpdateIndices(competitorDTO, tailsFromTime, tailsToTime, this);
                    } else {
                        fixesAndTails.updateTail(competitorDTO, tailsFromTime, tailsToTime, (int) (timeForPositionTransitionMillis==-1?-1:timeForPositionTransitionMillis/2));
                        if (!updateTailsOnly) {
                            competitorDTOsOfUnusedTails.remove(competitorDTO);
                        }
                        PolylineOptions newOptions = createTailStyle(competitorDTO, displayHighlighted(competitorDTO));
                        fixesAndTails.getTail(competitorDTO).setOptions(newOptions);
                    }
                    boolean usedExistingBoatCanvas = updateBoatCanvasForCompetitor(competitorDTO, newTime,
                            timeForPositionTransitionMillis);
                    if (usedExistingBoatCanvas && !updateTailsOnly) {
                        competitorDTOsOfUnusedBoatCanvases.remove(competitorDTO);
                    }
                }
            }
            if (!updateTailsOnly) {
                for (CompetitorDTO unusedBoatCanvasCompetitorDTO : competitorDTOsOfUnusedBoatCanvases) {
                    CanvasOverlayV3 boatCanvas = boatOverlays.get(unusedBoatCanvasCompetitorDTO);
                    boatCanvas.removeFromMap();
                    boatOverlays.remove(unusedBoatCanvasCompetitorDTO);
                }
                for (CompetitorDTO unusedTailCompetitorDTO : competitorDTOsOfUnusedTails) {
                    fixesAndTails.removeTail(unusedTailCompetitorDTO);
                }
            }
        }
    }

    /**
     * This algorithm is limited to distances such that dlon < pi/2, i.e., those that extend around less than one
     * quarter of the circumference of the earth in longitude. A completely general, but more complicated algorithm is
     * necessary if greater distances are allowed.
     */
    public LatLng calculatePositionAlongRhumbline(LatLng position, double bearingDeg, Distance distance) {
        double distanceRad = distance.getCentralAngleRad(); 
        double lat1 = position.getLatitude() / 180. * Math.PI;
        double lon1 = position.getLongitude() / 180. * Math.PI;
        double bearingRad = bearingDeg / 180. * Math.PI;
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distanceRad) + 
                        Math.cos(lat1) * Math.sin(distanceRad) * Math.cos(bearingRad));
        double lon2 = lon1 + Math.atan2(Math.sin(bearingRad)*Math.sin(distanceRad)*Math.cos(lat1), 
                       Math.cos(distanceRad)-Math.sin(lat1)*Math.sin(lat2));
        lon2 = (lon2+3*Math.PI) % (2*Math.PI) - Math.PI;  // normalize to -180..+180�
        // position is already in LatLng space, so no mapping through coordinateSystem is required here
        return LatLng.newInstance(lat2 / Math.PI * 180., lon2  / Math.PI * 180.);
    }
    
    /**
     * Returns a pair whose first component is the leg number (one-based) of the competitor returned as the second component.
     * The competitor returned currently has the best ranking in the quick ranks provided by the {@link #quickRanksDTOProvider}.
     */
    private com.sap.sse.common.Util.Pair<Integer, CompetitorDTO> getBestVisibleCompetitorWithOneBasedLegNumber(
            Iterable<CompetitorDTO> competitorsToShow) {
        CompetitorDTO leadingCompetitorDTO = null;
        int legOfLeaderCompetitor = -1;
        int bestOneBasedRank = Integer.MAX_VALUE;
        for (QuickRankDTO competitorFromBestToWorstAndOneBasedLegNumber : quickRanksDTOProvider.getQuickRanks().values()) {
            if (Util.contains(competitorsToShow, competitorFromBestToWorstAndOneBasedLegNumber.competitor) && 
                    competitorFromBestToWorstAndOneBasedLegNumber.legNumberOneBased != 0 &&
                    competitorFromBestToWorstAndOneBasedLegNumber.oneBasedRank < bestOneBasedRank) {
                leadingCompetitorDTO = competitorFromBestToWorstAndOneBasedLegNumber.competitor;
                legOfLeaderCompetitor = competitorFromBestToWorstAndOneBasedLegNumber.legNumberOneBased;
                bestOneBasedRank = competitorFromBestToWorstAndOneBasedLegNumber.oneBasedRank;
                if (bestOneBasedRank == 1) {
                    break; // as good as it gets
                }
            }
        }
        return leadingCompetitorDTO == null ? null :
            new com.sap.sse.common.Util.Pair<Integer, CompetitorDTO>(legOfLeaderCompetitor, leadingCompetitorDTO);
    }

    final static Distance advantageLineLength = new MeterDistance(1000); // TODO this should probably rather scale with the visible area of the map; bug 616
    private void showAdvantageLine(Iterable<CompetitorDTO> competitorsToShow, Date date, long timeForPositionTransitionMillis) {
        if (map != null && lastRaceTimesInfo != null && !quickRanksDTOProvider.getQuickRanks().isEmpty()
                && lastCombinedWindTrackInfoDTO != null) {
            boolean drawAdvantageLine = false;
            if (settings.getHelpLinesSettings().isVisible(HelpLineTypes.ADVANTAGELINE)) {
                // find competitor with highest rank
                com.sap.sse.common.Util.Pair<Integer, CompetitorDTO> visibleLeaderInfo = getBestVisibleCompetitorWithOneBasedLegNumber(competitorsToShow);
                // the boat fix may be null; may mean that no positions were loaded yet for the leading visible boat;
                // don't show anything
                GPSFixDTOWithSpeedWindTackAndLegType lastBoatFix = null;
                boolean isVisibleLeaderInfoComplete = false;
                boolean isLegTypeKnown = false;
                WindTrackInfoDTO windDataForLegMiddle = null;
                LegInfoDTO legInfoDTO = null;
                if (visibleLeaderInfo != null
                        && visibleLeaderInfo.getA() > 0
                        && visibleLeaderInfo.getA() <= lastRaceTimesInfo.getLegInfos().size()
                        // get wind at middle of leg for leading visible competitor
                        && (windDataForLegMiddle = lastCombinedWindTrackInfoDTO
                                .getCombinedWindOnLegMiddle(visibleLeaderInfo.getA() - 1)) != null
                        && !windDataForLegMiddle.windFixes.isEmpty()) {
                    isVisibleLeaderInfoComplete = true;
                    legInfoDTO = lastRaceTimesInfo.getLegInfos().get(visibleLeaderInfo.getA() - 1);
                    if (legInfoDTO.legType != null) {
                        isLegTypeKnown = true;
                    }
                    lastBoatFix = getBoatFix(visibleLeaderInfo.getB(), date);
                }
                if (isVisibleLeaderInfoComplete && isLegTypeKnown && lastBoatFix != null && lastBoatFix.speedWithBearing != null) {
                    BoatDTO boat = competitorSelection.getBoat(visibleLeaderInfo.getB());
                    Distance distanceFromBoatPositionInKm = boat.getBoatClass().getHullLength(); // one hull length
                    // implement and use Position.translateRhumb()
                    double bearingOfBoatInDeg = lastBoatFix.speedWithBearing.bearingInDegrees;
                    LatLng boatPosition = coordinateSystem.toLatLng(lastBoatFix.position);
                    LatLng posAheadOfFirstBoat = calculatePositionAlongRhumbline(boatPosition,
                            coordinateSystem.mapDegreeBearing(bearingOfBoatInDeg), distanceFromBoatPositionInKm);
                    final WindDTO windFix = windDataForLegMiddle.windFixes.get(0);
                    double bearingOfCombinedWindInDeg = windFix.trueWindBearingDeg;
                    double rotatedBearingDeg1 = 0.0;
                    double rotatedBearingDeg2 = 0.0;
                    if (lastBoatFix.legType == null) {
                        GWT.log("no legType to display advantage line");
                    } else {
                        switch (lastBoatFix.legType) {
                        case UPWIND:
                        case DOWNWIND: {
                            rotatedBearingDeg1 = bearingOfCombinedWindInDeg + 90.0;
                            if (rotatedBearingDeg1 >= 360.0) {
                                rotatedBearingDeg1 -= 360.0;
                            }
                            rotatedBearingDeg2 = bearingOfCombinedWindInDeg - 90.0;
                            if (rotatedBearingDeg2 < 0.0) {
                                rotatedBearingDeg2 += 360.0;
                            }
                                break;
                        }
                        case REACHING: {
                            rotatedBearingDeg1 = legInfoDTO.legBearingInDegrees + 90.0;
                            if (rotatedBearingDeg1 >= 360.0) {
                                rotatedBearingDeg1 -= 360.0;
                            }
                            rotatedBearingDeg2 = legInfoDTO.legBearingInDegrees - 90.0;
                            if (rotatedBearingDeg2 < 0.0) {
                                rotatedBearingDeg2 += 360.0;
                            }
                                break;
                        }
                        }
                        MVCArray<LatLng> nextPath = MVCArray.newInstance();
                        LatLng advantageLinePos1 = calculatePositionAlongRhumbline(posAheadOfFirstBoat,
                                coordinateSystem.mapDegreeBearing(rotatedBearingDeg1), advantageLineLength.scale(0.5));
                        LatLng advantageLinePos2 = calculatePositionAlongRhumbline(posAheadOfFirstBoat,
                                coordinateSystem.mapDegreeBearing(rotatedBearingDeg2), advantageLineLength.scale(0.5));
                        if (advantageLine == null) {
                            PolylineOptions options = PolylineOptions.newInstance();
                            options.setClickable(true);
                            options.setGeodesic(true);
                            options.setStrokeColor(ADVANTAGE_LINE_COLOR);
                            options.setStrokeWeight(1);
                            options.setStrokeOpacity(0.5);
                            
                            advantageLine = Polyline.newInstance(options);
                            advantageTimer = new AdvantageLineAnimator(advantageLine);
                            MVCArray<LatLng> pointsAsArray = MVCArray.newInstance();
                            pointsAsArray.insertAt(0, advantageLinePos1);
                            pointsAsArray.insertAt(1, advantageLinePos2);
                            advantageLine.setPath(pointsAsArray);
                            advantageLine.setMap(map);
                            Hoverline advantageHoverline = new Hoverline(advantageLine, options, this);
                            
                            advantageLineMouseOverHandler = new AdvantageLineMouseOverMapHandler(
                                    bearingOfCombinedWindInDeg, new Date(windFix.measureTimepoint));
                            advantageLine.addMouseOverHandler(advantageLineMouseOverHandler);
                            advantageHoverline.addMouseOutMoveHandler(new MouseOutMapHandler() {
                                @Override
                                public void onEvent(MouseOutMapEvent event) {
                                    map.setTitle("");
                                }
                            });
                        } else {
                            nextPath.push(advantageLinePos1);
                            nextPath.push(advantageLinePos2);
                            advantageTimer.setNextPositionAndTransitionMillis(nextPath, timeForPositionTransitionMillis);
                            if (advantageLineMouseOverHandler != null) {
                                advantageLineMouseOverHandler.setTrueWindBearing(bearingOfCombinedWindInDeg);
                                advantageLineMouseOverHandler.setDate(new Date(windFix.measureTimepoint));
                            }
                        }
                        drawAdvantageLine = true;
                        advantageLineCompetitor = visibleLeaderInfo.getB();
                    }
                }
            }
            if (!drawAdvantageLine) {
                if (advantageLine != null) {
                    advantageLine.setMap(null);
                    advantageLine = null;
                    advantageTimer = null;
                }
            }
        }
    }
    
    private final StringBuilder windwardStartLineMarkToFirstMarkLineText = new StringBuilder();
    private final StringBuilder leewardStartLineMarkToFirstMarkLineText = new StringBuilder();
    
    private void showStartLineToFirstMarkTriangle(final CoursePositionsDTO courseDTO){
        final List<Position> startMarkPositions = courseDTO.getStartMarkPositions();
        if (startMarkPositions.size() > 1 && courseDTO.waypointPositions.size() > 1) {
            final Position windwardStartLinePosition = startMarkPositions.get(0);
            final Position leewardStartLinePosition = startMarkPositions.get(1);
            final Position firstMarkPosition = courseDTO.waypointPositions.get(1);
            windwardStartLineMarkToFirstMarkLineText.replace(0, windwardStartLineMarkToFirstMarkLineText.length(),
                    stringMessages.startLineToFirstMarkTriangle(numberFormatOneDecimal
                            .format(windwardStartLinePosition.getDistance(firstMarkPosition)
                                    .getMeters())));
            leewardStartLineMarkToFirstMarkLineText.replace(0, leewardStartLineMarkToFirstMarkLineText.length(),
                    stringMessages.startLineToFirstMarkTriangle(numberFormatOneDecimal
                            .format(leewardStartLinePosition.getDistance(firstMarkPosition)
                                    .getMeters())));
            final LineInfoProvider windwardStartLineMarkToFirstMarkLineInfoProvider = new LineInfoProvider() {
                @Override
                public String getLineInfo() {
                    return windwardStartLineMarkToFirstMarkLineText.toString();
                }
            };
            final LineInfoProvider leewardStartLineMarkToFirstMarkLineInfoProvider = new LineInfoProvider() {
                @Override
                public String getLineInfo() {
                    return leewardStartLineMarkToFirstMarkLineText.toString();
                }
            };
            windwardStartLineMarkToFirstMarkLine = showOrRemoveOrUpdateLine(windwardStartLineMarkToFirstMarkLine, /* showLine */
                    (settings.getHelpLinesSettings().isVisible(HelpLineTypes.STARTLINETOFIRSTMARKTRIANGLE))
                            && startMarkPositions.size() > 1 && courseDTO.waypointPositions.size() > 1,
                    windwardStartLinePosition, firstMarkPosition, windwardStartLineMarkToFirstMarkLineInfoProvider,
                    "grey", STANDARD_LINE_STROKEWEIGHT, STANDARD_LINE_OPACITY);
            leewardStartLineMarkToFirstMarkLine = showOrRemoveOrUpdateLine(leewardStartLineMarkToFirstMarkLine, /* showLine */
                    (settings.getHelpLinesSettings().isVisible(HelpLineTypes.STARTLINETOFIRSTMARKTRIANGLE))
                            && startMarkPositions.size() > 1 && courseDTO.waypointPositions.size() > 1,
                    leewardStartLinePosition, firstMarkPosition, leewardStartLineMarkToFirstMarkLineInfoProvider,
                    "grey", STANDARD_LINE_STROKEWEIGHT, STANDARD_LINE_OPACITY);
        }
    }

    private final StringBuilder startLineAdvantageText = new StringBuilder();
    private final StringBuilder finishLineAdvantageText = new StringBuilder();
    final LineInfoProvider startLineInfoProvider = new LineInfoProvider() {
        @Override
        public String getLineInfo() {
            return stringMessages.startLine()+startLineAdvantageText;
        }
    };
    final LineInfoProvider finishLineInfoProvider = new LineInfoProvider() {
        @Override
        public String getLineInfo() {
            return stringMessages.finishLine()+finishLineAdvantageText;
        }
    };

    private void showStartAndFinishAndCourseMiddleLines(final CoursePositionsDTO courseDTO) {
        if (map != null && courseDTO != null && courseDTO.course != null && courseDTO.course.waypoints != null &&
                !courseDTO.course.waypoints.isEmpty()) {
            // draw the start line
            final WaypointDTO startWaypoint = courseDTO.course.waypoints.get(0);
            updateCountdownCanvas(startWaypoint);
            final int numberOfStartWaypointMarks = courseDTO.getStartMarkPositions() == null ? 0 : courseDTO.getStartMarkPositions().size();
            final int numberOfFinishWaypointMarks = courseDTO.getFinishMarkPositions() == null ? 0 : courseDTO.getFinishMarkPositions().size();
            final Position startLineLeftPosition = numberOfStartWaypointMarks == 0 ? null : courseDTO.getStartMarkPositions().get(0);
            final Position startLineRightPosition = numberOfStartWaypointMarks < 2 ? null : courseDTO.getStartMarkPositions().get(1);
            if (courseDTO.startLineAngleFromPortToStarboardWhenApproachingLineToCombinedWind != null) {
                startLineAdvantageText.replace(0, startLineAdvantageText.length(), " "+stringMessages.lineAngleToWindAndAdvantage(
                        numberFormatOneDecimal.format(courseDTO.startLineLengthInMeters),
                        numberFormatOneDecimal.format(Math.abs(courseDTO.startLineAngleFromPortToStarboardWhenApproachingLineToCombinedWind)),
                        courseDTO.startLineAdvantageousSide.name().charAt(0)+courseDTO.startLineAdvantageousSide.name().substring(1).toLowerCase(),
                        numberFormatOneDecimal.format(courseDTO.startLineAdvantageInMeters)));
            } else {
                startLineAdvantageText.delete(0, startLineAdvantageText.length());
            }
            final boolean showStartLineBasedOnCurrentLeg = numberOfStartWaypointMarks == 2 && courseDTO.currentLegNumber <= 1;
            final boolean showFinishLineBasedOnCurrentLeg = numberOfFinishWaypointMarks == 2 && courseDTO.currentLegNumber == courseDTO.totalLegsCount;
            // show the line when STARTLINE is selected and the current leg is around the start leg,
            // or when COURSEGEOMETRY is selected and the finish line isn't equal and wouldn't be shown at the same time based on the current leg.
            // With this, if COURSEGEOMETRY is selected and start and finish line are equal, the start line will not be displayed if
            // based on the race progress the finish line is to be preferred, so only the finish line will be shown.
            final boolean reallyShowStartLine =
                    (settings.getHelpLinesSettings().isVisible(HelpLineTypes.STARTLINE) && showStartLineBasedOnCurrentLeg) ||
                    (settings.getHelpLinesSettings().isVisible(HelpLineTypes.COURSEGEOMETRY) &&
                             (!showFinishLineBasedOnCurrentLeg || !startLineEqualsFinishLine(courseDTO)));
            // show the line when FINISHLINE is selected and the current leg is the last leg,
            // or when COURSEGEOMETRY is selected and the start line isn't equal or the current leg is the last leg.
            // With this, if COURSEGEOMETRY is selected and start and finish line are equal, the start line will be displayed unless
            // the finish line should take precedence based on race progress.
            final boolean reallyShowFinishLine = showFinishLineBasedOnCurrentLeg &&
                    (!showStartLineBasedOnCurrentLeg || !startLineEqualsFinishLine(courseDTO)) &&
                    (settings.getHelpLinesSettings().isVisible(HelpLineTypes.FINISHLINE) && showFinishLineBasedOnCurrentLeg) ||
                    (settings.getHelpLinesSettings().isVisible(HelpLineTypes.COURSEGEOMETRY) &&
                            (!startLineEqualsFinishLine(courseDTO) || showFinishLineBasedOnCurrentLeg));
            startLine = showOrRemoveOrUpdateLine(startLine, reallyShowStartLine, startLineLeftPosition,
                    startLineRightPosition, startLineInfoProvider, START_LINE_COLOR, STANDARD_LINE_STROKEWEIGHT,
                    STANDARD_LINE_OPACITY);
            // draw the finish line
            final Position finishLineLeftPosition = numberOfFinishWaypointMarks == 0 ? null : courseDTO.getFinishMarkPositions().get(0);
            final Position finishLineRightPosition = numberOfFinishWaypointMarks < 2 ? null : courseDTO.getFinishMarkPositions().get(1);
            if (courseDTO.finishLineAngleFromPortToStarboardWhenApproachingLineToCombinedWind != null) {
                finishLineAdvantageText.replace(0, finishLineAdvantageText.length(), " "+stringMessages.lineAngleToWindAndAdvantage(
                        numberFormatOneDecimal.format(courseDTO.finishLineLengthInMeters),
                        numberFormatOneDecimal.format(Math.abs(courseDTO.finishLineAngleFromPortToStarboardWhenApproachingLineToCombinedWind)),
                        courseDTO.finishLineAdvantageousSide.name().charAt(0)+courseDTO.finishLineAdvantageousSide.name().substring(1).toLowerCase(),
                        numberFormatOneDecimal.format(courseDTO.finishLineAdvantageInMeters)));
            } else {
                finishLineAdvantageText.delete(0, finishLineAdvantageText.length());
            }
            finishLine = showOrRemoveOrUpdateLine(finishLine, reallyShowFinishLine, finishLineLeftPosition,
                    finishLineRightPosition, finishLineInfoProvider, FINISH_LINE_COLOR, STANDARD_LINE_STROKEWEIGHT,
                    STANDARD_LINE_STROKEWEIGHT);
            // the control point pairs for which we already decided whether or not
            // to show a course middle line for; values tell whether to show the line and for which zero-based
            // start waypoint index to do so; when for an equal control point pair multiple decisions with different
            // outcome are made, a decision to show the line overrules the decision to not show it (OR-semantics)
            final Map<Set<ControlPointDTO>, Pair<Boolean, Integer>> keysAlreadyHandled = new HashMap<>();
            for (int zeroBasedIndexOfStartWaypoint = 0; zeroBasedIndexOfStartWaypoint<courseDTO.waypointPositions.size()-1; zeroBasedIndexOfStartWaypoint++) {
                final Set<ControlPointDTO> key = getCourseMiddleLinesKey(courseDTO, zeroBasedIndexOfStartWaypoint);
                boolean showCourseMiddleLine = keysAlreadyHandled.containsKey(key) && keysAlreadyHandled.get(key).getA() ||
                        settings.getHelpLinesSettings().isVisible(HelpLineTypes.COURSEGEOMETRY) ||
                        (settings.getHelpLinesSettings().isVisible(HelpLineTypes.COURSEMIDDLELINE)
                         && courseDTO.currentLegNumber > 0
                         && courseDTO.currentLegNumber-1 == zeroBasedIndexOfStartWaypoint);
                keysAlreadyHandled.put(key, new Pair<>(showCourseMiddleLine, zeroBasedIndexOfStartWaypoint));
            }
            Set<Set<ControlPointDTO>> keysToConsider = new HashSet<>(keysAlreadyHandled.keySet());
            keysToConsider.addAll(courseMiddleLines.keySet());
            for (final Set<ControlPointDTO> key : keysToConsider) {
                final int zeroBasedIndexOfStartWaypoint = keysAlreadyHandled.containsKey(key) ?
                        keysAlreadyHandled.get(key).getB() : 0; // if not handled, the line will be removed, so the waypoint index doesn't matter
                final Pair<Boolean, Integer> showLineAndZeroBasedIndexOfStartWaypoint = keysAlreadyHandled.get(key);
                final boolean showCourseMiddleLine = showLineAndZeroBasedIndexOfStartWaypoint != null && showLineAndZeroBasedIndexOfStartWaypoint.getA();
                courseMiddleLines.put(key, showOrRemoveCourseMiddleLine(courseDTO, courseMiddleLines.get(key), zeroBasedIndexOfStartWaypoint, showCourseMiddleLine));
            }
        }
    }

    private boolean startLineEqualsFinishLine(CoursePositionsDTO courseDTO) {
        final List<WaypointDTO> waypoints;
        return courseDTO != null && courseDTO.course != null &&
                (waypoints = courseDTO.course.waypoints) != null &&
                waypoints.get(0).controlPoint.equals(waypoints.get(waypoints.size()-1).controlPoint);
    }

    /**
     * Given a zero-based index into <code>courseDTO</code>'s {@link RaceCourseDTO#waypoints waypoints list} that denotes the start
     * waypoint of the leg in question, returns a key that can be used for the {@link #courseMiddleLines} map, consisting of a set
     * that holds the two {@link ControlPointDTO}s representing the start and finish control point of that leg.
     */
    private Set<ControlPointDTO> getCourseMiddleLinesKey(final CoursePositionsDTO courseDTO,
            final int zeroBasedIndexOfStartWaypoint) {
        ControlPointDTO startControlPoint = courseDTO.course.waypoints.get(zeroBasedIndexOfStartWaypoint).controlPoint;
        ControlPointDTO endControlPoint = courseDTO.course.waypoints.get(zeroBasedIndexOfStartWaypoint+1).controlPoint;
        final Set<ControlPointDTO> key = new HashSet<>();
        key.add(startControlPoint);
        key.add(endControlPoint);
        return key;
    }

    /**
     * @param showLine
     *            tells whether or not to show the line; if the <code>lineToShowOrRemoveOrUpdate</code> references a
     *            line but the line shall not be shown, the line is removed from the map; conversely, if the line is not
     *            yet shown but shall be, a new line is created, added to the map and returned. If the line is shown and
     *            shall continue to be shown, the line is returned after updating its vertex coordinates.
     * @return <code>null</code> if the line is not shown; the polyline object representing the line being displayed
     *         otherwise
     */
    private Polyline showOrRemoveCourseMiddleLine(final CoursePositionsDTO courseDTO, Polyline lineToShowOrRemoveOrUpdate,
            final int zeroBasedIndexOfStartWaypoint, final boolean showLine) {
        final Position position1DTO = courseDTO.waypointPositions.get(zeroBasedIndexOfStartWaypoint);
        final Position position2DTO = courseDTO.waypointPositions.get(zeroBasedIndexOfStartWaypoint+1);
        final LineInfoProvider lineInfoProvider = new LineInfoProvider() {
            @Override
            public String getLineInfo() {
                final StringBuilder sb = new StringBuilder();
                sb.append(stringMessages.courseMiddleLine());
                sb.append('\n');
                sb.append(numberFormatNoDecimal.format(
                        Math.abs(position1DTO.getDistance(position2DTO).getMeters()))+stringMessages.metersUnit());
                if (lastCombinedWindTrackInfoDTO != null) {
                    final WindTrackInfoDTO windTrackAtLegMiddle = lastCombinedWindTrackInfoDTO.getCombinedWindOnLegMiddle(zeroBasedIndexOfStartWaypoint);
                    if (windTrackAtLegMiddle != null && windTrackAtLegMiddle.windFixes != null && !windTrackAtLegMiddle.windFixes.isEmpty()) {
                        WindDTO windAtLegMiddle = windTrackAtLegMiddle.windFixes.get(0);
                        final double legBearingDeg = position1DTO.getBearingGreatCircle(position2DTO).getDegrees();
                        final String diff = numberFormatOneDecimal.format(
                                Math.min(Math.abs(windAtLegMiddle.dampenedTrueWindBearingDeg-legBearingDeg),
                                                     Math.abs(windAtLegMiddle.dampenedTrueWindFromDeg-legBearingDeg)));
                        sb.append(", ");
                        sb.append(stringMessages.degreesToWind(diff));
                    }
                }
                return sb.toString();
            }
        };
        return showOrRemoveOrUpdateLine(lineToShowOrRemoveOrUpdate, showLine, position1DTO, position2DTO,
                lineInfoProvider, "#2268a0", STANDARD_LINE_STROKEWEIGHT, STANDARD_LINE_OPACITY);
    }

    /**
     * @param showLine
     *            tells whether or not to show the line; if the <code>lineToShowOrRemoveOrUpdate</code> references a
     *            line but the line shall not be shown, the line is removed from the map; conversely, if the line is not
     *            yet shown but shall be, a new line is created, added to the map and returned. If the line is shown and
     *            shall continue to be shown, the line is returned after updating its vertex coordinates.
     * @return <code>null</code> if the line is not shown; the polyline object representing the line being displayed
     *         otherwise
     */
    Polyline showOrRemoveOrUpdateLine(Polyline lineToShowOrRemoveOrUpdate, final boolean showLine,
            final Position position1DTO, final Position position2DTO, final LineInfoProvider lineInfoProvider,
            String lineColorRGB, int strokeWeight, double strokeOpacity) {
        if (position1DTO != null && position2DTO != null) {
            if (showLine) {
                LatLng courseMiddleLinePoint1 = coordinateSystem.toLatLng(position1DTO);
                LatLng courseMiddleLinePoint2 = coordinateSystem.toLatLng(position2DTO);
                final MVCArray<LatLng> pointsAsArray;
                if (lineToShowOrRemoveOrUpdate == null) {
                    PolylineOptions options = PolylineOptions.newInstance();
                    options.setClickable(true);
                    options.setGeodesic(true);
                    options.setStrokeColor(lineColorRGB);
                    options.setStrokeWeight(strokeWeight);
                    options.setStrokeOpacity(strokeOpacity);
                    pointsAsArray = MVCArray.newInstance();
                    lineToShowOrRemoveOrUpdate = Polyline.newInstance(options);
                    lineToShowOrRemoveOrUpdate.setPath(pointsAsArray);
                    lineToShowOrRemoveOrUpdate.setMap(map);
                    Hoverline lineToShowOrRemoveOrUpdateHoverline = new Hoverline(lineToShowOrRemoveOrUpdate, options, this);
                    lineToShowOrRemoveOrUpdate.addMouseOverHandler(new MouseOverMapHandler() {
                        @Override
                        public void onEvent(MouseOverMapEvent event) {
                            map.setTitle(lineInfoProvider.getLineInfo());
                        }
                    });
                    lineToShowOrRemoveOrUpdateHoverline.addMouseOutMoveHandler(new MouseOutMapHandler() {
                        @Override
                        public void onEvent(MouseOutMapEvent event) {
                            map.setTitle("");
                        }
                    });
                } else {
                    pointsAsArray = lineToShowOrRemoveOrUpdate.getPath();
                    pointsAsArray.removeAt(1);
                    pointsAsArray.removeAt(0);
                }
                adjustInfoOverlayForVisibleLine(lineToShowOrRemoveOrUpdate, position1DTO, position2DTO, lineInfoProvider);
                pointsAsArray.insertAt(0, courseMiddleLinePoint1);
                pointsAsArray.insertAt(1, courseMiddleLinePoint2);
            } else {
                if (lineToShowOrRemoveOrUpdate != null) {
                    lineToShowOrRemoveOrUpdate.setMap(null);
                    adjustInfoOverlayForRemovedLine(lineToShowOrRemoveOrUpdate);
                    lineToShowOrRemoveOrUpdate = null;
                }
            }
        }
        return lineToShowOrRemoveOrUpdate;
    }

    private void adjustInfoOverlayForRemovedLine(Polyline lineToShowOrRemoveOrUpdate) {
        SmallTransparentInfoOverlay infoOverlay = infoOverlaysForLinesForCourseGeometry.remove(lineToShowOrRemoveOrUpdate);
        if (infoOverlay != null) {
            infoOverlay.removeFromMap();
        }
    }

    private void adjustInfoOverlayForVisibleLine(Polyline lineToShowOrRemoveOrUpdate, final Position position1DTO,
            final Position position2DTO, final LineInfoProvider lineInfoProvider) {
        if (lineInfoProvider.isShowInfoOverlayWithHelplines()) {
            SmallTransparentInfoOverlay infoOverlay = infoOverlaysForLinesForCourseGeometry.get(lineToShowOrRemoveOrUpdate);
            if (getSettings().getHelpLinesSettings().isVisible(HelpLineTypes.COURSEGEOMETRY)) {
                if (infoOverlay == null) {
                    infoOverlay = new SmallTransparentInfoOverlay(map, RaceMapOverlaysZIndexes.INFO_OVERLAY_ZINDEX,
                            lineInfoProvider.getLineInfo(), coordinateSystem);
                    infoOverlaysForLinesForCourseGeometry.put(lineToShowOrRemoveOrUpdate, infoOverlay);
                    infoOverlay.addToMap();
                } else {
                    infoOverlay.setInfoText(lineInfoProvider.getLineInfo());
                }
                infoOverlay.setPosition(position1DTO.translateGreatCircle(position1DTO.getBearingGreatCircle(position2DTO),
                                position1DTO.getDistance(position2DTO).scale(0.5)), /* transition time */ -1);
                infoOverlay.draw();
            } else {
                if (infoOverlay != null) {
                    infoOverlay.removeFromMap();
                    infoOverlaysForLinesForCourseGeometry.remove(lineToShowOrRemoveOrUpdate);
                }
            }
        }
    }
    
    /**
     * If, according to {@link #lastRaceTimesInfo} and {@link #timer} the race is
     * still in the pre-start phase, show a {@link SmallTransparentInfoOverlay} at the
     * start line that shows the count down.
     */
    private void updateCountdownCanvas(WaypointDTO startWaypoint) {
        if (!settings.isShowSelectedCompetitorsInfo() || startWaypoint == null || Util.isEmpty(startWaypoint.controlPoint.getMarks())
                || lastRaceTimesInfo == null || lastRaceTimesInfo.startOfRace == null || timer.getTime().after(lastRaceTimesInfo.startOfRace)) {
            if (countDownOverlay != null) {
                countDownOverlay.removeFromMap();
                countDownOverlay = null;
            }
        } else {
            long timeToStartInMs = lastRaceTimesInfo.startOfRace.getTime() - timer.getTime().getTime();
            String countDownText = timeToStartInMs > 1000 ? stringMessages.timeToStart(DateAndTimeFormatterUtil
                    .formatElapsedTime(timeToStartInMs)) : stringMessages.start();
            if (countDownOverlay == null) {
                countDownOverlay = new SmallTransparentInfoOverlay(map, RaceMapOverlaysZIndexes.INFO_OVERLAY_ZINDEX,
                        countDownText, coordinateSystem, getSettings().getStartCountDownFontSizeScaling());
                countDownOverlay.addToMap();
            } else {
                countDownOverlay.setInfoText(countDownText);
            }
            countDownOverlay.setPosition(startWaypoint.controlPoint.getMarks().iterator().next().position, /* transition time */ -1);
            countDownOverlay.draw();
        }
    }

    // Google scales coordinates so that the globe-tile has mercator-latitude [-pi, +pi], i.e. tile height of 2*pi
    // mercator-latitude pi corresponds to geo-latitude of approx. 85.0998 (where Google cuts off the map visualization)
    // official documentation: http://developers.google.com/maps/documentation/javascript/maptypes#TileCoordinates
    private double getMercatorLatitude(double lat) {
        // cutting-off for latitudes close to +-90 degrees is recommended (to avoid division by zero)
        double sine = Math.max(-0.9999, Math.min(0.9999, Math.sin(Math.PI * lat / 180)));
        return Math.log((1 + sine) / (1 - sine)) / 2;
    }

    public int getZoomLevel(LatLngBounds bounds) {
        int GLOBE_PXSIZE = 256; // a constant in Google's map projection
        int MAX_ZOOM = 18; // maximum zoom-level that should be automatically selected
        double LOG2 = Math.log(2.0);
        double deltaLng = bounds.getNorthEast().getLongitude() - bounds.getSouthWest().getLongitude();
        double deltaLat = getMercatorLatitude(bounds.getNorthEast().getLatitude()) - getMercatorLatitude(bounds.getSouthWest().getLatitude());
        if ((deltaLng == 0) && (deltaLat == 0)) {
            return MAX_ZOOM;
        }
        if (deltaLng < 0) {
            deltaLng += 360;
        }
        int zoomLng = (int) Math.floor(Math.log(map.getDiv().getClientWidth() * 360 / deltaLng / GLOBE_PXSIZE) / LOG2);
        int zoomLat = (int) Math.floor(Math.log(map.getDiv().getClientHeight() * 2 * Math.PI / deltaLat / GLOBE_PXSIZE) / LOG2);
        return Math.min(Math.min(zoomLat, zoomLng), MAX_ZOOM);
    }
   
    private void zoomMapToNewBounds(LatLngBounds newBounds) {
        if (newBounds != null) {
            LatLngBounds currentMapBounds;
            if (map.getBounds() == null
                    || !BoundsUtil.contains((currentMapBounds = map.getBounds()), newBounds)
                    || graticuleAreaRatio(currentMapBounds, newBounds) > 10) {
                // only change bounds if the new bounds don't fit into the current map zoom
                Iterable<ZoomTypes> oldZoomTypesToConsiderSettings = settings.getZoomSettings().getTypesToConsiderOnZoom();
                setAutoZoomInProgress(true);
                autoZoomLatLngBounds = newBounds;
                int newZoomLevel = getZoomLevel(autoZoomLatLngBounds); 
                if (newZoomLevel != map.getZoom()) {
                    // distinguish between zoom-in and zoom-out, because the sequence of panTo() and setZoom()
                    // appears different on the screen due to map-animations
                    // following sequences keep the selected boats allways visible:
                    //   zoom-in : 1. panTo(), 2. setZoom()
                    //   zoom-out: 1. setZoom(), 2. panTo() 
                    autoZoomIn = newZoomLevel > map.getZoom();
                    autoZoomOut = !autoZoomIn;
                    autoZoomLevel = newZoomLevel;
                    removeTransitions();
                    if (autoZoomIn) {
                        map.panTo(autoZoomLatLngBounds.getCenter());
                    } else {
                        map.setZoom(autoZoomLevel);
                    }
                } else {
                    map.panTo(autoZoomLatLngBounds.getCenter());
                }
                RaceMapZoomSettings restoredZoomSettings = new RaceMapZoomSettings(oldZoomTypesToConsiderSettings, settings.getZoomSettings().isZoomToSelectedCompetitors());
                settings = new RaceMapSettings(settings, restoredZoomSettings);
                setAutoZoomInProgress(false);
            }
        }
    }
    
    private double graticuleAreaRatio(LatLngBounds containing, LatLngBounds contained) {
        assert BoundsUtil.contains(containing, contained);
        double containingAreaRatio = getGraticuleArea(containing) / getGraticuleArea(contained);
        return containingAreaRatio;
    }

    /**
     * A much simplified "area" calculation for a {@link Bounds} object, multiplying the differences in latitude and longitude degrees.
     * The result therefore is in the order of magnitude of 60*60 square nautical miles.
     */
    private double getGraticuleArea(LatLngBounds bounds) {
        return ((BoundsUtil.isCrossesDateLine(bounds) ? bounds.getNorthEast().getLongitude()+360 : bounds.getNorthEast().getLongitude())-bounds.getSouthWest().getLongitude()) *
                (bounds.getNorthEast().getLatitude() - bounds.getSouthWest().getLatitude());
    }

    private void setAutoZoomInProgress(boolean autoZoomInProgress) {
        this.autoZoomInProgress = autoZoomInProgress;
    }
    
    boolean isAutoZoomInProgress() {
        return autoZoomInProgress;
    }
    
    /**
     * @param timeForPositionTransitionMillis use -1 to not animate the position transition, e.g., during map zoom or non-play
     */
    private boolean updateBoatCanvasForCompetitor(CompetitorDTO competitorDTO, Date date, long timeForPositionTransitionMillis) {
        boolean hasTimeJumped = timeForPositionTransitionMillis > 3 * timer.getRefreshInterval();
        if (hasTimeJumped) {
            timeForPositionTransitionMillis = -1;
        }
        boolean usedExistingCanvas = false;
        GPSFixDTOWithSpeedWindTackAndLegType lastBoatFix = getBoatFix(competitorDTO, date);
        if (lastBoatFix != null) {
            BoatOverlay boatOverlay = boatOverlays.get(competitorDTO);
            if (boatOverlay == null) {
                boatOverlay = createBoatOverlay(RaceMapOverlaysZIndexes.BOATS_ZINDEX, competitorDTO, displayHighlighted(competitorDTO));
                boatOverlays.put(competitorDTO, boatOverlay);
                boatOverlay.setDisplayMode(displayHighlighted(competitorDTO));
                boatOverlay.setBoatFix(lastBoatFix, timeForPositionTransitionMillis);
                boatOverlay.addToMap();
            } else {
                usedExistingCanvas = true;
                boatOverlay.setDisplayMode(displayHighlighted(competitorDTO));
                boatOverlay.setBoatFix(lastBoatFix, timeForPositionTransitionMillis);
                boatOverlay.draw();
            }
        }
        return usedExistingCanvas;
    }

    private DisplayMode displayHighlighted(CompetitorDTO competitorDTO) {
        boolean competitorisSelected = competitorSelection.isSelected(competitorDTO);
        if (!settings.isShowOnlySelectedCompetitors()) {
            if (competitorisSelected) {
                return DisplayMode.SELECTED;
            } else {
                if (isSomeOtherCompetitorSelected()) {
                    return DisplayMode.NOT_SELECTED;
                } else {
                    return DisplayMode.DEFAULT;
                }
            }
        }
        else{
            return competitorSelection.isSelected(competitorDTO) ? DisplayMode.SELECTED : DisplayMode.DEFAULT;
        }
       
    }
    
    private boolean isSomeOtherCompetitorSelected(){
        return Util.size(competitorSelection.getSelectedCompetitors()) > 0;
    }
    
    private class CourseMarkInfoWindowClickHandler implements ClickMapHandler {
        private final MarkDTO markDTO;
        private final CourseMarkOverlay courseMarkOverlay;
        
        public CourseMarkInfoWindowClickHandler(MarkDTO markDTO, CourseMarkOverlay courseMarkOverlay) {
            this.markDTO = markDTO;
            this.courseMarkOverlay = courseMarkOverlay;
        }
        
        @Override
        public void onEvent(ClickMapEvent event) {
            LatLng latlng = courseMarkOverlay.getMarkLatLngPosition();
            showMarkInfoWindow(markDTO, latlng);
        }
    }
    
    private void registerCourseMarkInfoWindowClickHandler(final String markDTOIdAsString) {
        final CourseMarkOverlay courseMarkOverlay = courseMarkOverlays.get(markDTOIdAsString);
        courseMarkClickHandlers.put(markDTOIdAsString, 
                courseMarkOverlay.addClickHandler(new CourseMarkInfoWindowClickHandler(markDTOs.get(markDTOIdAsString), courseMarkOverlay)));
    }
    
    public void registerAllCourseMarkInfoWindowClickHandlers() {
        for (String markDTOIdAsString : markDTOs.keySet()) {
            registerCourseMarkInfoWindowClickHandler(markDTOIdAsString);
        }
    }
    
    public void unregisterAllCourseMarkInfoWindowClickHandlers() {
        Iterator<Entry<String, HandlerRegistration>> iterator = courseMarkClickHandlers.entrySet().iterator();
        while(iterator.hasNext()) {
            Entry<String, HandlerRegistration> handler = iterator.next();
            handler.getValue().removeHandler();
            iterator.remove();
        }
    }

    private BoatOverlay createBoatOverlay(int zIndex, final CompetitorDTO competitorDTO, DisplayMode displayMode) {
        final BoatDTO boatOfCompetitor = competitorSelection.getBoat(competitorDTO);
        final BoatOverlay boatCanvas = new BoatOverlay(map, zIndex, boatOfCompetitor, competitorSelection.getColor(competitorDTO, raceIdentifier), coordinateSystem);
        boatCanvas.setDisplayMode(displayMode);
        boatCanvas.addClickHandler(event -> {
            GPSFixDTOWithSpeedWindTackAndLegType latestFixForCompetitor = getBoatFix(competitorDTO, timer.getTime());
            Widget content = getInfoWindowContent(competitorDTO, latestFixForCompetitor);
            LatLng where = coordinateSystem.toLatLng(latestFixForCompetitor.position);
            managedInfoWindow.openAtPosition(content, where);
        });

        boatCanvas.addMouseOverHandler(new MouseOverMapHandler() {
            @Override
            public void onEvent(MouseOverMapEvent event) {
                map.setTitle(boatOfCompetitor.getSailId());
            }
        });
        boatCanvas.addMouseOutMoveHandler(new MouseOutMapHandler() {
            @Override
            public void onEvent(MouseOutMapEvent event) {
                map.setTitle("");
            }
        });

        return boatCanvas;
    }

    protected WindSensorOverlay createWindSensorOverlay(int zIndex, final WindSource windSource, final WindTrackInfoDTO windTrackInfoDTO) {
        final WindSensorOverlay windSensorOverlay = new WindSensorOverlay(map, zIndex, raceMapImageManager, stringMessages, coordinateSystem);
        windSensorOverlay.setWindInfo(windTrackInfoDTO, windSource);
        windSensorOverlay.addClickHandler(new ClickMapHandler() {
            @Override
            public void onEvent(ClickMapEvent event) {
                showWindSensorInfoWindow(windSensorOverlay);
            }
        });
        return windSensorOverlay;
    }

    private void showMarkInfoWindow(MarkDTO markDTO, LatLng position) {
        managedInfoWindow.openAtPosition(getInfoWindowContent(markDTO), position);
    }

    private void showCompetitorInfoWindow(final CompetitorDTO competitorDTO, LatLng where) {
        final GPSFixDTOWithSpeedWindTackAndLegType latestFixForCompetitor = getBoatFix(competitorDTO, timer.getTime());
        final Widget content = getInfoWindowContent(competitorDTO, latestFixForCompetitor);
        managedInfoWindow.openAtPosition(content, where);
    }

    private void showWindSensorInfoWindow(final WindSensorOverlay windSensorOverlay) {
        WindSource windSource = windSensorOverlay.getWindSource();
        WindTrackInfoDTO windTrackInfoDTO = windSensorOverlay.getWindTrackInfoDTO();
        WindDTO windDTO = windTrackInfoDTO.windFixes.get(0);
        if (windDTO != null && windDTO.position != null) {
            final LatLng where = coordinateSystem.toLatLng(windDTO.position);
            final Widget content = getInfoWindowContent(windSource, windTrackInfoDTO);
            managedInfoWindow.openAtPosition(content, where);
        }
    }

    Widget createInfoWindowLabelAndValue(String labelName, String value) {
        Label valueLabel = new Label(value);
        valueLabel.setWordWrap(false);
        return createInfoWindowLabelWithWidget(labelName, valueLabel);
    }

    public Widget createInfoWindowLabelWithWidget(String labelName, Widget value) {
        FlowPanel flowPanel = new FlowPanel();
        Label label = new Label(labelName + ":");
        label.setWordWrap(false);
        label.getElement().getStyle().setFloat(Style.Float.LEFT);
        label.getElement().getStyle().setPadding(3, Style.Unit.PX);
        label.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
        value.getElement().getStyle().setFloat(Style.Float.LEFT);
        value.getElement().getStyle().setPadding(3, Style.Unit.PX);
        flowPanel.add(label);
        flowPanel.add(value);
        return flowPanel;
    }
    
    private Widget getInfoWindowContent(MarkDTO markDTO) {
        VerticalPanel vPanel = new VerticalPanel();
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.mark(), markDTO.getName()));
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.position(), markDTO.position.getAsDegreesAndDecimalMinutesWithCardinalPoints()));
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.position(), markDTO.position.getAsSignedDecimalDegrees()));
        return vPanel;
    }

    private Widget getInfoWindowContent(final WindSource windSource, WindTrackInfoDTO windTrackInfoDTO) {
        WindDTO windDTO = windTrackInfoDTO.windFixes.get(0);
        final VerticalPanel vPanel = new VerticalPanel();
        final Anchor windSourceNameAnchor = new Anchor(WindSourceTypeFormatter.format(windSource, stringMessages));
        vPanel.add(createInfoWindowLabelWithWidget(stringMessages.windSource(), windSourceNameAnchor));
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.wind(), Math.round(windDTO.dampenedTrueWindFromDeg) + " " + stringMessages.degreesShort()));
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.windSpeed(), numberFormatOneDecimal.format(windDTO.dampenedTrueWindSpeedInKnots)));
        final MillisecondsTimePoint timePoint = new MillisecondsTimePoint(windDTO.measureTimepoint);
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.time(), timePoint.asDate().toString()));
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.position(), windDTO.position.getAsDegreesAndDecimalMinutesWithCardinalPoints()));
        final Label positionInDecimalDegreesLabel = new Label(windDTO.position.getAsSignedDecimalDegrees());
        positionInDecimalDegreesLabel.setWordWrap(false);
        positionInDecimalDegreesLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        positionInDecimalDegreesLabel.getElement().getStyle().setPadding(3, Style.Unit.PX);
        positionInDecimalDegreesLabel.getElement().getStyle().setFontWeight(Style.FontWeight.LIGHTER);
        positionInDecimalDegreesLabel.getElement().getStyle().setFontSize(0.7, Unit.EM);
        vPanel.add(positionInDecimalDegreesLabel);
        if (windSource.getType() == WindSourceType.WINDFINDER) {
            final HorizontalPanel container = new HorizontalPanel();
            container.setSpacing(1);
            final WindfinderIcon windfinderIcon = new WindfinderIcon(raceMapImageManager, stringMessages);
            container.add(windfinderIcon);
            container.add(vPanel);
            sailingService.getWindFinderSpot(windSource.getId().toString(), new AsyncCallback<SpotDTO>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError(stringMessages.unableToResolveWindFinderSpotId(
                            windSource.getId().toString(), caught.getMessage()), /* silentMode */ true);
                }

                @Override
                public void onSuccess(SpotDTO result) {
                    final String url = result.getCurrentlyMostAppropriateUrl(timePoint);
                    windSourceNameAnchor.setTarget("_blank");
                    windSourceNameAnchor.setText(result.getName());
                    windSourceNameAnchor.setHref(url);
                    windfinderIcon.setHref(url);
                }
            });
            return container;
        } else {
            windSourceNameAnchor.addClickHandler(event -> showWindChartForProvider.accept(windSource));
        }
        return vPanel;
    }

    private Widget getInfoWindowContent(CompetitorDTO competitorDTO, GPSFixDTOWithSpeedWindTackAndLegType lastFix) {
        final VerticalPanel vPanel = new VerticalPanel();
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.competitor(), competitorDTO.getName()));
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.sailNumber(), competitorSelection.getBoat(competitorDTO).getSailId()));
        final Integer rank = getRank(competitorDTO);
        if (rank != null) {
            vPanel.add(createInfoWindowLabelAndValue(stringMessages.rank(), String.valueOf(rank)));
        }
        SpeedWithBearingDTO speedWithBearing = lastFix.speedWithBearing;
        if (speedWithBearing == null) {
            // TODO should we show the boat at all?
            speedWithBearing = new SpeedWithBearingDTO(0, 0);
        }
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.speed(),
                numberFormatOneDecimal.format(speedWithBearing.speedInKnots) + " "+stringMessages.knotsUnit()));
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.bearing(), (int) speedWithBearing.bearingInDegrees + " "+stringMessages.degreesShort()));
        if (lastFix.degreesBoatToTheWind != null) {
            vPanel.add(createInfoWindowLabelAndValue(stringMessages.degreesBoatToTheWind(),
                    (int) Math.abs(lastFix.degreesBoatToTheWind) + " " + stringMessages.degreesShort()));
        }
        if (raceIdentifier != null) {
            RegattaAndRaceIdentifier race = raceIdentifier;
            if (race != null) {
                final Map<CompetitorDTO, TimeRange> timeRange = new HashMap<>();
                final TimePoint from = new MillisecondsTimePoint(fixesAndTails.getFixes(competitorDTO)
                        .get(fixesAndTails.getFirstShownFix(competitorDTO)).timepoint);
                final TimePoint to = new MillisecondsTimePoint(getBoatFix(competitorDTO, timer.getTime()).timepoint);
                timeRange.put(competitorDTO, new TimeRangeImpl(from, to, true));
                sailingService.getDouglasPoints(race, timeRange, 3,
                        new AsyncCallback<Map<CompetitorDTO, List<GPSFixDTOWithSpeedWindTackAndLegType>>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error obtaining douglas positions: " + caught.getMessage(), true /*silentMode */);
                            }

                            @Override
                            public void onSuccess(Map<CompetitorDTO, List<GPSFixDTOWithSpeedWindTackAndLegType>> result) {
                                lastDouglasPeuckerResult = result;
                                if (douglasMarkers != null) {
                                    removeAllMarkDouglasPeuckerpoints();
                                }
                                if (!(timer.getPlayState() == PlayStates.Playing)) {
                                    if (settings.isShowDouglasPeuckerPoints()) {
                                        showMarkDouglasPeuckerPoints(result);
                                    }
                                }
                            }
                        });
                maneuverMarkersAndLossIndicators.getAndShowManeuvers(race, timeRange);
            }
        }
        return vPanel;
    }

    /**
     * @return the {@link CompetitorSelectionProvider#getSelectedCompetitors()} if
     *         {@link RaceMapSettings#isShowOnlySelectedCompetitors() only selected competitors are to be shown}, the
     *         {@link CompetitorSelectionProvider#getFilteredCompetitors() filtered competitors} otherwise. In both cases,
     *         if we have {@link RaceCompetitorSet information about the competitors participating} in this map's race,
     *         the result set is reduced to those, no matter if other regatta participants would otherwise have been in
     *         the result set
     */
    private Iterable<CompetitorDTO> getCompetitorsToShow() {
        final Set<CompetitorDTO> result = new HashSet<>();
        Iterable<CompetitorDTO> selection = competitorSelection.getSelectedCompetitors();
        final Set<String> raceCompetitorIdsAsString = raceCompetitorSet.getIdsOfCompetitorsParticipatingInRaceAsStrings();
        if (!settings.isShowOnlySelectedCompetitors() || Util.isEmpty(selection)) {
            for (final CompetitorDTO filteredCompetitor : competitorSelection.getFilteredCompetitors()) {
                if (raceCompetitorIdsAsString == null || raceCompetitorIdsAsString.contains(filteredCompetitor.getIdAsString())) {
                    result.add(filteredCompetitor);
                }
            }
        } else {
            for (final CompetitorDTO selectedCompetitor : selection) {
                if (raceCompetitorIdsAsString == null || raceCompetitorIdsAsString.contains(selectedCompetitor.getIdAsString())) {
                    result.add(selectedCompetitor);
                }
            }
        }
        return result;
    }
    
    protected void removeAllMarkDouglasPeuckerpoints() {
        if (douglasMarkers != null) {
            for (Marker marker : douglasMarkers) {
                marker.setMap((MapWidget) null);
            }
        }
        douglasMarkers = null;
    }

    private void showMarkDouglasPeuckerPoints(Map<CompetitorDTO, List<GPSFixDTOWithSpeedWindTackAndLegType>> gpsFixPointMapForCompetitors) {
        douglasMarkers = new HashSet<Marker>();
        if (map != null && gpsFixPointMapForCompetitors != null) {
            Set<CompetitorDTO> keySet = gpsFixPointMapForCompetitors.keySet();
            Iterator<CompetitorDTO> iter = keySet.iterator();
            while (iter.hasNext()) {
                CompetitorDTO competitorDTO = iter.next();
                List<GPSFixDTOWithSpeedWindTackAndLegType> gpsFix = gpsFixPointMapForCompetitors.get(competitorDTO);
                for (GPSFixDTOWithSpeedWindTackAndLegType fix : gpsFix) {
                    LatLng latLng = coordinateSystem.toLatLng(fix.position);
                    MarkerOptions options = MarkerOptions.newInstance();
                    options.setTitle(fix.timepoint+": "+fix.position+", "+fix.speedWithBearing.toString());
                    Marker marker = Marker.newInstance(options);
                    marker.setPosition(latLng);
                    douglasMarkers.add(marker);
                    marker.setMap(map);
                }
            }
        }
    }
    
    /**
     * @param date
     *            the point in time for which to determine the competitor's boat position; approximated by using the fix
     *            from {@link #fixes} whose time point comes closest to this date
     * 
     * @return The GPS fix for the given competitor from {@link #fixes} that is closest to <code>date</code>, or
     *         <code>null</code> if no fix is available
     */
    private GPSFixDTOWithSpeedWindTackAndLegType getBoatFix(CompetitorDTO competitorDTO, Date date) {
        GPSFixDTOWithSpeedWindTackAndLegType result = null;
        List<GPSFixDTOWithSpeedWindTackAndLegType> competitorFixes = fixesAndTails.getFixes(competitorDTO);
        if (competitorFixes != null && !competitorFixes.isEmpty()) {
            int i = Collections.binarySearch(competitorFixes, new GPSFixDTOWithSpeedWindTackAndLegType(date, null, null, (WindDTO) null, null, null, false),
                    new Comparator<GPSFixDTOWithSpeedWindTackAndLegType>() {
                @Override
                public int compare(GPSFixDTOWithSpeedWindTackAndLegType o1, GPSFixDTOWithSpeedWindTackAndLegType o2) {
                    return o1.timepoint.compareTo(o2.timepoint);
                }
            });
            if (i<0) {
                i = -i-1; // no perfect match; i is now the insertion point
                // if the insertion point is at the end, use last fix
                if (i >= competitorFixes.size()) {
                    result = competitorFixes.get(competitorFixes.size()-1);
                } else if (i == 0) {
                    // if the insertion point is at the beginning, use first fix
                    result = competitorFixes.get(0);
                } else {
                    // competitorFixes must have at least two elements, and i points neither to the end nor the beginning;
                    // get the fix from i and i+1 whose timepoint is closer to date
                    final GPSFixDTOWithSpeedWindTackAndLegType fixBefore = competitorFixes.get(i-1);
                    final GPSFixDTOWithSpeedWindTackAndLegType fixAfter = competitorFixes.get(i);
                    final GPSFixDTOWithSpeedWindTackAndLegType closer;
                    if (date.getTime() - fixBefore.timepoint.getTime() < fixAfter.timepoint.getTime() - date.getTime()) {
                        closer = fixBefore;
                    } else {
                        closer = fixAfter;
                    }
                    // now compute a weighted average depending on the time difference to "date" (see also bug 1924)
                    double factorForAfter = (double) (date.getTime()-fixBefore.timepoint.getTime()) / (double) (fixAfter.timepoint.getTime() - fixBefore.timepoint.getTime());
                    double factorForBefore = 1-factorForAfter;
                    DegreePosition betweenPosition = new DegreePosition(factorForBefore*fixBefore.position.getLatDeg() + factorForAfter*fixAfter.position.getLatDeg(),
                            factorForBefore*fixBefore.position.getLngDeg() + factorForAfter*fixAfter.position.getLngDeg());
                    final double betweenBearing;
                    if (fixBefore.speedWithBearing == null) {
                        if (fixAfter.speedWithBearing == null) {
                            betweenBearing = 0;
                        } else {
                            betweenBearing = fixAfter.speedWithBearing.bearingInDegrees;
                        }
                    } else if (fixAfter.speedWithBearing == null) {
                        betweenBearing = fixBefore.speedWithBearing.bearingInDegrees;
                    } else {
                        betweenBearing = new ScalableBearing(new DegreeBearingImpl(fixBefore.speedWithBearing.bearingInDegrees)).
                                multiply(factorForBefore).add(new ScalableBearing(new DegreeBearingImpl(fixAfter.speedWithBearing.bearingInDegrees)).
                                        multiply(factorForAfter)).divide(1).getDegrees();
                    }
                    SpeedWithBearingDTO betweenSpeed = new SpeedWithBearingDTO(
                            factorForBefore*(fixBefore.speedWithBearing==null?0:fixBefore.speedWithBearing.speedInKnots) +
                            factorForAfter*(fixAfter.speedWithBearing==null?0:fixAfter.speedWithBearing.speedInKnots),
                            betweenBearing);
                    result = new GPSFixDTOWithSpeedWindTackAndLegType(date, betweenPosition, betweenSpeed, closer.degreesBoatToTheWind,
                            closer.tack, closer.legType, fixBefore.extrapolated || fixAfter.extrapolated);
                }
            } else {
                // perfect match
                final GPSFixDTOWithSpeedWindTackAndLegType fixAfter = competitorFixes.get(i);
                result = fixAfter;
            }
        }
        return result;
    }

    public RaceMapSettings getSettings() {
        return settings;
    }

    @Override
    public void addedToSelection(CompetitorDTO competitor) {
        if (settings.isShowOnlySelectedCompetitors()) {
            if (Util.size(competitorSelection.getSelectedCompetitors()) == 1) {
                // first competitors selected; remove all others from map
                Iterator<Map.Entry<CompetitorDTO, BoatOverlay>> i = boatOverlays.entrySet().iterator();
                while (i.hasNext()) {
                    Entry<CompetitorDTO, BoatOverlay> next = i.next();
                    if (!next.getKey().equals(competitor)) {
                        CanvasOverlayV3 boatOverlay = next.getValue();
                        boatOverlay.removeFromMap();
                        fixesAndTails.removeTail(next.getKey());
                        i.remove(); // only this way a ConcurrentModificationException while looping can be avoided
                    }
                }
                showCompetitorInfoOnMap(timer.getTime(), -1, competitorSelection.getSelectedFilteredCompetitors());
            } else {
                // adding a single competitor; may need to re-load data, so refresh:
                redraw();
            }
        } else {
            // only change highlighting
            BoatOverlay boatCanvas = boatOverlays.get(competitor);
            if (boatCanvas != null) {
                boatCanvas.setDisplayMode(displayHighlighted(competitor));
                boatCanvas.draw();
                showCompetitorInfoOnMap(timer.getTime(), -1, competitorSelection.getSelectedFilteredCompetitors());
            } else {
                // seems like an internal error not to find the lowlighted marker; but maybe the
                // competitor was added late to the race;
                // data for newly selected competitor supposedly missing; refresh
                redraw();
            }
        }
        // Now update tails for all competitors because selection change may also affect all unselected competitors
        for (CompetitorDTO oneOfAllCompetitors : competitorSelection.getAllCompetitors()) {
            Polyline tail = fixesAndTails.getTail(oneOfAllCompetitors);
            if (tail != null) {
                PolylineOptions newOptions = createTailStyle(oneOfAllCompetitors, displayHighlighted(oneOfAllCompetitors));
                tail.setOptions(newOptions);
            }
        }
        // Trigger auto-zoom if needed
        RaceMapZoomSettings zoomSettings = settings.getZoomSettings();
        if (!zoomSettings.containsZoomType(ZoomTypes.NONE) && zoomSettings.isZoomToSelectedCompetitors()) {
            zoomMapToNewBounds(zoomSettings.getNewBounds(this));
        }
    }
    
    @Override
    public void removedFromSelection(CompetitorDTO competitor) {
        if (isShowAnyHelperLines()) {
            // helper lines depend on which competitor is visible, because the *visible* leader is used for
            // deciding which helper lines to show:
            redraw();
        } else {
            // try a more incremental update otherwise
            if (settings.isShowOnlySelectedCompetitors()) {
                // if selection is now empty, show all competitors
                if (Util.isEmpty(competitorSelection.getSelectedCompetitors())) {
                    redraw();
                } else {
                    // otherwise remove only deselected competitor's boat images and tail
                    BoatOverlay removedBoatOverlay = boatOverlays.remove(competitor);
                    if (removedBoatOverlay != null) {
                        removedBoatOverlay.removeFromMap();
                    }
                    fixesAndTails.removeTail(competitor);
                    showCompetitorInfoOnMap(timer.getTime(), -1, competitorSelection.getSelectedFilteredCompetitors());
                }
            } else {
                // "lowlight" currently selected competitor
                BoatOverlay boatCanvas = boatOverlays.get(competitor);
                if (boatCanvas != null) {
                    boatCanvas.setDisplayMode(displayHighlighted(competitor));
                    boatCanvas.draw();
                }
                showCompetitorInfoOnMap(timer.getTime(), -1, competitorSelection.getSelectedFilteredCompetitors());
            }
        }
        //Trigger auto-zoom if needed
        RaceMapZoomSettings zoomSettings = settings.getZoomSettings();
        if (!zoomSettings.containsZoomType(ZoomTypes.NONE) && zoomSettings.isZoomToSelectedCompetitors()) {
            zoomMapToNewBounds(zoomSettings.getNewBounds(this));
        }
    }

    private boolean isShowAnyHelperLines() {
        return settings.getHelpLinesSettings().isShowAnyHelperLines();
    }

    @Override
    public String getLocalizedShortName() {
        return raceMapLifecycle.getLocalizedShortName();
    }

    @Override
    public Widget getEntryWidget() {
        return this;
    }

    @Override
    public boolean hasSettings() {
        return raceMapLifecycle.hasSettings();
    }

    @Override
    public SettingsDialogComponent<RaceMapSettings> getSettingsDialogComponent(RaceMapSettings settings) {
        return new RaceMapSettingsDialogComponent(settings, stringMessages, this.isSimulationEnabled && this.hasPolar);
    }

    @Override
    public void updateSettings(RaceMapSettings newSettings) {
        boolean maneuverTypeSelectionChanged = false;
        boolean showManeuverLossChanged = false;
        boolean requiresRedraw = false;
        boolean requiresUpdateCoordinateSystem = false;

        if (newSettings.isShowManeuverLossVisualization() != settings.isShowManeuverLossVisualization()) {
            showManeuverLossChanged = true;
        }
        for (ManeuverType maneuverType : ManeuverType.values()) {
            if (newSettings.isShowManeuverType(maneuverType) != settings.isShowManeuverType(maneuverType)) {
                maneuverTypeSelectionChanged = true;
            }
        }
        if (newSettings.isShowDouglasPeuckerPoints() != settings.isShowDouglasPeuckerPoints()) {
            if (!(timer.getPlayState() == PlayStates.Playing) && lastDouglasPeuckerResult != null && newSettings.isShowDouglasPeuckerPoints()) {
                removeAllMarkDouglasPeuckerpoints();
                showMarkDouglasPeuckerPoints(lastDouglasPeuckerResult);
            } else if (!newSettings.isShowDouglasPeuckerPoints()) {
                removeAllMarkDouglasPeuckerpoints();
            }
        }
        if (newSettings.getTailLengthInMilliseconds() != settings.getTailLengthInMilliseconds()) {
            requiresRedraw = true;
        }
        if (!newSettings.getBuoyZoneRadius().equals(settings.getBuoyZoneRadius())) {
            requiresRedraw = true;
        }
        if (newSettings.isShowOnlySelectedCompetitors() != settings.isShowOnlySelectedCompetitors()) {
            requiresRedraw = true;
        }
        if (newSettings.isShowSelectedCompetitorsInfo() != settings.isShowSelectedCompetitorsInfo()) {
            requiresRedraw = true;
        }
        if (!newSettings.getZoomSettings().equals(settings.getZoomSettings())) {
            if (!newSettings.getZoomSettings().containsZoomType(ZoomTypes.NONE)) {
                removeTransitions();
                zoomMapToNewBounds(newSettings.getZoomSettings().getNewBounds(this));
            }
        }
        if (!newSettings.getHelpLinesSettings().equals(settings.getHelpLinesSettings())) {
            requiresRedraw = true;
        }
        if (!newSettings.isShowEstimatedDuration() && estimatedDurationOverlay != null){
            estimatedDurationOverlay.removeFromParent();
        }
        if (newSettings.isShowWindStreamletOverlay() != settings.isShowWindStreamletOverlay()) {
            streamletOverlay.setVisible(newSettings.isShowWindStreamletOverlay());
            streamletOverlay.setColors(newSettings.isShowWindStreamletColors());
        }
        if (newSettings.isShowWindStreamletColors() != settings.isShowWindStreamletColors()) {
            streamletOverlay.setColors(newSettings.isShowWindStreamletColors());
        }
        if (newSettings.isShowSimulationOverlay() != settings.isShowSimulationOverlay()) {
            showSimulationOverlay(newSettings.isShowSimulationOverlay());
        }
        if (newSettings.isWindUp() != settings.isWindUp()) {
            requiresUpdateCoordinateSystem = true;
            requiresRedraw = true;
        }
        if (!newSettings.isShowEstimatedDuration() && estimatedDurationOverlay != null){
            estimatedDurationOverlay.removeFromParent();
        }
        if (newSettings.getStartCountDownFontSizeScaling() != settings.getStartCountDownFontSizeScaling()) {
            if (countDownOverlay != null) {
                countDownOverlay.removeFromMap();
                countDownOverlay = null;
            }
            requiresRedraw = true;
        }
        this.settings = newSettings;
        if (maneuverTypeSelectionChanged || showManeuverLossChanged) {
            if (timer.getPlayState() != PlayStates.Playing) {
                maneuverMarkersAndLossIndicators.updateManeuverMarkersAfterSettingsChanged();
            }
        }
        if (requiresUpdateCoordinateSystem) {
            updateCoordinateSystemFromSettings();
        }
        if (requiresRedraw) {
            redraw();
        }
    }

    private void showSimulationOverlay(boolean visible) {
        simulationOverlay.setVisible(visible);
        if (visible) {
            simulationOverlay.updateLeg(Math.max(lastLegNumber,1), true, -1 /* ensure ui-update */);
        }
    }

    public static class BoatsBoundsCalculator extends LatLngBoundsCalculatorForSelected {
        @Override
        public LatLngBounds calculateNewBounds(RaceMap forMap) {
            LatLngBounds newBounds = null;
            Iterable<CompetitorDTO> selectedCompetitors = forMap.competitorSelection.getSelectedCompetitors();
            Iterable<CompetitorDTO> competitors = new ArrayList<>();
            if (selectedCompetitors == null || !selectedCompetitors.iterator().hasNext()) {
                competitors = forMap.getCompetitorsToShow();
            } else {
                competitors = isZoomOnlyToSelectedCompetitors(forMap) ? selectedCompetitors : forMap.getCompetitorsToShow();
            }
            for (CompetitorDTO competitor : competitors) {
                try {
                    GPSFixDTOWithSpeedWindTackAndLegType competitorFix = forMap.getBoatFix(competitor, forMap.timer.getTime());
                    Position competitorPosition = competitorFix != null ? competitorFix.position : null;
                    if (competitorPosition != null) {
                        if (newBounds == null) {
                            newBounds = BoundsUtil.getAsBounds(forMap.coordinateSystem.toLatLng(competitorPosition));
                        } else {
                            newBounds = newBounds.extend(forMap.coordinateSystem.toLatLng(competitorPosition));
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    // TODO can't this be predicted and the exception be avoided in the first place?
                    // Catch this in case the competitor has no GPS fixes at the current time (e.g. in race 'Finale 2' of STG)
                }
            }
            return newBounds;
        }
        
    }
    
    public static class TailsBoundsCalculator extends LatLngBoundsCalculatorForSelected {
        @Override
        public LatLngBounds calculateNewBounds(RaceMap racemap) {
            LatLngBounds newBounds = null;
            Iterable<CompetitorDTO> competitors = isZoomOnlyToSelectedCompetitors(racemap) ? racemap.competitorSelection.getSelectedCompetitors() : racemap.getCompetitorsToShow();
            for (CompetitorDTO competitor : competitors) {
                Polyline tail = racemap.fixesAndTails.getTail(competitor);
                LatLngBounds bounds = null;
                // TODO: Find a replacement for missing Polyline function getBounds() from v2
                // see also http://stackoverflow.com/questions/3284808/getting-the-bounds-of-a-polyine-in-google-maps-api-v3; 
                // optionally, consider providing a bounds cache with two sorted sets that organize the LatLng objects for O(1) bounds calculation and logarithmic add, ideally O(1) remove
                if (tail != null && tail.getPath().getLength() >= 1) {
                    bounds = BoundsUtil.getAsBounds(tail.getPath().get(0));
                    for (int i = 1; i < tail.getPath().getLength(); i++) {
                        bounds = bounds.extend(tail.getPath().get(i));
                    }
                }
                if (bounds != null) {
                    if (newBounds == null) {
                        newBounds = bounds;
                    } else {
                        newBounds = newBounds.union(bounds);
                    }
                }
            }
            return newBounds;
        }
    }
    
    public static class CourseMarksBoundsCalculator implements LatLngBoundsCalculator {
        @Override
        public LatLngBounds calculateNewBounds(RaceMap forMap) {
            LatLngBounds newBounds = null;
            Iterable<MarkDTO> marksToZoom = forMap.markDTOs.values();
            if (marksToZoom != null) {
                for (MarkDTO markDTO : marksToZoom) {
                    if (newBounds == null) {
                        newBounds = BoundsUtil.getAsBounds(forMap.coordinateSystem.toLatLng(markDTO.position));
                    } else {
                        newBounds = newBounds.extend(forMap.coordinateSystem.toLatLng(markDTO.position));
                    }
                }
            }
            return newBounds;
        }
    }

    public static class WindSensorsBoundsCalculator implements LatLngBoundsCalculator {
        @Override
        public LatLngBounds calculateNewBounds(RaceMap forMap) {
            LatLngBounds newBounds = null;
            Collection<WindSensorOverlay> marksToZoom = forMap.windSensorOverlays.values();
            if (marksToZoom != null) {
                for (WindSensorOverlay windSensorOverlay : marksToZoom) {
                    final LatLng latLngPosition = windSensorOverlay.getLatLngPosition();
                    if (Objects.nonNull(latLngPosition) && BoundsUtil.getAsPosition(latLngPosition) != null) {
                        LatLngBounds bounds = BoundsUtil.getAsBounds(latLngPosition);
                        if (newBounds == null) {
                            newBounds = bounds;
                        } else {
                            newBounds = newBounds.extend(latLngPosition);
                        }
                    }
                }
            }
            return newBounds;
        }
    }

    @Override
    public void initializeData(boolean showMapControls, boolean showHeaderPanel) {
        loadMapsAPIV3(showMapControls, showHeaderPanel);
    }

    @Override
    public boolean isDataInitialized() {
        return isMapInitialized;
    }

    @Override
    public void onResize() {
        if (map != null) {
            map.triggerResize();
            zoomMapToNewBounds(settings.getZoomSettings().getNewBounds(RaceMap.this));
        }
        // Adjust RaceMap headers to avoid overlapping based on the RaceMap width  
        boolean isCompactHeader = this.getOffsetWidth() <= 600;
        getLeftHeaderPanel().setStyleName(COMPACT_HEADER_STYLE, isCompactHeader);
        getRightHeaderPanel().setStyleName(COMPACT_HEADER_STYLE, isCompactHeader);
        
        // Adjust combined wind and true north indicator panel indent, based on the RaceMap height
        if (topLeftControlsWrapperPanel.getParent() != null) {
            this.adjustLeftControlsIndent();
        }
    }
    
    private void adjustLeftControlsIndent() {
        topLeftControlsWrapperPanel.getParent().setStyleName("TopLeftControlsWrapperPanelParentDiv");
        String leftControlsIndentStyle = getLeftControlsIndentStyle();
        if (leftControlsIndentStyle != null) {
            topLeftControlsWrapperPanel.getParent().addStyleName(leftControlsIndentStyle);
        }
    }

    @Override
    public void competitorsListChanged(Iterable<CompetitorDTO> competitors) {
        redraw();
    }
    
    @Override
    public void filteredCompetitorsListChanged(Iterable<CompetitorDTO> filteredCompetitors) {
        redraw();
    }
    
    @Override
    public void filterChanged(FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> oldFilterSet,
            FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> newFilterSet) {
        // nothing to do; if the list of filtered competitors has changed, a separate call to filteredCompetitorsListChanged will occur
    }

    @Override
    public PolylineOptions createTailStyle(CompetitorDTO competitor, DisplayMode displayMode) {
        PolylineOptions options = PolylineOptions.newInstance();
        options.setClickable(true);
        options.setGeodesic(true);
        options.setStrokeOpacity(1.0);

        switch(displayMode){
        case DEFAULT:
            options.setStrokeColor(competitorSelection.getColor(competitor, raceIdentifier).getAsHtml());
            options.setStrokeWeight(1);
            break;
        case SELECTED:
            options.setStrokeColor(competitorSelection.getColor(competitor, raceIdentifier).getAsHtml());
            options.setStrokeWeight(2);
            break;
        case NOT_SELECTED:
            options.setStrokeColor(LOWLIGHTED_TAIL_COLOR.getAsHtml());
            options.setStrokeOpacity(LOWLIGHTED_TAIL_OPACITY);
            break;
        }

        options.setZindex(RaceMapOverlaysZIndexes.BOATTAILS_ZINDEX);
        return options;
    }
    
    @Override
    public Polyline createTail(final CompetitorDTO competitor, List<LatLng> points) {
        final BoatDTO boat = competitorSelection.getBoat(competitor);
        PolylineOptions options = createTailStyle(competitor, displayHighlighted(competitor));
        Polyline result = Polyline.newInstance(options);
        MVCArray<LatLng> pointsAsArray = MVCArray.newInstance(points.toArray(new LatLng[0]));
        result.setPath(pointsAsArray);
        result.setMap(map);
        Hoverline resultHoverline = new Hoverline(result, options, this);
        final ClickMapHandler clickHandler = new ClickMapHandler() {
            @Override
            public void onEvent(ClickMapEvent event) {
                showCompetitorInfoWindow(competitor, event.getMouseEvent().getLatLng());
            }
        };
        result.addClickHandler(clickHandler);
        resultHoverline.addClickHandler(clickHandler);
        result.addMouseOverHandler(new MouseOverMapHandler() {
            @Override
            public void onEvent(MouseOverMapEvent event) {
                map.setTitle(boat.getSailId() + ", " + competitor.getName());
            }
        });
        resultHoverline.addMouseOutMoveHandler(new MouseOutMapHandler() {
            @Override
            public void onEvent(MouseOutMapEvent event) {
                map.setTitle("");
            }
        });
        return result;
    }

    @Override
    public Integer getRank(CompetitorDTO competitor) {
        final Integer result;
        QuickRankDTO quickRank = quickRanksDTOProvider.getQuickRanks().get(competitor.getIdAsString());
        if (quickRank != null) {
            result = quickRank.oneBasedRank;
        } else {
            result = null;
        }
        return result;
    }
    
    private Image createSAPLogo() {
        ImageResource sapLogoResource = resources.sapLogoOverlay();
        Image sapLogo = new Image(sapLogoResource);
        sapLogo.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.open("https://www.sap.com/sponsorships", "_blank", null);
            }
        });
        sapLogo.setStyleName("raceBoard-Logo");
        return sapLogo;
    }

    @Override
    public String getDependentCssClassName() {
        return "raceMap";
    }

    /**
     * The default zoom bounds are defined by the boats
     */
    private LatLngBounds getDefaultZoomBounds() {
        return new BoatsBoundsCalculator().calculateNewBounds(RaceMap.this);
    }

    private MapOptions getMapOptions(final boolean showMapControls, boolean windUp) {
        MapOptions mapOptions = MapOptions.newInstance();
          mapOptions.setScrollWheel(true);
          mapOptions.setMapTypeControl(showMapControls && !windUp);
          mapOptions.setPanControl(showMapControls);
          mapOptions.setZoomControl(showMapControls);
          mapOptions.setScaleControl(true);
          if (windUp) {
              mapOptions.setMinZoom(8);
          } else {
              mapOptions.setMinZoom(0);
          }
          MapTypeStyle[] mapTypeStyles = new MapTypeStyle[4];
          
          // hide all transit lines including ferry lines
          mapTypeStyles[0] = GoogleMapStyleHelper.createHiddenStyle(MapTypeStyleFeatureType.TRANSIT);
          // hide points of interest
          mapTypeStyles[1] = GoogleMapStyleHelper.createHiddenStyle(MapTypeStyleFeatureType.POI);
          // simplify road display
          mapTypeStyles[2] = GoogleMapStyleHelper.createSimplifiedStyle(MapTypeStyleFeatureType.ROAD);
          // set water color
          mapTypeStyles[3] = GoogleMapStyleHelper.createColorStyle(MapTypeStyleFeatureType.WATER, WATER_COLOR);
          
          MapTypeControlOptions mapTypeControlOptions = MapTypeControlOptions.newInstance();
          mapTypeControlOptions.setPosition(ControlPosition.BOTTOM_RIGHT);
          mapOptions.setMapTypeControlOptions(mapTypeControlOptions);

          mapOptions.setMapTypeStyles(mapTypeStyles);
          // no need to try to position the scale control; it always ends up at the right bottom corner
          mapOptions.setStreetViewControl(false);
          if (showMapControls) {
              ZoomControlOptions zoomControlOptions = ZoomControlOptions.newInstance();
              zoomControlOptions.setPosition(ControlPosition.RIGHT_TOP);
              mapOptions.setZoomControlOptions(zoomControlOptions);
              PanControlOptions panControlOptions = PanControlOptions.newInstance();
              panControlOptions.setPosition(ControlPosition.RIGHT_TOP);
              mapOptions.setPanControlOptions(panControlOptions);
          }
        return mapOptions;
    }

    /**
     * @return CSS style name to adjust the indent of left controls (combined wind and true north indicator).
     */
    protected String getLeftControlsIndentStyle() {
        return null;
    }

    public Map<String, CourseMarkOverlay> getCourseMarkOverlays() {
        return courseMarkOverlays;
    }
    
    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }
    
    public void hideAllHelplines() {
        if (startLine != null) {
            startLine.setVisible(false);
        }
        if (finishLine != null) {
            finishLine.setVisible(false);
        }
        if (advantageLine != null) {
            advantageLine.setVisible(false);
        }
        if (windwardStartLineMarkToFirstMarkLine != null && leewardStartLineMarkToFirstMarkLine != null) {
            windwardStartLineMarkToFirstMarkLine.setVisible(false);
            leewardStartLineMarkToFirstMarkLine.setVisible(false);
        }
        for (Polyline courseMiddleline : courseMiddleLines.values()) {
            if (courseMiddleline != null) {
                courseMiddleline.setVisible(false);
            }
        }
        for (Polygon courseSideline : courseSidelines.values()) {
            if (courseSideline != null) {
                courseSideline.setVisible(false);
            }
        }
    }
    
    public void showAllHelplinesToShow() {
        if (settings.getHelpLinesSettings().isShowAnyHelperLines()) {
            if (startLine != null && settings.getHelpLinesSettings().isVisible(HelpLineTypes.STARTLINE))
                startLine.setVisible(true);
            if (finishLine != null && settings.getHelpLinesSettings().isVisible(HelpLineTypes.FINISHLINE))
                finishLine.setVisible(true);
            if (advantageLine != null && settings.getHelpLinesSettings().isVisible(HelpLineTypes.ADVANTAGELINE))
                advantageLine.setVisible(true);
            if (windwardStartLineMarkToFirstMarkLine != null && leewardStartLineMarkToFirstMarkLine != null &&
                    settings.getHelpLinesSettings().isVisible(HelpLineTypes.STARTLINETOFIRSTMARKTRIANGLE)) {
                windwardStartLineMarkToFirstMarkLine.setVisible(true);
                leewardStartLineMarkToFirstMarkLine.setVisible(true);
            }
            if (settings.getHelpLinesSettings().isVisible(HelpLineTypes.COURSEMIDDLELINE)) {
                for (Polyline courseMiddleline : courseMiddleLines.values()) {
                    courseMiddleline.setVisible(true);
                }
            }
            if (settings.getHelpLinesSettings().isVisible(HelpLineTypes.COURSEGEOMETRY)) {
                for (Polygon courseSideline : courseSidelines.values()) {
                    courseSideline.setVisible(true);
                }
            }
        }
    }
    
    public void addCompetitorsForRaceDefinedListener(CompetitorsForRaceDefinedListener listener) {
        raceCompetitorSet.addCompetitorsForRaceDefinedListener(listener);
    }

    public void removeCompetitorsForRaceDefinedListener(CompetitorsForRaceDefinedListener listener) {
        raceCompetitorSet.removeCompetitorsForRaceDefinedListener(listener);
    }

    @Override
    public String getId() {
        return raceMapLifecycle.getComponentId();
    }
    
    public RaceMapLifecycle getLifecycle() {
        return raceMapLifecycle;
    }
}
