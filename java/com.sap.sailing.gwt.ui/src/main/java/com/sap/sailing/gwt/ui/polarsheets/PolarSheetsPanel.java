package com.sap.sailing.gwt.ui.polarsheets;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RegattaDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class PolarSheetsPanel extends FormPanel implements RaceSelectionChangeListener,
        RegattaDisplayer {

    // TODO UI stuff
    public static final String POLARSHEETS_STYLE = "polarSheets";

    private FlowPanel mainPanel;
    private SailingServiceAsync sailingService;
    private PolarSheetsTrackedRacesList polarSheetsTrackedRacesList;
    private ErrorReporter errorReporter;
    private StringMessages stringMessages;
    private RaceSelectionModel raceSelectionProvider;
    private PolarSheetsEntryPoint polarSheetsEntryPoint;
    private List<RegattaAndRaceIdentifier> selectedRaces;

    public PolarSheetsPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages, PolarSheetsEntryPoint polarSheetsEntryPoint) {
        this.polarSheetsEntryPoint = polarSheetsEntryPoint;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;

        this.mainPanel = new FlowPanel();
        mainPanel.setSize("100%", "100%");
        setWidget(mainPanel);
        addFilteredTrackedRacesList();
    }

    private void addFilteredTrackedRacesList() {
        VerticalPanel trackedRacesPanel = new VerticalPanel();
        trackedRacesPanel.setWidth("100%");
        
        createFilteredTrackedList();
        trackedRacesPanel.add(polarSheetsTrackedRacesList);
        
        mainPanel.add(trackedRacesPanel);
    }

    private Button createPolarSheetCalculationStartButton() {
        Button polarSheetGenerationStartButton = new Button(stringMessages.generatePolarSheet());
        polarSheetGenerationStartButton.ensureDebugId("PolarSheetGeneration");
        polarSheetGenerationStartButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                startPolarSheetGeneration();
            }
        });
        return polarSheetGenerationStartButton;
    }

    protected void startPolarSheetGeneration() {
        // TODO Auto-generated method stub
        
    }

    private void createFilteredTrackedList() {
        raceSelectionProvider = new RaceSelectionModel();
        polarSheetsTrackedRacesList = new PolarSheetsTrackedRacesList(sailingService, errorReporter, polarSheetsEntryPoint,
                raceSelectionProvider, stringMessages, true, new RaceFilter(true, true), createPolarSheetCalculationStartButton());
        raceSelectionProvider.addRaceSelectionChangeListener(this);
    }

    @Override
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
        this.selectedRaces = selectedRaces;
    }

    @Override
    public void fillRegattas(List<RegattaDTO> regattas) {
        polarSheetsTrackedRacesList.fillRegattas(regattas);
    }

}
