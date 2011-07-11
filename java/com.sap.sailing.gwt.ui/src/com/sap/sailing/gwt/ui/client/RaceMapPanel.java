package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.control.Control;
import com.google.gwt.maps.client.control.LargeMapControl3D;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.GPSFixDAO;
import com.sap.sailing.gwt.ui.shared.Pair;
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
    private final TimePanel timePanel;
    
    /**
     * Tails of competitors currently displayed as overlays on the map.
     */
    private final Map<CompetitorDAO, Polyline> tails;
    
    /**
     * Markers used as boat display on the map
     */
    private final Map<CompetitorDAO, Marker> boatMarkers;

    private final String mapsAPIKey = "ABQIAAAAmvjPh3ZpHbnwuX3a66lDqRTB4YHzt9A9TZNGGB87gEPRa24TnRQjCq1hRMRvlUmR4K97fo_4LwER6A";
    
    public RaceMapPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            final EventRefresher eventRefresher, StringConstants stringConstants) {
        this.sailingService = sailingService;
        this.stringConstants = stringConstants;
        this.errorReporter = errorReporter;
        tails = new HashMap<CompetitorDAO, Polyline>();
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
        timePanel = new TimePanel(stringConstants, /* delayBetweenAutoAdvancesInMilliseconds */ 3000);
        timePanel.addTimeListener(this);
        grid.setWidget(0, 1, timePanel);
    }

    private void loadMapsAPI() {
        Maps.loadMapsApi(mapsAPIKey, "2", false, new Runnable() {
            public void run() {
                LatLng cawkerCity = LatLng.newInstance(39.509, -98.434);
                map = new MapWidget(cawkerCity, 2);
                Control newZoomControl = new LargeMapControl3D();
                map.addControl(newZoomControl);
                // Add a marker
                map.addOverlay(new Marker(cawkerCity));

                // Add an info window to highlight a point of interest
                map.getInfoWindow().open(map.getCenter(),
                    new InfoWindowContent("World's Largest Ball of Sisal Twine"));

                // Add the map to the HTML host page
                grid.setWidget(1, 1, map);
                map.setSize("100%", "500px");
                map.setScrollWheelZoomEnabled(true);
                map.setContinuousZoom(true);
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
        if (selectedRace != null) {
            updateSlider(selectedRace);
        }
    }

    private void updateSlider(RaceDAO selectedRace) {
        timePanel.setMin(selectedRace.startOfTracking);
        timePanel.setMax(selectedRace.timePointOfNewestEvent);
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
            for (Map.Entry<CompetitorDAO, List<GPSFixDAO>> tail : result.entrySet()) {
                Polyline newTail = createTail(tail.getValue());
                map.addOverlay(newTail);
                tails.put(tail.getKey(), newTail);
                LatLngBounds bounds = newTail.getBounds();
                if (newMapBounds == null) {
                    newMapBounds = bounds;
                } else {
                    newMapBounds.extend(bounds.getNorthEast());
                    newMapBounds.extend(bounds.getSouthWest());
                }
                if (!tail.getValue().isEmpty()) {
                    GPSFixDAO lastPos = tail.getValue().get(tail.getValue().size() - 1);
                    Marker boatMarker = new Marker(LatLng.newInstance(lastPos.position.latDeg, lastPos.position.lngDeg));
                    map.addOverlay(boatMarker);
                    boatMarkers.put(tail.getKey(), boatMarker);
                }
            }
            map.setCenter(newMapBounds.getCenter());
            map.setZoomLevel(map.getBoundsZoomLevel(newMapBounds));
        }
    }

    private Polyline createTail(List<GPSFixDAO> value) {
        List<LatLng> points = new ArrayList<LatLng>();
        for (int i=0; i<value.size(); i++) {
            points.add(LatLng.newInstance(value.get(i).position.latDeg, value.get(i).position.lngDeg));
        }
        Polyline result = new Polyline(points.toArray(new LatLng[0]));
        return result;
    }
    
}
