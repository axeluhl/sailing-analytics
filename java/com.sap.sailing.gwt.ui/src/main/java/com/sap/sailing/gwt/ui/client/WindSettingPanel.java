package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.PositionDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.WindDAO;
import com.sap.sailing.gwt.ui.shared.Triple;

public class WindSettingPanel extends FormPanel {
    private final Button setWindButton;
    
    public WindSettingPanel(final SailingServiceAsync sailingService, final ErrorReporter errorReporter,
            final RaceSelectionProvider raceSelectionProvider, final WindShower windShower) {
        Grid grid = new Grid(6, 2);
        setWidget(grid);
        grid.setWidget(0, 0, new Label("Set Wind"));
        grid.setWidget(1, 0, new Label("Speed (kn)"));
        final DoubleBox speedInKnotsBox = new DoubleBox();
        grid.setWidget(1, 1, speedInKnotsBox);
        grid.setWidget(2, 0, new Label("From (deg)"));
        final DoubleBox fromInDegBox = new DoubleBox();
        grid.setWidget(2, 1, fromInDegBox);
        grid.setWidget(3, 0, new Label("Latitude (optional)"));
        final DoubleBox latDegBox = new DoubleBox();
        grid.setWidget(3, 1, latDegBox);
        grid.setWidget(4, 0, new Label("Longitude (optional)"));
        final DoubleBox lngDegBox = new DoubleBox();
        grid.setWidget(4, 1, lngDegBox);
        setWindButton = new Button("Set");
        grid.setWidget(5, 0, setWindButton);
        linkEnterToButton(setWindButton, speedInKnotsBox, fromInDegBox, latDegBox, lngDegBox);
        setWindButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                WindDAO wind = new WindDAO();
                wind.trueWindSpeedInKnots = speedInKnotsBox.getValue();
                wind.trueWindFromDeg = fromInDegBox.getValue();
                wind.timepoint = System.currentTimeMillis();
                if (latDegBox.getValue() != null && lngDegBox.getValue() != null) {
                    wind.position = new PositionDAO(latDegBox.getValue(), lngDegBox.getValue());
                }
                List<Triple<EventDAO, RegattaDAO, RaceDAO>> eventAndRaces = raceSelectionProvider.getSelectedEventAndRace();
                // TODO can we be sure of single selection?
                final Triple<EventDAO, RegattaDAO, RaceDAO> eventAndRace = eventAndRaces.get(eventAndRaces.size()-1);
                sailingService.setWind(eventAndRace.getA().name, eventAndRace.getC().name, wind, new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        windShower.showWind(eventAndRace.getA(), eventAndRace.getC());
                    }
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error setting wind for race "+eventAndRace.getC().name+": "+caught.getMessage());
                    }
                });
            }
        });
    }
    
    private void linkEnterToButton(final Button button, FocusWidget... widgets) {
        for (FocusWidget widget : widgets) {
            widget.addKeyPressHandler(new KeyPressHandler() {
                @Override
                public void onKeyPress(KeyPressEvent event) {
                    if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                        button.click();
                    }
                }
            });
        }
    }

    public void setEnabled(boolean enabled) {
        setWindButton.setEnabled(enabled);
    }

}
