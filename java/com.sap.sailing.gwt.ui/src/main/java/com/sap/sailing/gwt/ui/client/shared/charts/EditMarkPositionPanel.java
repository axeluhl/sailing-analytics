package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.events.mousedown.MouseDownMapEvent;
import com.google.gwt.maps.client.events.mousedown.MouseDownMapHandler;
import com.google.gwt.maps.client.events.mousemove.MouseMoveMapEvent;
import com.google.gwt.maps.client.events.mousemove.MouseMoveMapHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.CourseMarkOverlay;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class EditMarkPositionPanel  extends AbsolutePanel implements Component<AbstractSettings> {
    private final RaceMap raceMap;
    
    private Map<String, CourseMarkOverlay> courseMarkOverlays;
    private Set<HandlerRegistration> courseMarkHandlers;
    
    private Set<String> selectedMarks;

    private MapWidget map;
    private Set<HandlerRegistration> mapListeners;
    
    private boolean visible;
    
    public EditMarkPositionPanel(final RaceMap raceMap, final StringMessages stringMessages) {
        
        this.raceMap = raceMap;
        courseMarkHandlers = new HashSet<>();
        selectedMarks = new HashSet<>();
        mapListeners = new HashSet<>();
        this.getEntryWidget().setTitle(stringMessages.editMarkPositions());
        setVisible(false);
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
        if (visible) {
            raceMap.unregisterAllCourseMarkInfoWindowClickHandlers();
            unregisterCourseMarkHandlers();
            registerCourseMarkListeners();
        } else {
            raceMap.unregisterAllCourseMarkInfoWindowClickHandlers();
            raceMap.registerAllCourseMarkInfoWindowClickHandlers();
            unregisterCourseMarkHandlers(); 
        }
        super.setVisible(visible);
    }
    
    private void unregisterCourseMarkHandlers() {
        Iterator<HandlerRegistration> iterator = courseMarkHandlers.iterator();
        while(iterator.hasNext()) {
            HandlerRegistration handler = iterator.next();
            handler.removeHandler();
            iterator.remove();
        }
    }

    public void setMarkOverlays(final Map<String, CourseMarkOverlay> courseMarkOverlays) {
        this.courseMarkOverlays = courseMarkOverlays;
        if (visible) {
            registerCourseMarkListeners();
        }
    }

    private void registerCourseMarkListeners() {
        for (final Map.Entry<String, CourseMarkOverlay> courseMark : courseMarkOverlays.entrySet()) {
            courseMarkHandlers.add(courseMark.getValue().addMouseDownHandler(new MouseDownMapHandler() {
                @Override
                public void onEvent(MouseDownMapEvent event) {
                    selectedMarks.add(courseMark.getKey());
                    MapOptions mapOptions = MapOptions.newInstance(false);
                    mapOptions.setDraggable(false);
                    map.setOptions(mapOptions);
                }
            }));
        }
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
                    courseMarkOverlays.get(markName).setMarkPosition(event.getMouseEvent().getLatLng());
                }
            }
        }));
        mapListeners.add(this.map.addClickHandler(new ClickMapHandler() {
            @Override
            public void onEvent(ClickMapEvent event) {
                selectedMarks.removeAll(selectedMarks);
                MapOptions mapOptions = MapOptions.newInstance(false);
                mapOptions.setDraggable(true);
                map.setOptions(mapOptions);
            }
        }));
    }

}
