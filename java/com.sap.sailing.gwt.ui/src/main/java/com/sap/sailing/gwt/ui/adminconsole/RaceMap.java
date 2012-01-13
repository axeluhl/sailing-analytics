package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
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
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.Util.Triple;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.GPSFixDAO;
import com.sap.sailing.gwt.ui.shared.ManeuverDAO;
import com.sap.sailing.gwt.ui.shared.MarkDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.server.api.EventNameAndRaceName;

public class RaceMap implements TimeListener, CompetitorSelectionChangeListener, RaceSelectionChangeListener, CompetitorsDisplayer {
    protected MapWidget map;

    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;

    /**
     * Tails of competitors currently displayed as overlays on the map.
     */
    private final Map<CompetitorDAO, Polyline> tails;

    /**
     * Key set is equal to that of {@link #tails} and tells what the index in in {@link #fixes} of the first fix shown
     * in {@link #tails} is .
     */
    private final Map<CompetitorDAO, Integer> firstShownFix;

    /**
     * Key set is equal to that of {@link #tails} and tells what the index in in {@link #fixes} of the last fix shown in
     * {@link #tails} is .
     */
    private  final Map<CompetitorDAO, Integer> lastShownFix;

    /**
     * Fixes of each competitors tail. If a list is contained for a competitor, the list contains a timely "contiguous"
     * list of fixes for the competitor. This means the server has no more data for the time interval covered, unless
     * the last fix was {@link GPSFixDAO#extrapolated obtained by extrapolation}.
     */
    private final Map<CompetitorDAO, List<GPSFixDAO>> fixes;

    /**
     * Markers used as boat display on the map
     */
    protected final Map<CompetitorDAO, Marker> boatMarkers;

    private final Map<MarkDAO, Marker> buoyMarkers;

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

    protected Map<CompetitorDAO, List<ManeuverDAO>> lastManeuverResult;

    protected Map<CompetitorDAO, List<GPSFixDAO>> lastDouglasPeuckerResult;
    
    private LatLng lastMousePosition;

    private List<CompetitorDAO> allCompetitors;

    private Set<CompetitorDAO> selectedMapCompetitors;

    private List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedEventAndRace;

    /**
     * If the user explicitly zoomed or panned the map, don't adjust zoom/pan unless a new race is selected
     */
    private boolean mapZoomedOrPannedSinceLastRaceSelectionChange = false;

    /**
     * Used to check if the first initial zoom to the buoy markers was already done.
     */
    private boolean mapFirstZoomDone = false;

    // key for domain web4sap.com
    private final String mapsAPIKey = "ABQIAAAAmvjPh3ZpHbnwuX3a66lDqRRLCigyC_gRDASMpyomD2do5awpNhRCyD_q-27hwxKe_T6ivSZ_0NgbUg";

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

    private RaceMapSettings settings;
    
    public RaceMap(SailingServiceAsync sailingService, ErrorReporter errorReporter, Timer timer) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.timer = timer;

        tails = new HashMap<CompetitorDAO, Polyline>();
        firstShownFix = new HashMap<CompetitorDAO, Integer>();
        lastShownFix = new HashMap<CompetitorDAO, Integer>();
        buoyMarkers = new HashMap<MarkDAO, Marker>();
        boatMarkers = new HashMap<CompetitorDAO, Marker>();
        fixes = new HashMap<CompetitorDAO, List<GPSFixDAO>>();
        
        selectedMapCompetitors = new HashSet<CompetitorDAO>();
        allCompetitors = new ArrayList<CompetitorDAO>();
        
        settings = new RaceMapSettings();
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
    
    public void loadMapsAPI(final Grid grid, final int gridRow, final int gridColumn) {
        Maps.loadMapsApi(mapsAPIKey, "2", false, new Runnable() {
            public void run() {
                map = new MapWidget();
                map.addControl(new LargeMapControl3D());
                map.addControl(new MenuMapTypeControl());
                map.addControl(new ScaleControl());
                // Add the map to the HTML host page
                map.setSize("100%", "100%");
                map.setScrollWheelZoomEnabled(true);
                map.setContinuousZoom(true);

                grid.setWidget(gridRow, gridColumn, map);
                
                imageResources = new RaceMapResources(map);
                
                map.addMapZoomEndHandler(new MapZoomEndHandler() {
                    @Override
                    public void onZoomEnd(MapZoomEndEvent event) {
                        mapZoomedOrPannedSinceLastRaceSelectionChange = true;
                    }
                });
                map.addMapDragEndHandler(new MapDragEndHandler() {
                    @Override
                    public void onDragEnd(MapDragEndEvent event) {
                        mapZoomedOrPannedSinceLastRaceSelectionChange = true;
                    }
                });
                map.addMapMouseMoveHandler(new MapMouseMoveHandler() {
                    @Override
                    public void onMouseMove(MapMouseMoveEvent event) {
                        lastMousePosition = event.getLatLng();
                    }
                });
            }
        });
    }

