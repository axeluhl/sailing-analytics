package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.control.ControlAnchor;
import com.google.gwt.maps.client.control.ControlPosition;
import com.google.gwt.maps.client.control.LargeMapControl3D;
import com.google.gwt.maps.client.control.MenuMapTypeControl;
import com.google.gwt.maps.client.control.ScaleControl;
import com.google.gwt.maps.client.event.MapDragEndHandler;
import com.google.gwt.maps.client.event.MapMouseMoveHandler;
import com.google.gwt.maps.client.event.MapZoomEndHandler;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.event.PolylineClickHandler;
import com.google.gwt.maps.client.event.PolylineMouseOutHandler;
import com.google.gwt.maps.client.event.PolylineMouseOverHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.maps.client.overlay.PolylineOptions;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.actions.GetRaceMapDataAction;
import com.sap.sailing.gwt.ui.actions.GetWindInfoAction;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.NumberFormatterFactory;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.RequiresDataInitialization;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayStates;
import com.sap.sailing.gwt.ui.client.WindSourceTypeFormatter;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.CourseDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.LegInfoDTO;
import com.sap.sailing.gwt.ui.shared.ManeuverDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;
import com.sap.sailing.gwt.ui.shared.RaceMapDataDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.shared.racemap.RaceMapHelpLinesSettings.HelpLineTypes;
import com.sap.sailing.gwt.ui.shared.racemap.RaceMapZoomSettings.ZoomTypes;

