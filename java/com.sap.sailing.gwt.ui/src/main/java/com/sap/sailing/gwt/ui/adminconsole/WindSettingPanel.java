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
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;

public class WindSettingPanel extends FormPanel {
    private final Button addWindButton;
    
    private final VerticalPanel mainPanel;
    
    public WindSettingPanel(final SailingServiceAsync sailingService, final RaceSelectionProvider raceSelectionProvider,
            final WindShower windShower, final ErrorReporter errorReporter, final StringMessages stringMessages) {
        
        mainPanel = new VerticalPanel();
        setWidget(mainPanel);
        
        mainPanel.add(new Label("Add wind data"));
        
        Grid grid = new Grid(6, 2);
        mainPanel.add(grid);
        grid.setWidget(0, 0, new Label("Speed (kn):"));
        final DoubleBox speedInKnotsBox = new DoubleBox();
        grid.setWidget(0, 1, speedInKnotsBox);
        grid.setWidget(1, 0, new Label("From (deg):"));
        final DoubleBox fromInDegBox = new DoubleBox();
        grid.setWidget(1, 1, fromInDegBox);
        grid.setWidget(2, 0, new Label("Latitude (optional):"));
        final DoubleBox latDegBox = new DoubleBox();
        grid.setWidget(2, 1, latDegBox);
        grid.setWidget(3, 0, new Label("Longitude (optional):"));
        final DoubleBox lngDegBox = new DoubleBox();
        grid.setWidget(3, 1, lngDegBox);
        final DateBox timeBox = new DateBox();
        grid.setWidget(4, 0, new Label("Timepoint (optional):"));
        grid.setWidget(4, 1, timeBox);
        
        addWindButton = new Button(stringMessages.add());
        grid.setWidget(5, 1, addWindButton);
        AbstractEntryPoint.linkEnterToButton(addWindButton, speedInKnotsBox, fromInDegBox, latDegBox, lngDegBox);
        addWindButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                WindDTO wind = new WindDTO();
                wind.trueWindSpeedInKnots = speedInKnotsBox.getValue();
                wind.trueWindFromDeg = fromInDegBox.getValue();
                wind.timepoint = timeBox.getValue() != null ? timeBox.getValue().getTime() : System.currentTimeMillis();
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
                        
                        // clear the field values for new input
                        speedInKnotsBox.setValue(null, true);
                        fromInDegBox.setValue(null, true);
                        latDegBox.setValue(null, true);
                        lngDegBox.setValue(null, true);
                        timeBox.setValue(null, true);
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
        addWindButton.setEnabled(enabled);
    }

}
