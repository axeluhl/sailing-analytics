package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.GPSFixDAO;
import com.sap.sailing.gwt.ui.shared.MarkDAO;
import com.sap.sailing.gwt.ui.shared.Pair;
import com.sap.sailing.gwt.ui.shared.QuickRankDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;

public class RaceMapPanel extends FormPanel implements EventDisplayer, TimeListener {
    private final StringConstants stringConstants;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final Grid grid;
    private MapWidget map;
    private final List<Pair<EventDAO, RaceDAO>> raceList;
    private final ListBox raceListBox;
    private final ListBox quickRanksBox;
    private final List<CompetitorDAO> quickRanksList;
    private final TimePanel timePanel;
    private Icon boatIcon;
    private Icon boatIconHighlighted;
    private Icon buoyIcon;
    private LatLng lastMousePosition;
    private CompetitorDAO selectedCompetitor;
    
    /**
     * If the user explicitly zoomed or panned the map, don't adjust zoom/pan unless a new race
     * is selected
     */
    private boolean mapZoomedOrPannedSinceLastRaceSelectionChange = false;
    
    /**
     * Tails of competitors currently displayed as overlays on the map.
     */
    private final Map<CompetitorDAO, Polyline> tails;
    
    /**
     * Markers used as boat display on the map
     */
    private final Map<CompetitorDAO, Marker> boatMarkers;
    
    private final Map<MarkDAO, Marker> buoyMarkers;

    private final String mapsAPIKey = "ABQIAAAAmvjPh3ZpHbnwuX3a66lDqRTB4YHzt9A9TZNGGB87gEPRa24TnRQjCq1hRMRvlUmR4K97fo_4LwER6A";
    
