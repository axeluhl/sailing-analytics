package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.events.mousedown.MouseDownMapEvent;
import com.google.gwt.maps.client.events.mousedown.MouseDownMapHandler;
import com.google.gwt.maps.client.events.mousemove.MouseMoveMapEvent;
import com.google.gwt.maps.client.events.mousemove.MouseMoveMapHandler;
import com.google.gwt.maps.client.overlays.MarkerOptions;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.CourseMarkOverlay;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class EditMarkPositionPanel  extends AbsolutePanel implements Component<AbstractSettings>, RaceSelectionChangeListener {
    private Map<String, CourseMarkOverlay> courseMarkOverlays;
    private Set<HandlerRegistration> courseMarkListeners;
    
    private Set<String> selectedMarks;

    private MapWidget map;
    private Set<HandlerRegistration> mapListeners;
    
    public EditMarkPositionPanel(final SailingServiceAsync sailingService, final RegattaAndRaceIdentifier raceIdentifier,
            final StringMessages stringMessages, final ErrorReporter errorReporter, final Timer timer) {
        this.getEntryWidget().setTitle(stringMessages.editMarkPositions());
        courseMarkListeners = new HashSet<>();
        selectedMarks = new HashSet<>();
        mapListeners = new HashSet<>();
    }
    
    public void setMarkPositions(final Map<String, CourseMarkOverlay> courseMarkOverlays) {
        this.courseMarkOverlays = courseMarkOverlays;
        MarkerOptions.newInstance().setDraggable(true);
        for (final HandlerRegistration listener : courseMarkListeners) {
            listener.removeHandler();
        }
        for (final Map.Entry<String, CourseMarkOverlay> courseMark : courseMarkOverlays.entrySet()) {
            courseMarkListeners.add(courseMark.getValue().addMouseDownHandler(new MouseDownMapHandler() {
                @Override
                public void onEvent(MouseDownMapEvent event) {
                    GWT.log(courseMark.getKey() + " add");
                    selectedMarks.add(courseMark.getKey());
                    MapOptions mapOptions = MapOptions.newInstance(false);
                    mapOptions.setDraggable(false);
                    map.setOptions(mapOptions);
                }
            }));
        }
    }

    @Override
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {        
    }

    @Override
    public String getLocalizedShortName() {
        return null;
    }

    @Override
    public Widget getEntryWidget() {
        return this;
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<AbstractSettings> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(AbstractSettings newSettings) {
        
    }

    @Override
    public String getDependentCssClassName() {
        return null;
    }

    public void setMap(final MapWidget map) {
        this.map = map;
        for( HandlerRegistration listener : mapListeners ) {
            listener.removeHandler();
        }
        mapListeners.add(this.map.addMouseMoveHandler(new MouseMoveMapHandler() {
            @Override
            public void onEvent(MouseMoveMapEvent event) {
                for (final String markName : selectedMarks) {
                    String log = markName + " move: " + courseMarkOverlays.get(markName).getPosition() + " / ";
                    courseMarkOverlays.get(markName).setMarkPosition(event.getMouseEvent().getLatLng());
                    GWT.log(log + courseMarkOverlays.get(markName).getPosition());
                }
            }
        }));
        mapListeners.add(this.map.addClickHandler(new ClickMapHandler() {
            @Override
            public void onEvent(ClickMapEvent event) {
                GWT.log("click");
                selectedMarks.removeAll(selectedMarks);
                MapOptions mapOptions = MapOptions.newInstance(false);
                mapOptions.setDraggable(true);
                map.setOptions(mapOptions);
            }
        }));
    }

}