    public void redraw() {
        timeChanged(timer.getTime());
    }
    
    @Override
    public void onRaceSelectionChange(List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedRaces) {
        mapFirstZoomDone = false;
        mapZoomedOrPannedSinceLastRaceSelectionChange = false;
        
        this.selectedEventAndRace = selectedRaces;
    }

    @Override
    public void onCompetitorSelectionChange(List<CompetitorDAO> newSelectedCompetitors) {

        for (CompetitorDAO competitorDAO : newSelectedCompetitors) {
            
            if (selectedMapCompetitors.contains(competitorDAO)) {
                // "lowlight" currently selected competitor
                Marker highlightedMarker = boatMarkers.get(competitorDAO);
                if (highlightedMarker != null) {
                    Marker lowlightedMarker = createBoatMarker(competitorDAO, false);
                    map.removeOverlay(highlightedMarker);
                    map.addOverlay(lowlightedMarker);
                    boatMarkers.put(competitorDAO, lowlightedMarker);
                    selectedMapCompetitors.remove(competitorDAO);
                }
            } else {
                Marker lowlightedMarker = boatMarkers.get(competitorDAO);
                if (lowlightedMarker != null) {
                    Marker highlightedMarker = createBoatMarker(competitorDAO, true);
                    map.removeOverlay(lowlightedMarker);
                    map.addOverlay(highlightedMarker);
                    boatMarkers.put(competitorDAO, highlightedMarker);
                }
                // add the competitor even if not currently contained in map
                selectedMapCompetitors.add(competitorDAO);
            }
        }
    }

    @Override
    public void timeChanged(final Date date) {
        if (date != null) {
            if (selectedEventAndRace != null && !selectedEventAndRace.isEmpty()) {
                EventDAO event = selectedEventAndRace.get(selectedEventAndRace.size() - 1).getA();
                RaceDAO race = selectedEventAndRace.get(selectedEventAndRace.size() - 1).getC();
                if (event != null && race != null) {
                    final Triple<Map<CompetitorDAO, Date>, Map<CompetitorDAO, Date>, Map<CompetitorDAO, Boolean>> fromAndToAndOverlap = 
                            computeFromAndTo(date, getCompetitorsToShow());
                    final int requestID = boatPositionRequestIDCounter++;
                    sailingService.getBoatPositions(new EventNameAndRaceName(event.name, race.name),
                            fromAndToAndOverlap.getA(), fromAndToAndOverlap.getB(), true,
                            new AsyncCallback<Map<CompetitorDAO, List<GPSFixDAO>>>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError("Error obtaining boat positions: " + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(Map<CompetitorDAO, List<GPSFixDAO>> result) {
                                    // process response only if not received out of order
                                    if (startedProcessingRequestID < requestID) {
                                        startedProcessingRequestID = requestID;
                                        Date from = new Date(date.getTime() - settings.getTailLengthInMilliSeconds());
                                        updateFixes(result, fromAndToAndOverlap.getC());
                                        showBoatsOnMap(from, date, getCompetitorsToShow());
                                        if (douglasMarkers != null) {
                                            removeAllMarkDouglasPeuckerpoints();
                                        }
                                        if (maneuverMarkers != null) {
                                            removeAllManeuverMarkers();
                                        }
                                    }
                                }
                            });
                    sailingService.getMarkPositions(new EventNameAndRaceName(event.name, race.name), date,
                            new AsyncCallback<List<MarkDAO>>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError("Error trying to obtain mark positions: "
                                            + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(List<MarkDAO> result) {
                                    showMarksOnMap(result);
                                }
                            });
                }
            }
        }
    }

    public void fillCompetitors(List<CompetitorDAO> competitors) {
        allCompetitors.clear();
        allCompetitors.addAll(competitors);
    }

