package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
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
import com.google.gwt.maps.client.event.MarkerMouseOutHandler;
import com.google.gwt.maps.client.event.MarkerMouseOverHandler;
import com.google.gwt.maps.client.event.PolylineClickHandler;
import com.google.gwt.maps.client.event.PolylineMouseOutHandler;
import com.google.gwt.maps.client.event.PolylineMouseOverHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.maps.client.overlay.PolylineOptions;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.actions.GetRaceMapDataAction;
import com.sap.sailing.gwt.ui.actions.GetWindInfoAction;
import com.sap.sailing.gwt.ui.adminconsole.RaceMapZoomSettings.ZoomTypes;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RequiresDataInitialization;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.Timer.PlayStates;
import com.sap.sailing.gwt.ui.client.WindSourceTypeFormatter;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.ManeuverDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;
import com.sap.sailing.gwt.ui.shared.RaceMapDataDTO;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class RaceMap extends AbsolutePanel implements TimeListener, CompetitorSelectionChangeListener, RaceSelectionChangeListener,
        Component<RaceMapSettings>, RequiresDataInitialization, RequiresResize {
    protected MapWidget map;

    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;

    /**
     * Tails of competitors currently displayed as overlays on the map.
     */
    private final Map<CompetitorDTO, Polyline> tails;

    /**
     * Key set is equal to that of {@link #tails} and tells what the index in in {@link #fixes} of the first fix shown
     * in {@link #tails} is .
     */
    private final Map<CompetitorDTO, Integer> firstShownFix;

    /**
     * Key set is equal to that of {@link #tails} and tells what the index in in {@link #fixes} of the last fix shown in
     * {@link #tails} is .
     */
    private  final Map<CompetitorDTO, Integer> lastShownFix;

    /**
     * Fixes of each competitors tail. If a list is contained for a competitor, the list contains a timely "contiguous"
     * list of fixes for the competitor. This means the server has no more data for the time interval covered, unless
     * the last fix was {@link GPSFixDTO#extrapolated obtained by extrapolation}.
     */
    private final Map<CompetitorDTO, List<GPSFixDTO>> fixes;

    /**
     * Markers used as boat display on the map
     */
    private final Map<CompetitorDTO, Marker> boatMarkers;

    /**
     * Markers used to display wind sensors
     */
    private final Map<WindSource, Marker> windSensorMarkers;
    
    /**
     * For each value in {@link #windSensorMarkers} holds the corresponding wind data displayed by the marker.
     * This can then be used to dynamically produce a title / tooltip.
     */
    private final Map<Marker, WindDTO> windForMarkers;

    private final Map<MarkDTO, Marker> buoyMarkers;

    /**
     * markers displayed in response to
     * {@link SailingServiceAsync#getDouglasPoints(String, String, Map, Map, double, AsyncCallback)}
     */
    protected Set<Marker> douglasMarkers;

    /**
     * markers displayed in response to
     * {@link SailingServiceAsync#getDouglasPoints(String, String, Map, Map, double, AsyncCallback)}
     */
    protected Set<Marker> maneuverMarkers;

    protected Map<CompetitorDTO, List<ManeuverDTO>> lastManeuverResult;

    protected Map<CompetitorDTO, List<GPSFixDTO>> lastDouglasPeuckerResult;
    
    private LatLng lastMousePosition;

    private CompetitorSelectionProvider competitorSelection;

    private List<RaceIdentifier> selectedRaces;

    /**
     * Used to check if the first initial zoom to the buoy markers was already done.
     */
    private boolean mapFirstZoomDone = false;

    // key for domains web4sap.com, sapsailing.com and sapcoe-app01.pironet-ndh.com
    private final String mapsAPIKey = "AIzaSyD1Se4tIkt-wglccbco3S7twaHiG20hR9E";

    private final Timer timer;

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

    private RaceMapResources imageResources; 

    private final RaceMapSettings settings;
    
    private final StringMessages stringMessages;
    
    private boolean dataInitialized;
    
    private Date lastTimeChangeBeforeInitialization;

    /**
     * The last quick ranks received from a call to {@link SailingServiceAsync#getQuickRanks(RaceIdentifier, Date, AsyncCallback)} upon
     * the last {@link #timeChanged(Date)} event. Therefore, the ranks listed here correspond to the {@link #timer}'s time.
     */
    private List<QuickRankDTO> quickRanks;

    private final CombinedWindPanel windPanel;

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
        imageResources = new RaceMapResources();
        tails = new HashMap<CompetitorDTO, Polyline>();
        firstShownFix = new HashMap<CompetitorDTO, Integer>();
        lastShownFix = new HashMap<CompetitorDTO, Integer>();
        buoyMarkers = new HashMap<MarkDTO, Marker>();
        boatMarkers = new HashMap<CompetitorDTO, Marker>();
        windSensorMarkers = new HashMap<WindSource, Marker>();
        windForMarkers = new HashMap<Marker, WindDTO>();
        fixes = new HashMap<CompetitorDTO, List<GPSFixDTO>>();
        this.competitorSelection = competitorSelection;
        competitorSelection.addCompetitorSelectionChangeListener(this);
        settings = new RaceMapSettings();
        lastTimeChangeBeforeInitialization = null;
        dataInitialized = false;
        initializeData();
        
        windPanel = new CombinedWindPanel(sailingService, asyncActionsExecutor, errorReporter, stringMessages, timer);
        windPanel.setVisible(false);
    }

    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        // the calculation is based on the Haversine formula
        double earthRadius = 6371; // in km
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;

        return dist;
    }
    
    private void loadMapsAPI() {
        Maps.loadMapsApi(mapsAPIKey, "2", false, new Runnable() {
            public void run() {
                map = new MapWidget();
                imageResources.setMap(map);
                map.addControl(new LargeMapControl3D(), new ControlPosition(ControlAnchor.TOP_RIGHT, /* offsetX */ 0, /* offsetY */ 30));
                map.addControl(new MenuMapTypeControl());
                map.addControl(new ScaleControl(), new ControlPosition(ControlAnchor.BOTTOM_RIGHT, /* offsetX */ 10, /* offsetY */ 20));
                // Add the map to the HTML host page
                map.setScrollWheelZoomEnabled(true);
                map.setContinuousZoom(true);
                RaceMap.this.add(map, 0, 0);
                RaceMap.this.add(windPanel, 10, 10);

                map.setSize("100%", "100%");
                map.addMapZoomEndHandler(new MapZoomEndHandler() {
                    @Override
                    public void onZoomEnd(MapZoomEndEvent event) {
                        map.checkResizeAndCenter();
                        final List<RaceMapZoomSettings.ZoomTypes> emptyList = Collections.emptyList();
                        getSettings().getZoomSettings().setTypesToConsiderOnZoom(emptyList);
                        Set<CompetitorDTO> competitorDTOsOfUnusedMarkers = new HashSet<CompetitorDTO>(boatMarkers.keySet());
                        for (CompetitorDTO competitorDTO : getCompetitorsToShow()) {
                                boolean usedExistingMarker = updateBoatMarkerForCompetitor(competitorDTO);
                                if (usedExistingMarker) {
                                    competitorDTOsOfUnusedMarkers.remove(competitorDTO);
                                }
                        }
                        for (CompetitorDTO unusedMarkerCompetitorDTO : competitorDTOsOfUnusedMarkers) {
                            map.removeOverlay(boatMarkers.remove(unusedMarkerCompetitorDTO));
                        }
                    }
                });
                map.addMapDragEndHandler(new MapDragEndHandler() {
                    @Override
                    public void onDragEnd(MapDragEndEvent event) {
                        final List<RaceMapZoomSettings.ZoomTypes> emptyList = Collections.emptyList();
                        getSettings().getZoomSettings().setTypesToConsiderOnZoom(emptyList);
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
    public void onRaceSelectionChange(List<RaceIdentifier> selectedRaces) {
        mapFirstZoomDone = false;
        // TODO bug 494: reset zoom settings to user preferences
        this.selectedRaces = selectedRaces;
        windPanel.onRaceSelectionChange(selectedRaces);
    }

    @Override
    public void timeChanged(final Date date) {
        if (date != null) {
            if (selectedRaces != null && !selectedRaces.isEmpty()) {
                RaceIdentifier race = selectedRaces.get(selectedRaces.size() - 1);
                
                if (race != null) {
                    final Triple<Map<CompetitorDTO, Date>, Map<CompetitorDTO, Date>, Map<CompetitorDTO, Boolean>> fromAndToAndOverlap = 
                            computeFromAndTo(date, getCompetitorsToShow());
                    final int requestID = ++boatPositionRequestIDCounter;

                    GetRaceMapDataAction getRaceMapDataAction = new GetRaceMapDataAction(sailingService, race, date, 
                            fromAndToAndOverlap.getA(), fromAndToAndOverlap.getB(), true, new AsyncCallback<RaceMapDataDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError("Error obtaining racemap data: " + caught.getMessage(), timer.getPlayMode() == PlayModes.Live);
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
                                    Date from = new Date(date.getTime() - settings.getTailLengthInMilliseconds());
                                    updateFixes(boatData, fromAndToAndOverlap.getC());
                                    showBoatsOnMap(from, date, getCompetitorsToShow());
                                    if (douglasMarkers != null) {
                                        removeAllMarkDouglasPeuckerpoints();
                                    }
                                    if (maneuverMarkers != null) {
                                        removeAllManeuverMarkers();
                                    }
                                    // Do mark specific actions
                                    List<MarkDTO> markData = raceMapDataDTO.markPositions;
                                    showMarksOnMap(markData);
                                    // Rezoom the map
                                    // TODO make this a loop across the LatLongBoundsCalculators, pulling them from a collection updated in updateSettings
                                    if (!getSettings().getZoomSettings().contains(ZoomTypes.NONE)) { // Auto zoom if setting is not manual
                                        zoomMapToNewBounds(getSettings().getZoomSettings().getNewBounds(RaceMap.this));
                                        mapFirstZoomDone = true;
                                    } else if (!mapFirstZoomDone) { // Zoom once to the buoys
                                        zoomMapToNewBounds(new BuoysBoundsCalculater().calculateNewBounds(RaceMap.this));
                                        mapFirstZoomDone = true;
                                        /*
                                         * Reset the mapZoomedOrPannedSinceLastRaceSelection: In spite of the fact that
                                         * the map was just zoomed to the bounds of the buoys, it was not a zoom or pan
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
                    GetWindInfoAction getWindInfoAction = new GetWindInfoAction(sailingService, race, date, 1000L, 1, windSourceTypeNames,
                        new AsyncCallback<WindInfoForRaceDTO>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError("Error obtaining wind information: " + caught.getMessage(), timer.getPlayMode() == PlayModes.Live);
                                }

                                @Override
                                public void onSuccess(WindInfoForRaceDTO windInfo) {
                                    List<Pair<WindSource, WindDTO>> windSourcesToShow = new ArrayList<Pair<WindSource, WindDTO>>();
                                    if(windInfo != null) {
                                        for(WindSource windSource: windInfo.windTrackInfoByWindSource.keySet()) {
                                            WindTrackInfoDTO windTrackInfoDTO = windInfo.windTrackInfoByWindSource.get(windSource);
                                            switch (windSource.getType()) {
                                                case EXPEDITION:
                                                {
                                                    if(windTrackInfoDTO.windFixes.size() > 0) {
                                                        WindDTO windDTO = windTrackInfoDTO.windFixes.get(0);
                                                        windSourcesToShow.add(new Pair<WindSource, WindDTO>(windSource, windDTO));
                                                    }
                                                }
                                                break;
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
        Date tailstart = new Date(upTo.getTime() - settings.getTailLengthInMilliseconds());
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

    protected void showMarksOnMap(List<MarkDTO> result) {
        if (map != null) {
            Set<MarkDTO> toRemove = new HashSet<MarkDTO>(buoyMarkers.keySet());
            for (MarkDTO markDTO : result) {
                Marker buoyMarker = buoyMarkers.get(markDTO);
                if (buoyMarker == null) {
                    buoyMarker = createBuoyMarker(markDTO);
                    buoyMarkers.put(markDTO, buoyMarker);
                    map.addOverlay(buoyMarker);
                } else {
                    buoyMarker.setLatLng(LatLng.newInstance(markDTO.position.latDeg, markDTO.position.lngDeg));
                    toRemove.remove(markDTO);
                }
            }
            for (MarkDTO toRemoveMarkDTO : toRemove) {
                Marker marker = buoyMarkers.remove(toRemoveMarkDTO);
                map.removeOverlay(marker);
            }
        }
    }

    protected void showWindSensorsOnMap(List<Pair<WindSource, WindDTO>> windSensorsList) {
        if (map != null) {
            Set<WindSource> toRemoveWindSources = new HashSet<WindSource>(windSensorMarkers.keySet());
            for (Pair<WindSource, WindDTO> windSourcePair : windSensorsList) {
                WindSource windSource = windSourcePair.getA(); 
                WindDTO windDTO = windSourcePair.getB();
                Marker windSensorMarker = windSensorMarkers.get(windSource);
                if (windSensorMarker == null) {
                    windSensorMarker = createWindSensorMarker(windSource, windDTO);
                    windSensorMarkers.put(windSource, windSensorMarker);
                    map.addOverlay(windSensorMarker);
                } else {
                    double rotationDegOfWindSymbol = windDTO.dampenedTrueWindBearingDeg;
                    ImageTransformer transformer = imageResources.expeditionWindIconTransformer;
                    String transformedImageURL = transformer.getTransformedImageURL(rotationDegOfWindSymbol, 1.0);
                    windSensorMarker.setImage(transformedImageURL);
                    windSensorMarker.setLatLng(LatLng.newInstance(windDTO.position.latDeg, windDTO.position.lngDeg));
                    toRemoveWindSources.remove(windSource);
                }
                windForMarkers.put(windSensorMarker, windDTO);
            }
            for (WindSource toRemoveWindSource : toRemoveWindSources) {
                Marker marker = windSensorMarkers.remove(toRemoveWindSource);
                map.removeOverlay(marker);
                windForMarkers.remove(marker);
            }
        }
    }

    /**
     * @param from
     *            time point for first fix to show in tails
     * @param to
     *            time point for last fix to show in tails
     */
    protected void showBoatsOnMap(final Date from, final Date to, final Iterable<CompetitorDTO> competitorsToShow) {
        if (map != null) {
            Set<CompetitorDTO> competitorDTOsOfUnusedTails = new HashSet<CompetitorDTO>(tails.keySet());
            Set<CompetitorDTO> competitorDTOsOfUnusedMarkers = new HashSet<CompetitorDTO>(boatMarkers.keySet());
            for (CompetitorDTO competitorDTO : competitorsToShow) {
                if (fixes.containsKey(competitorDTO)) {
                    Polyline tail = tails.get(competitorDTO);
                    if (tail == null) {
                        tail = createTailAndUpdateIndices(competitorDTO, from, to);
                        map.addOverlay(tail);
                    } else {
                        updateTail(tail, competitorDTO, from, to);
                        competitorDTOsOfUnusedTails.remove(competitorDTO);
                    }
                    boolean usedExistingMarker = updateBoatMarkerForCompetitor(competitorDTO);
                    if (usedExistingMarker) {
                        competitorDTOsOfUnusedMarkers.remove(competitorDTO);
                    }
                }
            }
            for (CompetitorDTO unusedMarkerCompetitorDTO : competitorDTOsOfUnusedMarkers) {
                map.removeOverlay(boatMarkers.remove(unusedMarkerCompetitorDTO));
            }
            for (CompetitorDTO unusedTailCompetitorDTO : competitorDTOsOfUnusedTails) {
                map.removeOverlay(tails.remove(unusedTailCompetitorDTO));
            }
        }
    }

    private void zoomMapToNewBounds(LatLngBounds newBounds) {
        if (newBounds != null) {
            List<ZoomTypes> oldZoomSettings = getSettings().getZoomSettings().getTypesToConsiderOnZoom();
            map.setCenter(newBounds.getCenter());
            map.setZoomLevel(map.getBoundsZoomLevel(newBounds));
            getSettings().getZoomSettings().setTypesToConsiderOnZoom(oldZoomSettings);
        }
    }
    
    private boolean updateBoatMarkerForCompetitor(CompetitorDTO competitorDTO) {
        boolean usedExistingMarker = false;
        if (lastShownFix.containsKey(competitorDTO) && lastShownFix.get(competitorDTO) != -1) {
            GPSFixDTO lastPos = getBoatFix(competitorDTO);
            Marker boatMarker = boatMarkers.get(competitorDTO);
            if (boatMarker == null) {
                boatMarker = createBoatMarker(competitorDTO, displayHighlighted(competitorDTO));
                map.addOverlay(boatMarker);
                boatMarkers.put(competitorDTO, boatMarker);
            } else {
                usedExistingMarker = true;
                // check if anchors match; re-use marker with setImage only if anchors match
                ImageTransformer transformer = imageResources.getBoatImageTransformer(lastPos,
                        competitorSelection.isSelected(competitorDTO));
                Point newAnchor = transformer.getAnchor(imageResources.getRealBoatSizeScaleFactor(transformer.getImageSize()));
                Point oldAnchor = boatMarker.getIcon().getIconAnchor();
                if (oldAnchor.getX() == newAnchor.getX() && oldAnchor.getY() == newAnchor.getY()) {
                    boatMarker.setLatLng(LatLng.newInstance(lastPos.position.latDeg,
                            lastPos.position.lngDeg));
                    boatMarker.setImage(imageResources.getBoatImageURL(lastPos,
                            displayHighlighted(competitorDTO)));
                } else {
                    // anchors don't match; replace marker
                    map.removeOverlay(boatMarker);
                    boatMarker = createBoatMarker(competitorDTO, displayHighlighted(competitorDTO));
                    map.addOverlay(boatMarker);
                    boatMarkers.put(competitorDTO, boatMarker);
                }
            }
        }
        return usedExistingMarker;
    }
    
    private boolean displayHighlighted(CompetitorDTO competitorDTO) {
        return !getSettings().isShowOnlySelectedCompetitors() && competitorSelection.isSelected(competitorDTO);
    }

    protected Marker createBuoyMarker(final MarkDTO markDTO) {
        MarkerOptions options = MarkerOptions.newInstance();
        if (imageResources.buoyIcon != null) {
            options.setIcon(imageResources.buoyIcon);
        }
        options.setTitle(markDTO.name);
        final Marker buoyMarker = new Marker(LatLng.newInstance(markDTO.position.latDeg, markDTO.position.lngDeg),
                options);
        buoyMarker.addMarkerClickHandler(new MarkerClickHandler() {
            @Override
            public void onClick(MarkerClickEvent event) {
                LatLng latlng = buoyMarker.getLatLng();
                showMarkInfoWindow(markDTO, latlng);
            }
        });
        return buoyMarker;
    }

    protected Marker createBoatMarker(final CompetitorDTO competitorDTO, boolean highlighted) {
        GPSFixDTO boatFix = getBoatFix(competitorDTO);
        double latDeg = boatFix.position.latDeg;
        double lngDeg = boatFix.position.lngDeg;
        MarkerOptions options = MarkerOptions.newInstance();
        Icon icon = imageResources.getBoatImageIcon(boatFix, highlighted);
        options.setIcon(icon);
        options.setTitle(competitorDTO.name);
        final Marker boatMarker = new Marker(LatLng.newInstance(latDeg, lngDeg), options);
        boatMarker.addMarkerClickHandler(new MarkerClickHandler() {
            @Override
            public void onClick(MarkerClickEvent event) {
                LatLng latlng = boatMarker.getLatLng();
                showCompetitorInfoWindow(competitorDTO, latlng);
            }
        });
        boatMarker.addMarkerMouseOverHandler(new MarkerMouseOverHandler() {
            @Override
            public void onMouseOver(MarkerMouseOverEvent event) {
                map.setTitle(competitorDTO.name);
            }
        });
        boatMarker.addMarkerMouseOutHandler(new MarkerMouseOutHandler() {
            @Override
            public void onMouseOut(MarkerMouseOutEvent event) {
                map.setTitle("");
            }
        });
        return boatMarker;
    }

    protected Marker createWindSensorMarker(final WindSource windSource, final WindDTO windDTO) {
        double latDeg = windDTO.position.latDeg;
        double lngDeg = windDTO.position.lngDeg;
        MarkerOptions options = MarkerOptions.newInstance();

        double windFromDeg = windDTO.dampenedTrueWindFromDeg;
        ImageTransformer transformer = imageResources.expeditionWindIconTransformer;

        double rotationDegOfWindSymbol = 180.0 + windFromDeg;
        if(rotationDegOfWindSymbol >= 360.0)
            rotationDegOfWindSymbol = rotationDegOfWindSymbol - 360; 
        String transformedImageURL = transformer.getTransformedImageURL(rotationDegOfWindSymbol, 1.0);
        
        Icon icon = Icon.newInstance(transformedImageURL);
        icon.setIconAnchor(Point.newInstance(7, 13));
        options.setIcon(icon);
        final Marker windSensorMarker = new Marker(LatLng.newInstance(latDeg, lngDeg), options);
        windSensorMarker.addMarkerClickHandler(new MarkerClickHandler() {
            @Override
            public void onClick(MarkerClickEvent event) {
                LatLng latlng = windSensorMarker.getLatLng();
                showWindSensorInfoWindow(windSource, windDTO, latlng);
            }
        });
        windSensorMarker.addMarkerMouseOverHandler(new MarkerMouseOverHandler() {
            @Override
            public void onMouseOver(MarkerMouseOverEvent event) {
                WindDTO windForMarker = windForMarkers.get(windSensorMarker);
                if (windForMarker != null) {
                    String title = stringMessages.wind() + " "
                            + Math.round(windForMarker.dampenedTrueWindFromDeg) + " " + stringMessages.degreesShort()+ " ("
                                    + WindSourceTypeFormatter.format(windSource, stringMessages) + ")";
                    map.setTitle(title);
                }
            }
        });
        windSensorMarker.addMarkerMouseOutHandler(new MarkerMouseOutHandler() {
            @Override
            public void onMouseOut(MarkerMouseOutEvent event) {
                map.setTitle("");
            }
        });
        return windSensorMarker;
    }

    private void showMarkInfoWindow(MarkDTO markDTO, LatLng latlng) {
        map.getInfoWindow().open(latlng, new InfoWindowContent(getInfoWindowContent(markDTO)));
    }

    private void showCompetitorInfoWindow(final CompetitorDTO competitorDTO, LatLng where) {
        GPSFixDTO latestFixForCompetitor = getBoatFix(competitorDTO);
        map.getInfoWindow().open(where,
                new InfoWindowContent(getInfoWindowContent(competitorDTO, latestFixForCompetitor)));
    }

    private String formatPosition(double lat, double lng) {
        NumberFormat numberFormat = NumberFormat.getFormat("0.0");
        String result = stringMessages.position() + ": " + numberFormat.format(lat) + " lat, " + numberFormat.format(lng) + " lng";
        return result;
    }
    
    private void showWindSensorInfoWindow(final WindSource windSource, final WindDTO windDTO, LatLng where) {
        map.getInfoWindow().open(where,
                new InfoWindowContent(getInfoWindowContent(windSource, windDTO)));
    }

    private Widget getInfoWindowContent(MarkDTO markDTO) {
        VerticalPanel result = new VerticalPanel();
        result.add(new Label("Mark: " + markDTO.name));
        result.add(new Label(formatPosition(markDTO.position.latDeg, markDTO.position.lngDeg)));
        return result;
    }

    private Widget getInfoWindowContent(WindSource windSource, WindDTO windDTO) {
        NumberFormat numberFormat = NumberFormat.getFormat("0.0");
        VerticalPanel result = new VerticalPanel();
        result.add(new Label(stringMessages.windSource() + ": " + WindSourceTypeFormatter.format(windSource, stringMessages)));
        result.add(new Label(stringMessages.wind() + ": " +  Math.round(windDTO.dampenedTrueWindFromDeg) + " " + stringMessages.degreesShort()));
        result.add(new Label(stringMessages.windSpeed() + ": " + numberFormat.format(windDTO.dampenedTrueWindSpeedInKnots)));
        result.add(new Label(formatPosition(windDTO.position.latDeg, windDTO.position.lngDeg)));
        return result;
    }

    private Widget getInfoWindowContent(CompetitorDTO competitorDTO, GPSFixDTO lastFix) {
        final VerticalPanel result = new VerticalPanel();
        result.add(new Label(stringMessages.competitor() + ": " + competitorDTO.name));
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
            result.add(new Label(stringMessages.rank() + ": " + rank));
        }
        result.add(new Label(stringMessages.speed() + ": "
                + NumberFormat.getDecimalFormat().format(lastFix.speedWithBearing.speedInKnots) + " "+stringMessages.averageSpeedInKnotsUnit()));
        result.add(new Label(stringMessages.bearing() + ": "+ (int) lastFix.speedWithBearing.bearingInDegrees + " "+stringMessages.degreesShort()));
        //TODO Introduce user role dependent view (Spectator, Admin). Comments underneath are necessary for other views
//      result.add(new Label("" + lastFix.position));
//      result.add(new Label("Tack: " + lastFix.tack.name()));
        if (!selectedRaces.isEmpty()) {
            RaceIdentifier race = selectedRaces.get(selectedRaces.size() - 1);
            if (race != null) {
                Map<CompetitorDTO, Date> from = new HashMap<CompetitorDTO, Date>();
                from.put(competitorDTO, fixes.get(competitorDTO).get(firstShownFix.get(competitorDTO)).timepoint);
                Map<CompetitorDTO, Date> to = new HashMap<CompetitorDTO, Date>();
                to.put(competitorDTO, getBoatFix(competitorDTO).timepoint);
                sailingService.getDouglasPoints(race, from, to, 3,
                        new AsyncCallback<Map<CompetitorDTO, List<GPSFixDTO>>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error obtaining douglas positions: " + caught.getMessage());
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
                                errorReporter.reportError("Error obtaining maneuvers: " + caught.getMessage());
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
        return result;
    }

    private Iterable<CompetitorDTO> getCompetitorsToShow() {
        Iterable<CompetitorDTO> result;
        Iterable<CompetitorDTO> selection = competitorSelection.getSelectedCompetitors();
        if (!getSettings().isShowOnlySelectedCompetitors() || Util.isEmpty(selection)) {
            result = competitorSelection.getAllCompetitors();
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
                map.setTitle(competitorDTO.name);
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
                    if (getSettings().isShowManeuverType(maneuver.type)) {
                        LatLng latLng = LatLng.newInstance(maneuver.position.latDeg, maneuver.position.lngDeg);
                        MarkerOptions options = MarkerOptions.newInstance();
                        //TODO Introduce user role dependent view (Spectator, Admin)
                        SpeedWithBearingDTO before = maneuver.speedWithBearingBefore;
                        SpeedWithBearingDTO after = maneuver.speedWithBearingAfter;
                        
                        String timeAndManeuver = DateTimeFormat.getFormat(PredefinedFormat.TIME_FULL).format(maneuver.timepoint)
                                + ": " + maneuver.type.name();
                        String directionChange = stringMessages.directionChange() + ": "
                                + ((int) maneuver.directionChangeInDegrees) + " "+stringMessages.degreesShort()+" ("
                                + ((int) before.bearingInDegrees) + " deg -> " + ((int) after.bearingInDegrees) + " "+stringMessages.degreesShort()+")";
                        String speedChange = stringMessages.speedChange() + ": " 
                                + NumberFormat.getDecimalFormat().format(after.speedInKnots - before.speedInKnots) + " "+stringMessages.averageSpeedInKnotsUnit()+" ("
                                + NumberFormat.getDecimalFormat().format(before.speedInKnots) + " "+stringMessages.averageSpeedInKnotsUnit()+" -> "
                                + NumberFormat.getDecimalFormat().format(after.speedInKnots) + " "+stringMessages.averageSpeedInKnotsUnit();
                        
                        options.setTitle(timeAndManeuver + "; " + directionChange + "; " + speedChange);
                        options.setIcon(imageResources.maneuverIconsForTypeAndTargetTack
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
        if (indexOfFirstShownFix != -1) {
            firstShownFix.put(competitorDTO, indexOfFirstShownFix);
        }
        if (indexOfLastShownFix != -1) {
            lastShownFix.put(competitorDTO, indexOfLastShownFix);
        }
    }

    /**
     * @return The last shown GPS fix for the given competitor, or <code>null</code> if no fix is available
     */
    protected GPSFixDTO getBoatFix(CompetitorDTO competitorDTO) {
        return fixes.containsKey(competitorDTO) ? fixes.get(competitorDTO).get(lastShownFix.get(competitorDTO)) : null;
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
        if (getSettings().isShowOnlySelectedCompetitors()) {
            if (Util.size(competitorSelection.getSelectedCompetitors()) == 1) {
                // first competitors selected; remove all others from map
                Iterator<Map.Entry<CompetitorDTO, Marker>> i = boatMarkers.entrySet().iterator();
                while (i.hasNext()) {
                    Entry<CompetitorDTO, Marker> next = i.next();
                    if (!next.getKey().equals(competitor)) {
                        map.removeOverlay(next.getValue());
                        removeTail(next.getKey());
                        i.remove(); // only this way a ConcurrentModificationException while looping can be avoided
                    }
                }
            } else {
                // adding a single competitor; may need to re-load data, so refresh:
                timeChanged(timer.getTime());
            }
        } else {
            // only change highlighting
            Marker lowlightedMarker = boatMarkers.get(competitor);
            if (lowlightedMarker != null) {
                Marker highlightedMarker = createBoatMarker(competitor, displayHighlighted(competitor));
                map.removeOverlay(lowlightedMarker);
                map.addOverlay(highlightedMarker);
                boatMarkers.put(competitor, highlightedMarker);
            } else {
                // seems like an internal error not to find the lowlighted marker; but maybe the
                // competitor was added late to the race;
                // data for newly selected competitor supposedly missing; refresh
                timeChanged(timer.getTime());
            }
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
        if (getSettings().isShowOnlySelectedCompetitors()) {
            // if selection is now empty, show all competitors
            if (Util.isEmpty(competitorSelection.getSelectedCompetitors())) {
                timeChanged(timer.getTime());
            } else {
                // otherwise remove only deselected competitor's boat marker and tail
                Marker removed = boatMarkers.remove(competitor);
                if (removed != null) {
                    map.removeOverlay(removed);
                }
                removeTail(competitor);
            }
        } else {
            // "lowlight" currently selected competitor
            Marker highlightedMarker = boatMarkers.get(competitor);
            if (highlightedMarker != null) {
                Marker lowlightedMarker = createBoatMarker(competitor, displayHighlighted(competitor));
                if (highlightedMarker != null) {
                    map.removeOverlay(highlightedMarker);
                }
                map.addOverlay(lowlightedMarker);
                boatMarkers.put(competitor, lowlightedMarker);
            }
        }
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
        return new RaceMapSettingsDialogComponent(getSettings(), stringMessages);
    }

    @Override
    public void updateSettings(RaceMapSettings newSettings) {
        boolean maneuverTypeSelectionChanged = false;
        for (ManeuverType maneuverType : ManeuverType.values()) {
            if (newSettings.isShowManeuverType(maneuverType) != getSettings().isShowManeuverType(maneuverType)) {
                maneuverTypeSelectionChanged = true;
                getSettings().showManeuverType(maneuverType, newSettings.isShowManeuverType(maneuverType));
            }
        }
        if (maneuverTypeSelectionChanged) {
            if (!(timer.getPlayState() == PlayStates.Playing) && lastManeuverResult != null) {
                removeAllManeuverMarkers();
                showManeuvers(lastManeuverResult);
            }
        }
        if (newSettings.isShowDouglasPeuckerPoints() != getSettings().isShowDouglasPeuckerPoints()) {
            if (!(timer.getPlayState() == PlayStates.Playing) && lastDouglasPeuckerResult != null && newSettings.isShowDouglasPeuckerPoints()) {
                getSettings().setShowDouglasPeuckerPoints(true);
                removeAllMarkDouglasPeuckerpoints();
                showMarkDouglasPeuckerPoints(lastDouglasPeuckerResult);
            } else if (!newSettings.isShowDouglasPeuckerPoints()) {
                getSettings().setShowDouglasPeuckerPoints(false);
                removeAllMarkDouglasPeuckerpoints();
            }
        }
        if (newSettings.getTailLengthInMilliseconds() != getSettings().getTailLengthInMilliseconds()) {
            getSettings().setTailLengthInMilliseconds(newSettings.getTailLengthInMilliseconds());
            redraw();
        }
        if (newSettings.isShowOnlySelectedCompetitors() != getSettings().isShowOnlySelectedCompetitors()) {
            getSettings().setShowOnlySelectedCompetitors(newSettings.isShowOnlySelectedCompetitors());
            redraw();
        }
        if (!newSettings.getZoomSettings().equals(getSettings().getZoomSettings())) {
            getSettings().setZoomSettings(newSettings.getZoomSettings());
            if (!getSettings().getZoomSettings().contains(ZoomTypes.NONE)) {
                zoomMapToNewBounds(getSettings().getZoomSettings().getNewBounds(this));
            }
        }
    }
    
    public static class BoatsBoundsCalculater extends LatLngBoundsCalculaterForSelected {

        @Override
        public LatLngBounds calculateNewBounds(RaceMap forMap) {
            LatLngBounds newBounds = null;
            Iterable<CompetitorDTO> selectedCompetitors = forMap.competitorSelection.getSelectedCompetitors();
            Iterable<CompetitorDTO> competitors = new ArrayList<CompetitorDTO>();
            if (selectedCompetitors == null || !selectedCompetitors.iterator().hasNext()) {
                competitors = forMap.getCompetitorsToShow();
            } else {
                competitors = isZoomOnlyToSelectedCompetitors() ? selectedCompetitors : forMap.getCompetitorsToShow();
            }
            for (CompetitorDTO competitor : competitors) {
                try {
                    GPSFixDTO competitorFix = forMap.getBoatFix(competitor);
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
    
    public static class TailsBoundsCalculater extends LatLngBoundsCalculaterForSelected {

        @Override
        public LatLngBounds calculateNewBounds(RaceMap forMap) {
            LatLngBounds newBounds = null;
            Iterable<CompetitorDTO> competitors = isZoomOnlyToSelectedCompetitors() ? forMap.competitorSelection.getSelectedCompetitors() : forMap.getCompetitorsToShow();
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
    
    public static class BuoysBoundsCalculater implements LatLngBoundsCalculator {
        @Override
        public LatLngBounds calculateNewBounds(RaceMap forMap) {
            LatLngBounds newBounds = null;
            Iterable<MarkDTO> marksToZoom = forMap.buoyMarkers.keySet();
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

    public static class WindSensorsBoundsCalculater implements LatLngBoundsCalculator {
        @Override
        public LatLngBounds calculateNewBounds(RaceMap forMap) {
            LatLngBounds newBounds = null;
            Collection<Marker> marksToZoom = forMap.windSensorMarkers.values();
            if (marksToZoom != null) {
                for (Marker marker: marksToZoom) {
                    LatLng markLatLng = marker.getLatLng();
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
            zoomMapToNewBounds(getSettings().getZoomSettings().getNewBounds(RaceMap.this));
        }
    }

    public Map<CompetitorDTO, Marker> getBoatMarkers() {
        return boatMarkers;
    }

    @Override
    public void competitorsListChanged(Iterable<CompetitorDTO> competitors) {
        timeChanged(timer.getTime());
    }
}
