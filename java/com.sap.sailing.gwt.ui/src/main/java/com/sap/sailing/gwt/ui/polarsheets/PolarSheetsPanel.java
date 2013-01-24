package com.sap.sailing.gwt.ui.polarsheets;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RegattaDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class PolarSheetsPanel extends FormPanel implements RaceSelectionChangeListener, RegattaDisplayer {

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
    private PolarSheetsChartPanel chartPanel;

    public PolarSheetsPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages, PolarSheetsEntryPoint polarSheetsEntryPoint) {
        this.polarSheetsEntryPoint = polarSheetsEntryPoint;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;

        this.mainPanel = new FlowPanel();
        setSize("100%", "100%");

        mainPanel.setSize("100%", "100%");
        setWidget(mainPanel);
        HorizontalPanel splitPanel = createSplitPanel();
        addFilteredTrackedRacesList(splitPanel);
        addPolarSheetsChartPanel(splitPanel);
        mainPanel.add(splitPanel);
    }

    private void addPolarSheetsChartPanel(HorizontalPanel splitPanel) {
        chartPanel = new PolarSheetsChartPanel(stringMessages);
        splitPanel.add(chartPanel);
        splitPanel.setCellWidth(chartPanel, "50%");
    }

    private HorizontalPanel createSplitPanel() {
        HorizontalPanel splitPanel = new HorizontalPanel();
        splitPanel.setSize("100%", "100%");
        return splitPanel;
    }

    private void addFilteredTrackedRacesList(HorizontalPanel splitPanel) {
        VerticalPanel trackedRacesPanel = new VerticalPanel();
        trackedRacesPanel.setWidth("100%");

        createPolarSheetsTrackedRacesList();
        trackedRacesPanel.add(polarSheetsTrackedRacesList);

        splitPanel.add(trackedRacesPanel);
        splitPanel.setCellWidth(trackedRacesPanel, "50%");
    }

    private void createPolarSheetsTrackedRacesList() {
        raceSelectionProvider = new RaceSelectionModel();
        ClickHandler polarSheetsGenerationButtonClickHandler = new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                startPolarSheetGeneration();
            }
        };
        polarSheetsTrackedRacesList = new PolarSheetsTrackedRacesList(sailingService, errorReporter,
                polarSheetsEntryPoint, raceSelectionProvider, stringMessages, true, new RaceFilter(true, true),
                polarSheetsGenerationButtonClickHandler);
        raceSelectionProvider.addRaceSelectionChangeListener(this);
    }

    protected void startPolarSheetGeneration() {
        sailingService.generatePolarSheetForRaces(selectedRaces, new AsyncCallback<String>() {

            @Override
            public void onSuccess(String result) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub

            }
        });
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
