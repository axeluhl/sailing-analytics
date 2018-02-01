package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.overlays.InfoWindow;
import com.google.gwt.maps.client.overlays.InfoWindowOptions;
import com.sap.sailing.gwt.ui.shared.ManeuverDTO;

public class ManeuverMarkerClickedListener implements ClickMapHandler {

    private RaceMap racemap;
    private ManeuverDTO maneuver;

    public ManeuverMarkerClickedListener(RaceMap racemap, ManeuverDTO maneuver) {
        this.racemap = racemap;
        this.maneuver = maneuver;
    }

    @Override
    public void onEvent(ClickMapEvent event) {
        final InfoWindow lastInfoWindow = racemap.getLastInfoWindow();
        if (lastInfoWindow != null) {
            lastInfoWindow.close();
        }
        LatLng where = racemap.getCoordinateSystem().toLatLng(maneuver.position);
        InfoWindowOptions options = InfoWindowOptions.newInstance();
        InfoWindow infoWindow = InfoWindow.newInstance(options);
        infoWindow.setContent(racemap.getInfoWindowContent(maneuver));
        infoWindow.setPosition(where);
        racemap.setLastInfoWindow(infoWindow);
        infoWindow.open(racemap.getMap());
    }
}