    /**
     * From {@link #fixes} as well as the selection of {@link #getCompetitorsToShow competitors to show}, computes the
     * from/to times for which to request GPS raceMapData.fixes from the server. No update is performed here to {@link #fixes}. The
     * result guarantees that, when used in
     * {@link SailingServiceAsync#getBoatPositions(String, String, Map, Map, boolean, AsyncCallback)}, for each
     * competitor from {@link #competitorsToShow} there are all raceMapData.fixes known by the server for that competitor starting
     * at <code>upTo-{@link #tailLengthInMilliSeconds}</code> and ending at <code>upTo</code> (exclusive).
     * 
     * @return a triple whose {@link Triple#getA() first} component contains the "from", and whose {@link Triple#getB()
     *         second} component contains the "to" times for the competitors whose trails / positions to show; the
     *         {@link Triple#getC() third} component tells whether the existing raceMapData.fixes can remain and be augmented by
     *         those requested or need to be replaced
     */
    protected Triple<Map<CompetitorDAO, Date>, Map<CompetitorDAO, Date>, Map<CompetitorDAO, Boolean>> computeFromAndTo(
            Date upTo, Collection<CompetitorDAO> competitorsToShow) {
        Date tailstart = new Date(upTo.getTime() - settings.getTailLengthInMilliSeconds());
        Map<CompetitorDAO, Date> from = new HashMap<CompetitorDAO, Date>();
        Map<CompetitorDAO, Date> to = new HashMap<CompetitorDAO, Date>();
        Map<CompetitorDAO, Boolean> overlapWithKnownFixes = new HashMap<CompetitorDAO, Boolean>();
        
        for (CompetitorDAO competitor : competitorsToShow) {
            List<GPSFixDAO> fixesForCompetitor = fixes.get(competitor);
            Date fromDate;
            Date toDate;
            Date timepointOfLastKnownFix = fixesForCompetitor == null ? null : getTimepointOfLastNonExtrapolated(fixesForCompetitor);
            Date timepointOfFirstKnownFix = fixesForCompetitor == null ? null : getTimepointOfFirstNonExtrapolated(fixesForCompetitor);
            boolean overlap = false;
            if (fixesForCompetitor != null && timepointOfFirstKnownFix != null
                    && !tailstart.before(timepointOfFirstKnownFix) && timepointOfLastKnownFix != null
                    && !tailstart.after(timepointOfLastKnownFix)) {
                // the beginning of what we need is contained in the interval we already have; skip what we already have
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
        return new Triple<Map<CompetitorDAO, Date>, Map<CompetitorDAO, Date>, Map<CompetitorDAO, Boolean>>(from, to,
                overlapWithKnownFixes);
    }


    /**
     * Adds the raceMapData.fixes received in <code>result</code> to {@link #fixes} and ensures they are still contiguous for each
     * competitor. If <code>overlapsWithKnownraceMapData.fixes</code> indicates that the raceMapData.fixes received in <code>result</code>
     * overlap with those already known, the raceMapData.fixes are merged into the list of already known raceMapData.fixes for the competitor.
     * Otherwise, the raceMapData.fixes received in <code>result</code> replace those known so far for the respective competitor.
     */
    protected void updateFixes(Map<CompetitorDAO, List<GPSFixDAO>> result,
            Map<CompetitorDAO, Boolean> overlapsWithKnownFixes) {
        for (Map.Entry<CompetitorDAO, List<GPSFixDAO>> e : result.entrySet()) {
            if (e.getValue() != null && !e.getValue().isEmpty()) {
                List<GPSFixDAO> fixesForCompetitor = fixes.get(e.getKey());
                if (fixesForCompetitor == null) {
                    fixesForCompetitor = new ArrayList<GPSFixDAO>();
                    fixes.put(e.getKey(), fixesForCompetitor);
                }
                if (!overlapsWithKnownFixes.get(e.getKey())) {
                    fixesForCompetitor.clear();
                    // to re-establish the invariants for raceMapData.tails, raceMapData.firstShownFix and raceMapData.lastShownFix, we now need to remove
                    // all
                    // points from the competitor's polyline and clear the entries in raceMapData.firstShownFix and raceMapData.lastShownFix
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

    protected void showMarksOnMap(List<MarkDAO> result) {
        if (map != null) {
            Set<MarkDAO> toRemove = new HashSet<MarkDAO>(buoyMarkers.keySet());
            for (MarkDAO markDAO : result) {
                Marker buoyMarker = buoyMarkers.get(markDAO);
                if (buoyMarker == null) {
                    buoyMarker = createBuoyMarker(markDAO);
                    buoyMarkers.put(markDAO, buoyMarker);
                    map.addOverlay(buoyMarker);
                } else {
                    buoyMarker.setLatLng(LatLng.newInstance(markDAO.position.latDeg, markDAO.position.lngDeg));
                    toRemove.remove(markDAO);
                }
            }
            for (MarkDAO toRemoveMarkDAO : toRemove) {
                Marker marker = buoyMarkers.remove(toRemoveMarkDAO);
                map.removeOverlay(marker);
            }
            zoomMapFirstTimeToMarks(buoyMarkers.keySet());
        }
    }

/**
     * Zooms the map to the given marks if the map was not zoomed yet. If the map zoom to the marks was successful one
     * time, the initial zoom to these marks can not be done again except the race selection is changed via {@link RaceMapPanel#onRaceSelectionChange(List).
     * 
     * @param marksToZoomAt
     *            the marks to zoom at
     */
    protected void zoomMapFirstTimeToMarks(Set<MarkDAO> marksToZoomAt) {
        if (!mapZoomedOrPannedSinceLastRaceSelectionChange && !mapFirstZoomDone) {
            LatLngBounds newBounds = null;
            if (marksToZoomAt != null && !marksToZoomAt.isEmpty()) {
                for (MarkDAO markDAO : marksToZoomAt) {
                    LatLng latLngZoomFirstTime = LatLng.newInstance(markDAO.position.latDeg, markDAO.position.lngDeg);
                    LatLngBounds bounds = LatLngBounds.newInstance(latLngZoomFirstTime, latLngZoomFirstTime);
                    if (newBounds == null) {
                        newBounds = bounds;
                    } else {
                        newBounds.extend(bounds.getNorthEast());
                        newBounds.extend(bounds.getSouthWest());
                    }
                }

            }
            if (newBounds != null) {
                map.setZoomLevel(map.getBoundsZoomLevel(newBounds));
                map.setCenter(newBounds.getCenter());
                map.checkResizeAndCenter();
                mapFirstZoomDone = true;
                /*
                 * Reset the mapZoomedOrPannedSinceLastRaceSelection: In spite of the fact that the map was just zoomed
                 * to the bounds of the buoys, it was not a zoom or pan triggered by the user. As a consequence the
                 * mapZoomedOrPannedSinceLastRaceSelection option has to reset again.
                 */
                mapZoomedOrPannedSinceLastRaceSelectionChange = false;
            }
        }
    }

    /**
     * @param from
     *            time point for first fix to show in tails
     * @param to
     *            time point for last fix to show in tails
     */
    protected void showBoatsOnMap(Date from, Date to, Collection<CompetitorDAO> competitorsToShow) {
        if (map != null) {
            LatLngBounds newMapBounds = null;
            Set<CompetitorDAO> competitorDAOsOfUnusedTails = new HashSet<CompetitorDAO>(tails.keySet());
            Set<CompetitorDAO> competitorDAOsOfUnusedMarkers = new HashSet<CompetitorDAO>(boatMarkers.keySet());

            for (CompetitorDAO competitorDAO : competitorsToShow) {
                if (fixes.containsKey(competitorDAO)) {
                    Polyline tail = tails.get(competitorDAO);
                    if (tail == null) {
                        tail = createTailAndUpdateIndices(competitorDAO, from, to);
                        map.addOverlay(tail);
                    } else {
                        updateTail(tail, competitorDAO, from, to);
                        competitorDAOsOfUnusedTails.remove(competitorDAO);
                    }
                    LatLngBounds bounds = tail.getBounds();
                    if (newMapBounds == null) {
                        newMapBounds = bounds;
                    } else {
                        newMapBounds.extend(bounds.getNorthEast());
                        newMapBounds.extend(bounds.getSouthWest());
                    }
                    if (lastShownFix.containsKey(competitorDAO) && lastShownFix.get(competitorDAO) != -1) {
                        GPSFixDAO lastPos = getBoatFix(competitorDAO);
                        Marker boatMarker = boatMarkers.get(competitorDAO);
                        if (boatMarker == null) {
                            boatMarker = createBoatMarker(competitorDAO, false);
                            map.addOverlay(boatMarker);
                            boatMarkers.put(competitorDAO, boatMarker);
                        } else {
                            competitorDAOsOfUnusedMarkers.remove(competitorDAO);
                            // check if anchors match; re-use marker with setImage only if anchors match
                            Point newAnchor = imageResources.getBoatImageTransformator(lastPos,
                                    selectedMapCompetitors.contains(competitorDAO)).getAnchor();
                            Point oldAnchor = boatMarker.getIcon().getIconAnchor();
                            if (oldAnchor.getX() == newAnchor.getX() && oldAnchor.getY() == newAnchor.getY()) {
                                boatMarker.setLatLng(LatLng.newInstance(lastPos.position.latDeg,
                                        lastPos.position.lngDeg));
                                boatMarker.setImage(imageResources.getBoatImageURL(lastPos,
                                        selectedMapCompetitors.contains(competitorDAO)));
                            } else {
                                // anchors don't match; replace marker
                                map.removeOverlay(boatMarker);
                                boatMarker = createBoatMarker(competitorDAO,
                                        selectedMapCompetitors.contains(competitorDAO));
                                map.addOverlay(boatMarker);
                                boatMarkers.put(competitorDAO, boatMarker);
                            }
                        }
                    }
                }
            }
            if (!mapZoomedOrPannedSinceLastRaceSelectionChange && newMapBounds != null) {
                map.setZoomLevel(map.getBoundsZoomLevel(newMapBounds));
                map.setCenter(newMapBounds.getCenter());
                mapFirstZoomDone = true;
            }
            for (CompetitorDAO unusedMarkerCompetitorDAO : competitorDAOsOfUnusedMarkers) {
                map.removeOverlay(boatMarkers.remove(unusedMarkerCompetitorDAO));
            }
            for (CompetitorDAO unusedTailCompetitorDAO : competitorDAOsOfUnusedTails) {
                map.removeOverlay(tails.remove(unusedTailCompetitorDAO));
            }
        }
    }
    
    protected Marker createBuoyMarker(final MarkDAO markDAO) {
        MarkerOptions options = MarkerOptions.newInstance();
        if (imageResources.buoyIcon != null) {
            options.setIcon(imageResources.buoyIcon);
        }
        options.setTitle(markDAO.name);
        final Marker buoyMarker = new Marker(LatLng.newInstance(markDAO.position.latDeg, markDAO.position.lngDeg),
                options);
        buoyMarker.addMarkerClickHandler(new MarkerClickHandler() {
            @Override
            public void onClick(MarkerClickEvent event) {
                LatLng latlng = buoyMarker.getLatLng();
                showMarkInfoWindow(markDAO, latlng);
            }
        });
        return buoyMarker;
    }

    protected Marker createBoatMarker(final CompetitorDAO competitorDAO, boolean highlighted) {
        GPSFixDAO boatFix = getBoatFix(competitorDAO);
        double latDeg = boatFix.position.latDeg;
        double lngDeg = boatFix.position.lngDeg;
        MarkerOptions options = MarkerOptions.newInstance();
        Icon icon = imageResources.getBoatImageIcon(boatFix, highlighted);
        options.setIcon(icon);
        options.setTitle(competitorDAO.name);
        final Marker boatMarker = new Marker(LatLng.newInstance(latDeg, lngDeg), options);
        boatMarker.addMarkerClickHandler(new MarkerClickHandler() {
            @Override
            public void onClick(MarkerClickEvent event) {
                LatLng latlng = boatMarker.getLatLng();
                showCompetitorInfoWindow(competitorDAO, latlng);
            }
        });
        boatMarker.addMarkerMouseOverHandler(new MarkerMouseOverHandler() {
            @Override
            public void onMouseOver(MarkerMouseOverEvent event) {
                map.setTitle(competitorDAO.name);
                // setSelectedInMap(competitorDAO, true);
                // quickRanksBox.setItemSelected(quickRanksList.indexOf(competitorDAO), true);
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

    private void showMarkInfoWindow(MarkDAO markDAO, LatLng latlng) {
        map.getInfoWindow().open(latlng, new InfoWindowContent(getInfoWindowContent(markDAO)));
    }

    private void showCompetitorInfoWindow(final CompetitorDAO competitorDAO, LatLng where) {
        GPSFixDAO latestFixForCompetitor = getBoatFix(competitorDAO);
        map.getInfoWindow().open(where,
                new InfoWindowContent(getInfoWindowContent(competitorDAO, latestFixForCompetitor)));
    }

    private Widget getInfoWindowContent(MarkDAO markDAO) {
        VerticalPanel result = new VerticalPanel();
        result.add(new Label("Mark " + markDAO.name));
        result.add(new Label("" + markDAO.position));
        return result;
    }

    private Widget getInfoWindowContent(CompetitorDAO competitorDAO, GPSFixDAO lastFix) {
        final VerticalPanel result = new VerticalPanel();
        result.add(new Label("Competitor " + competitorDAO.name));
        result.add(new Label("" + lastFix.position));
        result.add(new Label(lastFix.speedWithBearing.speedInKnots + "kts " + lastFix.speedWithBearing.bearingInDegrees
                + "deg"));
        result.add(new Label("Tack: " + lastFix.tack.name()));
        if (!selectedEventAndRace.isEmpty()) {
            EventDAO event = selectedEventAndRace.get(selectedEventAndRace.size() - 1).getA();
            RaceDAO race = selectedEventAndRace.get(selectedEventAndRace.size() - 1).getC();
            if (event != null && race != null) {
                Map<CompetitorDAO, Date> from = new HashMap<CompetitorDAO, Date>();
                from.put(competitorDAO, fixes.get(competitorDAO).get(firstShownFix.get(competitorDAO)).timepoint);
                Map<CompetitorDAO, Date> to = new HashMap<CompetitorDAO, Date>();
                to.put(competitorDAO, getBoatFix(competitorDAO).timepoint);
                /* currently not showing Douglas-Peucker points; TODO use checkboxes to select what to show (Bug #6) */
                sailingService.getDouglasPoints(new EventNameAndRaceName(event.name, race.name), from, to, 3,
                        new AsyncCallback<Map<CompetitorDAO, List<GPSFixDAO>>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error obtaining douglas positions: " + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(Map<CompetitorDAO, List<GPSFixDAO>> result) {
                                lastDouglasPeuckerResult = result;
                                if (douglasMarkers != null) {
                                    removeAllMarkDouglasPeuckerpoints();
                                }
                                if (!timer.isPlaying()) {
                                    if (settings.isShowDouglasPeuckerPoints()) {
                                        showMarkDouglasPeuckerPoints(result);
                                    }
                                }
                            }
                        });
                sailingService.getManeuvers(new EventNameAndRaceName(event.name, race.name), from, to,
                        new AsyncCallback<Map<CompetitorDAO, List<ManeuverDAO>>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error obtaining maneuvers: " + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(Map<CompetitorDAO, List<ManeuverDAO>> result) {
                                lastManeuverResult = result;
                                if (maneuverMarkers != null) {
                                    removeAllManeuverMarkers();
                                }
                                if (!timer.isPlaying()) {
                                    showManeuvers(result);
                                }
                            }
                        });

            }
        }
        return result;
    }

    private Collection<CompetitorDAO> getCompetitorsToShow() {
        if (settings.isShowOnlySelectedCompetitors()) {
            return selectedMapCompetitors;
        } else {
            return allCompetitors;
        }
    }
    
    private String getColorString(CompetitorDAO competitorDAO) {
        // TODO green no more than 70, red no less than 120
        return "#" + (Integer.toHexString(competitorDAO.hashCode()) + "000000").substring(0, 4).toUpperCase() + "00";
    }

    /**
     * Creates a polyline for the competitor represented by <code>competitorDAO</code>, taking the fixes from
     * {@link #fixes fixes.get(competitorDAO)} and using the fixes starting at time point <code>from</code> (inclusive)
     * up to the last fix with time point before <code>to</code>. The polyline is returned. Updates are applied to
     * {@link #lastShownFix}, {@link #firstShownFix} and {@link #tails}.
     */
    protected Polyline createTailAndUpdateIndices(final CompetitorDAO competitorDAO, Date from, Date to) {
        List<LatLng> points = new ArrayList<LatLng>();
        List<GPSFixDAO> fixesForCompetitor = fixes.get(competitorDAO);
        int indexOfFirst = -1;
        int indexOfLast = -1;
        int i = 0;
        for (Iterator<GPSFixDAO> fixIter = fixesForCompetitor.iterator(); fixIter.hasNext() && indexOfLast == -1;) {
            GPSFixDAO fix = fixIter.next();
            if (!fix.timepoint.before(to)) {
                indexOfLast = i;
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
            firstShownFix.put(competitorDAO, indexOfFirst);
            lastShownFix.put(competitorDAO, indexOfLast);
        }
        PolylineOptions options = PolylineOptions.newInstance(
        /* clickable */true, /* geodesic */true);
        Polyline result = new Polyline(points.toArray(new LatLng[0]), getColorString(competitorDAO), /* width */3,
        /* opacity */0.5, options);
        result.addPolylineClickHandler(new PolylineClickHandler() {
            @Override
            public void onClick(PolylineClickEvent event) {
                showCompetitorInfoWindow(competitorDAO, lastMousePosition);
            }
        });
        result.addPolylineMouseOverHandler(new PolylineMouseOverHandler() {
            @Override
            public void onMouseOver(PolylineMouseOverEvent event) {
                map.setTitle(competitorDAO.name);
            }
        });
        result.addPolylineMouseOutHandler(new PolylineMouseOutHandler() {
            @Override
            public void onMouseOut(PolylineMouseOutEvent event) {
                map.setTitle("");
            }
        });
        tails.put(competitorDAO, result);
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

    protected void showMarkDouglasPeuckerPoints(Map<CompetitorDAO, List<GPSFixDAO>> gpsFixPointMapForCompetitors) {
        douglasMarkers = new HashSet<Marker>();
        if (map != null && gpsFixPointMapForCompetitors != null) {
            Set<CompetitorDAO> keySet = gpsFixPointMapForCompetitors.keySet();
            Iterator<CompetitorDAO> iter = keySet.iterator();
            while (iter.hasNext()) {
                CompetitorDAO competitorDAO = iter.next();
                List<GPSFixDAO> gpsFix = gpsFixPointMapForCompetitors.get(competitorDAO);
                for (GPSFixDAO fix : gpsFix) {
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

    protected void showManeuvers(Map<CompetitorDAO, List<ManeuverDAO>> maneuvers) {
        maneuverMarkers = new HashSet<Marker>();
        if (map != null && maneuvers != null) {
            Set<CompetitorDAO> keySet = maneuvers.keySet();
            Iterator<CompetitorDAO> iter = keySet.iterator();
            while (iter.hasNext()) {
                CompetitorDAO competitorDAO = iter.next();
                List<ManeuverDAO> maneuversForCompetitor = maneuvers.get(competitorDAO);
                for (ManeuverDAO maneuver : maneuversForCompetitor) {
                    boolean showThisManeuver = true;
                    LatLng latLng = LatLng.newInstance(maneuver.position.latDeg, maneuver.position.lngDeg);
                    MarkerOptions options = MarkerOptions.newInstance();
                    options.setTitle("" + maneuver.timepoint + ": " + maneuver.type + " "
                            + maneuver.directionChangeInDegrees + "deg from " + maneuver.speedWithBearingBefore
                            + " to " + maneuver.speedWithBearingAfter);
                    if (maneuver.type.equals("TACK") && settings.isShowManeuverTack()) {
                        if (maneuver.newTack.equals("PORT")) {
                            options.setIcon(imageResources.tackToPortIcon);
                        } else {
                            options.setIcon(imageResources.tackToStarboardIcon);
                        }
                    } else if (maneuver.type.equals("JIBE") && settings.isShowManeuverJibe()) {
                        if (maneuver.newTack.equals("PORT")) {
                            options.setIcon(imageResources.jibeToPortIcon);
                        } else {
                            options.setIcon(imageResources.jibeToStarboardIcon);
                        }
                    } else if (maneuver.type.equals("HEAD_UP") && settings.isShowManeuverHeadUp()) {
                        if (maneuver.newTack.equals("PORT")) {
                            options.setIcon(imageResources.headUpOnPortIcon);
                        } else {
                            options.setIcon(imageResources.headUpOnStarboardIcon);
                        }
                    } else if (maneuver.type.equals("BEAR_AWAY") && settings.isShowManeuverBearAway()) {
                        if (maneuver.newTack.equals("PORT")) {
                            options.setIcon(imageResources.bearAwayOnPortIcon);
                        } else {
                            options.setIcon(imageResources.bearAwayOnStarboardIcon);
                        }
                    } else if (maneuver.type.equals("PENALTY_CIRCLE") && settings.isShowManeuverPenaltyCircle()) {
                        if (maneuver.newTack.equals("PORT")) {
                            options.setIcon(imageResources.penaltyCircleToPortIcon);
                        } else {
                            options.setIcon(imageResources.penaltyCircleToStarboardIcon);
                        }
                    } else if (maneuver.type.equals("MARK_PASSING") && settings.isShowManeuverMarkPassing()) {
                        if (maneuver.newTack.equals("PORT")) {
                            options.setIcon(imageResources.markPassingToPortIcon);
                        } else {
                            options.setIcon(imageResources.markPassingToStarboardIcon);
                        }
                    } else {
                        if (maneuver.type.equals("UNKNOWN") && settings.isShowManeuverOther()) {
                            options.setIcon(imageResources.unknownManeuverIcon);
                        } else {
                            showThisManeuver = false;
                        }
                    }
                    if (showThisManeuver) {
                        Marker marker = new Marker(latLng, options);
                        maneuverMarkers.add(marker);
                        map.addOverlay(marker);
                    }
                }
            }
        }
    }
    
    protected Date getTimepointOfFirstNonExtrapolated(List<GPSFixDAO> fixesForCompetitor) {
        for (GPSFixDAO fix : fixesForCompetitor) {
            if (!fix.extrapolated) {
                return fix.timepoint;
            }
        }
        return null;
    }

    protected Date getTimepointOfLastNonExtrapolated(List<GPSFixDAO> fixesForCompetitor) {
        if (!fixesForCompetitor.isEmpty()) {
            for (ListIterator<GPSFixDAO> fixIter = fixesForCompetitor.listIterator(fixesForCompetitor.size() - 1); fixIter
                    .hasPrevious();) {
                GPSFixDAO fix = fixIter.previous();
                if (!fix.extrapolated) {
                    return fix.timepoint;
                }
            }
        }
        return null;
    }

    /**
     * While updating the {@link #fixes} for <code>competitorDAO</code>, the invariants for {@link #tails} and
     * {@link #firstShownFix} and {@link #lastShownFix} are maintained: each time a fix is inserted, the
     * {@link #firstShownFix}/{@link #lastShownFix} records for <code>competitorDAO</code> are incremented if they are
     * greater or equal to the insertion index and we have a tail in {@link #tails} for <code>competitorDAO</code>.
     * Additionally, if the fix is in between the fixes shown in the competitor's tail, the tail is adjusted by
     * inserting the corresponding fix.
     */
    protected void mergeFixes(CompetitorDAO competitorDAO, List<GPSFixDAO> mergeThis) {
        List<GPSFixDAO> intoThis = fixes.get(competitorDAO);
        int indexOfFirstShownFix = firstShownFix.get(competitorDAO) == null ? -1 : firstShownFix.get(competitorDAO);
        int indexOfLastShownFix = lastShownFix.get(competitorDAO) == null ? -1 : lastShownFix.get(competitorDAO);
        Polyline tail = tails.get(competitorDAO);
        int intoThisIndex = 0;
        for (GPSFixDAO mergeThisFix : mergeThis) {
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
    }

    protected GPSFixDAO getBoatFix(CompetitorDAO competitorDAO) {
        return fixes.get(competitorDAO).get(lastShownFix.get(competitorDAO));
    }

    /**
     * If the tail starts before <code>from</code>, removes leading vertices from <code>tail</code> that are before
     * <code>from</code>. This is determined by using the {@link #firstShownFix} index which tells us where in
     * {@link #fixes} we find the sequence of fixes currently represented in the tail.
     * <p>
     * 
     * If the tail starts after <code>from</code>, vertices for those {@link #fixes} for <code>competitorDAO</code> at
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
    protected void updateTail(Polyline tail, CompetitorDAO competitorDAO, Date from, Date to) {
        List<GPSFixDAO> fixesForCompetitor = fixes.get(competitorDAO);
        int indexOfFirstShownFix = firstShownFix.get(competitorDAO) == null ? -1 : firstShownFix.get(competitorDAO);
        while (indexOfFirstShownFix != -1 && tail.getVertexCount() > 0
                && fixesForCompetitor.get(indexOfFirstShownFix).timepoint.before(from)) {
            tail.deleteVertex(0);
            indexOfFirstShownFix++;
        }
        // now the polyline contains no more vertices representing fixes before "from";
        // go back in time starting at indexOfFirstShownFix while the fixes are still at or after "from"
        // and insert corresponding vertices into the polyline
        while (indexOfFirstShownFix > 0 && !fixesForCompetitor.get(indexOfFirstShownFix - 1).timepoint.before(from)) {
            indexOfFirstShownFix--;
            GPSFixDAO fix = fixesForCompetitor.get(indexOfFirstShownFix);
            tail.insertVertex(0, LatLng.newInstance(fix.position.latDeg, fix.position.lngDeg));
        }
        // now adjust the polylines tail: remove excess vertices that are after "to"
        int indexOfLastShownFix = lastShownFix.get(competitorDAO) == null ? -1 : lastShownFix.get(competitorDAO);
        while (indexOfLastShownFix != -1 && tail.getVertexCount() > 0
                && fixesForCompetitor.get(indexOfLastShownFix).timepoint.after(to)) {
            tail.deleteVertex(tail.getVertexCount() - 1);
            indexOfLastShownFix--;
        }
        // now the polyline contains no more vertices representing fixes after "to";
        // go forward in time starting at indexOfLastShownFix while the fixes are still at or before "to"
        // and insert corresponding vertices into the polyline
        while (indexOfLastShownFix < fixesForCompetitor.size() - 1
                && !fixesForCompetitor.get(indexOfLastShownFix + 1).timepoint.after(to)) {
            indexOfLastShownFix++;
            GPSFixDAO fix = fixesForCompetitor.get(indexOfLastShownFix);
            tail.insertVertex(tail.getVertexCount(), LatLng.newInstance(fix.position.latDeg, fix.position.lngDeg));
        }
        firstShownFix.put(competitorDAO, indexOfFirstShownFix);
        lastShownFix.put(competitorDAO, indexOfLastShownFix);
    }

    public Set<CompetitorDAO> getSelectedMapCompetitors() {
        return selectedMapCompetitors;
    }

    public RaceMapSettings getSettings() {
        return settings;
    }

    public void setSettings(RaceMapSettings settings) {
        this.settings = settings;
    }

}
