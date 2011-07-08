package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.control.Control;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.control.LargeMapControl3D;
import com.google.gwt.maps.client.control.SmallZoomControl3D;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.shared.EventDAO;

public class RaceMapPanel extends FormPanel implements EventDisplayer {
    private final StringConstants stringConstants;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final EventRefresher eventRefresher;
    private final VerticalPanel verticalPanel;
    private MapWidget map;
    
    private final String mapsAPIKey = "ABQIAAAAmvjPh3ZpHbnwuX3a66lDqRTB4YHzt9A9TZNGGB87gEPRa24TnRQjCq1hRMRvlUmR4K97fo_4LwER6A";
    
    public RaceMapPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter, EventRefresher eventRefresher, StringConstants stringConstants) {
        this.sailingService = sailingService;
        this.stringConstants = stringConstants;
        this.errorReporter = errorReporter;
        this.eventRefresher = eventRefresher;
        this.verticalPanel = new VerticalPanel();
        setWidget(verticalPanel);
        verticalPanel.setSize("100%", "100%");
        
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
                verticalPanel.add(map);
                map.setSize("75%", "768px");
                map.setScrollWheelZoomEnabled(true);
                map.setContinuousZoom(true);
          }
        });
    }

    @Override
    public void fillEvents(List<EventDAO> result) {
        // TODO Auto-generated method stub
        
    }
}
