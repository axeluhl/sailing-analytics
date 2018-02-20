package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.overlays.InfoWindow;
import com.google.gwt.maps.client.overlays.InfoWindowOptions;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class ManagedInfoWindow {

    private final InfoWindowOptions infoWindowOptions = InfoWindowOptions.newInstance();
    private final InfoWindow infoWindow = InfoWindow.newInstance(infoWindowOptions);
    private final MapWidget map;

    private final InfoWindowContent container = new InfoWindowContent();

    private boolean shown = false;

    public ManagedInfoWindow(final MapWidget map) {
        this.map = map;
        this.infoWindow.setContent(container);
        this.infoWindow.addDomReadyHandler(event -> container.maybeAttach());
        this.infoWindow.addCloseClickHandler(event -> {
            container.maybeDetach();
            shown = false;
        });
    }

    public void openAtPosition(final Widget content, final LatLng position) {
        this.container.setWidget(content);
        this.infoWindow.setPosition(position);
        this.infoWindow.open(map);
        this.shown = true;
    }

    public void close() {
        if (shown) {
            this.container.maybeDetach();
            this.infoWindow.close();
            this.shown = false;
        }
    }

    private class InfoWindowContent extends SimplePanel {

        private boolean attached = false;

        private void maybeAttach() {
            if (!attached) {
                this.attached = true;
                onAttach();
            }
        }

        private void maybeDetach() {
            if (attached) {
                this.attached = false;
                onDetach();
            }
        }
    }

}
