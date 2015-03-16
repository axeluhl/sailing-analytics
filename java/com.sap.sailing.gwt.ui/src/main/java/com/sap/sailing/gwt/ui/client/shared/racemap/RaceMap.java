package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.google.gwt.maps.client.overlays.InfoWindow;
import com.google.gwt.maps.client.overlays.InfoWindowOptions;
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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.Bounds;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.PositionDTO;
import com.sap.sailing.domain.common.impl.BoundsImpl;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalableBearing;
import com.sap.sailing.gwt.ui.actions.GetPolarAction;
import com.sap.sailing.gwt.ui.actions.GetRaceMapDataAction;
import com.sap.sailing.gwt.ui.actions.GetWindInfoAction;
import com.sap.sailing.gwt.ui.client.ClientResources;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.NumberFormatterFactory;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.RequiresDataInitialization;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.WindSourceTypeFormatter;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.client.shared.filter.QuickRankProvider;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapHelpLinesSettings.HelpLineTypes;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapZoomSettings.ZoomTypes;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.CoursePositionsDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.LegInfoDTO;
import com.sap.sailing.gwt.ui.shared.ManeuverDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;
import com.sap.sailing.gwt.ui.shared.RaceMapDataDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.SidelineDTO;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sailing.gwt.ui.shared.racemap.GoogleMapAPIKey;
import com.sap.sailing.gwt.ui.shared.racemap.GoogleMapStyleHelper;
import com.sap.sailing.gwt.ui.shared.racemap.RaceSimulationOverlay;
import com.sap.sailing.gwt.ui.shared.racemap.WindStreamletsRaceboardOverlay;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.common.impl.RGBColor;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;

