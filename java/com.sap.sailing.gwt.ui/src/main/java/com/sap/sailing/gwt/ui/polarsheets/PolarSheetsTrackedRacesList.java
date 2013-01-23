package com.sap.sailing.gwt.ui.polarsheets;

import java.util.List;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class PolarSheetsTrackedRacesList extends AbstractFilteredTrackedRacesList {

    private Button btnPolarSheetGeneration;

    public PolarSheetsTrackedRacesList(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            RegattaRefresher regattaRefresher, RaceSelectionProvider raceSelectionProvider,
            StringMessages stringMessages, boolean hasMultiSelection, RaceFilter filter, Button btnPolarSheetGeneration) {
        super(sailingService, errorReporter, regattaRefresher, raceSelectionProvider, stringMessages, hasMultiSelection, filter);
        this.btnPolarSheetGeneration = btnPolarSheetGeneration;
    }
    
    @Override
    protected void addControlButtons(HorizontalPanel trackedRacesButtonPanel) {
        trackedRacesButtonPanel.add(btnPolarSheetGeneration);
    }


    @Override
    protected void makeControlsReactToSelectionChange(List<RaceDTO> selectedRaces) {
        if (selectedRaces.isEmpty()) {
            btnPolarSheetGeneration.setEnabled(false);
        } else {
            btnPolarSheetGeneration.setEnabled(true);
        }
    }

    @Override
    protected void makeControlsReactToFillRegattas(List<RegattaDTO> regattas) {
        if (regattas.isEmpty()) {
            btnPolarSheetGeneration.setVisible(false);
        } else {
            btnPolarSheetGeneration.setVisible(true);
            btnPolarSheetGeneration.setEnabled(false);
        }
    }

}