    public RaceMapPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            final EventRefresher eventRefresher, StringConstants stringConstants) {
        this.sailingService = sailingService;
        this.stringConstants = stringConstants;
        this.errorReporter = errorReporter;
        tails = new HashMap<CompetitorDAO, Polyline>();
        buoyMarkers = new HashMap<MarkDAO, Marker>();
        boatMarkers = new HashMap<CompetitorDAO, Marker>();
        this.grid = new Grid(2, 2);
        setWidget(grid);
        grid.setSize("100%", "100%");
        grid.getColumnFormatter().setWidth(0, "20%");
        grid.getColumnFormatter().setWidth(1, "80%");
        loadMapsAPI();
        raceListBox = new ListBox();
        raceList = new ArrayList<Pair<EventDAO, RaceDAO>>();
        VerticalPanel vp = new VerticalPanel();
        HorizontalPanel labelAndRefreshButton = new HorizontalPanel();
        labelAndRefreshButton.setSpacing(20);
        vp.add(labelAndRefreshButton);
        labelAndRefreshButton.add(new Label(stringConstants.races()));
        vp.add(raceListBox);
        Button btnRefresh = new Button(stringConstants.refresh());
        btnRefresh.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventRefresher.fillEvents();
            }
        });
        labelAndRefreshButton.add(btnRefresh);
        grid.setWidget(0,  0, vp);
        quickRanksList = new ArrayList<CompetitorDAO>();
        quickRanksBox = new ListBox(/* isMultipleSelect */ true);
        quickRanksBox.setVisibleItemCount(20);
        quickRanksBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                int selectedIndex = quickRanksBox.getSelectedIndex();
                if (selectedIndex >= 0) {
                    select(quickRanksList.get(selectedIndex));
                }
            }
        });
        quickRanksBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int selectedIndex = quickRanksBox.getSelectedIndex();
                if (selectedIndex >= 0) {
                    select(quickRanksList.get(selectedIndex));
                }
            }
        });
        grid.setWidget(1, 0, quickRanksBox);
        timePanel = new TimePanel(stringConstants, /* delayBetweenAutoAdvancesInMilliseconds */ 3000);
        timePanel.addTimeListener(this);
        grid.setWidget(0, 1, timePanel);
    }

    private void loadMapsAPI() {
        Maps.loadMapsApi(mapsAPIKey, "2", false, new Runnable() {
            public void run() {
                map = new MapWidget();
                map.addControl(new LargeMapControl3D());
                map.addControl(new MenuMapTypeControl());
                // Add the map to the HTML host page
                grid.setWidget(1, 1, map);
                map.setSize("100%", "500px");
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
        raceList.clear();
        raceListBox.clear();
        for (EventDAO event : result) {
            for (RegattaDAO regatta : event.regattas) {
                for (RaceDAO race : regatta.races) {
                    raceList.add(new Pair<EventDAO, RaceDAO>(event, race));
                }
            }
        }
        Collections.sort(raceList, new Comparator<Pair<EventDAO, RaceDAO>>() {
            @Override
            public int compare(Pair<EventDAO, RaceDAO> o1, Pair<EventDAO, RaceDAO> o2) {
                String name1 = RaceMapPanel.this.toString(o1);
                String name2 = RaceMapPanel.this.toString(o2);
                return name1.compareTo(name2);
            }

        });
        for (Pair<EventDAO, RaceDAO> p : raceList) {
            raceListBox.addItem(toString(p));
        }
        raceListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                updateMapFromSelectedRace();
            }
        });
        raceListBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateMapFromSelectedRace();
            }
        });
        updateMapFromSelectedRace();
    }

    private String toString(Pair<EventDAO, RaceDAO> pair) {
        return pair.getA().name+" - "+pair.getB().name+(pair.getB().currentlyTracked ? " ("+stringConstants.tracked()+")" : "");
    }

    private void updateMapFromSelectedRace() {
        RaceDAO selectedRace = getSelectedRace();
        mapZoomedOrPannedSinceLastRaceSelectionChange = false;
        if (selectedRace != null) {
            updateSlider(selectedRace);
        }
        // force display of currently selected race
        timeChanged(timePanel.getTime());
    }

    private void updateSlider(RaceDAO selectedRace) {
        if (selectedRace.startOfTracking != null) {
            timePanel.setMin(selectedRace.startOfTracking);
        }
        if (selectedRace.timePointOfNewestEvent != null) {
            timePanel.setMax(selectedRace.timePointOfNewestEvent);
        }
    }

    private RaceDAO getSelectedRace() {
        int i = raceListBox.getSelectedIndex();
        RaceDAO result = null;
        if (i >= 0) {
            result = raceList.get(i).getB();
        }
        return result;
    }

    private EventDAO getSelectedEvent() {
        int i = raceListBox.getSelectedIndex();
        EventDAO result = null;
        if (i >= 0) {
            result = raceList.get(i).getA();
        }
        return result;
    }

    @Override
    public void timeChanged(Date date) {
        EventDAO event = getSelectedEvent();
        RaceDAO race = getSelectedRace();
        if (event != null && race != null) {
            sailingService.getBoatPositions(event.name, race.name, date, /* tailLengthInMilliseconds */ 30000l,
                    true, new AsyncCallback<Map<CompetitorDAO, List<GPSFixDAO>>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error obtaining boat positions: "+caught.getMessage());
                }

                @Override
                public void onSuccess(Map<CompetitorDAO, List<GPSFixDAO>> result) {
                    showBoatsOnMap(result);
                }
            });
            sailingService.getMarkPositions(event.name, race.name, date, new AsyncCallback<List<MarkDAO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error trying to obtain mark positions: "+caught.getMessage());
                }

                @Override
                public void onSuccess(List<MarkDAO> result) {
                    showMarksOnMap(result);
                }
            });
            sailingService.getQuickRanks(event.name, race.name, date, new AsyncCallback<List<QuickRankDAO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error obtaining quick rankings: "+caught.getMessage());
                }

                @Override
                public void onSuccess(List<QuickRankDAO> result) {
                    showQuickRanks(result);
                }
            });
        }
    }

    private void showQuickRanks(List<QuickRankDAO> result) {
        quickRanksBox.clear();
        quickRanksList.clear();
        for (QuickRankDAO quickRank : result) {
            quickRanksList.add(quickRank.competitor);
            quickRanksBox.addItem(""+quickRank.rank+". "+quickRank.competitor.name+" ("+quickRank.competitor.threeLetterIocCountryCode+") in leg #"+
                    (quickRank.legNumber+1));
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

    private void showBoatsOnMap(Map<CompetitorDAO, List<GPSFixDAO>> result) {
        if (map != null) {
            LatLngBounds newMapBounds = null;
            for (Map.Entry<CompetitorDAO, Polyline> tail : tails.entrySet()) {
                map.removeOverlay(tail.getValue());
            }
            for (Map.Entry<CompetitorDAO, Marker> boatMarker : boatMarkers.entrySet()) {
                map.removeOverlay(boatMarker.getValue());
            }
            tails.clear();
            boatMarkers.clear();
            for (final Map.Entry<CompetitorDAO, List<GPSFixDAO>> tail : result.entrySet()) {
                if (!tail.getValue().isEmpty()) {
                    final CompetitorDAO competitorDAO = tail.getKey();
                    Polyline newTail = createTail(competitorDAO, tail.getValue());
                    map.addOverlay(newTail);
                    tails.put(competitorDAO, newTail);
                    LatLngBounds bounds = newTail.getBounds();
                    if (newMapBounds == null) {
                        newMapBounds = bounds;
                    } else {
                        newMapBounds.extend(bounds.getNorthEast());
                        newMapBounds.extend(bounds.getSouthWest());
                    }
                    GPSFixDAO lastPos = tail.getValue().get(tail.getValue().size() - 1);
                    Marker boatMarker = createBoatMarker(competitorDAO, lastPos.position.latDeg, lastPos.position.lngDeg, false);
                    map.addOverlay(boatMarker);
                    boatMarkers.put(competitorDAO, boatMarker);
                }
            }
            if (!mapZoomedOrPannedSinceLastRaceSelectionChange && newMapBounds != null) {
                map.setZoomLevel(map.getBoundsZoomLevel(newMapBounds));
                map.setCenter(newMapBounds.getCenter());
            }
        }
    }
    
    /**
     * Highlights the competitor's marker on the map. If another competitor's marker is currently
     * highlighted, it is "lowlighted."
     */
    private void select(CompetitorDAO competitor) {
        if (selectedCompetitor != null) {
            if ((competitor == null && selectedCompetitor != null) ||
                    (competitor != null && !competitor.equals(selectedCompetitor))) {
                // "lowlight" currently selected competitor
                Marker highlightedMarker = boatMarkers.get(selectedCompetitor);
                if (highlightedMarker != null) {
                    Marker lowlightedMarker = createBoatMarker(selectedCompetitor, highlightedMarker.getLatLng()
                            .getLatitude(), highlightedMarker.getLatLng().getLongitude(), /* highlighted */
                            false);
                    map.removeOverlay(highlightedMarker);
                    map.addOverlay(lowlightedMarker);
                    boatMarkers.put(selectedCompetitor, lowlightedMarker);
                }
            }
        }
        if (competitor != null && !competitor.equals(selectedCompetitor)) {
            Marker lowlightedMarker = boatMarkers.get(competitor);
            if (lowlightedMarker != null) {
                Marker highlightedMarker = createBoatMarker(competitor, lowlightedMarker.getLatLng().getLatitude(),
                        lowlightedMarker.getLatLng().getLongitude(), /* highlighted */
                        true);
                map.removeOverlay(lowlightedMarker);
                map.addOverlay(highlightedMarker);
                boatMarkers.put(competitor, highlightedMarker);
                int selectionIndex = quickRanksList.indexOf(competitor);
                quickRanksBox.setSelectedIndex(selectionIndex);
            }
        }
        selectedCompetitor = competitor;
    }

    private Marker createBuoyMarker(final MarkDAO markDAO) {
        MarkerOptions options = MarkerOptions.newInstance();
        if (buoyIcon != null) {
            options.setIcon(buoyIcon);
        }
        options.setTitle(markDAO.name);
        final Marker buoyMarker = new Marker(LatLng.newInstance(markDAO.position.latDeg, markDAO.position.lngDeg), options);
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
                select(competitorDAO);
            }
        });
        boatMarker.addMarkerMouseOutHandler(new MarkerMouseOutHandler() {
            @Override
            public void onMouseOut(MarkerMouseOutEvent event) {
                select(null);
            }
        });
        return boatMarker;
    }


    private void showMarkInfoWindow(MarkDAO markDAO, LatLng latlng) {
        map.getInfoWindow().open(latlng,
                new InfoWindowContent(getInfoWindowContent(markDAO)));
    }

    private void showCompetitorInfoWindow(final CompetitorDAO competitorDAO, LatLng latlng) {
        map.getInfoWindow().open(latlng,
                new InfoWindowContent(getInfoWindowContent(competitorDAO)));
    }
    
    private Widget getInfoWindowContent(MarkDAO markDAO) {
        VerticalPanel result = new VerticalPanel();
        result.add(new Label("Mark "+markDAO.name));
        return result;
    }

    private Widget getInfoWindowContent(CompetitorDAO competitorDAO) {
        VerticalPanel result = new VerticalPanel();
        result.add(new Label("Competitor "+competitorDAO.name));
        return result;
    }
    
    private String getColorString(CompetitorDAO competitorDAO) {
        return "#"+Integer.toHexString(competitorDAO.hashCode()).substring(0, 6).toUpperCase();
    }

    private Polyline createTail(final CompetitorDAO competitorDAO, List<GPSFixDAO> value) {
        List<LatLng> points = new ArrayList<LatLng>();
        for (int i=0; i<value.size(); i++) {
            points.add(LatLng.newInstance(value.get(i).position.latDeg, value.get(i).position.lngDeg));
        }
        PolylineOptions options = PolylineOptions.newInstance(/* clickable */ true, /* geodesic */ true);
        Polyline result = new Polyline(points.toArray(new LatLng[0]), getColorString(competitorDAO), /* width */3,
                /* opacity */ 0.5, options);
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
                select(competitorDAO);
            }
        });
        result.addPolylineMouseOutHandler(new PolylineMouseOutHandler() {
            @Override
            public void onMouseOut(PolylineMouseOutEvent event) {
                map.setTitle("");
                select(null);
            }
        });
        return result;
    }
    
}