public class RaceMap extends AbsolutePanel implements TimeListener, CompetitorSelectionChangeListener, RaceSelectionChangeListener,
        RaceTimesInfoProviderListener, Component<RaceMapSettings>, RequiresDataInitialization, RequiresResize {
    private MapWidget map;

    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;

    /**
     * Tails of competitors currently displayed as overlays on the map.
     */
    private final Map<CompetitorDTO, Polyline> tails;

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

    /**
     * Polyline for the course middle line.
     */
    private Polyline courseMiddleLine;

    private WindTrackInfoDTO lastCombinedWindTrackInfoDTO;
    
    /**
     * Key set is equal to that of {@link #tails} and tells what the index in in {@link #fixes} of the first fix shown
     * in {@link #tails} is. If a key is contained in this map, it is also contained in {@link #lastShownFix} and vice versa.
     */
    private final Map<CompetitorDTO, Integer> firstShownFix;

    /**
     * Key set is equal to that of {@link #tails} and tells what the index in in {@link #fixes} of the last fix shown in
     * {@link #tails} is. If a key is contained in this map, it is also contained in {@link #firstShownFix} and vice versa.
     */
    private final Map<CompetitorDTO, Integer> lastShownFix;

    /**
     * Fixes of each competitors tail. If a list is contained for a competitor, the list contains a timely "contiguous"
     * list of fixes for the competitor. This means the server has no more data for the time interval covered, unless
     * the last fix was {@link GPSFixDTO#extrapolated obtained by extrapolation}.
     */
    private final Map<CompetitorDTO, List<GPSFixDTO>> fixes;

    /**
     * html5 canvases used as boat display on the map
     */
    private final Map<CompetitorDTO, BoatCanvasOverlay> boatCanvasOverlays;

    /**
     * html5 canvases used for competitor info display on the map
     */
    private final Map<CompetitorDTO, CompetitorInfoOverlay> competitorInfoOverlays;

    /**
     * Map overlays with html5 canvas used to display wind sensors
     */
    private final Map<WindSource, WindSensorOverlay> windSensorOverlays;
    
    private final Map<String, Marker> courseMarkMarkers;
    
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
    
    private LatLng lastMousePosition;

    private CompetitorSelectionProvider competitorSelection;

    private List<RegattaAndRaceIdentifier> selectedRaces;

    /**
     * Used to check if the first initial zoom to the mark markers was already done.
     */
    private boolean mapFirstZoomDone = false;

    // key for domains web4sap.com, sapsailing.com and sapcoe-app01.pironet-ndh.com
    private final String mapsAPIKey = "AIzaSyD1Se4tIkt-wglccbco3S7twaHiG20hR9E";

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

    private RaceMapImageManager raceMapImageManager; 

    private final RaceMapSettings settings;
    
    private final StringMessages stringMessages;
    
    private boolean dataInitialized;

    private Date lastTimeChangeBeforeInitialization;

    /**
     * The last quick ranks received from a call to {@link SailingServiceAsync#getQuickRanks(RaceIdentifier, Date, AsyncCallback)} upon
     * the last {@link #timeChanged(Date)} event. Therefore, the ranks listed here correspond to the {@link #timer}'s time.
     */
    private List<QuickRankDTO> quickRanks;

    private final CombinedWindPanel combinedWindPanel;
    
    private final AsyncActionsExecutor asyncActionsExecutor;

    public RaceMap(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, ErrorReporter errorReporter, Timer timer,
            CompetitorSelectionProvider competitorSelection, StringMessages stringMessages) {
        this.setSize("100%", "100%");
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.errorReporter = errorReporter;
        this.timer = timer;
        timer.addTimeListener(this);
        raceMapImageManager = new RaceMapImageManager();
        tails = new HashMap<CompetitorDTO, Polyline>();
        firstShownFix = new HashMap<CompetitorDTO, Integer>();
        lastShownFix = new HashMap<CompetitorDTO, Integer>();
        courseMarkMarkers = new HashMap<String, Marker>();
        markDTOs = new HashMap<String, MarkDTO>();
        boatCanvasOverlays = new HashMap<CompetitorDTO, BoatCanvasOverlay>();
        competitorInfoOverlays = new HashMap<CompetitorDTO, CompetitorInfoOverlay>();
        windSensorOverlays = new HashMap<WindSource, WindSensorOverlay>();
        fixes = new HashMap<CompetitorDTO, List<GPSFixDTO>>();
        this.competitorSelection = competitorSelection;
        competitorSelection.addCompetitorSelectionChangeListener(this);
        settings = new RaceMapSettings();
        lastTimeChangeBeforeInitialization = null;
        dataInitialized = false;
        initializeData();
        
        combinedWindPanel = new CombinedWindPanel(raceMapImageManager, stringMessages);
        combinedWindPanel.setVisible(false);
    }
    
    private void loadMapsAPI() {
        Maps.loadMapsApi(mapsAPIKey, "2", false, new Runnable() {
            public void run() {
                map = new MapWidget();
                map.addControl(new MenuMapTypeControl());
                map.addControl(new ScaleControl(), new ControlPosition(ControlAnchor.BOTTOM_RIGHT, /* offsetX */ 10, /* offsetY */ 20));
                map.addControl(new LargeMapControl3D(), new ControlPosition(ControlAnchor.TOP_RIGHT, /* offsetX */ 0, /* offsetY */ 30));
                // Add the map to the HTML host page
                map.setScrollWheelZoomEnabled(true);
                map.setContinuousZoom(true);
                RaceMap.this.add(map, 0, 0);
                RaceMap.this.add(combinedWindPanel, 10, 10);
                RaceMap.this.raceMapImageManager.loadMapIcons(map);
                map.setSize("100%", "100%");
                map.addMapZoomEndHandler(new MapZoomEndHandler() {
                    @Override
                    public void onZoomEnd(MapZoomEndEvent event) {
                        map.checkResizeAndCenter();
                        final List<RaceMapZoomSettings.ZoomTypes> emptyList = Collections.emptyList();
                        settings.getZoomSettings().setTypesToConsiderOnZoom(emptyList);
                        Set<CompetitorDTO> competitorDTOsOfUnusedMarkers = new HashSet<CompetitorDTO>(boatCanvasOverlays.keySet());
                        for (CompetitorDTO competitorDTO : getCompetitorsToShow()) {
                                boolean usedExistingMarker = updateBoatCanvasForCompetitor(competitorDTO, timer.getTime());
                                if (usedExistingMarker) {
                                    competitorDTOsOfUnusedMarkers.remove(competitorDTO);
                                }
                        }
                        for (CompetitorDTO unusedMarkerCompetitorDTO : competitorDTOsOfUnusedMarkers) {
                            BoatCanvasOverlay boatCanvas = boatCanvasOverlays.get(unusedMarkerCompetitorDTO);
                            RaceMap.this.map.removeOverlay(boatCanvas);
                            boatCanvasOverlays.remove(unusedMarkerCompetitorDTO);
                        }
                    }
                });
                
                map.addMapDragEndHandler(new MapDragEndHandler() {
                    @Override
                    public void onDragEnd(MapDragEndEvent event) {
                        final List<RaceMapZoomSettings.ZoomTypes> emptyList = Collections.emptyList();
                        settings.getZoomSettings().setTypesToConsiderOnZoom(emptyList);
                    }
                });
                map.addMapMouseMoveHandler(new MapMouseMoveHandler() {
                    @Override
                    public void onMouseMove(MapMouseMoveEvent event) {
                        lastMousePosition = event.getLatLng();
                    }
                });
                
                //If there was a time change before the API was loaded, reset the time
                if (lastTimeChangeBeforeInitialization != null) {
                    timeChanged(lastTimeChangeBeforeInitialization);
                    lastTimeChangeBeforeInitialization = null;
                }
                //Data has been initialized
                RaceMap.this.dataInitialized = true;
            }
        });
    }

    
    
    public void redraw() {
        timeChanged(timer.getTime());
    }
    
    @Override
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
        mapFirstZoomDone = false;
        // TODO bug 494: reset zoom settings to user preferences
        this.selectedRaces = selectedRaces;
    }

    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfos) {
        this.lastRaceTimesInfo = raceTimesInfos.get(selectedRaces.get(0));        
    }

    @Override
    public void timeChanged(final Date date) {
        if (date != null) {
            if (selectedRaces != null && !selectedRaces.isEmpty()) {
                RegattaAndRaceIdentifier race = selectedRaces.get(selectedRaces.size() - 1);
                final Iterable<CompetitorDTO> competitorsToShow = getCompetitorsToShow();
                
                if (race != null) {
                    final Triple<Map<CompetitorDTO, Date>, Map<CompetitorDTO, Date>, Map<CompetitorDTO, Boolean>> fromAndToAndOverlap = 
                            computeFromAndTo(date, competitorsToShow);
                    final int requestID = ++boatPositionRequestIDCounter;

                    GetRaceMapDataAction getRaceMapDataAction = new GetRaceMapDataAction(sailingService, race, date,
                            fromAndToAndOverlap.getA(), fromAndToAndOverlap.getB(), true, new AsyncCallback<RaceMapDataDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError("Error obtaining racemap data: " + caught.getMessage(), true /*silentMode */);
                        }

                        @Override
                        public void onSuccess(RaceMapDataDTO raceMapDataDTO) {
                            if (map != null && raceMapDataDTO != null) {
                                quickRanks = raceMapDataDTO.quickRanks;
                                // process response only if not received out of order
                                if (startedProcessingRequestID < requestID) {
                                    startedProcessingRequestID = requestID;
                                    // Do boat specific actions
                                    Map<CompetitorDTO, List<GPSFixDTO>> boatData = raceMapDataDTO.boatPositions;
                                    updateFixes(boatData, fromAndToAndOverlap.getC());
                                    showBoatsOnMap(date, getCompetitorsToShow());
                                    showCompetitorInfoOnMap(date, competitorSelection.getSelectedCompetitors());
                                    if (douglasMarkers != null) {
                                        removeAllMarkDouglasPeuckerpoints();
                                    }
                                    if (maneuverMarkers != null) {
                                        removeAllManeuverMarkers();
                                    }
                                    
                                    // Do mark specific actions
                                    showCourseMarksOnMap(raceMapDataDTO.coursePositions);
                                    showStartAndFinishLines(raceMapDataDTO.coursePositions);
                                    showAdvantageLine(competitorsToShow, date);
                                        
                                    // Rezoom the map
                                    // TODO make this a loop across the LatLngBoundsCalculators, pulling them from a collection updated in updateSettings
                                    if (!settings.getZoomSettings().containsZoomType(ZoomTypes.NONE)) { // Auto zoom if setting is not manual
                                        LatLngBounds bounds = settings.getZoomSettings().getNewBounds(RaceMap.this);
                                        zoomMapToNewBounds(bounds);
                                        mapFirstZoomDone = true;
                                    } else if (!mapFirstZoomDone) { // Zoom once to the marks
                                        zoomMapToNewBounds(new CourseMarksBoundsCalculator().calculateNewBounds(RaceMap.this));
                                        mapFirstZoomDone = true;
                                        /*
                                         * Reset the mapZoomedOrPannedSinceLastRaceSelection: In spite of the fact that
                                         * the map was just zoomed to the bounds of the marks, it was not a zoom or pan
                                         * triggered by the user. As a consequence the
                                         * mapZoomedOrPannedSinceLastRaceSelection option has to reset again.
                                         */
                                        // TODO bug 494: consider initial user-specific zoom settings
                                    }
                                }
                            } else {
                                lastTimeChangeBeforeInitialization = date;
                            }
                        }
                    });
                    asyncActionsExecutor.execute(getRaceMapDataAction);
                    // draw the wind into the map, get the combined wind
                    List<String> windSourceTypeNames = new ArrayList<String>();
                    windSourceTypeNames.add(WindSourceType.EXPEDITION.name());
                    windSourceTypeNames.add(WindSourceType.COMBINED.name());
                    
                    GetWindInfoAction getWindInfoAction = new GetWindInfoAction(sailingService, race, date, 1000L, 1, windSourceTypeNames,
                        new AsyncCallback<WindInfoForRaceDTO>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError("Error obtaining wind information: " + caught.getMessage(), true /*silentMode */);
                                }

                                @Override
                                public void onSuccess(WindInfoForRaceDTO windInfo) {
                                    List<Pair<WindSource, WindTrackInfoDTO>> windSourcesToShow = new ArrayList<Pair<WindSource, WindTrackInfoDTO>>();
                                    if(windInfo != null) {
                                        for(WindSource windSource: windInfo.windTrackInfoByWindSource.keySet()) {
                                            WindTrackInfoDTO windTrackInfoDTO = windInfo.windTrackInfoByWindSource.get(windSource);
                                            switch (windSource.getType()) {
                                                case EXPEDITION:
                                                    // we filter out measured wind sources with a very little confidence
                                                    if(windTrackInfoDTO.minWindConfidence > 0.01) {
                                                        windSourcesToShow.add(new Pair<WindSource, WindTrackInfoDTO>(windSource, windTrackInfoDTO));
                                                    }
                                                    break;
                                                case COMBINED:
                                                    showCombinedWindOnMap(windSource, windTrackInfoDTO);
                                                    if(windTrackInfoDTO != null) {
                                                        lastCombinedWindTrackInfoDTO = windTrackInfoDTO; 
                                                    }
                                                    break;
                                        		default:
                                        			//Which wind sources are requested is defined in a list above this action.
                                        			//So we throw here an exception to notice a missing source.
                                        			throw new UnsupportedOperationException("Theres currently no support for the enum value '" + windSource.getType() + "' in this method.");
                                            }
                                        }
                                    }
                                    showWindSensorsOnMap(windSourcesToShow);
                                }
                            });
                    
                    asyncActionsExecutor.execute(getWindInfoAction);
                }
            }
        }
    }

    /**
     * From {@link #fixes} as well as the selection of {@link #getCompetitorsToShow competitors to show}, computes the
     * from/to times for which to request GPS fixes from the server. No update is performed here to {@link #fixes}. The
     * result guarantees that, when used in
     * {@link SailingServiceAsync#getBoatPositions(String, String, Map, Map, boolean, AsyncCallback)}, for each
     * competitor from {@link #competitorsToShow} there are all fixes known by the server for that competitor starting
     * at <code>upTo-{@link #tailLengthInMilliSeconds}</code> and ending at <code>upTo</code> (exclusive).
     * 
     * @return a triple whose {@link Triple#getA() first} component contains the "from", and whose {@link Triple#getB()
     *         second} component contains the "to" times for the competitors whose trails / positions to show; the
     *         {@link Triple#getC() third} component tells whether the existing fixes can remain and be augmented by
     *         those requested or need to be replaced
     */
    protected Triple<Map<CompetitorDTO, Date>, Map<CompetitorDTO, Date>, Map<CompetitorDTO, Boolean>> computeFromAndTo(
            Date upTo, Iterable<CompetitorDTO> competitorsToShow) {
        Date tailstart = new Date(upTo.getTime() - settings.getEffectiveTailLengthInMilliseconds());
        Map<CompetitorDTO, Date> from = new HashMap<CompetitorDTO, Date>();
        Map<CompetitorDTO, Date> to = new HashMap<CompetitorDTO, Date>();
        Map<CompetitorDTO, Boolean> overlapWithKnownFixes = new HashMap<CompetitorDTO, Boolean>();
        
        for (CompetitorDTO competitor : competitorsToShow) {
            List<GPSFixDTO> fixesForCompetitor = fixes.get(competitor);
            Date fromDate;
            Date toDate;
            Date timepointOfLastKnownFix = fixesForCompetitor == null ? null : getTimepointOfLastNonExtrapolated(fixesForCompetitor);
            Date timepointOfFirstKnownFix = fixesForCompetitor == null ? null : getTimepointOfFirstNonExtrapolated(fixesForCompetitor);
            boolean overlap = false;
            if (fixesForCompetitor != null && timepointOfFirstKnownFix != null
                    && !tailstart.before(timepointOfFirstKnownFix) && timepointOfLastKnownFix != null
                    && !tailstart.after(timepointOfLastKnownFix)) {
                // the beginning of what we need is contained in the interval we already have; skip what we already have
                // FIXME requests the lastKnownFix again because "from" is *inclusive*; could lead to bug 319
                fromDate = timepointOfLastKnownFix;
                overlap = true;
            } else {
                fromDate = tailstart;
            }
            if (fixesForCompetitor != null && timepointOfFirstKnownFix != null
                    && !upTo.before(timepointOfFirstKnownFix) && timepointOfLastKnownFix != null
                    && !upTo.after(timepointOfLastKnownFix)) {
                // the end of what we need is contained in the interval we already have; skip what we already have
                toDate = timepointOfFirstKnownFix;
                overlap = true;
            } else {
                toDate = upTo;
            }
            // only request something for the competitor if we're missing information at all
            if (fromDate.before(toDate) || fromDate.equals(toDate)) {
                from.put(competitor, fromDate);
                to.put(competitor, toDate);
                overlapWithKnownFixes.put(competitor, overlap);
            }
        }
        return new Triple<Map<CompetitorDTO, Date>, Map<CompetitorDTO, Date>, Map<CompetitorDTO, Boolean>>(from, to,
                overlapWithKnownFixes);
    }


    /**
     * Adds the fixes received in <code>result</code> to {@link #fixes} and ensures they are still contiguous for each
     * competitor. If <code>overlapsWithKnownFixes</code> indicates that the fixes received in <code>result</code>
     * overlap with those already known, the fixes are merged into the list of already known fixes for the competitor.
     * Otherwise, the fixes received in <code>result</code> replace those known so far for the respective competitor.
     */
    protected void updateFixes(Map<CompetitorDTO, List<GPSFixDTO>> result,
            Map<CompetitorDTO, Boolean> overlapsWithKnownFixes) {
        for (Map.Entry<CompetitorDTO, List<GPSFixDTO>> e : result.entrySet()) {
            if (e.getValue() != null && !e.getValue().isEmpty()) {
                List<GPSFixDTO> fixesForCompetitor = fixes.get(e.getKey());
                if (fixesForCompetitor == null) {
                    fixesForCompetitor = new ArrayList<GPSFixDTO>();
                    fixes.put(e.getKey(), fixesForCompetitor);
                }
                if (!overlapsWithKnownFixes.get(e.getKey())) {
                    fixesForCompetitor.clear();
                    // to re-establish the invariants for tails, firstShownFix and lastShownFix, we now need to remove
                    // all points from the competitor's polyline and clear the entries in firstShownFix and lastShownFix
                    if (map != null && tails.containsKey(e.getKey())) {
                        map.removeOverlay(tails.remove(e.getKey()));
                    }
                    firstShownFix.remove(e.getKey());
                    lastShownFix.remove(e.getKey());
                    fixesForCompetitor.addAll(e.getValue());
                } else {
                    mergeFixes(e.getKey(), e.getValue());
                }
            }
        }
    }

    protected void showCourseMarksOnMap(CourseDTO courseDTO) {
        if (map != null && courseDTO != null) {
            Map<String, MarkDTO> toRemove = new HashMap<String, MarkDTO>(markDTOs);
            if (courseDTO.marks != null) {
                for (MarkDTO markDTO : courseDTO.marks) {
                    Marker markMarker = courseMarkMarkers.get(markDTO.name);
                    if (markMarker == null) {
                        markMarker = createCourseMarkMarker(markDTO);
                        courseMarkMarkers.put(markDTO.name, markMarker);
                        markDTOs.put(markDTO.name, markDTO);
                        map.addOverlay(markMarker);
                    } else {
                        markMarker.setLatLng(LatLng.newInstance(markDTO.position.latDeg, markDTO.position.lngDeg));
                        toRemove.remove(markDTO.name);
                    }
                }
                for (String toRemoveMarkName : toRemove.keySet()) {
                    Marker marker = courseMarkMarkers.remove(toRemoveMarkName);
                    map.removeOverlay(marker);
                    markDTOs.remove(toRemoveMarkName);
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

    protected void showWindSensorsOnMap(List<Pair<WindSource, WindTrackInfoDTO>> windSensorsList) {
        if (map != null) {
            Set<WindSource> toRemoveWindSources = new HashSet<WindSource>(windSensorOverlays.keySet());
            for (Pair<WindSource, WindTrackInfoDTO> windSourcePair : windSensorsList) {
                WindSource windSource = windSourcePair.getA(); 
                WindTrackInfoDTO windTrackInfoDTO = windSourcePair.getB();

                WindSensorOverlay windSensorOverlay = windSensorOverlays.get(windSource);
                if (windSensorOverlay == null) {
                    windSensorOverlay = createWindSensorOverlay(windSource, windTrackInfoDTO);
                    windSensorOverlays.put(windSource, windSensorOverlay);
                    map.addOverlay(windSensorOverlay);
                } else {
                    windSensorOverlay.setWindInfo(windTrackInfoDTO, windSource);
                    windSensorOverlay.redraw(true);
                    toRemoveWindSources.remove(windSource);
                }
            }
            for (WindSource toRemoveWindSource : toRemoveWindSources) {
                WindSensorOverlay marker = windSensorOverlays.remove(toRemoveWindSource);
                map.removeOverlay(marker);
            }
        }
    }

    protected void showCompetitorInfoOnMap(final Date date, final Iterable<CompetitorDTO> competitorsToShow) {
        if (map != null) {
        	if(settings.isShowSelectedCompetitorsInfo()) {
                Set<CompetitorDTO> toRemoveCompetorInfoOverlays = new HashSet<CompetitorDTO>(competitorInfoOverlays.keySet());
                for (CompetitorDTO competitorDTO : competitorsToShow) {
                    if (fixes.containsKey(competitorDTO)) {
                        GPSFixDTO lastBoatFix = getBoatFix(competitorDTO, date);
                        if (lastBoatFix != null) {
                        	CompetitorInfoOverlay competitorInfoOverlay = competitorInfoOverlays.get(competitorDTO);
                            if (competitorInfoOverlay == null) {
                            	competitorInfoOverlay = createCompetitorInfoOverlay(competitorDTO);
                            	competitorInfoOverlays.put(competitorDTO, competitorInfoOverlay);
                                map.addOverlay(competitorInfoOverlay);
                                competitorInfoOverlay.setBoatFix(lastBoatFix);
                            	competitorInfoOverlay.redraw(true);
                            } else {
                                competitorInfoOverlay.setBoatFix(lastBoatFix);
                            	competitorInfoOverlay.redraw(true);
                            }
                        	toRemoveCompetorInfoOverlays.remove(competitorDTO);
                        }
                    }
                }
                for (CompetitorDTO toRemoveCompetorDTO : toRemoveCompetorInfoOverlays) {
                	CompetitorInfoOverlay competitorInfoOverlay = competitorInfoOverlays.get(toRemoveCompetorDTO);
                    map.removeOverlay(competitorInfoOverlay);
                    competitorInfoOverlays.remove(toRemoveCompetorDTO);
                }
        	} else {
        		// remove all overlays
        		for(CompetitorInfoOverlay competitorInfoOverlay: competitorInfoOverlays.values()) {
                    map.removeOverlay(competitorInfoOverlay);
        		}
        		competitorInfoOverlays.clear();
        	}
        }
    }
    
    protected void showBoatsOnMap(final Date date, final Iterable<CompetitorDTO> competitorsToShow) {
        if (map != null) {
            Date tailsFromTime = new Date(date.getTime() - settings.getEffectiveTailLengthInMilliseconds());
            Date tailsToTime = new Date(date.getTime());
            Set<CompetitorDTO> competitorDTOsOfUnusedTails = new HashSet<CompetitorDTO>(tails.keySet());
            Set<CompetitorDTO> competitorDTOsOfUnusedBoatCanvases = new HashSet<CompetitorDTO>(boatCanvasOverlays.keySet());
            for (CompetitorDTO competitorDTO : competitorsToShow) {
                if (fixes.containsKey(competitorDTO)) {
                    Polyline tail = tails.get(competitorDTO);
                    if (tail == null) {
                        tail = createTailAndUpdateIndices(competitorDTO, tailsFromTime, tailsToTime);
                        map.addOverlay(tail);
                    } else {
                        updateTail(tail, competitorDTO, tailsFromTime, tailsToTime);
                        competitorDTOsOfUnusedTails.remove(competitorDTO);
                    }
                    boolean usedExistingBoatCanvas = updateBoatCanvasForCompetitor(competitorDTO, date);
                    if (usedExistingBoatCanvas) {
                        competitorDTOsOfUnusedBoatCanvases.remove(competitorDTO);
                    }
                }
            }
            for (CompetitorDTO unusedBoatCanvasCompetitorDTO : competitorDTOsOfUnusedBoatCanvases) {
                BoatCanvasOverlay boatCanvas = boatCanvasOverlays.get(unusedBoatCanvasCompetitorDTO);
                map.removeOverlay(boatCanvas);
                boatCanvasOverlays.remove(unusedBoatCanvasCompetitorDTO);
            }
            for (CompetitorDTO unusedTailCompetitorDTO : competitorDTOsOfUnusedTails) {
                map.removeOverlay(tails.remove(unusedTailCompetitorDTO));
            }
        }
    }

    /*
     * This algorithm is limited to distances such that dlon < pi/2, i.e those that extend around less than one quarter of the circumference 
     * of the earth in longitude. A completely general, but more complicated algorithm is necessary if greater distances are allowed. 
     */
    public LatLng calculatePositionAlongRhumbline(LatLng position, double bearingDeg, double distanceInKm) {
        double distianceRad = distanceInKm / 6371.0;  // r = 6371 means earth's radius in km 
        double lat1 = position.getLatitudeRadians();
        double lon1 = position.getLongitudeRadians();
        double bearingRad = bearingDeg / 180. * Math.PI;

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distianceRad) + 
                        Math.cos(lat1) * Math.sin(distianceRad) * Math.cos(bearingRad));
        double lon2 = lon1 + Math.atan2(Math.sin(bearingRad)*Math.sin(distianceRad)*Math.cos(lat1), 
                       Math.cos(distianceRad)-Math.sin(lat1)*Math.sin(lat2));
        lon2 = (lon2+3*Math.PI) % (2*Math.PI) - Math.PI;  // normalize to -180..+180�
        
        return LatLng.newInstance(lat2 / Math.PI * 180., lon2  / Math.PI * 180.);
    }
    
    private Pair<Integer, CompetitorDTO> getLeadingVisibleCompetitorInfo(Iterable<CompetitorDTO> competitorsToShow) {
        CompetitorDTO leadingCompetitorDTO = null;
        int legOfLeaderCompetitor = -1;
        // this only works because the quickRanks are sorted
        for (QuickRankDTO quickRank : quickRanks) {
            if (Util.contains(competitorsToShow, quickRank.competitor)) {
                leadingCompetitorDTO = quickRank.competitor;
                legOfLeaderCompetitor = quickRank.legNumber;
                return new Pair<Integer, CompetitorDTO>(legOfLeaderCompetitor, leadingCompetitorDTO);
            }
        }
        return null;
    }

    private void showAdvantageLine(Iterable<CompetitorDTO> competitorsToShow, Date date) {
        if (map != null && lastRaceTimesInfo != null && quickRanks != null && lastCombinedWindTrackInfoDTO != null
                && lastCombinedWindTrackInfoDTO.windFixes.size() > 0) {
            boolean drewAdvantageLine = false;
            if (settings.getHelpLinesSettings().isVisible(HelpLineTypes.ADVANTAGELINE)) {
                // find competitor with highest rank
                Pair<Integer, CompetitorDTO> visibleLeaderInfo = getLeadingVisibleCompetitorInfo(competitorsToShow);
                // the boat fix may be null; may mean that no positions were loaded yet for the leading visible boat;
                // don't show anything
                GPSFixDTO lastBoatFix = null;
                boolean isVisibleLeaderInfoComplete = false;
                boolean isLegTypeKnown = false;
                if (visibleLeaderInfo != null && visibleLeaderInfo.getA() > 0
                        && visibleLeaderInfo.getA() <= lastRaceTimesInfo.getLegInfos().size()) {
                    isVisibleLeaderInfoComplete = true;
                    LegInfoDTO legInfoDTO = lastRaceTimesInfo.getLegInfos().get(visibleLeaderInfo.getA() - 1);
                    if (legInfoDTO.legType != null) {
                        isLegTypeKnown = true;
                    }
                    lastBoatFix = getBoatFix(visibleLeaderInfo.getB(), date);
                }
                if (isVisibleLeaderInfoComplete && isLegTypeKnown && lastBoatFix != null) {
                    LegInfoDTO legInfoDTO = lastRaceTimesInfo.getLegInfos().get(visibleLeaderInfo.getA() - 1);
                    double advantageLineLengthInKm = 1.0; // TODO this should probably rather scale with the visible
                                                          // area of the map; bug 616
                    double distanceFromBoatPositionInKm = visibleLeaderInfo.getB().boatClass.getHullLengthInMeters() / 1000.; // one hull length
                    // implement and use Position.translateRhumb()
                    double bearingOfBoatInDeg = lastBoatFix.speedWithBearing.bearingInDegrees;
                    LatLng boatPosition = LatLng.newInstance(lastBoatFix.position.latDeg, lastBoatFix.position.lngDeg);
                    LatLng posAheadOfFirstBoat = calculatePositionAlongRhumbline(boatPosition, bearingOfBoatInDeg,
                            distanceFromBoatPositionInKm);
                    double bearingOfCombinedWindInDeg = lastCombinedWindTrackInfoDTO.windFixes.get(0).trueWindBearingDeg;
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

                    LatLng[] advantageLinePoints = new LatLng[2];
                    advantageLinePoints[0] = LatLng.newInstance(advantageLinePos1.getLatitude(),
                            advantageLinePos1.getLongitude());
                    advantageLinePoints[1] = LatLng.newInstance(advantageLinePos2.getLatitude(),
                            advantageLinePos2.getLongitude());
                    if (advantageLine == null) {
                        PolylineOptions options = PolylineOptions.newInstance(/* clickable must be true for hover sensitivity */ true,
                                                                              /* geodesic */true);
                        advantageLine = new Polyline(advantageLinePoints, /* color */"#000000", /* width */1, /* opacity */
                                0.5, options);
                        advantageLine.addPolylineMouseOverHandler(new PolylineMouseOverHandler() {
                            @Override
                            public void onMouseOver(PolylineMouseOverEvent event) {
                                map.setTitle(stringMessages.advantageLine());
                            }
                        });
                        advantageLine.addPolylineMouseOutHandler(new PolylineMouseOutHandler() {
                            @Override
                            public void onMouseOut(PolylineMouseOutEvent event) {
                                map.setTitle("");
                            }
                        });
                        map.addOverlay(advantageLine);
                    } else {
                        advantageLine.deleteVertex(1);
                        advantageLine.deleteVertex(0);
                        advantageLine.insertVertex(0, advantageLinePoints[0]);
                        advantageLine.insertVertex(1, advantageLinePoints[1]);
                    }
                    drewAdvantageLine = true;
                }
            }
            if (!drewAdvantageLine) {
                if (advantageLine != null) {
                    map.removeOverlay(advantageLine);
                    advantageLine = null;
                }
            }
        }
    }

    private void showStartAndFinishLines(final CourseDTO courseDTO) {
        if (map != null && courseDTO != null && lastRaceTimesInfo != null) {
            Pair<Integer, CompetitorDTO> leadingVisibleCompetitorInfo = getLeadingVisibleCompetitorInfo(getCompetitorsToShow());
            int legOfLeadingCompetitor = leadingVisibleCompetitorInfo == null ? -1 : leadingVisibleCompetitorInfo.getA();
            int numberOfLegs = lastRaceTimesInfo.legInfos.size();
            // draw the start line
            if (legOfLeadingCompetitor <= 1 && 
                    settings.getHelpLinesSettings().isVisible(HelpLineTypes.STARTLINE) && courseDTO.startMarkPositions.size() == 2) {
                LatLng[] startGatePoints = new LatLng[2];
                startGatePoints[0] = LatLng.newInstance(courseDTO.startMarkPositions.get(0).latDeg, courseDTO.startMarkPositions.get(0).lngDeg); 
                startGatePoints[1] = LatLng.newInstance(courseDTO.startMarkPositions.get(1).latDeg, courseDTO.startMarkPositions.get(1).lngDeg); 
                if (startLine == null) {
                    PolylineOptions options = PolylineOptions.newInstance(/* clickable must be true for hover sensititivy*/ true, /* geodesic */true);
                    startLine = new Polyline(startGatePoints, /* color */ "#FFFFFF", /* width */ 1, /* opacity */1.0, options);
                    startLine.addPolylineMouseOverHandler(new PolylineMouseOverHandler() {
                        @Override
                        public void onMouseOver(PolylineMouseOverEvent event) {
                            // TODO bug 1026: add start line bias to tool tip; requires wind data to be available at this point
                            map.setTitle(stringMessages.startLine());
                        }
                    });
                    startLine.addPolylineMouseOutHandler(new PolylineMouseOutHandler() {
                        @Override
                        public void onMouseOut(PolylineMouseOutEvent event) {
                            map.setTitle("");
                        }
                    });
                    map.addOverlay(startLine);
                } else {
                    startLine.deleteVertex(1);
                    startLine.deleteVertex(0);
                    startLine.insertVertex(0, startGatePoints[0]);
                    startLine.insertVertex(1, startGatePoints[1]);
                }
            }
            else {
                if (startLine != null) {
                    map.removeOverlay(startLine);
                    startLine = null;
                }
            }
            // draw the finish line
            if (legOfLeadingCompetitor > 0 && legOfLeadingCompetitor == numberOfLegs &&
                settings.getHelpLinesSettings().isVisible(HelpLineTypes.FINISHLINE) && courseDTO.finishMarkPositions.size() == 2) {
                LatLng[] finishGatePoints = new LatLng[2];
                finishGatePoints[0] = LatLng.newInstance(courseDTO.finishMarkPositions.get(0).latDeg, courseDTO.finishMarkPositions.get(0).lngDeg); 
                finishGatePoints[1] = LatLng.newInstance(courseDTO.finishMarkPositions.get(1).latDeg, courseDTO.finishMarkPositions.get(1).lngDeg); 
                if(finishLine == null) {
                    PolylineOptions options = PolylineOptions.newInstance(/* clickable must be true for hover sensitivity */ true, /* geodesic */true);
                    finishLine = new Polyline(finishGatePoints, /* color */ "#000000", /* width */ 1, /* opacity */1.0, options);
                    finishLine.addPolylineMouseOverHandler(new PolylineMouseOverHandler() {
                        @Override
                        public void onMouseOver(PolylineMouseOverEvent event) {
                            map.setTitle(stringMessages.finishLine());
                        }
                    });
                    finishLine.addPolylineMouseOutHandler(new PolylineMouseOutHandler() {
                        @Override
                        public void onMouseOut(PolylineMouseOutEvent event) {
                            map.setTitle("");
                        }
                    });
                    map.addOverlay(finishLine);
                } else {
                    finishLine.deleteVertex(1);
                    finishLine.deleteVertex(0);
                    finishLine.insertVertex(0, finishGatePoints[0]);
                    finishLine.insertVertex(1, finishGatePoints[1]);
                }
            }
            else {
                if (finishLine != null) {
                    map.removeOverlay(finishLine);
                    finishLine = null;
                }
            }
            // draw the course middle line
            if (legOfLeadingCompetitor > 0 && courseDTO.waypointPositions.size() > legOfLeadingCompetitor &&
                    settings.getHelpLinesSettings().isVisible(HelpLineTypes.COURSEMIDDLELINE)) {
                LatLng[] courseMiddleLinePoints = new LatLng[2];
                double p1Lat = courseDTO.waypointPositions.get(legOfLeadingCompetitor-1).latDeg;
                double p1Lng = courseDTO.waypointPositions.get(legOfLeadingCompetitor-1).lngDeg;
                courseMiddleLinePoints[0] = LatLng.newInstance(p1Lat, p1Lng);
                courseMiddleLinePoints[1] = LatLng.newInstance(courseDTO.waypointPositions.get(legOfLeadingCompetitor).latDeg,
                        courseDTO.waypointPositions.get(legOfLeadingCompetitor).lngDeg); 
                if (courseMiddleLine == null) {
                    PolylineOptions options = PolylineOptions.newInstance(/* clickable must be true for hover sensitivity */ true, /* geodesic */true);
                    courseMiddleLine = new Polyline(courseMiddleLinePoints, /* color */ "#6896c6", /* width */ 1, /* opacity */1.0, options);
                    courseMiddleLine.addPolylineMouseOverHandler(new PolylineMouseOverHandler() {
                        @Override
                        public void onMouseOver(PolylineMouseOverEvent event) {
                            map.setTitle(stringMessages.courseMiddleLine());
                        }
                    });
                    courseMiddleLine.addPolylineMouseOutHandler(new PolylineMouseOutHandler() {
                        @Override
                        public void onMouseOut(PolylineMouseOutEvent event) {
                            map.setTitle("");
                        }
                    });
                    map.addOverlay(courseMiddleLine);
                } else {
                    courseMiddleLine.deleteVertex(1);
                    courseMiddleLine.deleteVertex(0);
                    courseMiddleLine.insertVertex(0, courseMiddleLinePoints[0]);
                    courseMiddleLine.insertVertex(1, courseMiddleLinePoints[1]);
                }
            }
            else {
                if (courseMiddleLine != null) {
                    map.removeOverlay(courseMiddleLine);
                    courseMiddleLine = null;
                }
            }
        }
    }
    
    private void zoomMapToNewBounds(LatLngBounds newBounds) {
        if (newBounds != null) {
            List<ZoomTypes> oldZoomSettings = settings.getZoomSettings().getTypesToConsiderOnZoom();
            map.setCenter(newBounds.getCenter());
            map.setZoomLevel(map.getBoundsZoomLevel(newBounds));
            settings.getZoomSettings().setTypesToConsiderOnZoom(oldZoomSettings);
        }
    }
    
    private boolean updateBoatCanvasForCompetitor(CompetitorDTO competitorDTO, Date date) {
        boolean usedExistingCanvas = false;
        GPSFixDTO lastBoatFix = getBoatFix(competitorDTO, date);
        if (lastBoatFix != null) {
            BoatCanvasOverlay boatCanvas = boatCanvasOverlays.get(competitorDTO);
            if (boatCanvas == null) {
                boatCanvas = createBoatCanvas(competitorDTO, displayHighlighted(competitorDTO));
                map.addOverlay(boatCanvas);
                boatCanvasOverlays.put(competitorDTO, boatCanvas);
                boatCanvas.setSelected(displayHighlighted(competitorDTO));
                boatCanvas.setBoatFix(lastBoatFix);
                boatCanvas.redraw(true);
            } else {
                usedExistingCanvas = true;
                boatCanvas.setSelected(displayHighlighted(competitorDTO));
                boatCanvas.setBoatFix(lastBoatFix);
                boatCanvas.redraw(true);
            }
        }
        return usedExistingCanvas;
    }

    private boolean displayHighlighted(CompetitorDTO competitorDTO) {
        return !settings.isShowOnlySelectedCompetitors() && competitorSelection.isSelected(competitorDTO);
    }

    protected Marker createCourseMarkMarker(final MarkDTO markDTO) {
        MarkerOptions options = MarkerOptions.newInstance();
        final Icon markIcon = raceMapImageManager.resolveMarkIcon(markDTO.type, markDTO.color, markDTO.shape, markDTO.pattern);
        if (markIcon != null) {
            options.setIcon(markIcon);
        }
        options.setTitle(markDTO.name);
        final Marker courseMarkMarker = new Marker(LatLng.newInstance(markDTO.position.latDeg, markDTO.position.lngDeg),
                options);
        courseMarkMarker.addMarkerClickHandler(new MarkerClickHandler() {
            @Override
            public void onClick(MarkerClickEvent event) {
                LatLng latlng = courseMarkMarker.getLatLng();
                showMarkInfoWindow(markDTO, latlng);
            }
        });
        return courseMarkMarker;
    }

    private CompetitorInfoOverlay createCompetitorInfoOverlay(final CompetitorDTO competitorDTO) {
        return new CompetitorInfoOverlay(competitorDTO, raceMapImageManager);
    }
    
    private BoatCanvasOverlay createBoatCanvas(final CompetitorDTO competitorDTO, boolean highlighted) {
        final BoatCanvasOverlay boatCanvas = new BoatCanvasOverlay(competitorDTO);
        boatCanvas.setSelected(highlighted);
        boatCanvas.getCanvas().setTitle(competitorDTO.sailID + ", " + competitorDTO.name);
        boatCanvas.getCanvas().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                GPSFixDTO latestFixForCompetitor = getBoatFix(competitorDTO, timer.getTime());
                LatLng where = LatLng.newInstance(latestFixForCompetitor.position.latDeg, latestFixForCompetitor.position.lngDeg);
                map.getInfoWindow().open(where,
                        new InfoWindowContent(getInfoWindowContent(competitorDTO, latestFixForCompetitor)));
            }
        });
        return boatCanvas;
    }

    protected WindSensorOverlay createWindSensorOverlay(final WindSource windSource, final WindTrackInfoDTO windTrackInfoDTO) {
        final WindSensorOverlay windSensorOverlay = new WindSensorOverlay(raceMapImageManager, stringMessages);
        windSensorOverlay.setWindInfo(windTrackInfoDTO, windSource);
        windSensorOverlay.getCanvas().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showWindSensorInfoWindow(windSensorOverlay);
            }
        });
        return windSensorOverlay;
    }

    private void showMarkInfoWindow(MarkDTO markDTO, LatLng latlng) {
        map.getInfoWindow().open(latlng, new InfoWindowContent(getInfoWindowContent(markDTO)));
    }

    private void showCompetitorInfoWindow(final CompetitorDTO competitorDTO, LatLng where) {
        GPSFixDTO latestFixForCompetitor = getBoatFix(competitorDTO, timer.getTime()); 
        //TODO find closed fixed where the mouse was (where) BUG 470
        InfoWindowContent infoWindowContent = new InfoWindowContent(getInfoWindowContent(competitorDTO, latestFixForCompetitor));
        map.getInfoWindow().open(where, infoWindowContent);
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
            LatLng where = LatLng.newInstance(windDTO.position.latDeg, windDTO.position.lngDeg);
            map.getInfoWindow().open(where, new InfoWindowContent(getInfoWindowContent(windSource, windTrackInfoDTO)));
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
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.mark(), markDTO.name));
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
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.competitor(), competitorDTO.name));
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.sailNumber(), competitorDTO.sailID));
        Integer rank = null;
        if (quickRanks != null) {
            for (QuickRankDTO quickRank : quickRanks) {
                if (quickRank.competitor.equals(competitorDTO)) {
                    rank = quickRank.rank;
                    break;
                }
            }
        }
        if (rank != null) {
            vPanel.add(createInfoWindowLabelAndValue(stringMessages.rank(), String.valueOf(rank)));
        }
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.speed(),
                NumberFormatterFactory.getDecimalFormat(1).format(lastFix.speedWithBearing.speedInKnots) + " "+stringMessages.knotsUnit()));
        vPanel.add(createInfoWindowLabelAndValue(stringMessages.bearing(), (int) lastFix.speedWithBearing.bearingInDegrees + " "+stringMessages.degreesShort()));
        if (lastFix.wind != null) {
            vPanel.add(createInfoWindowLabelAndValue(stringMessages.degreesBoatToTheWind(), (int) Math.abs(
                    new DegreeBearingImpl(lastFix.speedWithBearing.bearingInDegrees).getDifferenceTo(
                    new DegreeBearingImpl(lastFix.wind.dampenedTrueWindFromDeg)).getDegrees()) + " "+stringMessages.degreesShort()));
        }
        if (!selectedRaces.isEmpty()) {
            RegattaAndRaceIdentifier race = selectedRaces.get(selectedRaces.size() - 1);
            if (race != null) {
                Map<CompetitorDTO, Date> from = new HashMap<CompetitorDTO, Date>();
                from.put(competitorDTO, fixes.get(competitorDTO).get(firstShownFix.get(competitorDTO)).timepoint);
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
            if(settings.isShowAllCompetitors()) {
                result = competitorSelection.getAllCompetitors();
            } else {
                int visibleCompetitorsCount = settings.getMaxVisibleCompetitorsCount();
                if(quickRanks != null && quickRanks.size() >= visibleCompetitorsCount) {
                    Set<CompetitorDTO> competitorList = new HashSet<CompetitorDTO>();
                    int i = 1;
                    for(QuickRankDTO quickRank: quickRanks) {
                        if(i++ <= visibleCompetitorsCount) {
                            competitorList.add(quickRank.competitor);
                        } else {
                            break;
                        }
                    }
                    result = competitorList;
                } else {
                    result = competitorSelection.getAllCompetitors();
                }
            }
        } else {
            result = selection;
        }
        return result;
    }
    
    /**
     * Creates a polyline for the competitor represented by <code>competitorDTO</code>, taking the fixes from
     * {@link #fixes fixes.get(competitorDTO)} and using the fixes starting at time point <code>from</code> (inclusive)
     * up to the last fix with time point before <code>to</code>. The polyline is returned. Updates are applied to
     * {@link #lastShownFix}, {@link #firstShownFix} and {@link #tails}.
     */
    protected Polyline createTailAndUpdateIndices(final CompetitorDTO competitorDTO, Date from, Date to) {
        List<LatLng> points = new ArrayList<LatLng>();
        List<GPSFixDTO> fixesForCompetitor = fixes.get(competitorDTO);
        int indexOfFirst = -1;
        int indexOfLast = -1;
        int i = 0;
        for (Iterator<GPSFixDTO> fixIter = fixesForCompetitor.iterator(); fixIter.hasNext() && indexOfLast == -1;) {
            GPSFixDTO fix = fixIter.next();
            if (!fix.timepoint.before(to)) {
                indexOfLast = i-1;
            } else {
                LatLng point = null;
                if (indexOfFirst == -1) {
                    if (!fix.timepoint.before(from)) {
                        indexOfFirst = i;
                        point = LatLng.newInstance(fix.position.latDeg, fix.position.lngDeg);
                    }
                } else {
                    point = LatLng.newInstance(fix.position.latDeg, fix.position.lngDeg);
                }
                if (point != null) {
                    points.add(point);
                }
            }
            i++;
        }
        if (indexOfLast == -1) {
            indexOfLast = i - 1;
        }
        if (indexOfFirst != -1 && indexOfLast != -1) {
            firstShownFix.put(competitorDTO, indexOfFirst);
            lastShownFix.put(competitorDTO, indexOfLast);
        }
        PolylineOptions options = PolylineOptions.newInstance(
        /* clickable */true, /* geodesic */true);
        Polyline result = new Polyline(points.toArray(new LatLng[0]), competitorSelection.getColor(competitorDTO), /* width */ 1,
        /* opacity */0.5, options);
        result.addPolylineClickHandler(new PolylineClickHandler() {
            @Override
            public void onClick(PolylineClickEvent event) {
                showCompetitorInfoWindow(competitorDTO, lastMousePosition);
            }
        });
        result.addPolylineMouseOverHandler(new PolylineMouseOverHandler() {
            @Override
            public void onMouseOver(PolylineMouseOverEvent event) {
                map.setTitle(competitorDTO.sailID + ", " + competitorDTO.name);
            }
        });
        result.addPolylineMouseOutHandler(new PolylineMouseOutHandler() {
            @Override
            public void onMouseOut(PolylineMouseOutEvent event) {
                map.setTitle("");
            }
        });
        tails.put(competitorDTO, result);
        return result;
    }

    protected void removeAllMarkDouglasPeuckerpoints() {
        if (douglasMarkers != null) {
            for (Marker marker : douglasMarkers) {
                map.removeOverlay(marker);
            }
        }
        douglasMarkers = null;
    }

    protected void removeAllManeuverMarkers() {
        if (maneuverMarkers != null) {
            for (Marker marker : maneuverMarkers) {
                map.removeOverlay(marker);
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
                    Marker marker = new Marker(latLng, options);
                    douglasMarkers.add(marker);
                    map.addOverlay(marker);
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
                        MarkerOptions options = MarkerOptions.newInstance();
                        //TODO Introduce user role dependent view (Spectator, Admin)
                        options.setTitle(maneuver.toString(stringMessages));
                        options.setIcon(raceMapImageManager.maneuverIconsForTypeAndTargetTack
                                .get(new Util.Pair<ManeuverType, Tack>(maneuver.type, maneuver.newTack)));
                        Marker marker = new Marker(latLng, options);
                        maneuverMarkers.add(marker);
                        map.addOverlay(marker);
                    }
                }
            }
        }
    }

    protected Date getTimepointOfFirstNonExtrapolated(List<GPSFixDTO> fixesForCompetitor) {
        for (GPSFixDTO fix : fixesForCompetitor) {
            if (!fix.extrapolated) {
                return fix.timepoint;
            }
        }
        return null;
    }

    protected Date getTimepointOfLastNonExtrapolated(List<GPSFixDTO> fixesForCompetitor) {
        if (!fixesForCompetitor.isEmpty()) {
            for (ListIterator<GPSFixDTO> fixIter = fixesForCompetitor.listIterator(fixesForCompetitor.size() - 1); fixIter
                    .hasPrevious();) {
                GPSFixDTO fix = fixIter.previous();
                if (!fix.extrapolated) {
                    return fix.timepoint;
                }
            }
        }
        return null;
    }

    /**
     * While updating the {@link #fixes} for <code>competitorDTO</code>, the invariants for {@link #tails} and
     * {@link #firstShownFix} and {@link #lastShownFix} are maintained: each time a fix is inserted, the
     * {@link #firstShownFix}/{@link #lastShownFix} records for <code>competitorDTO</code> are incremented if they are
     * greater or equal to the insertion index and we have a tail in {@link #tails} for <code>competitorDTO</code>.
     * Additionally, if the fix is in between the fixes shown in the competitor's tail, the tail is adjusted by
     * inserting the corresponding fix.
     */
    protected void mergeFixes(CompetitorDTO competitorDTO, List<GPSFixDTO> mergeThis) {
        List<GPSFixDTO> intoThis = fixes.get(competitorDTO);
        int indexOfFirstShownFix = firstShownFix.get(competitorDTO) == null ? -1 : firstShownFix.get(competitorDTO);
        int indexOfLastShownFix = lastShownFix.get(competitorDTO) == null ? -1 : lastShownFix.get(competitorDTO);
        Polyline tail = tails.get(competitorDTO);
        int intoThisIndex = 0;
        for (GPSFixDTO mergeThisFix : mergeThis) {
            while (intoThisIndex < intoThis.size()
                    && intoThis.get(intoThisIndex).timepoint.before(mergeThisFix.timepoint)) {
                intoThisIndex++;
            }
            if (intoThisIndex < intoThis.size() && intoThis.get(intoThisIndex).timepoint.equals(mergeThisFix.timepoint)) {
                // exactly same time point; replace with fix from mergeThis
                intoThis.set(intoThisIndex, mergeThisFix);
            } else {
                intoThis.add(intoThisIndex, mergeThisFix);
                if (indexOfFirstShownFix >= intoThisIndex) {
                    indexOfFirstShownFix++;
                }
                if (indexOfLastShownFix >= intoThisIndex) {
                    indexOfLastShownFix++;
                }
                if (tail != null && intoThisIndex >= indexOfFirstShownFix && intoThisIndex <= indexOfLastShownFix) {
                    tail.insertVertex(intoThisIndex - indexOfFirstShownFix,
                            LatLng.newInstance(mergeThisFix.position.latDeg, mergeThisFix.position.lngDeg));
                }
            }
            intoThisIndex++;
        }
        // invariant: for one CompetitorDTO, either both of firstShownFix and lastShownFix have an entry for that key,
        // or both don't
        if (indexOfFirstShownFix != -1) {
            firstShownFix.put(competitorDTO, indexOfFirstShownFix);
        }
        if (indexOfLastShownFix != -1) {
            lastShownFix.put(competitorDTO, indexOfLastShownFix);
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
    protected GPSFixDTO getBoatFix(CompetitorDTO competitorDTO, Date date) {
        GPSFixDTO result = null;
        List<GPSFixDTO> competitorFixes = fixes.get(competitorDTO);
        if (competitorFixes != null && !competitorFixes.isEmpty()) {
            int i = Collections.binarySearch(competitorFixes, new GPSFixDTO(date, null, null, null, null, null, false), new Comparator<GPSFixDTO>() {
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
                    if (date.getTime() - competitorFixes.get(i-1).timepoint.getTime() < competitorFixes.get(i).timepoint.getTime() - date.getTime()) {
                        result = competitorFixes.get(i-1);
                    } else {
                        result = competitorFixes.get(i);
                    }
                }
            } else {
                result = competitorFixes.get(i);
            }
        }
        return result;
    }

    /**
     * If the tail starts before <code>from</code>, removes leading vertices from <code>tail</code> that are before
     * <code>from</code>. This is determined by using the {@link #firstShownFix} index which tells us where in
     * {@link #fixes} we find the sequence of fixes currently represented in the tail.
     * <p>
     * 
     * If the tail starts after <code>from</code>, vertices for those {@link #fixes} for <code>competitorDTO</code> at
     * or after time point <code>from</code> and before the time point of the first fix displayed so far in the tail and
     * before <code>to</code> are prepended to the tail.
     * <p>
     * 
     * Now to the end of the tail: if the existing tail's end exceeds <code>to</code>, the vertices in excess are
     * removed (aided by {@link #lastShownFix}). Otherwise, for the competitor's fixes starting at the tail's end up to
     * <code>to</code> are appended to the tail.
     * <p>
     * 
     * When this method returns, {@link #firstShownFix} and {@link #lastShownFix} have been updated accordingly.
     */
    protected void updateTail(Polyline tail, CompetitorDTO competitorDTO, Date from, Date to) {
        int vertexCount = tail.getVertexCount();
        final List<GPSFixDTO> fixesForCompetitor = fixes.get(competitorDTO);
        int indexOfFirstShownFix = firstShownFix.get(competitorDTO) == null ? -1 : firstShownFix.get(competitorDTO);
        while (indexOfFirstShownFix != -1 && vertexCount > 0
                && fixesForCompetitor.get(indexOfFirstShownFix).timepoint.before(from)) {
            tail.deleteVertex(0);
            vertexCount--;
            indexOfFirstShownFix++;
        }
        // now the polyline contains no more vertices representing fixes before "from";
        // go back in time starting at indexOfFirstShownFix while the fixes are still at or after "from"
        // and insert corresponding vertices into the polyline
        while (indexOfFirstShownFix > 0 && !fixesForCompetitor.get(indexOfFirstShownFix - 1).timepoint.before(from)) {
            indexOfFirstShownFix--;
            GPSFixDTO fix = fixesForCompetitor.get(indexOfFirstShownFix);
            tail.insertVertex(0, LatLng.newInstance(fix.position.latDeg, fix.position.lngDeg));
            vertexCount++;
        }
        // now adjust the polylines tail: remove excess vertices that are after "to"
        int indexOfLastShownFix = lastShownFix.get(competitorDTO) == null ? -1 : lastShownFix.get(competitorDTO);
        while (indexOfLastShownFix != -1 && vertexCount > 0
                && fixesForCompetitor.get(indexOfLastShownFix).timepoint.after(to)) {
            tail.deleteVertex(--vertexCount);
            indexOfLastShownFix--;
        }
        // now the polyline contains no more vertices representing fixes after "to";
        // go forward in time starting at indexOfLastShownFix while the fixes are still at or before "to"
        // and insert corresponding vertices into the polyline
        while (indexOfLastShownFix < fixesForCompetitor.size() - 1
                && !fixesForCompetitor.get(indexOfLastShownFix + 1).timepoint.after(to)) {
            indexOfLastShownFix++;
            GPSFixDTO fix = fixesForCompetitor.get(indexOfLastShownFix);
            tail.insertVertex(vertexCount++, LatLng.newInstance(fix.position.latDeg, fix.position.lngDeg));
        }
        firstShownFix.put(competitorDTO, indexOfFirstShownFix);
        lastShownFix.put(competitorDTO, indexOfLastShownFix);
    }

    public RaceMapSettings getSettings() {
        return settings;
    }

    @Override
    public void addedToSelection(CompetitorDTO competitor) {
        if (settings.isShowOnlySelectedCompetitors()) {
            if (Util.size(competitorSelection.getSelectedCompetitors()) == 1) {
                // first competitors selected; remove all others from map
                Iterator<Map.Entry<CompetitorDTO, BoatCanvasOverlay>> i = boatCanvasOverlays.entrySet().iterator();
                while (i.hasNext()) {
                    Entry<CompetitorDTO, BoatCanvasOverlay> next = i.next();
                    if (!next.getKey().equals(competitor)) {
                        BoatCanvasOverlay value = next.getValue();
                        map.removeOverlay(value);
                        removeTail(next.getKey());
                        i.remove(); // only this way a ConcurrentModificationException while looping can be avoided
                    }
                }
                showCompetitorInfoOnMap(timer.getTime(), competitorSelection.getSelectedCompetitors());
            } else {
                // adding a single competitor; may need to re-load data, so refresh:
                timeChanged(timer.getTime());
            }
        } else {
            // only change highlighting
            BoatCanvasOverlay boatCanvas = boatCanvasOverlays.get(competitor);
            if (boatCanvas != null) {
                boatCanvas.setSelected(displayHighlighted(competitor));
                boatCanvas.redraw(true);
                showCompetitorInfoOnMap(timer.getTime(), competitorSelection.getSelectedCompetitors());
            } else {
                // seems like an internal error not to find the lowlighted marker; but maybe the
                // competitor was added late to the race;
                // data for newly selected competitor supposedly missing; refresh
                timeChanged(timer.getTime());
            }
        }
        //Trigger auto-zoom if needed
        RaceMapZoomSettings zoomSettings = settings.getZoomSettings();
        if (!zoomSettings.containsZoomType(ZoomTypes.NONE) && zoomSettings.isZoomToSelectedCompetitors()) {
            zoomMapToNewBounds(zoomSettings.getNewBounds(this));
        }
    }
    
    /**
     * Consistently removes the <code>competitor</code>'s tail from {@link #tails} and from the map, and the corresponding position
     * data from {@link #firstShownFix} and {@link #lastShownFix}.
     */
    private void removeTail(CompetitorDTO competitor) {
        Polyline removed = tails.remove(competitor);
        if (removed != null) {
            map.removeOverlay(removed);
        }
        firstShownFix.remove(competitor);
        lastShownFix.remove(competitor);
    }

    @Override
    public void removedFromSelection(CompetitorDTO competitor) {
        if (isShowAnyHelperLines()) {
            // helper lines depend on which competitor is visible, because the *visible* leader is used for
            // deciding which helper lines to show:
            timeChanged(timer.getTime());
        } else {
            // try a more incremental update otherwise
            if (settings.isShowOnlySelectedCompetitors()) {
                // if selection is now empty, show all competitors
                if (Util.isEmpty(competitorSelection.getSelectedCompetitors())) {
                    timeChanged(timer.getTime());
                } else {
                    // otherwise remove only deselected competitor's boat images and tail
                    BoatCanvasOverlay removed = boatCanvasOverlays.remove(competitor);
                    if (removed != null) {
                        map.removeOverlay(removed);
                    }
                    removeTail(competitor);
                    showCompetitorInfoOnMap(timer.getTime(), competitorSelection.getSelectedCompetitors());
                }
            } else {
                // "lowlight" currently selected competitor
                BoatCanvasOverlay boatCanvas = boatCanvasOverlays.get(competitor);
                if (boatCanvas != null) {
                    boatCanvas.setSelected(displayHighlighted(competitor));
                    boatCanvas.redraw(true);
                }
                showCompetitorInfoOnMap(timer.getTime(), competitorSelection.getSelectedCompetitors());
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
        return new RaceMapSettingsDialogComponent(settings, stringMessages);
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
                zoomMapToNewBounds(settings.getZoomSettings().getNewBounds(this));
            }
        }
        if (!newSettings.getHelpLinesSettings().equals(settings.getHelpLinesSettings())) {
            settings.setHelpLinesSettings(newSettings.getHelpLinesSettings());
            requiredRedraw = true;
        }
        if (newSettings.isShowAllCompetitors() != settings.isShowAllCompetitors() ||
                newSettings.getMaxVisibleCompetitorsCount() != settings.getMaxVisibleCompetitorsCount()) {
            settings.setShowAllCompetitors(newSettings.isShowAllCompetitors());
            settings.setMaxVisibleCompetitorsCount(newSettings.getMaxVisibleCompetitorsCount());
            requiredRedraw = true;
        }
        if (requiredRedraw) {
            redraw();
        }
    }
    
    public static class BoatsBoundsCalculator extends LatLngBoundsCalculatorForSelected {

        @Override
        public LatLngBounds calculateNewBounds(RaceMap forMap) {
            LatLngBounds newBounds = null;
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
                    LatLng competitorLatLng = competitorPosition != null ? LatLng.newInstance(competitorPosition.latDeg,
                            competitorPosition.lngDeg) : null;
                    LatLngBounds bounds = competitorLatLng != null ? LatLngBounds.newInstance(competitorLatLng,
                            competitorLatLng) : null;
                    if (bounds != null) {
                        if (newBounds == null) {
                            newBounds = bounds;
                        } else {
                            newBounds.extend(bounds.getNorthEast());
                            newBounds.extend(bounds.getSouthWest());
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
        public LatLngBounds calculateNewBounds(RaceMap forMap) {
            LatLngBounds newBounds = null;
            Iterable<CompetitorDTO> competitors = isZoomOnlyToSelectedCompetitors(forMap) ? forMap.competitorSelection.getSelectedCompetitors() : forMap.getCompetitorsToShow();
            for (CompetitorDTO competitor : competitors) {
                Polyline tail = forMap.tails.get(competitor);
                LatLngBounds bounds = tail != null ? tail.getBounds() : null;
                if (bounds != null) {
                    if (newBounds == null) {
                        newBounds = bounds;
                    } else {
                        newBounds.extend(bounds.getNorthEast());
                        newBounds.extend(bounds.getSouthWest());
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
                    LatLng markLatLng = LatLng.newInstance(markDTO.position.latDeg, markDTO.position.lngDeg);
                    LatLngBounds bounds = LatLngBounds.newInstance(markLatLng, markLatLng);
                    if (newBounds == null) {
                        newBounds = bounds;
                    } else {
                        newBounds.extend(markLatLng);
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
                for (WindSensorOverlay windSensorOverlay: marksToZoom) {
                    LatLng windSensorLatLng = windSensorOverlay.getLatLngPosition();
                    if(windSensorLatLng != null) {
                        LatLngBounds bounds = LatLngBounds.newInstance(windSensorLatLng, windSensorLatLng);
                        if (newBounds == null) {
                            newBounds = bounds;
                        } else {
                            newBounds.extend(windSensorLatLng);
                        }
                    }
                }
            }
            return newBounds;
        }
    }

    @Override
    public void initializeData() {
        loadMapsAPI();
    }

    @Override
    public boolean isDataInitialized() {
        return dataInitialized;
    }

    @Override
    public void onResize() {
        if (map != null) {
            map.checkResize();
            zoomMapToNewBounds(settings.getZoomSettings().getNewBounds(RaceMap.this));
        }
    }

    @Override
    public void competitorsListChanged(Iterable<CompetitorDTO> competitors) {
        timeChanged(timer.getTime());
    }
}
