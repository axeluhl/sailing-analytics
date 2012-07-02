package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;

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
        AbstractEntryPoint.linkEnterToButton(setWindButton, speedInKnotsBox, fromInDegBox, latDegBox, lngDegBox);
        setWindButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                WindDTO wind = new WindDTO();
                wind.trueWindSpeedInKnots = speedInKnotsBox.getValue();
                wind.trueWindFromDeg = fromInDegBox.getValue();
                wind.timepoint = System.currentTimeMillis();
                if (latDegBox.getValue() != null && lngDegBox.getValue() != null) {
                    wind.position = new PositionDTO(latDegBox.getValue(), lngDegBox.getValue());
                }
                List<RegattaAndRaceIdentifier> selectedRaces = raceSelectionProvider.getSelectedRaces();
                // Here we assume that single selection is enabled because the WindPanel creates a TrackedComposite with disabled multi selection.
                final RegattaAndRaceIdentifier raceIdentifier = selectedRaces.get(selectedRaces.size()-1);
                sailingService.setWind(raceIdentifier, wind, new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        windShower.showWind(raceIdentifier);
                    }
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error setting wind for race "+raceIdentifier+": "+caught.getMessage());
                    }
                });
            }
        });
    }
    
    public void setEnabled(boolean enabled) {
        setWindButton.setEnabled(enabled);
    }

}