public class RaceMap extends AbsolutePanel implements TimeListener, CompetitorSelectionChangeListener, RaceSelectionChangeListener,
        RaceTimesInfoProviderListener, TailFactory, Component<RaceMapSettings>, RequiresDataInitialization, RequiresResize, QuickRankProvider {
    public static final String GET_RACE_MAP_DATA_CATEGORY = "getRaceMapData";
    public static final String GET_WIND_DATA_CATEGORY = "getWindData";
    
    private MapWidget map;
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
     * Polyline for the course middle line.
     */
    private Polyline courseMiddleLine;

    private Map<SidelineDTO, Polygon> courseSidelines;
    
    /**
     * Wind data used to display the advantage line. Retrieved by a {@link GetWindInfoAction} execution and used in
     * {@link #showAdvantageLine(Iterable, Date)}.
     */
    private WindInfoForRaceDTO lastCombinedWindTrackInfoDTO;
    
    /**
     * Manages the cached set of {@link GPSFixDTO}s for the boat positions as well as their graphical counterpart in the
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
    private final Map<CompetitorDTO, CompetitorInfoOverlay> competitorInfoOverlays;
    
    private SmallTransparentInfoOverlay countDownOverlay;

    /**
     * Map overlays with html5 canvas used to display wind sensors
     */
    private final Map<WindSource, WindSensorOverlay> windSensorOverlays;

    /**
     * Map overlays with html5 canvas used to display course marks including buoy zones
     */
    private final Map<String, CourseMarkOverlay> courseMarkOverlays;

    private final Map<String, MarkDTO> markDTOs;

    /**
     * markers displayed in response to
     * {@link SailingServiceAsync#getDouglasPoints(String, String, Map, Map, double, AsyncCallback)}
     */
    protected Set<Marker> douglasMarkers;

    /**
     * markers displayed in response to
     * {@link SailingServiceAsync#getDouglasPoints(String, String, Map, Map, double, AsyncCallback)}
     */
    private Set<Marker> maneuverMarkers;

    private Map<CompetitorDTO, List<ManeuverDTO>> lastManeuverResult;

    private Map<CompetitorDTO, List<GPSFixDTO>> lastDouglasPeuckerResult;
    
    private CompetitorSelectionProvider competitorSelection;

    private List<RegattaAndRaceIdentifier> selectedRaces;

    /**
     * Used to check if the first initial zoom to the mark markers was already done.
     */
    private boolean mapFirstZoomDone = false;

    private final Timer timer;

    private RaceTimesInfoDTO lastRaceTimesInfo;
    
    private InfoWindow lastInfoWindow = null;
    
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

    private RaceMapImageManager raceMapImageManager; 

    private final RaceMapSettings settings;
    
    private final StringMessages stringMessages;
    
    private boolean isMapInitialized;

    private Date lastTimeChangeBeforeInitialization;

    /**
     * The last quick ranks received from a call to {@link SailingServiceAsync#getQuickRanks(RaceIdentifier, Date, AsyncCallback)} upon
     * the last {@link #timeChanged(Date, Date)} event. Therefore, the ranks listed here correspond to the {@link #timer}'s time.
     */
    private LinkedHashMap<CompetitorDTO, QuickRankDTO> quickRanks;

    private final CombinedWindPanel combinedWindPanel;
    
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
    private final boolean showViewStreamlets;
    private final boolean showViewSimulation;
    
    private static final String GET_POLAR_CATEGORY = "getPolar";
    
    /**
     * Tells about the availability of polar / VPP data for this race. If available, the simulation feature can be
     * offered to the user.
     */
    private boolean hasPolar;
    
    private final RegattaAndRaceIdentifier raceIdentifier;

    public RaceMap(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            ErrorReporter errorReporter, Timer timer, CompetitorSelectionProvider competitorSelection,
            StringMessages stringMessages, boolean showMapControls, boolean showViewStreamlets, boolean showViewSimulation,
            RegattaAndRaceIdentifier raceIdentifier) {
        this.setSize("100%", "100%");
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.errorReporter = errorReporter;
        this.timer = timer;
        timer.addTimeListener(this);
        raceMapImageManager = new RaceMapImageManager();
        fixesAndTails = new FixesAndTails();
        markDTOs = new HashMap<String, MarkDTO>();
        courseSidelines = new HashMap<SidelineDTO, Polygon>();
        boatOverlays = new HashMap<CompetitorDTO, BoatOverlay>();
        competitorInfoOverlays = new HashMap<CompetitorDTO, CompetitorInfoOverlay>();
        windSensorOverlays = new HashMap<WindSource, WindSensorOverlay>();
        courseMarkOverlays = new HashMap<String, CourseMarkOverlay>();
        this.competitorSelection = competitorSelection;
        competitorSelection.addCompetitorSelectionChangeListener(this);
        settings = new RaceMapSettings();
        lastTimeChangeBeforeInitialization = null;
        isMapInitialized = false;
        this.showViewStreamlets = showViewStreamlets;
        this.showViewSimulation = showViewSimulation;
        this.hasPolar = false;
        headerPanel = new FlowPanel();
        headerPanel.setStyleName("RaceMap-HeaderPanel");
        panelForLeftHeaderLabels = new AbsolutePanel();
        panelForRightHeaderLabels = new AbsolutePanel();
        initializeData(showMapControls);
        
        combinedWindPanel = new CombinedWindPanel(raceMapImageManager, stringMessages);
        combinedWindPanel.setVisible(false);
    }
    
    private void loadMapsAPIV3(final boolean showMapControls) {
        boolean sensor = true;

        // load all the libs for use in the maps
        ArrayList<LoadLibrary> loadLibraries = new ArrayList<LoadApi.LoadLibrary>();
        loadLibraries.add(LoadLibrary.DRAWING);
        loadLibraries.add(LoadLibrary.GEOMETRY);

        Runnable onLoad = new Runnable() {
          @Override
          public void run() {
              MapOptions mapOptions = MapOptions.newInstance();
              mapOptions.setScrollWheel(true);
              mapOptions.setMapTypeControl(showMapControls);
              mapOptions.setPanControl(showMapControls);
              mapOptions.setZoomControl(showMapControls);
              mapOptions.setScaleControl(true);
              
              MapTypeStyle[] mapTypeStyles = new MapTypeStyle[4];
              
              // hide all transit lines including ferry lines
              mapTypeStyles[0] = GoogleMapStyleHelper.createHiddenStyle(MapTypeStyleFeatureType.TRANSIT);
              // hide points of interest
              mapTypeStyles[1] = GoogleMapStyleHelper.createHiddenStyle(MapTypeStyleFeatureType.POI);
              // simplify road display
              mapTypeStyles[2] = GoogleMapStyleHelper.createSimplifiedStyle(MapTypeStyleFeatureType.ROAD);
              // set water color
              // To play with the styles, check out http://gmaps-samples-v3.googlecode.com/svn/trunk/styledmaps/wizard/index.html.
              // To convert an RGB color into the strange hue/saturation/lightness model used by the Google Map use
              // http://software.stadtwerk.org/google_maps_colorizr/#water/all/123456/.
              mapTypeStyles[3] = GoogleMapStyleHelper.createColorStyle(MapTypeStyleFeatureType.WATER, new RGBColor(0, 136, 255), 0, -70);
              
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
              map = new MapWidget(mapOptions);
              RaceMap.this.add(map, 0, 0);
              Image sapLogo = createSAPLogo();
              RaceMap.this.add(sapLogo);
              map.setControls(ControlPosition.LEFT_TOP, combinedWindPanel);
              combinedWindPanel.getParent().addStyleName("CombinedWindPanelParentDiv");

              RaceMap.this.raceMapImageManager.loadMapIcons(map);
              map.setSize("100%", "100%");
              map.addZoomChangeHandler(new ZoomChangeMapHandler() {
                  @Override
                  public void onEvent(ZoomChangeMapEvent event) {
                      if (!autoZoomIn && !autoZoomOut) {
                          // stop automatic zoom after a manual zoom event; automatic zoom in zoomMapToNewBounds will restore old settings
                          final List<RaceMapZoomSettings.ZoomTypes> emptyList = Collections.emptyList();
                          settings.getZoomSettings().setTypesToConsiderOnZoom(emptyList);
                      }
                  }
              });
              map.addDragEndHandler(new DragEndMapHandler() {
                  @Override
                  public void onEvent(DragEndMapEvent event) {
                      // stop automatic zoom after a manual drag event
                      autoZoomIn = false;
                      autoZoomOut = false;
                      final List<RaceMapZoomSettings.ZoomTypes> emptyList = Collections.emptyList();
                      settings.getZoomSettings().setTypesToConsiderOnZoom(emptyList);
                  }
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
                      if ((streamletOverlay != null) && !map.getBounds().equals(currentMapBounds)) {
                          streamletOverlay.onBoundsChanged(newZoomLevel != currentZoomLevel);
                      }
                      if ((simulationOverlay != null) && !map.getBounds().equals(currentMapBounds)) {
                          simulationOverlay.onBoundsChanged(newZoomLevel != currentZoomLevel);
                      }
                      currentMapBounds = map.getBounds();
                      currentZoomLevel = newZoomLevel;
                      headerPanel.getElement().getStyle().setWidth(map.getOffsetWidth(), Unit.PX);
                  }
              });
              
              // If there was a time change before the API was loaded, reset the time
              if (lastTimeChangeBeforeInitialization != null) {
                  timeChanged(lastTimeChangeBeforeInitialization, null);
                  lastTimeChangeBeforeInitialization = null;
              }
              // Initialize streamlet canvas for wind visualization; it shouldn't be doing anything unless it's visible
              streamletOverlay = new WindStreamletsRaceboardOverlay(getMap(), /* zIndex */ 0,
                      timer, raceIdentifier, sailingService, asyncActionsExecutor, stringMessages);
              streamletOverlay.addToMap();
              if (showViewStreamlets) {
                  streamletOverlay.setVisible(true);
              }

              if (showViewSimulation) {
            	  // determine availability of polar diagram
            	  setHasPolar();
                  // initialize simulation canvas
                  simulationOverlay = new RaceSimulationOverlay(getMap(), /* zIndex */ 0, raceIdentifier, sailingService, stringMessages, asyncActionsExecutor);
                  simulationOverlay.addToMap();
                  simulationOverlay.setVisible(false);
              }
              
              createHeaderPanel(map);
              createSettingsButton(map);

              // Data has been initialized
              RaceMap.this.isMapInitialized = true;
              RaceMap.this.redraw();
          }
        };

        LoadApi.go(onLoad, loadLibraries, sensor, "key="+GoogleMapAPIKey.V3_APIKey); 
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
        add(panelForRightHeaderLabels);
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
        settingsButton.setTitle(stringMessages.settings());
        settingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new SettingsDialog<RaceMapSettings>(component, stringMessages).show();
            }
        });
        map.setControls(ControlPosition.RIGHT_TOP, settingsButton);
    }

    private void removeTransitions() {
        // remove the canvas animations for boats
        for (BoatOverlay boatOverlay : RaceMap.this.getBoatOverlays().values()) {
            boatOverlay.removeCanvasPositionAndRotationTransition();
        }
        // remove the canvas animations for the info overlays of the selected boats
        for (CompetitorInfoOverlay infoOverlay : competitorInfoOverlays.values()) {
            infoOverlay.removeCanvasPositionAndRotationTransition();
        }
    }

    public void redraw() {
        timeChanged(timer.getTime(), null);
    }
    
    Map<CompetitorDTO, BoatOverlay> getBoatOverlays() {
        return Collections.unmodifiableMap(boatOverlays);
    }
    
    public MapWidget getMap() {
        return map;
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
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
        mapFirstZoomDone = false;
        // TODO bug 494: reset zoom settings to user preferences
        this.selectedRaces = selectedRaces;
    }

    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfos, long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest, long clientTimeWhenResponseWasReceived) {
        timer.adjustClientServerOffset(clientTimeWhenRequestWasSent, serverTimeDuringRequest, clientTimeWhenResponseWasReceived);
        this.lastRaceTimesInfo = raceTimesInfos.get(selectedRaces.get(0));        
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

    @Override
    public void timeChanged(final Date newTime, final Date oldTime) {
        if (newTime != null && isMapInitialized) {
            if (selectedRaces != null && !selectedRaces.isEmpty()) {
                RegattaAndRaceIdentifier race = selectedRaces.get(selectedRaces.size() - 1);
                final Iterable<CompetitorDTO> competitorsToShow = getCompetitorsToShow();
                
                if (race != null) {
                    final com.sap.sse.common.Util.Triple<Map<CompetitorDTO, Date>, Map<CompetitorDTO, Date>, Map<CompetitorDTO, Boolean>> fromAndToAndOverlap = 
                            fixesAndTails.computeFromAndTo(newTime, competitorsToShow, settings.getEffectiveTailLengthInMilliseconds());
                    int requestID = ++boatPositionRequestIDCounter;
                    // For those competitors for which the tails don't overlap (and therefore will be replaced by the new tail coming from the server)
                    // we expect some potential delay in computing the full tail. Therefore, in those cases we fire two requests: one fetching only the
                    // boat positions at newTime with zero tail length; and another one fetching everything else.
                    GetRaceMapDataAction getRaceMapDataForAllOverlappingAndTipsOfNonOverlapping = getRaceMapDataForAllOverlappingAndTipsOfNonOverlapping(fromAndToAndOverlap, race, newTime);
                    if (getRaceMapDataForAllOverlappingAndTipsOfNonOverlapping != null) {
                        asyncActionsExecutor.execute(getRaceMapDataForAllOverlappingAndTipsOfNonOverlapping, GET_RACE_MAP_DATA_CATEGORY,
                                getRaceMapDataCallback(oldTime, newTime, fromAndToAndOverlap.getC(), competitorsToShow, requestID));
                        requestID = ++boatPositionRequestIDCounter;
                    }
                    // next, do the full thing; being the later call, if request throttling kicks in, the later call
                    // supersedes the earlier call which may get dropped then
                    GetRaceMapDataAction getRaceMapDataAction = new GetRaceMapDataAction(sailingService, competitorSelection.getAllCompetitors(), race,
                            useNullAsTimePoint() ? null : newTime, fromAndToAndOverlap.getA(), fromAndToAndOverlap.getB(), /* extrapolate */ true, (simulationOverlay==null ? null : simulationOverlay.getLegIdentifier()));
                    asyncActionsExecutor.execute(getRaceMapDataAction, GET_RACE_MAP_DATA_CATEGORY,
                            getRaceMapDataCallback(oldTime, newTime, fromAndToAndOverlap.getC(), competitorsToShow, requestID));
                    
                    // draw the wind into the map, get the combined wind
                    // TODO bug2057 also fetch wind for LEG_MIDDLE for all legs because this needs to be the basis for the advantage line display
                    List<String> windSourceTypeNames = new ArrayList<String>();
                    windSourceTypeNames.add(WindSourceType.EXPEDITION.name());
                    windSourceTypeNames.add(WindSourceType.COMBINED.name());
                    GetWindInfoAction getWindInfoAction = new GetWindInfoAction(sailingService, race, newTime, 1000L, 1, windSourceTypeNames,
                            /* onlyUpToNewestEvent==false means get us any data we can get by a best effort */ false);
                    asyncActionsExecutor.execute(getWindInfoAction, GET_WIND_DATA_CATEGORY, new AsyncCallback<WindInfoForRaceDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError("Error obtaining wind information: " + caught.getMessage(), true /*silentMode */);
                        }

                        @Override
                        public void onSuccess(WindInfoForRaceDTO windInfo) {
                            List<com.sap.sse.common.Util.Pair<WindSource, WindTrackInfoDTO>> windSourcesToShow = new ArrayList<com.sap.sse.common.Util.Pair<WindSource, WindTrackInfoDTO>>();
                            if (windInfo != null) {
                                lastCombinedWindTrackInfoDTO = windInfo; 
                                showAdvantageLine(competitorsToShow, newTime);
                                for (WindSource windSource: windInfo.windTrackInfoByWindSource.keySet()) {
                                    WindTrackInfoDTO windTrackInfoDTO = windInfo.windTrackInfoByWindSource.get(windSource);
                                    switch (windSource.getType()) {
                                        case EXPEDITION:
                                            // we filter out measured wind sources with vary low confidence
                                            if (windTrackInfoDTO.minWindConfidence > 0.0001) {
                                                windSourcesToShow.add(new com.sap.sse.common.Util.Pair<WindSource, WindTrackInfoDTO>(windSource, windTrackInfoDTO));
                                            }
                                            break;
                                        case COMBINED:
                                            showCombinedWindOnMap(windSource, windTrackInfoDTO);
                                            break;
                                    default:
                                        // Which wind sources are requested is defined in a list above this
                                        // action. So we throw here an exception to notice a missing source.
                                        throw new UnsupportedOperationException(
                                                "There is currently no support for the enum value '"
                                                        + windSource.getType() + "' in this method.");
                                    }
                                }
                            }
                            showWindSensorsOnMap(windSourcesToShow);
                        }
                    });
                }
            }
        }
    }

    /**
     * We assume that overlapping segments usually don't require a lot of loading time as the most typical case will be to update a longer
     * tail with a few new fixes that were received since the last time tick. Non-overlapping position requests typically occur for the
     * first request when no fix at all is known for the competitor yet, and when the user has radically moved the time slider to some
     * other time such that given the current tail length setting the new tail segment does not overlap with the old one, requiring a full
     * load of the entire tail data for that competitor.<p>
     * 
     * For the non-overlapping requests, this method creates a separate request which only loads boat positions, quick ranks, sidelines and
     * mark positions for the zero-length interval at <code>newTime</code>, assuming that this will work fairly fast and in particular in
     * O(1) time regardless of tail length, compared to fetching the entire tail for all competitors.
     */
    private GetRaceMapDataAction getRaceMapDataForAllOverlappingAndTipsOfNonOverlapping(
            Triple<Map<CompetitorDTO, Date>, Map<CompetitorDTO, Date>, Map<CompetitorDTO, Boolean>> fromAndToAndOverlap,
            RegattaAndRaceIdentifier race, Date newTime) {
        Map<CompetitorDTO, Date> fromTimes = new HashMap<>();
        Map<CompetitorDTO, Date> toTimes = new HashMap<>();
        for (Map.Entry<CompetitorDTO, Boolean> e : fromAndToAndOverlap.getC().entrySet()) {
            if (!e.getValue()) {
                // no overlap; add competitor to request
                fromTimes.put(e.getKey(), newTime);
                toTimes.put(e.getKey(), newTime);
            }
        }
        final GetRaceMapDataAction result;
        if (!fromTimes.isEmpty()) {
            result = new GetRaceMapDataAction(sailingService, competitorSelection.getAllCompetitors(),
                race, useNullAsTimePoint() ? null : newTime, fromTimes, toTimes, /* extrapolate */true, (simulationOverlay==null ? null: simulationOverlay.getLegIdentifier()));
        } else {
            result = null;
        }
        return result;
    }

    private AsyncCallback<RaceMapDataDTO> getRaceMapDataCallback(
            final Date oldTime,
            final Date newTime,
            final Map<CompetitorDTO, Boolean> hasTailOverlapForCompetitor,
            final Iterable<CompetitorDTO> competitorsToShow, final int requestID) {
        return new AsyncCallback<RaceMapDataDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error obtaining racemap data: " + caught.getMessage(), true /*silentMode */);
            }
            
            @Override
            public void onSuccess(RaceMapDataDTO raceMapDataDTO) {
                if (map != null && raceMapDataDTO != null) {
                    quickRanks = raceMapDataDTO.quickRanks;
                    if (showViewSimulation && settings.isShowSimulationOverlay()) {
                    	simulationOverlay.updateLeg(getCurrentLeg(), /* clearCanvas */ false, raceMapDataDTO.simulationResultVersion);
                    }
                    // process response only if not received out of order
                    if (startedProcessingRequestID < requestID) {
                        startedProcessingRequestID = requestID;
                        // Do boat specific actions
                        Map<CompetitorDTO, List<GPSFixDTO>> boatData = raceMapDataDTO.boatPositions;
                        long timeForPositionTransitionMillis = calculateTimeForPositionTransition(newTime, oldTime);
                        fixesAndTails.updateFixes(boatData, hasTailOverlapForCompetitor, RaceMap.this, timeForPositionTransitionMillis);
                        showBoatsOnMap(newTime, timeForPositionTransitionMillis, getCompetitorsToShow());
                        showCompetitorInfoOnMap(newTime, timeForPositionTransitionMillis, competitorSelection.getSelectedFilteredCompetitors());
                        if (douglasMarkers != null) {
                            removeAllMarkDouglasPeuckerpoints();
                        }
                        if (maneuverMarkers != null) {
                            removeAllManeuverMarkers();
                        }
                        
                        // Do mark specific actions
                        showCourseMarksOnMap(raceMapDataDTO.coursePositions);
                        showCourseSidelinesOnMap(raceMapDataDTO.courseSidelines);                            
                        showStartAndFinishLines(raceMapDataDTO.coursePositions);
                        // even though the wind data is retrieved by a separate call, re-draw the advantage line because it needs to
                        // adjust to new boat positions
                        showAdvantageLine(competitorsToShow, newTime);
                            
                        // Rezoom the map
                        Bounds zoomToBounds = null;
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
                    }
                } else {
                    lastTimeChangeBeforeInitialization = newTime;
                }
            }
        };
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
                        sidelinePoints[i] = LatLng.newInstance(sidelineMark.position.latDeg, sidelineMark.position.lngDeg);
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
       
    protected void showCourseMarksOnMap(CoursePositionsDTO courseDTO) {
        if (map != null && courseDTO != null) {
            Map<String, CourseMarkOverlay> toRemoveCourseMarks = new HashMap<String, CourseMarkOverlay>(courseMarkOverlays);
            if (courseDTO.marks != null) {
                for (MarkDTO markDTO : courseDTO.marks) {
                    CourseMarkOverlay courseMarkOverlay = courseMarkOverlays.get(markDTO.getName());
                    if (courseMarkOverlay == null) {
                        courseMarkOverlay = createCourseMarkOverlay(RaceMapOverlaysZIndexes.COURSEMARK_ZINDEX, markDTO);
                        courseMarkOverlay.setShowBuoyZone(settings.getHelpLinesSettings().isVisible(HelpLineTypes.BUOYZONE));
                        courseMarkOverlay.setBuoyZoneRadiusInMeter(settings.getBuoyZoneRadiusInMeters());
                        courseMarkOverlays.put(markDTO.getName(), courseMarkOverlay);
                        markDTOs.put(markDTO.getName(), markDTO);
                        courseMarkOverlay.addToMap();
                    } else {
                        courseMarkOverlay.setMarkPosition(markDTO.position);
                        courseMarkOverlay.setShowBuoyZone(settings.getHelpLinesSettings().isVisible(HelpLineTypes.BUOYZONE));
                        courseMarkOverlay.setBuoyZoneRadiusInMeter(settings.getBuoyZoneRadiusInMeters());
                        courseMarkOverlay.draw();
                        toRemoveCourseMarks.remove(markDTO.getName());
                    }
                }
            }
            for (String toRemoveMarkName : toRemoveCourseMarks.keySet()) {
                CourseMarkOverlay removedOverlay = courseMarkOverlays.remove(toRemoveMarkName);
                if(removedOverlay != null) {
                    removedOverlay.removeFromMap();
                }
            }
        }
    }

    protected void showCombinedWindOnMap(WindSource windSource, WindTrackInfoDTO windTrackInfoDTO) {
        if (map != null) {
            combinedWindPanel.setWindInfo(windTrackInfoDTO, windSource);
            combinedWindPanel.redraw();
        }
    }

    protected void showWindSensorsOnMap(List<com.sap.sse.common.Util.Pair<WindSource, WindTrackInfoDTO>> windSensorsList) {
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

    protected void showCompetitorInfoOnMap(final Date newTime, final long timeForPositionTransitionMillis, final Iterable<CompetitorDTO> competitorsToShow) {
        if (map != null) {
            if (settings.isShowSelectedCompetitorsInfo()) {
                Set<CompetitorDTO> toRemoveCompetorInfoOverlays = new HashSet<CompetitorDTO>(
                        competitorInfoOverlays.keySet());
                for (CompetitorDTO competitorDTO : competitorsToShow) {
                    if (fixesAndTails.hasFixesFor(competitorDTO)) {
                        GPSFixDTO lastBoatFix = getBoatFix(competitorDTO, newTime);
                        if (lastBoatFix != null) {
                            CompetitorInfoOverlay competitorInfoOverlay = competitorInfoOverlays.get(competitorDTO);
                            if (competitorInfoOverlay == null) {
                                competitorInfoOverlay = createCompetitorInfoOverlay(RaceMapOverlaysZIndexes.INFO_OVERLAY_ZINDEX, competitorDTO);
                                competitorInfoOverlays.put(competitorDTO, competitorInfoOverlay);
                                competitorInfoOverlay.setPosition(lastBoatFix.position, timeForPositionTransitionMillis);
                                competitorInfoOverlay.addToMap();
                            } else {
                                competitorInfoOverlay.setPosition(lastBoatFix.position, timeForPositionTransitionMillis);
                                competitorInfoOverlay.draw();
                            }
                            toRemoveCompetorInfoOverlays.remove(competitorDTO);
                        }
                    }
                }
                for (CompetitorDTO toRemoveCompetorDTO : toRemoveCompetorInfoOverlays) {
                    CompetitorInfoOverlay competitorInfoOverlay = competitorInfoOverlays.get(toRemoveCompetorDTO);
                    competitorInfoOverlay.removeFromMap();
                    competitorInfoOverlays.remove(toRemoveCompetorDTO);
                }
            } else {
                // remove all overlays
                for (CompetitorInfoOverlay competitorInfoOverlay : competitorInfoOverlays.values()) {
                    competitorInfoOverlay.removeFromMap();
                }
                competitorInfoOverlays.clear();
            }
        }
    }

    private long calculateTimeForPositionTransition(final Date newTime, final Date oldTime) {
        final long timeForPositionTransitionMillis;
        boolean hasTimeJumped = oldTime != null && Math.abs(oldTime.getTime() - newTime.getTime()) > 3*timer.getRefreshInterval();
        if (timer.getPlayState() == PlayStates.Playing && !hasTimeJumped) {
            // choose 130% of the refresh interval as transition period to make it unlikely that the transition
            // stops before the next update has been received
            timeForPositionTransitionMillis = 1300 * timer.getRefreshInterval() / 1000; 
        } else {
            timeForPositionTransitionMillis = -1; // -1 means 'no transition
        }
        return timeForPositionTransitionMillis;
    }
    
    protected void showBoatsOnMap(final Date newTime, final long timeForPositionTransitionMillis, final Iterable<CompetitorDTO> competitorsToShow) {
        if (map != null) {
            Date tailsFromTime = new Date(newTime.getTime() - settings.getEffectiveTailLengthInMilliseconds());
            Date tailsToTime = newTime;
            Set<CompetitorDTO> competitorDTOsOfUnusedTails = new HashSet<CompetitorDTO>(fixesAndTails.getCompetitorsWithTails());
            Set<CompetitorDTO> competitorDTOsOfUnusedBoatCanvases = new HashSet<CompetitorDTO>(boatOverlays.keySet());
            for (CompetitorDTO competitorDTO : competitorsToShow) {
                if (fixesAndTails.hasFixesFor(competitorDTO)) {
                    Polyline tail = fixesAndTails.getTail(competitorDTO);
                    if (tail == null) {
                        tail = fixesAndTails.createTailAndUpdateIndices(competitorDTO, tailsFromTime, tailsToTime, this);
                        tail.setMap(map);
                    } else {
                        fixesAndTails.updateTail(tail, competitorDTO, tailsFromTime, tailsToTime,
                                (int) (timeForPositionTransitionMillis==-1?-1:timeForPositionTransitionMillis/2));
                        competitorDTOsOfUnusedTails.remove(competitorDTO);
                        PolylineOptions newOptions = createTailStyle(competitorDTO, displayHighlighted(competitorDTO));
                        tail.setOptions(newOptions);
                    }
                    boolean usedExistingBoatCanvas = updateBoatCanvasForCompetitor(competitorDTO, newTime, timeForPositionTransitionMillis);
                    if (usedExistingBoatCanvas) {
                        competitorDTOsOfUnusedBoatCanvases.remove(competitorDTO);
                    }
                }
            }
            for (CompetitorDTO unusedBoatCanvasCompetitorDTO : competitorDTOsOfUnusedBoatCanvases) {
                BoatOverlay boatCanvas = boatOverlays.get(unusedBoatCanvasCompetitorDTO);
                boatCanvas.removeFromMap();
                boatOverlays.remove(unusedBoatCanvasCompetitorDTO);
            }
            for (CompetitorDTO unusedTailCompetitorDTO : competitorDTOsOfUnusedTails) {
                fixesAndTails.removeTail(unusedTailCompetitorDTO);
            }
        }
    }

    /**
     * This algorithm is limited to distances such that dlon < pi/2, i.e., those that extend around less than one
     * quarter of the circumference of the earth in longitude. A completely general, but more complicated algorithm is
     * necessary if greater distances are allowed.
     */
    public LatLng calculatePositionAlongRhumbline(LatLng position, double bearingDeg, double distanceInKm) {
        double distianceRad = distanceInKm / 6371.0;  // r = 6371 means earth's radius in km 
        double lat1 = position.getLatitude() / 180. * Math.PI;
        double lon1 = position.getLongitude() / 180. * Math.PI;
        double bearingRad = bearingDeg / 180. * Math.PI;

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distianceRad) + 
                        Math.cos(lat1) * Math.sin(distianceRad) * Math.cos(bearingRad));
        double lon2 = lon1 + Math.atan2(Math.sin(bearingRad)*Math.sin(distianceRad)*Math.cos(lat1), 
                       Math.cos(distianceRad)-Math.sin(lat1)*Math.sin(lat2));
        lon2 = (lon2+3*Math.PI) % (2*Math.PI) - Math.PI;  // normalize to -180..+180�
        
        return LatLng.newInstance(lat2 / Math.PI * 180., lon2  / Math.PI * 180.);
    }
    
    /**
     * Returns a pair whose first component is the leg number (one-based) of the competitor returned as the second component.
     */
    private com.sap.sse.common.Util.Pair<Integer, CompetitorDTO> getLeadingVisibleCompetitorWithOneBasedLegNumber(
            Iterable<CompetitorDTO> competitorsToShow) {
        CompetitorDTO leadingCompetitorDTO = null;
        int legOfLeaderCompetitor = -1;
        // this only works because the quickRanks are sorted
        for (QuickRankDTO quickRank : quickRanks.values()) {
            if (Util.contains(competitorsToShow, quickRank.competitor)) {
                leadingCompetitorDTO = quickRank.competitor;
                legOfLeaderCompetitor = quickRank.legNumberOneBased;
                return new com.sap.sse.common.Util.Pair<Integer, CompetitorDTO>(legOfLeaderCompetitor, leadingCompetitorDTO);
            }
        }
        return null;
    }

    private void showAdvantageLine(Iterable<CompetitorDTO> competitorsToShow, Date date) {
        if (map != null && lastRaceTimesInfo != null && quickRanks != null && lastCombinedWindTrackInfoDTO != null) {
            boolean drawAdvantageLine = false;
            if (settings.getHelpLinesSettings().isVisible(HelpLineTypes.ADVANTAGELINE)) {
                // find competitor with highest rank
                com.sap.sse.common.Util.Pair<Integer, CompetitorDTO> visibleLeaderInfo = getLeadingVisibleCompetitorWithOneBasedLegNumber(competitorsToShow);
                // the boat fix may be null; may mean that no positions were loaded yet for the leading visible boat;
                // don't show anything
                GPSFixDTO lastBoatFix = null;
                boolean isVisibleLeaderInfoComplete = false;
                boolean isLegTypeKnown = false;
                WindTrackInfoDTO windDataForLegMiddle = null;
                if (visibleLeaderInfo != null
                        && visibleLeaderInfo.getA() > 0
                        && visibleLeaderInfo.getA() <= lastRaceTimesInfo.getLegInfos().size()
                        // get wind at middle of leg for leading visible competitor
                        && (windDataForLegMiddle = lastCombinedWindTrackInfoDTO
                                .getCombinedWindOnLegMiddle(visibleLeaderInfo.getA() - 1)) != null
                        && !windDataForLegMiddle.windFixes.isEmpty()) {
                    isVisibleLeaderInfoComplete = true;
                    LegInfoDTO legInfoDTO = lastRaceTimesInfo.getLegInfos().get(visibleLeaderInfo.getA() - 1);
                    if (legInfoDTO.legType != null) {
                        isLegTypeKnown = true;
                    }
                    lastBoatFix = getBoatFix(visibleLeaderInfo.getB(), date);
                }
                if (isVisibleLeaderInfoComplete && isLegTypeKnown && lastBoatFix != null && lastBoatFix.speedWithBearing != null) {
                    LegInfoDTO legInfoDTO = lastRaceTimesInfo.getLegInfos().get(visibleLeaderInfo.getA() - 1);
                    double advantageLineLengthInKm = 1.0; // TODO this should probably rather scale with the visible
                                                          // area of the map; bug 616
                    double distanceFromBoatPositionInKm = visibleLeaderInfo.getB().getBoatClass()
                            .getHullLengthInMeters() / 1000.; // one hull length
                    // implement and use Position.translateRhumb()
                    double bearingOfBoatInDeg = lastBoatFix.speedWithBearing.bearingInDegrees;
                    LatLng boatPosition = LatLng.newInstance(lastBoatFix.position.latDeg, lastBoatFix.position.lngDeg);
                    LatLng posAheadOfFirstBoat = calculatePositionAlongRhumbline(boatPosition, bearingOfBoatInDeg,
                            distanceFromBoatPositionInKm);
                    final WindDTO windFix = windDataForLegMiddle.windFixes.get(0);
                    double bearingOfCombinedWindInDeg = windFix.trueWindBearingDeg;
                    double rotatedBearingDeg1 = 0.0;
                    double rotatedBearingDeg2 = 0.0;
                    switch (legInfoDTO.legType) {
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
                    }
                        break;
                    case REACHING: {
                        rotatedBearingDeg1 = legInfoDTO.legBearingInDegrees + 90.0;
                        if (rotatedBearingDeg1 >= 360.0) {
                            rotatedBearingDeg1 -= 360.0;
                        }
                        rotatedBearingDeg2 = legInfoDTO.legBearingInDegrees - 90.0;
                        if (rotatedBearingDeg2 < 0.0) {
                            rotatedBearingDeg2 += 360.0;
                        }
                    }
                        break;
                    }
                    LatLng advantageLinePos1 = calculatePositionAlongRhumbline(posAheadOfFirstBoat, rotatedBearingDeg1,
                            advantageLineLengthInKm / 2.0);
                    LatLng advantageLinePos2 = calculatePositionAlongRhumbline(posAheadOfFirstBoat, rotatedBearingDeg2,
                            advantageLineLengthInKm / 2.0);

                    if (advantageLine == null) {
                        PolylineOptions options = PolylineOptions.newInstance();
                        options.setClickable(true);
                        options.setGeodesic(true);
                        options.setStrokeColor("#000000");
                        options.setStrokeWeight(1);
                        options.setStrokeOpacity(0.5);

                        advantageLine = Polyline.newInstance(options);
                        MVCArray<LatLng> pointsAsArray = MVCArray.newInstance();
                        pointsAsArray.insertAt(0, advantageLinePos1);
                        pointsAsArray.insertAt(1, advantageLinePos2);
                        advantageLine.setPath(pointsAsArray);

                        advantageLineMouseOverHandler = new AdvantageLineMouseOverMapHandler(
                                bearingOfCombinedWindInDeg, new Date(windFix.measureTimepoint));
                        advantageLine.addMouseOverHandler(advantageLineMouseOverHandler);
                        advantageLine.addMouseOutMoveHandler(new MouseOutMapHandler() {
                            @Override
                            public void onEvent(MouseOutMapEvent event) {
                                map.setTitle("");
                            }
                        });
                        advantageLine.setMap(map);
                    } else {
                        advantageLine.getPath().removeAt(1);
                        advantageLine.getPath().removeAt(0);
                        advantageLine.getPath().insertAt(0, advantageLinePos1);
                        advantageLine.getPath().insertAt(1, advantageLinePos2);
                        advantageLineMouseOverHandler.setTrueWindBearing(bearingOfCombinedWindInDeg);
                        advantageLineMouseOverHandler.setDate(new Date(windFix.measureTimepoint));
                    }
                    drawAdvantageLine = true;
                }
            }
            if (!drawAdvantageLine) {
                if (advantageLine != null) {
                    advantageLine.setMap(null);
                    advantageLine = null;
                }
            }
        }
    }
    
    final StringBuilder startLineAdvantageText = new StringBuilder();
    final StringBuilder finishLineAdvantageText = new StringBuilder();

    /**
     * Tells whether currently an auto-zoom is in progress; this is used particularly to keep the smooth CSS boat transitions
     * active while auto-zooming whereas stopping them seems the better option for manual zooms.
     */
    private boolean autoZoomInProgress;

    private void showStartAndFinishLines(final CoursePositionsDTO courseDTO) {
        if (map != null && courseDTO != null && lastRaceTimesInfo != null) {
            com.sap.sse.common.Util.Pair<Integer, CompetitorDTO> leadingVisibleCompetitorInfo = getLeadingVisibleCompetitorWithOneBasedLegNumber(getCompetitorsToShow());
            int legOfLeadingCompetitor = leadingVisibleCompetitorInfo == null ? -1 : leadingVisibleCompetitorInfo.getA();
            int numberOfLegs = lastRaceTimesInfo.legInfos.size();
            // draw the start line
            updateCountdownCanvas(courseDTO.startMarkPositions);
            if (legOfLeadingCompetitor <= 1 && 
                    settings.getHelpLinesSettings().isVisible(HelpLineTypes.STARTLINE) && courseDTO.startMarkPositions != null && courseDTO.startMarkPositions.size() == 2) {
                LatLng startLinePoint1 = LatLng.newInstance(courseDTO.startMarkPositions.get(0).latDeg, courseDTO.startMarkPositions.get(0).lngDeg); 
                LatLng startLinePoint2 = LatLng.newInstance(courseDTO.startMarkPositions.get(1).latDeg, courseDTO.startMarkPositions.get(1).lngDeg); 
                if (courseDTO.startLineAngleToCombinedWind != null) {
                    startLineAdvantageText.replace(0, startLineAdvantageText.length(), " "+stringMessages.lineAngleToWindAndAdvantage(
                            NumberFormat.getFormat("0.0").format(courseDTO.startLineLengthInMeters),
                            NumberFormat.getFormat("0.0").format(Math.abs(courseDTO.startLineAngleToCombinedWind)),
                            courseDTO.startLineAdvantageousSide.name().charAt(0)+courseDTO.startLineAdvantageousSide.name().substring(1).toLowerCase(),
                            NumberFormat.getFormat("0.0").format(courseDTO.startLineAdvantageInMeters)));
                } else {
                    startLineAdvantageText.delete(0, startLineAdvantageText.length());
                }
                if (startLine == null) {
                    PolylineOptions options = PolylineOptions.newInstance();
                    options.setClickable(true);
                    options.setGeodesic(true);
                    options.setStrokeColor("#FFFFFF");
                    options.setStrokeWeight(1);
                    options.setStrokeOpacity(1.0);
                    
                    MVCArray<LatLng> pointsAsArray = MVCArray.newInstance();
                    pointsAsArray.insertAt(0, startLinePoint1);
                    pointsAsArray.insertAt(1, startLinePoint2);

                    startLine = Polyline.newInstance(options);
                    startLine.setPath(pointsAsArray);

                    startLine.addMouseOverHandler(new MouseOverMapHandler() {
                        @Override
                        public void onEvent(MouseOverMapEvent event) {
                            map.setTitle(stringMessages.startLine()+startLineAdvantageText);
                        }
                    });
                    startLine.addMouseOutMoveHandler(new MouseOutMapHandler() {
                        @Override
                        public void onEvent(MouseOutMapEvent event) {
                            map.setTitle("");
                        }
                    });
                    startLine.setMap(map);
                } else {
                    startLine.getPath().removeAt(1);
                    startLine.getPath().removeAt(0);
                    startLine.getPath().insertAt(0, startLinePoint1);
                    startLine.getPath().insertAt(1, startLinePoint2);
                }
            } else {
                if (startLine != null) {
                    startLine.setMap(null);
                    startLine = null;
                }
            }
            // draw the finish line
            if (legOfLeadingCompetitor > 0 && legOfLeadingCompetitor == numberOfLegs &&
                settings.getHelpLinesSettings().isVisible(HelpLineTypes.FINISHLINE) && courseDTO.finishMarkPositions != null && courseDTO.finishMarkPositions.size() == 2) {
                LatLng finishLinePoint1 = LatLng.newInstance(courseDTO.finishMarkPositions.get(0).latDeg, courseDTO.finishMarkPositions.get(0).lngDeg); 
                LatLng finishLinePoint2 = LatLng.newInstance(courseDTO.finishMarkPositions.get(1).latDeg, courseDTO.finishMarkPositions.get(1).lngDeg); 
                if (courseDTO.startLineAngleToCombinedWind != null) {
                    finishLineAdvantageText.replace(0, finishLineAdvantageText.length(), " "+stringMessages.lineAngleToWindAndAdvantage(
                            NumberFormat.getFormat("0.0").format(courseDTO.finishLineLengthInMeters),
                            NumberFormat.getFormat("0.0").format(Math.abs(courseDTO.finishLineAngleToCombinedWind)),
                            courseDTO.finishLineAdvantageousSide.name().charAt(0)+courseDTO.finishLineAdvantageousSide.name().substring(1).toLowerCase(),
                            NumberFormat.getFormat("0.0").format(courseDTO.finishLineAdvantageInMeters)));
                } else {
                    finishLineAdvantageText.delete(0, finishLineAdvantageText.length());
                }
                if (finishLine == null) {
                    PolylineOptions options = PolylineOptions.newInstance();
                    options.setClickable(true);
                    options.setGeodesic(true);
                    options.setStrokeColor("#000000");
                    options.setStrokeWeight(1);
                    options.setStrokeOpacity(1.0);
                   
                    MVCArray<LatLng> pointsAsArray = MVCArray.newInstance();
                    pointsAsArray.insertAt(0, finishLinePoint1);
                    pointsAsArray.insertAt(1, finishLinePoint2);

                    finishLine = Polyline.newInstance(options);
                    finishLine.setPath(pointsAsArray);

                    finishLine.addMouseOverHandler(new MouseOverMapHandler() {
                        @Override
                        public void onEvent(MouseOverMapEvent event) {
                            map.setTitle(stringMessages.finishLine()+finishLineAdvantageText);
                        }
                    });
                    finishLine.addMouseOutMoveHandler(new MouseOutMapHandler() {
                        @Override
                        public void onEvent(MouseOutMapEvent event) {
                            map.setTitle("");
                        }
                    });
                    finishLine.setMap(map);
                } else {
                    finishLine.getPath().removeAt(1);
                    finishLine.getPath().removeAt(0);
                    finishLine.getPath().insertAt(0, finishLinePoint1);
                    finishLine.getPath().insertAt(1, finishLinePoint2);
                }
            }
            else {
                if (finishLine != null) {
                    finishLine.setMap(null);
                    finishLine = null;
                }
            }
            // draw the course middle line
            if (legOfLeadingCompetitor > 0 && courseDTO.waypointPositions.size() > legOfLeadingCompetitor &&
                    settings.getHelpLinesSettings().isVisible(HelpLineTypes.COURSEMIDDLELINE)) {
                PositionDTO position1DTO = courseDTO.waypointPositions.get(legOfLeadingCompetitor-1);
                PositionDTO position2DTO = courseDTO.waypointPositions.get(legOfLeadingCompetitor);
                LatLng courseMiddleLinePoint1 = LatLng.newInstance(position1DTO.latDeg, position1DTO.lngDeg);
                LatLng courseMiddleLinePoint2 = LatLng.newInstance(position2DTO.latDeg, position2DTO.lngDeg); 
                if (courseMiddleLine == null) {
                    PolylineOptions options = PolylineOptions.newInstance();
                    options.setClickable(true);
                    options.setGeodesic(true);
                    options.setStrokeColor("#2268a0");
                    options.setStrokeWeight(1);
                    options.setStrokeOpacity(1.0);
                    
                    MVCArray<LatLng> pointsAsArray = MVCArray.newInstance();
                    pointsAsArray.insertAt(0, courseMiddleLinePoint1);
                    pointsAsArray.insertAt(1, courseMiddleLinePoint2);

                    courseMiddleLine = Polyline.newInstance(options);
                    courseMiddleLine.setPath(pointsAsArray);

                    courseMiddleLine.addMouseOverHandler(new MouseOverMapHandler() {
                        @Override
                        public void onEvent(MouseOverMapEvent event) {
                            map.setTitle(stringMessages.courseMiddleLine());
                        }
                    });
                    courseMiddleLine.addMouseOutMoveHandler(new MouseOutMapHandler() {
                        @Override
                        public void onEvent(MouseOutMapEvent event) {
                            map.setTitle("");
                        }
                    });
                    courseMiddleLine.setMap(map);
                } else {
                    courseMiddleLine.getPath().removeAt(1);
                    courseMiddleLine.getPath().removeAt(0);
                    courseMiddleLine.getPath().insertAt(0, courseMiddleLinePoint1);
                    courseMiddleLine.getPath().insertAt(1, courseMiddleLinePoint2);
                }
            }
            else {
                if (courseMiddleLine != null) {
                    courseMiddleLine.setMap(null);
                    courseMiddleLine = null;
                }
            }
        }
    }
    
    /**
     * If, according to {@link #lastRaceTimesInfo} and {@link #timer} the race is
     * still in the pre-start phase, show a {@link SmallTransparentInfoOverlay} at the
     * start line that shows the count down.
     */
    private void updateCountdownCanvas(List<PositionDTO> startMarkPositions) {
        if (!settings.isShowSelectedCompetitorsInfo() || startMarkPositions == null || startMarkPositions.isEmpty()
                || lastRaceTimesInfo.startOfRace == null || timer.getTime().after(lastRaceTimesInfo.startOfRace)) {
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
                        countDownText);
                countDownOverlay.addToMap();
            } else {
                countDownOverlay.setInfoText(countDownText);
            }
            countDownOverlay.setPosition(startMarkPositions.get(startMarkPositions.size() - 1), -1);
            countDownOverlay.draw();
        }
    }

    private int getZoomLevel(LatLngBounds bounds) {
        int GLOBE_PXSIZE = 256; // a constant in Google's map projection
        int MAX_ZOOM = 20; // maximum zoom-level that should be automatically selected
        double LOG2 = Math.log(2.0);
        double deltaLng = bounds.getNorthEast().getLongitude() - bounds.getSouthWest().getLongitude();
        double deltaLat = bounds.getNorthEast().getLatitude() - bounds.getSouthWest().getLatitude();
        if ((deltaLng == 0) && (deltaLat == 0)) {
            return MAX_ZOOM;
        }
        if (deltaLng < 0) {
            deltaLng += 360;
        }
        int zoomLng = (int) Math.floor(Math.log(map.getDiv().getClientWidth() * 360 / deltaLng / GLOBE_PXSIZE) / LOG2);
        if (deltaLat < 0) {
            deltaLat += 180;
        }
        int zoomLat = (int) Math.floor(Math.log(map.getDiv().getClientHeight() * 180 / deltaLat / GLOBE_PXSIZE) / LOG2);
        return Math.min(Math.min(zoomLat, zoomLng), MAX_ZOOM);
    }
    
    private void zoomMapToNewBounds(Bounds newBounds) {
        if (newBounds != null) {
            Bounds currentMapBounds;
            if (map.getBounds() == null
                    || !(currentMapBounds = BoundsUtil.getAsBounds(map.getBounds())).contains(newBounds)
                    || graticuleAreaRatio(currentMapBounds, newBounds) > 10) {
                // only change bounds if the new bounds don't fit into the current map zoom
                List<ZoomTypes> oldZoomSettings = settings.getZoomSettings().getTypesToConsiderOnZoom();
                setAutoZoomInProgress(true);
                autoZoomLatLngBounds = BoundsUtil.getAsLatLngBounds(newBounds);
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
                settings.getZoomSettings().setTypesToConsiderOnZoom(oldZoomSettings);
                setAutoZoomInProgress(false);
            }
        }
    }
    
    private double graticuleAreaRatio(Bounds containing, Bounds contained) {
        assert containing.contains(contained);
        double containingAreaRatio = getGraticuleArea(containing) / getGraticuleArea(contained);
        return containingAreaRatio;
    }

    /**
     * A much simplified "area" calculation for a {@link Bounds} object, multiplying the differences in latitude and longitude degrees.
     * The result therefore is in the order of magnitude of 60*60 square nautical miles.
     */
    private double getGraticuleArea(Bounds bounds) {
        return ((bounds.isCrossesDateLine() ? bounds.getNorthEast().getLngDeg()+360 : bounds.getNorthEast().getLngDeg())-bounds.getSouthWest().getLngDeg()) *
                (bounds.getNorthEast().getLatDeg() - bounds.getSouthWest().getLatDeg());
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
        boolean usedExistingCanvas = false;
        GPSFixDTO lastBoatFix = getBoatFix(competitorDTO, date);
        if (lastBoatFix != null) {
            BoatOverlay boatOverlay = boatOverlays.get(competitorDTO);
            if (boatOverlay == null) {
                boatOverlay = createBoatOverlay(RaceMapOverlaysZIndexes.BOATS_ZINDEX, competitorDTO, displayHighlighted(competitorDTO));
                boatOverlays.put(competitorDTO, boatOverlay);
                boatOverlay.setSelected(displayHighlighted(competitorDTO));
                boatOverlay.setBoatFix(lastBoatFix, timeForPositionTransitionMillis);
                boatOverlay.addToMap();
            } else {
                usedExistingCanvas = true;
                boatOverlay.setSelected(displayHighlighted(competitorDTO));
                boatOverlay.setBoatFix(lastBoatFix, timeForPositionTransitionMillis);
                boatOverlay.draw();
            }
        }

        return usedExistingCanvas;
    }

    private boolean displayHighlighted(CompetitorDTO competitorDTO) {
        return !settings.isShowOnlySelectedCompetitors() && competitorSelection.isSelected(competitorDTO);
    }

    protected CourseMarkOverlay createCourseMarkOverlay(int zIndex, final MarkDTO markDTO) {
        final CourseMarkOverlay courseMarkOverlay = new CourseMarkOverlay(map, zIndex, markDTO);
        courseMarkOverlay.addClickHandler(new ClickMapHandler() {
            @Override
            public void onEvent(ClickMapEvent event) {
                LatLng latlng = courseMarkOverlay.getMarkPosition();
                showMarkInfoWindow(markDTO, latlng);
            }
        });
        return courseMarkOverlay;
    }

    private CompetitorInfoOverlay createCompetitorInfoOverlay(int zIndex, final CompetitorDTO competitorDTO) {
        String infoText = competitorDTO.getSailID() == null || competitorDTO.getSailID().isEmpty() ? competitorDTO.getName() : competitorDTO.getSailID();
        return new CompetitorInfoOverlay(map, zIndex, competitorSelection.getColor(competitorDTO), infoText);
    }
    
    private BoatOverlay createBoatOverlay(int zIndex, final CompetitorDTO competitorDTO, boolean highlighted) {
        final BoatOverlay boatCanvas = new BoatOverlay(map, zIndex, competitorDTO, competitorSelection.getColor(competitorDTO));
        boatCanvas.setSelected(highlighted);
        boatCanvas.addClickHandler(new ClickMapHandler() {
            @Override
            public void onEvent(ClickMapEvent event) {
                if (lastInfoWindow != null) {
                    lastInfoWindow.close();
                }
                GPSFixDTO latestFixForCompetitor = getBoatFix(competitorDTO, timer.getTime());
                LatLng where = LatLng.newInstance(latestFixForCompetitor.position.latDeg, latestFixForCompetitor.position.lngDeg);
                InfoWindowOptions options = InfoWindowOptions.newInstance();
                InfoWindow infoWindow = InfoWindow.newInstance(options);
                infoWindow.setContent(getInfoWindowContent(competitorDTO, latestFixForCompetitor));
                infoWindow.setPosition(where);
                lastInfoWindow = infoWindow;
                infoWindow.open(map);
            }
        });

        boatCanvas.addMouseOverHandler(new MouseOverMapHandler() {
            @Override
            public void onEvent(MouseOverMapEvent event) {
                map.setTitle(competitorDTO.getSailID());
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
        final WindSensorOverlay windSensorOverlay = new WindSensorOverlay(map, zIndex, raceMapImageManager, stringMessages);
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
        if(lastInfoWindow != null) {
            lastInfoWindow.close();
        }
        InfoWindowOptions options = InfoWindowOptions.newInstance();
        InfoWindow infoWindow = InfoWindow.newInstance(options);
        infoWindow.setContent(getInfoWindowContent(markDTO));
        infoWindow.setPosition(position);
        lastInfoWindow = infoWindow;
        infoWindow.open(map);
    }

    private void showCompetitorInfoWindow(final CompetitorDTO competitorDTO, LatLng where) {
        if(lastInfoWindow != null) {
            lastInfoWindow.close();
        }
        GPSFixDTO latestFixForCompetitor = getBoatFix(competitorDTO, timer.getTime()); 
        // TODO find close fix where the mouse was; see BUG 470
        InfoWindowOptions options = InfoWindowOptions.newInstance();
        InfoWindow infoWindow = InfoWindow.newInstance(options);
        infoWindow.setContent(getInfoWindowContent(competitorDTO, latestFixForCompetitor));
        infoWindow.setPosition(where);
        lastInfoWindow = infoWindow;
        infoWindow.open(map);
    }

    private String formatPosition(double lat, double lng) {
        NumberFormat numberFormat = NumberFormat.getFormat("0.00000");
        String result = numberFormat.format(lat) + " lat, " + numberFormat.format(lng) + " lng";
        return result;
    }
    
    private void showWindSensorInfoWindow(final WindSensorOverlay windSensorOverlay) {
    	WindSource windSource = windSensorOverlay.getWindSource();
    	WindTrackInfoDTO windTrackInfoDTO = windSensorOverlay.getWindTrackInfoDTO();
        WindDTO windDTO = windTrackInfoDTO.windFixes.get(0);
        if(windDTO != null && windDTO.position != null) {
            if(lastInfoWindow != null) {
                lastInfoWindow.close();
            }
            LatLng where = LatLng.newInstance(windDTO.position.latDeg, windDTO.position.lngDeg);
            InfoWindowOptions options = InfoWindowOptions.newInstance();
            InfoWindow infoWindow = InfoWindow.newInstance(options);
            infoWindow.setContent(getInfoWindowContent(windSource, windTrackInfoDTO));
            infoWindow.setPosition(where);
            lastInfoWindow = infoWindow;
            infoWindow.open(map);
        }
    }

    private Widget createInfoWindowLabelAndValue(String labelName, String value) {
    	FlowPanel flowPanel = new FlowPanel();
        Label label = new Label(labelName + ":");
        label.setWordWrap(false);
        label.getElement().getStyle().setFloat(Style.Float.LEFT);
        label.getElement().getStyle().setPadding(3, Style.Unit.PX);
        label.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
        flowPanel.add(label);

        Label valueLabel = new Label(value);
        valueLabel.setWordWrap(false);
        valueLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        valueLabel.getElement().getStyle().setPadding(3, Style.Unit.PX);
        flowPanel.add(valueLabel);

        return flowPanel;
    }
    
    private Widget getInfoWindowContent(MarkDTO markDTO) {
        VerticalPanel vPanel = new VerticalPanel();
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.mark(), markDTO.getName()));
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.position(), formatPosition(markDTO.position.latDeg, markDTO.position.lngDeg)));
        return vPanel;
    }

    private Widget getInfoWindowContent(WindSource windSource, WindTrackInfoDTO windTrackInfoDTO) {
        WindDTO windDTO = windTrackInfoDTO.windFixes.get(0);
        NumberFormat numberFormat = NumberFormat.getFormat("0.0");
        VerticalPanel vPanel = new VerticalPanel();
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.windSource(), WindSourceTypeFormatter.format(windSource, stringMessages)));
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.wind(), Math.round(windDTO.dampenedTrueWindFromDeg) + " " + stringMessages.degreesShort()));
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.windSpeed(), numberFormat.format(windDTO.dampenedTrueWindSpeedInKnots)));
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.position(), formatPosition(windDTO.position.latDeg, windDTO.position.lngDeg)));
        return vPanel;
    }

    private Widget getInfoWindowContent(CompetitorDTO competitorDTO, GPSFixDTO lastFix) {
        final VerticalPanel vPanel = new VerticalPanel();
        vPanel.setWidth("350px");
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.competitor(), competitorDTO.getName()));
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.sailNumber(), competitorDTO.getSailID()));
        Integer rank = null;
        if (quickRanks != null) {
            QuickRankDTO quickRank = quickRanks.get(competitorDTO);
            if (quickRank != null) {
                rank = quickRank.rank;
            }
        }
        if (rank != null) {
            vPanel.add(createInfoWindowLabelAndValue(stringMessages.rank(), String.valueOf(rank)));
        }
        SpeedWithBearingDTO speedWithBearing = lastFix.speedWithBearing;
        if (speedWithBearing == null) {
            // TODO should we show the boat at all?
            speedWithBearing = new SpeedWithBearingDTO(0, 0);
        }
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.speed(),
                NumberFormatterFactory.getDecimalFormat(1).format(speedWithBearing.speedInKnots) + " "+stringMessages.knotsUnit()));
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.bearing(), (int) speedWithBearing.bearingInDegrees + " "+stringMessages.degreesShort()));
        if (lastFix.degreesBoatToTheWind != null) {
            vPanel.add(createInfoWindowLabelAndValue(stringMessages.degreesBoatToTheWind(),
                    (int) Math.abs(lastFix.degreesBoatToTheWind) + " " + stringMessages.degreesShort()));
        }
        if (!selectedRaces.isEmpty()) {
            RegattaAndRaceIdentifier race = selectedRaces.get(selectedRaces.size() - 1);
            if (race != null) {
                Map<CompetitorDTO, Date> from = new HashMap<CompetitorDTO, Date>();
                from.put(competitorDTO, fixesAndTails.getFixes(competitorDTO).get(fixesAndTails.getFirstShownFix(competitorDTO)).timepoint);
                Map<CompetitorDTO, Date> to = new HashMap<CompetitorDTO, Date>();
                to.put(competitorDTO, getBoatFix(competitorDTO, timer.getTime()).timepoint);
                sailingService.getDouglasPoints(race, from, to, 3,
                        new AsyncCallback<Map<CompetitorDTO, List<GPSFixDTO>>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error obtaining douglas positions: " + caught.getMessage(), true /*silentMode */);
                            }

                            @Override
                            public void onSuccess(Map<CompetitorDTO, List<GPSFixDTO>> result) {
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
                sailingService.getManeuvers(race, from, to,
                        new AsyncCallback<Map<CompetitorDTO, List<ManeuverDTO>>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error obtaining maneuvers: " + caught.getMessage(), true /*silentMode */);
                            }

                            @Override
                            public void onSuccess(Map<CompetitorDTO, List<ManeuverDTO>> result) {
                                lastManeuverResult = result;
                                if (maneuverMarkers != null) {
                                    removeAllManeuverMarkers();
                                }
                                if (!(timer.getPlayState() == PlayStates.Playing)) {
                                    showManeuvers(result);
                                }
                            }
                        });

            }
        }
        return vPanel;
    }

    private Iterable<CompetitorDTO> getCompetitorsToShow() {
        Iterable<CompetitorDTO> result;
        Iterable<CompetitorDTO> selection = competitorSelection.getSelectedCompetitors();
        if (!settings.isShowOnlySelectedCompetitors() || Util.isEmpty(selection)) {
            result = competitorSelection.getFilteredCompetitors();
        } else {
            result = selection;
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

    protected void removeAllManeuverMarkers() {
        if (maneuverMarkers != null) {
            for (Marker marker : maneuverMarkers) {
                marker.setMap((MapWidget) null);
            }
            maneuverMarkers = null;
        }
    }

    protected void showMarkDouglasPeuckerPoints(Map<CompetitorDTO, List<GPSFixDTO>> gpsFixPointMapForCompetitors) {
        douglasMarkers = new HashSet<Marker>();
        if (map != null && gpsFixPointMapForCompetitors != null) {
            Set<CompetitorDTO> keySet = gpsFixPointMapForCompetitors.keySet();
            Iterator<CompetitorDTO> iter = keySet.iterator();
            while (iter.hasNext()) {
                CompetitorDTO competitorDTO = iter.next();
                List<GPSFixDTO> gpsFix = gpsFixPointMapForCompetitors.get(competitorDTO);
                for (GPSFixDTO fix : gpsFix) {
                    LatLng latLng = LatLng.newInstance(fix.position.latDeg, fix.position.lngDeg);
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

    protected void showManeuvers(Map<CompetitorDTO, List<ManeuverDTO>> maneuvers) {
        maneuverMarkers = new HashSet<Marker>();
        if (map != null && maneuvers != null) {
            Set<CompetitorDTO> keySet = maneuvers.keySet();
            Iterator<CompetitorDTO> iter = keySet.iterator();
            while (iter.hasNext()) {
                CompetitorDTO competitorDTO = iter.next();
                List<ManeuverDTO> maneuversForCompetitor = maneuvers.get(competitorDTO);
                for (ManeuverDTO maneuver : maneuversForCompetitor) {
                    if (settings.isShowManeuverType(maneuver.type)) {
                        LatLng latLng = LatLng.newInstance(maneuver.position.latDeg, maneuver.position.lngDeg);
                        Marker maneuverMarker = raceMapImageManager.maneuverIconsForTypeAndTargetTack.get(new com.sap.sse.common.Util.Pair<ManeuverType, Tack>(maneuver.type, maneuver.newTack));
                        MarkerOptions options = MarkerOptions.newInstance();
                        options.setTitle(maneuver.toString(stringMessages));
                        options.setIcon(maneuverMarker.getIcon_MarkerImage());
                        Marker marker = Marker.newInstance(options);
                        marker.setPosition(latLng);
                        maneuverMarkers.add(marker);
                        marker.setMap(map);
                    }
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
    private GPSFixDTO getBoatFix(CompetitorDTO competitorDTO, Date date) {
        GPSFixDTO result = null;
        List<GPSFixDTO> competitorFixes = fixesAndTails.getFixes(competitorDTO);
        if (competitorFixes != null && !competitorFixes.isEmpty()) {
            int i = Collections.binarySearch(competitorFixes, new GPSFixDTO(date, null, null, (WindDTO) null, null, null, false),
                    new Comparator<GPSFixDTO>() {
                @Override
                public int compare(GPSFixDTO o1, GPSFixDTO o2) {
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
                    final GPSFixDTO fixBefore = competitorFixes.get(i-1);
                    final GPSFixDTO fixAfter = competitorFixes.get(i);
                    final GPSFixDTO closer;
                    if (date.getTime() - fixBefore.timepoint.getTime() < fixAfter.timepoint.getTime() - date.getTime()) {
                        closer = fixBefore;
                    } else {
                        closer = fixAfter;
                    }
                    // now compute a weighted average depending on the time difference to "date" (see also bug 1924)
                    double factorForAfter = (double) (date.getTime()-fixBefore.timepoint.getTime()) / (double) (fixAfter.timepoint.getTime() - fixBefore.timepoint.getTime());
                    double factorForBefore = 1-factorForAfter;
                    PositionDTO betweenPosition = new PositionDTO(factorForBefore*fixBefore.position.latDeg + factorForAfter*fixAfter.position.latDeg,
                            factorForBefore*fixBefore.position.lngDeg + factorForAfter*fixAfter.position.lngDeg);
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
                    result = new GPSFixDTO(date, betweenPosition, betweenSpeed, closer.degreesBoatToTheWind,
                            closer.tack, closer.legType, fixBefore.extrapolated || fixAfter.extrapolated);
                }
            } else {
                // perfect match
                final GPSFixDTO fixAfter = competitorFixes.get(i);
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
                        BoatOverlay boatOverlay = next.getValue();
                        boatOverlay.removeFromMap();
                        fixesAndTails.removeTail(next.getKey());
                        i.remove(); // only this way a ConcurrentModificationException while looping can be avoided
                    }
                }
                showCompetitorInfoOnMap(timer.getTime(), -1, competitorSelection.getSelectedFilteredCompetitors());
            } else {
                // adding a single competitor; may need to re-load data, so refresh:
                timeChanged(timer.getTime(), null);
            }
        } else {
            // only change highlighting
            BoatOverlay boatCanvas = boatOverlays.get(competitor);
            if (boatCanvas != null) {
                boatCanvas.setSelected(displayHighlighted(competitor));
                boatCanvas.draw();
                showCompetitorInfoOnMap(timer.getTime(), -1, competitorSelection.getSelectedFilteredCompetitors());
            } else {
                // seems like an internal error not to find the lowlighted marker; but maybe the
                // competitor was added late to the race;
                // data for newly selected competitor supposedly missing; refresh
                timeChanged(timer.getTime(), null);
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
            timeChanged(timer.getTime(), null);
        } else {
            // try a more incremental update otherwise
            if (settings.isShowOnlySelectedCompetitors()) {
                // if selection is now empty, show all competitors
                if (Util.isEmpty(competitorSelection.getSelectedCompetitors())) {
                    timeChanged(timer.getTime(), null);
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
                    boatCanvas.setSelected(displayHighlighted(competitor));
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
        return stringMessages.map();
    }

    @Override
    public Widget getEntryWidget() {
        return this;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<RaceMapSettings> getSettingsDialogComponent() {
        return new RaceMapSettingsDialogComponent(settings, stringMessages, this.showViewSimulation && this.hasPolar);
    }

    @Override
    public void updateSettings(RaceMapSettings newSettings) {
        boolean maneuverTypeSelectionChanged = false;
        boolean requiredRedraw = false;
        for (ManeuverType maneuverType : ManeuverType.values()) {
            if (newSettings.isShowManeuverType(maneuverType) != settings.isShowManeuverType(maneuverType)) {
                maneuverTypeSelectionChanged = true;
                settings.showManeuverType(maneuverType, newSettings.isShowManeuverType(maneuverType));
            }
        }
        if (maneuverTypeSelectionChanged) {
            if (!(timer.getPlayState() == PlayStates.Playing) && lastManeuverResult != null) {
                removeAllManeuverMarkers();
                showManeuvers(lastManeuverResult);
            }
        }
        if (newSettings.isShowDouglasPeuckerPoints() != settings.isShowDouglasPeuckerPoints()) {
            if (!(timer.getPlayState() == PlayStates.Playing) && lastDouglasPeuckerResult != null && newSettings.isShowDouglasPeuckerPoints()) {
                settings.setShowDouglasPeuckerPoints(true);
                removeAllMarkDouglasPeuckerpoints();
                showMarkDouglasPeuckerPoints(lastDouglasPeuckerResult);
            } else if (!newSettings.isShowDouglasPeuckerPoints()) {
                settings.setShowDouglasPeuckerPoints(false);
                removeAllMarkDouglasPeuckerpoints();
            }
        }
        if (newSettings.getTailLengthInMilliseconds() != settings.getTailLengthInMilliseconds()) {
            settings.setTailLengthInMilliseconds(newSettings.getTailLengthInMilliseconds());
            requiredRedraw = true;
        }
        if (newSettings.getBuoyZoneRadiusInMeters() != settings.getBuoyZoneRadiusInMeters()) {
            settings.setBuoyZoneRadiusInMeters(newSettings.getBuoyZoneRadiusInMeters());
            requiredRedraw = true;
        }
        if (newSettings.isShowOnlySelectedCompetitors() != settings.isShowOnlySelectedCompetitors()) {
            settings.setShowOnlySelectedCompetitors(newSettings.isShowOnlySelectedCompetitors());
            requiredRedraw = true;
        }
        if (newSettings.isShowSelectedCompetitorsInfo() != settings.isShowSelectedCompetitorsInfo()) {
            settings.setShowSelectedCompetitorsInfo(newSettings.isShowSelectedCompetitorsInfo());
            requiredRedraw = true;
        }
        if (!newSettings.getZoomSettings().equals(settings.getZoomSettings())) {
            settings.setZoomSettings(newSettings.getZoomSettings());                    
            if (!settings.getZoomSettings().containsZoomType(ZoomTypes.NONE)) {
                removeTransitions();
                zoomMapToNewBounds(settings.getZoomSettings().getNewBounds(this));
            }
        }
        if (!newSettings.getHelpLinesSettings().equals(settings.getHelpLinesSettings())) {
            settings.setHelpLinesSettings(newSettings.getHelpLinesSettings());
            requiredRedraw = true;
        }
        if (newSettings.isShowWindStreamletOverlay() != settings.isShowWindStreamletOverlay()) {
            settings.setShowWindStreamletOverlay(newSettings.isShowWindStreamletOverlay());
            streamletOverlay.setVisible(newSettings.isShowWindStreamletOverlay());
        }
        if (newSettings.isShowSimulationOverlay() != settings.isShowSimulationOverlay()) {
            settings.setShowSimulationOverlay(newSettings.isShowSimulationOverlay());
            simulationOverlay.setVisible(newSettings.isShowSimulationOverlay());
            if (newSettings.isShowSimulationOverlay()) {
                simulationOverlay.updateLeg(getCurrentLeg(), true, -1 /* ensure ui-update */);
            }
        }
        if (requiredRedraw) {
            redraw();
        }
    }
    
    public static class BoatsBoundsCalculator extends LatLngBoundsCalculatorForSelected {

        @Override
        public Bounds calculateNewBounds(RaceMap forMap) {
            Bounds newBounds = null;
            Iterable<CompetitorDTO> selectedCompetitors = forMap.competitorSelection.getSelectedCompetitors();
            Iterable<CompetitorDTO> competitors = new ArrayList<CompetitorDTO>();
            if (selectedCompetitors == null || !selectedCompetitors.iterator().hasNext()) {
                competitors = forMap.getCompetitorsToShow();
            } else {
                competitors = isZoomOnlyToSelectedCompetitors(forMap) ? selectedCompetitors : forMap.getCompetitorsToShow();
            }
            for (CompetitorDTO competitor : competitors) {
                try {
                    GPSFixDTO competitorFix = forMap.getBoatFix(competitor, forMap.timer.getTime());
                    PositionDTO competitorPosition = competitorFix != null ? competitorFix.position : null;
                    if (competitorPosition != null) {
                        if (newBounds == null) {
                            newBounds = BoundsUtil.getAsBounds(competitorPosition);
                        } else {
                            newBounds = newBounds.extend(BoundsUtil.getAsPosition(competitorPosition));
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
        public Bounds calculateNewBounds(RaceMap racemap) {
            Bounds newBounds = null;
            Iterable<CompetitorDTO> competitors = isZoomOnlyToSelectedCompetitors(racemap) ? racemap.competitorSelection.getSelectedCompetitors() : racemap.getCompetitorsToShow();
            for (CompetitorDTO competitor : competitors) {
                Polyline tail = racemap.fixesAndTails.getTail(competitor);
                Bounds bounds = null;
                // TODO: Find a replacement for missing Polyline function getBounds() from v2
                // see also http://stackoverflow.com/questions/3284808/getting-the-bounds-of-a-polyine-in-google-maps-api-v3; 
                // optionally, consider providing a bounds cache with two sorted sets that organize the LatLng objects for O(1) bounds calculation and logarithmic add, ideally O(1) remove
                if (tail != null && tail.getPath().getLength() >= 1) {
                    bounds = BoundsUtil.getAsBounds(BoundsUtil.getAsPosition(tail.getPath().get(0)));
                    for (int i = 1; i < tail.getPath().getLength(); i++) {
                        bounds = bounds.extend(BoundsUtil.getAsPosition(tail.getPath().get(i)));
                    }
                }
                if (bounds != null) {
                    if (newBounds == null) {
                        newBounds = bounds;
                    } else {
                        newBounds = newBounds.extend(bounds);
                    }
                }
            }
            return newBounds;
        }
    }
    
    public static class CourseMarksBoundsCalculator implements LatLngBoundsCalculator {
        @Override
        public Bounds calculateNewBounds(RaceMap forMap) {
            Bounds newBounds = null;
            Iterable<MarkDTO> marksToZoom = forMap.markDTOs.values();
            if (marksToZoom != null) {
                for (MarkDTO markDTO : marksToZoom) {
                    Bounds bounds = BoundsUtil.getAsBounds(markDTO.position);
                    if (newBounds == null) {
                        newBounds = bounds;
                    } else {
                        newBounds = newBounds.extend(bounds);
                    }
                }
            }
            return newBounds;
        }
    }

    public static class WindSensorsBoundsCalculator implements LatLngBoundsCalculator {
        @Override
        public Bounds calculateNewBounds(RaceMap forMap) {
            Bounds newBounds = null;
            Collection<WindSensorOverlay> marksToZoom = forMap.windSensorOverlays.values();
            if (marksToZoom != null) {
                for (WindSensorOverlay windSensorOverlay : marksToZoom) {
                    Position windSensorPosition = BoundsUtil.getAsPosition(windSensorOverlay.getLatLngPosition());
                    if (windSensorPosition != null) {
                        Bounds bounds = new BoundsImpl(windSensorPosition, windSensorPosition);
                        if (newBounds == null) {
                            newBounds = bounds;
                        } else {
                            newBounds = newBounds.extend(windSensorPosition);
                        }
                    }
                }
            }
            return newBounds;
        }
    }

    @Override
    public void initializeData(boolean showMapControls) {
        loadMapsAPIV3(showMapControls);
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
    }

    @Override
    public void competitorsListChanged(Iterable<CompetitorDTO> competitors) {
        timeChanged(timer.getTime(), null);
    }
    
    @Override
    public void filteredCompetitorsListChanged(Iterable<CompetitorDTO> filteredCompetitors) {
        timeChanged(timer.getTime(), null);
    }
    
    @Override
    public void filterChanged(FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> oldFilterSet,
            FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> newFilterSet) {
        // nothing to do; if the list of filtered competitors has changed, a separate call to filteredCompetitorsListChanged will occur
    }

    @Override
    public PolylineOptions createTailStyle(CompetitorDTO competitor, boolean isHighlighted) {
        PolylineOptions options = PolylineOptions.newInstance();
        options.setClickable(true);
        options.setGeodesic(true);
        options.setStrokeOpacity(1.0);
        boolean noCompetitorSelected = Util.isEmpty(competitorSelection.getSelectedCompetitors());
        if (isHighlighted || noCompetitorSelected) {
            options.setStrokeColor(competitorSelection.getColor(competitor).getAsHtml());
        } else {
            options.setStrokeColor(CssColor.make(200, 200,  200).toString());
        }
        if (isHighlighted) {
            options.setStrokeWeight(2);
        } else {
            options.setStrokeWeight(1);
        }
        options.setZindex(RaceMapOverlaysZIndexes.BOATTAILS_ZINDEX);
        return options;
    }
    
    @Override
    public Polyline createTail(final CompetitorDTO competitor, List<LatLng> points) {
        PolylineOptions options = createTailStyle(competitor, displayHighlighted(competitor));
        Polyline result = Polyline.newInstance(options);

        MVCArray<LatLng> pointsAsArray = MVCArray.newInstance(points.toArray(new LatLng[0]));
        result.setPath(pointsAsArray);
        
        result.addClickHandler(new ClickMapHandler() {
            @Override
            public void onEvent(ClickMapEvent event) {
                showCompetitorInfoWindow(competitor, event.getMouseEvent().getLatLng());
            }
        });
        result.addMouseOverHandler(new MouseOverMapHandler() {
            @Override
            public void onEvent(MouseOverMapEvent event) {
                map.setTitle(competitor.getSailID() + ", " + competitor.getName());
            }
        });
        result.addMouseOutMoveHandler(new MouseOutMapHandler() {
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
        QuickRankDTO quickRank = quickRanks.get(competitor);
        if (quickRank != null) {
            result = quickRank.rank;
        } else {
            result = null;
        }
        return result;
    }
    
    public int getCurrentLeg() {
        com.sap.sse.common.Util.Pair<Integer, CompetitorDTO> leaderWithLeg = this
                .getLeadingVisibleCompetitorWithOneBasedLegNumber(getCompetitorsToShow());
        if (leaderWithLeg == null) {
            return 1; // before start, show simulation for leg 1
        } else {
            return leaderWithLeg.getA();
        }
    }

    private Image createSAPLogo() {
        ImageResource sapLogoResource = resources.sapLogoOverlay();
        Image sapLogo = new Image(sapLogoResource);
        sapLogo.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.open("http://www.sap.com", "_blank", null);
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
    private Bounds getDefaultZoomBounds() {
        return new BoatsBoundsCalculator().calculateNewBounds(RaceMap.this);
    }
}
