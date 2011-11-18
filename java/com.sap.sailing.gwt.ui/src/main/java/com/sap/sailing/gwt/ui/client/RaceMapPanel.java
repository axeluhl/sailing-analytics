package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.control.LargeMapControl3D;
import com.google.gwt.maps.client.control.MenuMapTypeControl;
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
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.GPSFixDAO;
import com.sap.sailing.gwt.ui.shared.MarkDAO;
import com.sap.sailing.gwt.ui.shared.Pair;
import com.sap.sailing.gwt.ui.shared.PositionDAO;
import com.sap.sailing.gwt.ui.shared.QuickRankDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.Triple;

public class RaceMapPanel extends FormPanel implements EventDisplayer, TimeListener, ProvidesResize, RequiresResize,
        RaceSelectionChangeListener {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final Grid grid;
    private MapWidget map;
    private final RacesListBoxPanel newRaceListBox;
    private final ListBox quickRanksBox;
    private final List<CompetitorDAO> quickRanksList;
    private final TimePanel timePanel;
    private Icon boatIcon;
    private Icon boatIconHighlighted;
    private Icon buoyIcon;
    private LatLng lastMousePosition;
    private final Set<CompetitorDAO> competitorsSelectedInMap;
    private final Timer timer;

    private long TAILLENGTHINMILLISECONDS = 30000l;

    /**
     * If the user explicitly zoomed or panned the map, don't adjust zoom/pan unless a new race is selected
     */
    private boolean mapZoomedOrPannedSinceLastRaceSelectionChange = false;

    /**
     * Tails of competitors currently displayed as overlays on the map.
     */
    private final Map<CompetitorDAO, Polyline> tails;
    
    /**
     * Key set is equal to that of {@link #tails} and tells what the index of the first shown fix is in {@link #fixes}.
     */
    private final Map<CompetitorDAO, Integer> firstShownFix;

    /**
     * Key set is equal to that of {@link #tails} and tells what the index of the last shown fix is in {@link #fixes}.
     */
    private final Map<CompetitorDAO, Integer> lastShownFix;

    /**
     * First/Last Fix of each competitors tail. lastShownFix should be the one with the boatMarker on it.
     */
    private final Map<CompetitorDAO, List<GPSFixDAO>> fixes;
    
    private final Set<CompetitorDAO> competitorsToShow;

    /**
     * Markers used as boat display on the map
     */
    private final Map<CompetitorDAO, Marker> boatMarkers;

    private final Map<MarkDAO, Marker> buoyMarkers;

    // key for domain web4sap.com
    private final String mapsAPIKey = "ABQIAAAAmvjPh3ZpHbnwuX3a66lDqRRLCigyC_gRDASMpyomD2do5awpNhRCyD_q-27hwxKe_T6ivSZ_0NgbUg";

    public RaceMapPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            final EventRefresher eventRefresher, StringConstants stringConstants) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.timer = new Timer(/* delayBetweenAutoAdvancesInMilliseconds */3000);
        competitorsSelectedInMap = new HashSet<CompetitorDAO>();
        tails = new HashMap<CompetitorDAO, Polyline>();
        firstShownFix = new HashMap<CompetitorDAO, Integer>();
        lastShownFix = new HashMap<CompetitorDAO, Integer>();
        buoyMarkers = new HashMap<MarkDAO, Marker>();
        boatMarkers = new HashMap<CompetitorDAO, Marker>();
        competitorsToShow = new HashSet<CompetitorDAO>();
        fixes = new HashMap<CompetitorDAO, List<GPSFixDAO>>();
        this.grid = new Grid(3, 2);
        setWidget(grid);
        grid.setSize("100%", "100%");
        grid.getColumnFormatter().setWidth(0, "20%");
        grid.getColumnFormatter().setWidth(1, "80%");
        grid.getCellFormatter().setHeight(2, 1, "100%");
        loadMapsAPI();
        newRaceListBox = new RacesListBoxPanel(eventRefresher, stringConstants);
        newRaceListBox.addRaceSelectionChangeListener(this);
        grid.setWidget(0, 0, newRaceListBox);
        PositionDAO pos = new PositionDAO();
        if (!boatMarkers.isEmpty()) {
            LatLng latLng = boatMarkers.values().iterator().next().getLatLng();
            pos.latDeg = latLng.getLatitude();
            pos.lngDeg = latLng.getLongitude();
        }
        SmallWindHistoryPanel windHistory = new SmallWindHistoryPanel(sailingService, pos, /*
                                                                                            * number of wind displays
                                                                                            */5,
        /* time interval between displays in milliseconds */5000, stringConstants, errorReporter);
        newRaceListBox.addRaceSelectionChangeListener(windHistory);
        grid.setWidget(1, 0, windHistory);
        quickRanksList = new ArrayList<CompetitorDAO>();
        quickRanksBox = new ListBox(/* isMultipleSelect */true);
        quickRanksBox.setVisibleItemCount(20);
        quickRanksBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateBoatSelection();
            }
        });
        quickRanksBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                updateBoatSelection();
            }
        });
        grid.setWidget(2, 0, quickRanksBox);
        timePanel = new TimePanel(stringConstants, timer);
        timer.addTimeListener(this);
        timer.addTimeListener(windHistory);
        grid.setWidget(1, 1, timePanel);
    }

    private void updateBoatSelection() {
        for (int i = 0; i < quickRanksBox.getItemCount(); i++) {
            setSelectedInMap(quickRanksList.get(i), quickRanksBox.isItemSelected(i));
        }
    }

    private void setSelectedInMap(CompetitorDAO competitorDAO, boolean itemSelected) {
        if (!itemSelected && competitorsSelectedInMap.contains(competitorDAO)) {
            // "lowlight" currently selected competitor
            Marker highlightedMarker = boatMarkers.get(competitorDAO);
            if (highlightedMarker != null) {
                Marker lowlightedMarker = createBoatMarker(competitorDAO, highlightedMarker.getLatLng().getLatitude(),
                        highlightedMarker.getLatLng().getLongitude(), /* highlighted */
                        false);
                map.removeOverlay(highlightedMarker);
                map.addOverlay(lowlightedMarker);
                boatMarkers.put(competitorDAO, lowlightedMarker);
                competitorsSelectedInMap.remove(competitorDAO);
            }
        } else if (itemSelected && !competitorsSelectedInMap.contains(competitorDAO)) {
            Marker lowlightedMarker = boatMarkers.get(competitorDAO);
            if (lowlightedMarker != null) {
                Marker highlightedMarker = createBoatMarker(competitorDAO, lowlightedMarker.getLatLng().getLatitude(),
                        lowlightedMarker.getLatLng().getLongitude(), /* highlighted */
                        true);
                map.removeOverlay(lowlightedMarker);
                map.addOverlay(highlightedMarker);
                boatMarkers.put(competitorDAO, highlightedMarker);
                int selectionIndex = quickRanksList.indexOf(competitorDAO);
                quickRanksBox.setItemSelected(selectionIndex, true);
                competitorsSelectedInMap.add(competitorDAO);
            }
        }
    }

    private void loadMapsAPI() {
        Maps.loadMapsApi(mapsAPIKey, "2", false, new Runnable() {
            public void run() {
                map = new MapWidget();
                map.addControl(new LargeMapControl3D());
                map.addControl(new MenuMapTypeControl());
                // Add the map to the HTML host page
                grid.setWidget(2, 1, map);
                map.setSize("100%", "100%");
                map.setScrollWheelZoomEnabled(true);
                map.setContinuousZoom(true);
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
                boatIcon = Icon.newInstance("/images/boat16.png");
                boatIcon.setIconAnchor(Point.newInstance(8, 8));
                boatIconHighlighted = Icon.newInstance("/images/boat-selected16.png");
                boatIconHighlighted.setIconAnchor(Point.newInstance(8, 8));
                buoyIcon = Icon.newInstance("/images/safe-water-small.png");
                buoyIcon.setIconAnchor(Point.newInstance(10, 19));
            }
        });
    }

    @Override
    public void fillEvents(List<EventDAO> result) {
        newRaceListBox.fillEvents(result);
    }

    @Override
    public void onRaceSelectionChange(List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedRaces) {
        mapZoomedOrPannedSinceLastRaceSelectionChange = false;
        if (!selectedRaces.isEmpty()) {
            RaceDAO raceDAO = selectedRaces.get(selectedRaces.size() - 1).getC();
            updateSlider(raceDAO);
            // by default show all competitors
            competitorsToShow.clear();
            for (CompetitorDAO competitorDAO : raceDAO.competitors) {
                competitorsToShow.add(competitorDAO);
            }
        }
        // force display of currently selected race
        timeChanged(timer.getTime());
    }

    private void updateSlider(RaceDAO selectedRace) {
        if (selectedRace.startOfTracking != null) {
            timePanel.setMin(selectedRace.startOfTracking);
        }
        if (selectedRace.timePointOfNewestEvent != null) {
            timePanel.setMax(selectedRace.timePointOfNewestEvent);
        }
    }

    @Override
    public void timeChanged(final Date date) {
        if (date != null) {
            List<Triple<EventDAO, RegattaDAO, RaceDAO>> selection = newRaceListBox.getSelectedEventAndRace();
            if (!selection.isEmpty()) {
                EventDAO event = selection.get(selection.size() - 1).getA();
                RaceDAO race = selection.get(selection.size() - 1).getC();
                if (event != null && race != null) {
                    Pair<Map<CompetitorDAO, Date>, Map<CompetitorDAO, Date>> fromAndTo = computeFromAndTo(date);
                    sailingService.getBoatPositions(event.name, race.name, fromAndTo.getA(), fromAndTo.getB(), true,
                            new AsyncCallback<Map<CompetitorDAO, List<GPSFixDAO>>>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError("Error obtaining boat positions: " + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(Map<CompetitorDAO, List<GPSFixDAO>> result) {
                                    updateFixes(result);
                                    showBoatsOnMap(result, new Date(date.getTime()-TAILLENGTHINMILLISECONDS), date);
                                }
                            });
                    sailingService.getMarkPositions(event.name, race.name, date, new AsyncCallback<List<MarkDAO>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError("Error trying to obtain mark positions: " + caught.getMessage());
                        }

                        @Override
                        public void onSuccess(List<MarkDAO> result) {
                            showMarksOnMap(result);
                        }
                    });
                    sailingService.getQuickRanks(event.name, race.name, date, new AsyncCallback<List<QuickRankDAO>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError("Error obtaining quick rankings: " + caught.getMessage());
                        }

                        @Override
                        public void onSuccess(List<QuickRankDAO> result) {
                            showQuickRanks(result);
                        }
                    });
                }
            }
        }
    }

    /**
     * From {@link #fixes} as well as the selection of {@link #competitorsToShow competitors to show}, computes the
     * from/to times for which to request GPS fixes from the server. No update is performed here to {@link #fixes}. The
     * result guarantees that, when used in
     * {@link SailingServiceAsync#getBoatPositions(String, String, Map, Map, boolean, AsyncCallback)}, for each
     * competitor from {@link #competitorsToShow} there are all fixes known by the server for that competitor starting
     * at <code>upTo-{@link #TAILLENGTHINMILLISECONDS}</code> and ending at <code>upTo</code> (exclusive).
     * 
     * @return a pair whose {@link Pair#getA() first} component contains the "from", and whose {@link Pair#getB()
     *         second} component contains the "to" times for the competitors whose trails / positions to show.
     */
    private Pair<Map<CompetitorDAO, Date>, Map<CompetitorDAO, Date>> computeFromAndTo(Date upTo) {
        Date tailStart = new Date(upTo.getTime() - TAILLENGTHINMILLISECONDS);
        Map<CompetitorDAO, Date> from = new HashMap<CompetitorDAO, Date>();
        Map<CompetitorDAO, Date> to = new HashMap<CompetitorDAO, Date>();
        for (CompetitorDAO competitor : competitorsToShow) {
            List<GPSFixDAO> fixesForCompetitor = fixes.get(competitor);
            Date fromDate;
            Date toDate;
            if (fixesForCompetitor == null || fixesForCompetitor.get(0).timepoint.after(tailStart)) {
                fromDate = tailStart;
            } else {
                fromDate = fixesForCompetitor.get(fixesForCompetitor.size()-1).timepoint;
            }
            if (fixesForCompetitor == null || fixesForCompetitor.get(fixesForCompetitor.size()-1).timepoint.before(upTo)) {
                toDate = upTo;
            } else {
                toDate = fixesForCompetitor.get(0).timepoint;
            }
            // only request something for the competitor if we're missing information at all
            if (fromDate.before(toDate) || fromDate.equals(toDate)) {
                from.put(competitor, fromDate);
                to.put(competitor, toDate);
            }
        }
        return new Pair<Map<CompetitorDAO,Date>, Map<CompetitorDAO,Date>>(from, to);
    }

    private void updateFixes(Map<CompetitorDAO, List<GPSFixDAO>> result) {
        for (Map.Entry<CompetitorDAO, List<GPSFixDAO>> e : result.entrySet()) {
            if (e.getValue() != null && !e.getValue().isEmpty()) {
                List<GPSFixDAO> fixesForCompetitor = fixes.get(e.getKey());
                if (fixesForCompetitor == null) {
                    fixesForCompetitor = new ArrayList<GPSFixDAO>();
                    fixes.put(e.getKey(), fixesForCompetitor);
                }
                GPSFixDAO firstNewFix = e.getValue().get(0);
                GPSFixDAO lastNewFix = e.getValue().get(e.getValue().size()-1);
                if (firstNewFix.timepoint.before(fixesForCompetitor.get(0).timepoint)) {
                    if (lastNewFix.timepoint.after(fixesForCompetitor.get(fixesForCompetitor.size()-1).timepoint)) {
                        // known fixes are a true subset; replace:
                        fixesForCompetitor.clear();
                        fixesForCompetitor.addAll(e.getValue());
                    } else {
                        // new fixes start earlier but end somewhere within known sequence; prepend new fixes
                        Date timeOfFirstKnownFix = fixesForCompetitor.get(0).timepoint;
                        int insertPosition = 0;
                        Iterator<GPSFixDAO> newFixIter = e.getValue().iterator();
                        GPSFixDAO newFix = newFixIter.next();
                        while (newFix != null && newFix.timepoint.before(timeOfFirstKnownFix)) {
                            fixesForCompetitor.add(insertPosition++, newFix);
                            if (newFixIter.hasNext()) {
                                newFix = newFixIter.next();
                            } else {
                                newFix = null;
                            }
                        }
                    }
                } else {
                    if (lastNewFix.timepoint.after(fixesForCompetitor.get(fixesForCompetitor.size()-1).timepoint)) {
                        // new fixes may overlap but exceed at the end
                        Date timeOfFirstKnownFix = fixesForCompetitor.get(0).timepoint;
                        int insertPosition = 0;
                        Iterator<GPSFixDAO> newFixIter = e.getValue().iterator();
                        GPSFixDAO newFix = newFixIter.next();
                        while (newFix != null && newFix.timepoint.before(timeOfFirstKnownFix)) {
                            fixesForCompetitor.add(insertPosition++, newFix);
                            if (newFixIter.hasNext()) {
                                newFix = newFixIter.next();
                            } else {
                                newFix = null;
                            }
                        }
                    } // else new fixes are a true subset; nothing to do at all
                }
            }
        }
    }

    private void showQuickRanks(List<QuickRankDAO> result) {
        quickRanksBox.clear();
        quickRanksList.clear();
        for (QuickRankDAO quickRank : result) {
            quickRanksList.add(quickRank.competitor);
            quickRanksBox.addItem("" + quickRank.rank + ". " + quickRank.competitor.name + " ("
                    + quickRank.competitor.threeLetterIocCountryCode + ") in leg #" + (quickRank.legNumber + 1));
        }
    }

    private void showMarksOnMap(List<MarkDAO> result) {
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
        }
    }

    /**
     * @param from time point for first fix to show in tails
     * @param to time point for last fix to show in tails
     */
    private void showBoatsOnMap(Map<CompetitorDAO, List<GPSFixDAO>> result, Date from, Date to) {
        if (map != null) {
            LatLngBounds newMapBounds = null;
            Set<CompetitorDAO> competitorDAOsOfUnusedTails = new HashSet<CompetitorDAO>(tails.keySet());
                Set<CompetitorDAO> competitorDAOsOfUnusedMarkers = new HashSet<CompetitorDAO>(boatMarkers.keySet());
                for (final Map.Entry<CompetitorDAO, List<GPSFixDAO>> tailEntry : result.entrySet()) {
                    if (!tailEntry.getValue().isEmpty()) {
                        final CompetitorDAO competitorDAO = tailEntry.getKey();
                        Polyline tail = tails.get(competitorDAO);
                        if (tail == null) {
                            tail = createTail(competitorDAO, tailEntry.getValue(), from, to);
                            map.addOverlay(tail);
                            tails.put(competitorDAO, tail);
                        } else {
                            updateTail(tail, tailEntry.getValue());
                            competitorDAOsOfUnusedTails.remove(tail);
                        }
                        LatLngBounds bounds = tail.getBounds();
                        if (newMapBounds == null) {
                            newMapBounds = bounds;
                        } else {
                            newMapBounds.extend(bounds.getNorthEast());
                            newMapBounds.extend(bounds.getSouthWest());
                        }
                        GPSFixDAO lastPos = tailEntry.getValue().get(tailEntry.getValue().size() - 1);
                        Marker boatMarker = boatMarkers.get(competitorDAO);
                        if (boatMarker == null) {
                            boatMarker = createBoatMarker(competitorDAO, lastPos.position.latDeg, lastPos.position.lngDeg, false);
                            map.addOverlay(boatMarker);
                            boatMarkers.put(competitorDAO, boatMarker);
                        } else {
                            competitorDAOsOfUnusedMarkers.remove(competitorDAO);
                            boatMarker.setLatLng(LatLng.newInstance(lastPos.position.latDeg, lastPos.position.lngDeg));
                        }
                    }
                }
                if (!mapZoomedOrPannedSinceLastRaceSelectionChange && newMapBounds != null) {
                    map.setZoomLevel(map.getBoundsZoomLevel(newMapBounds));
                    map.setCenter(newMapBounds.getCenter());
                }
                for (CompetitorDAO unusedMarkerCompetitorDAO : competitorDAOsOfUnusedMarkers) {
                    map.removeOverlay(boatMarkers.remove(unusedMarkerCompetitorDAO));
                }
                for (CompetitorDAO unusedTailCompetitorDAO : competitorDAOsOfUnusedTails) {
                    map.removeOverlay(tails.remove(unusedTailCompetitorDAO));
                }
            /* } else {
                for (Map.Entry<CompetitorDAO, Polyline> tailEntry : tails.entrySet()) {
                    try {
                        final CompetitorDAO competitorDAO = tailEntry.getKey();
                        Polyline tail = tailEntry.getValue();
                        List<GPSFixDAO> gpsFixDao = result.get(competitorDAO);
                        int newPointsCount = gpsFixDao.size();
                        for (int i = 0; i < newPointsCount; i++) {
                            tail.insertVertex(tail.getVertexCount(), LatLng.newInstance(
                                    gpsFixDao.get(i).position.latDeg, gpsFixDao.get(i).position.lngDeg));
                            tail.deleteVertex(0);
                        }
                        GPSFixDAO lastFix = gpsFixDao.get(gpsFixDao.size() - 1);
                        Marker bMarker = boatMarkers.get(competitorDAO);
                        bMarker.setLatLng(LatLng.newInstance(lastFix.position.latDeg, lastFix.position.lngDeg));
                        lastShownFix.put(competitorDAO, lastFix);

                        LatLngBounds bounds = tail.getBounds();
                        if (newMapBounds == null) {
                            newMapBounds = bounds;
                        } else {
                            newMapBounds.extend(bounds.getNorthEast());
                            newMapBounds.extend(bounds.getSouthWest());
                        }

                        if (!mapZoomedOrPannedSinceLastRaceSelectionChange && newMapBounds != null) {
                            map.setZoomLevel(map.getBoundsZoomLevel(newMapBounds));
                            map.setCenter(newMapBounds.getCenter());
                        }
                    } catch (Exception e) {
                        final CompetitorDAO competitorDAO = tailEntry.getKey();
                        Polyline tail = tailEntry.getValue();
                        List<GPSFixDAO> gpsFixDao = result.get(competitorDAO);
                        map.removeOverlay(tail);
                        Polyline newTail = createTail(competitorDAO, gpsFixDao);
                        map.addOverlay(newTail);
                        tails.put(competitorDAO, newTail);
                        LatLngBounds bounds = newTail.getBounds();
                        if (newMapBounds == null) {
                            newMapBounds = bounds;
                        } else {
                            newMapBounds.extend(bounds.getNorthEast());
                            newMapBounds.extend(bounds.getSouthWest());
                        }
                        GPSFixDAO lastPos = gpsFixDao.get(gpsFixDao.size() - 1);
                        Marker boatMarker = createBoatMarker(competitorDAO, lastPos.position.latDeg,
                                lastPos.position.lngDeg, false);
                        map.addOverlay(boatMarker);
                        boatMarkers.put(competitorDAO, boatMarker);
                    } finally {

                    }
                }
            }*/
        }

    }

    private void updateTail(Polyline tail, List<GPSFixDAO> value) {
        // TODO Auto-generated method stub
        
    }

    private Marker createBuoyMarker(final MarkDAO markDAO) {
        MarkerOptions options = MarkerOptions.newInstance();
        if (buoyIcon != null) {
            options.setIcon(buoyIcon);
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

    private Marker createBoatMarker(final CompetitorDAO competitorDAO, double latDeg, double lngDeg, boolean highlighted) {
        MarkerOptions options = MarkerOptions.newInstance();
        if (highlighted) {
            if (boatIconHighlighted != null) {
                options.setIcon(boatIconHighlighted);
            }
        } else {
            if (boatIcon != null) {
                options.setIcon(boatIcon);
            }
        }
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
                setSelectedInMap(competitorDAO, true);
                quickRanksBox.setItemSelected(quickRanksList.indexOf(competitorDAO), true);
            }
        });
        boatMarker.addMarkerMouseOutHandler(new MarkerMouseOutHandler() {
            @Override
            public void onMouseOut(MarkerMouseOutEvent event) {
                setSelectedInMap(competitorDAO, false);
                quickRanksBox.setItemSelected(quickRanksList.indexOf(competitorDAO), false);
            }
        });
        return boatMarker;
    }

    private void showMarkInfoWindow(MarkDAO markDAO, LatLng latlng) {
        map.getInfoWindow().open(latlng, new InfoWindowContent(getInfoWindowContent(markDAO)));
    }

    private void showCompetitorInfoWindow(final CompetitorDAO competitorDAO, LatLng where) {
        GPSFixDAO latestFixForCompetitor = fixes.get(competitorDAO).get(lastShownFix.get(competitorDAO));
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
        VerticalPanel result = new VerticalPanel();
        result.add(new Label("Competitor " + competitorDAO.name));
        result.add(new Label("" + lastFix.position));
        result.add(new Label(lastFix.speedWithBearing.speedInKnots + "kts " + lastFix.speedWithBearing.bearingInDegrees
                + "deg"));
        result.add(new Label("Tack: " + lastFix.tack));
        return result;
    }

    private String getColorString(CompetitorDAO competitorDAO) {
        // TODO try to avoid colors close to the light blue water display color
        // of the underlying 2D map
        return "#" + Integer.toHexString(competitorDAO.hashCode()).substring(0, 6).toUpperCase();
    }

    private Polyline createTail(final CompetitorDAO competitorDAO, List<GPSFixDAO> value, Date from, Date to) {
        if (!value.isEmpty()) {
            firstShownFix.put(competitorDAO, value.get(0));
            lastShownFix.put(competitorDAO, value.get(value.size() - 1));
        }

        List<LatLng> points = new ArrayList<LatLng>();
        for (int i = 0; i < value.size(); i++) {
            points.add(LatLng.newInstance(value.get(i).position.latDeg, value.get(i).position.lngDeg));
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
                setSelectedInMap(competitorDAO, true);
                quickRanksBox.setItemSelected(quickRanksList.indexOf(competitorDAO), true);
            }
        });
        result.addPolylineMouseOutHandler(new PolylineMouseOutHandler() {
            @Override
            public void onMouseOut(PolylineMouseOutEvent event) {
                map.setTitle("");
                setSelectedInMap(competitorDAO, false);
                quickRanksBox.setItemSelected(quickRanksList.indexOf(competitorDAO), false);
            }
        });
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.user.client.ui.RequiresResize#onResize()
     */
    @Override
    public void onResize() {
        // handle what is required by @link{ProvidesResize}
        Widget child = getWidget();
        if (child instanceof RequiresResize) {
            ((RequiresResize) child).onResize();
        }
        // and ensure the map (indirect child) is also informed about resize
        if (this.map != null) {
            this.map.onResize();
        }
    }

}
