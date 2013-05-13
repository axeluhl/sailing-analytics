package com.sap.sailing.gwt.ui.polarsheets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.moxieapps.gwt.highcharts.client.events.PointMouseOverEvent;
import org.moxieapps.gwt.highcharts.client.events.PointMouseOverEventHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.PolarSheetGenerationTriggerResponse;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.PolarSheetsHistogramData;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.actions.GetPolarSheetDataByAngleAction;
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
    private PolarSheetsHistogramPanel histogramPanel;
    private AsyncActionsExecutor asyncActionsExecutor;

    private Label polarSheetsGenerationLabel;
    private Label dataCountLabel;
    
    //Dual mapping. Another solution would be using commons BidiMap. 
    //But this would require to attach source code for gwt
    private Map<String,String> idNameMapping;
    private Map<String,String> nameIdMapping;

    public PolarSheetsPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages, PolarSheetsEntryPoint polarSheetsEntryPoint) {
        this.polarSheetsEntryPoint = polarSheetsEntryPoint;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        
        idNameMapping = new HashMap<String, String>();
        nameIdMapping = new HashMap<String, String>();

        this.mainPanel = new FlowPanel();
        setSize("100%", "100%");

        mainPanel.setSize("100%", "100%");
        setWidget(mainPanel);
        HorizontalPanel splitPanel = createSplitPanel();
        VerticalPanel leftPanel = addFilteredTrackedRacesList(splitPanel);
        polarSheetsGenerationLabel = createPolarSheetGenerationStatusLabel();
        leftPanel.add(polarSheetsGenerationLabel);
        dataCountLabel = new Label();
        leftPanel.add(dataCountLabel);
        VerticalPanel rightPanel = addPolarSheetsChartPanel(splitPanel);
        histogramPanel = new PolarSheetsHistogramPanel(stringMessages);
        histogramPanel.getElement().setAttribute("align", "top");
        rightPanel.add(histogramPanel);
        mainPanel.add(splitPanel);

        asyncActionsExecutor = new AsyncActionsExecutor();
        setEventListenersForPolarSheetChart();
    }

    private void setEventListenersForPolarSheetChart() {
        PointMouseOverEventHandler pointMouseOverHandler = new PointMouseOverEventHandler() {

            @Override
            public boolean onMouseOver(PointMouseOverEvent pointMouseOverEvent) {
                int angle = (int) pointMouseOverEvent.getXAsLong();
                String polarSheetNameWithWind = pointMouseOverEvent.getSeriesName();
                String[] split = polarSheetNameWithWind.split("-");
                String polarSheetName = split[0] + "-" + split[1];
                String polarSheetId = nameIdMapping.get(polarSheetName);
                int windSpeed = Integer.parseInt(split[2]); 
                
                GetPolarSheetDataByAngleAction action = new GetPolarSheetDataByAngleAction(sailingService,
                        polarSheetId, angle, windSpeed, new AsyncCallback<PolarSheetsHistogramData>() {

                            @Override
                            public void onSuccess(PolarSheetsHistogramData result) {
                                if (result != null) {
                                    histogramPanel.setData(result);
                                }
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(caught.getLocalizedMessage());
                            }

                        });
                asyncActionsExecutor.execute(action);
                return true;
            }
        };
        chartPanel.setSeriesPointMouseOverHandler(pointMouseOverHandler);
    }

    private Label createPolarSheetGenerationStatusLabel() {
        Label polarSheetsGenerationStatusLabel = new Label();
        return polarSheetsGenerationStatusLabel;
    }

    private VerticalPanel addPolarSheetsChartPanel(HorizontalPanel splitPanel) {
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setSize("100%", "100%");
        chartPanel = new PolarSheetsChartPanel(stringMessages);
        verticalPanel.add(chartPanel);
        verticalPanel.setCellHeight(chartPanel, "800px");
        splitPanel.add(verticalPanel);
        splitPanel.setCellWidth(verticalPanel, "50%");
        return verticalPanel;
    }

    private HorizontalPanel createSplitPanel() {
        HorizontalPanel splitPanel = new HorizontalPanel();
        splitPanel.setSize("100%", "100%");
        return splitPanel;
    }

    private VerticalPanel addFilteredTrackedRacesList(HorizontalPanel splitPanel) {
        VerticalPanel trackedRacesPanel = new VerticalPanel();
        trackedRacesPanel.setWidth("100%");

        createPolarSheetsTrackedRacesList();
        trackedRacesPanel.add(polarSheetsTrackedRacesList);

        splitPanel.add(trackedRacesPanel);
        splitPanel.setCellWidth(trackedRacesPanel, "50%");
        return trackedRacesPanel;
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
        // List conversion, to make List serializable
        final List<RegattaAndRaceIdentifier> selectedRacesInArrayList = new ArrayList<RegattaAndRaceIdentifier>();
        selectedRacesInArrayList.addAll(selectedRaces);
        polarSheetsTrackedRacesList.changeGenerationButtonState(false);
        sailingService.generatePolarSheetForRaces(selectedRacesInArrayList, new AsyncCallback<PolarSheetGenerationTriggerResponse>() {

            @Override
            public void onSuccess(PolarSheetGenerationTriggerResponse result) {
                // TODO string messages
                setCompletionLabel("Generating...");
                startPullingResults(result.getId());
                addNameForPolarSheet(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getLocalizedMessage());
            }
        });
    }

    protected void addNameForPolarSheet(PolarSheetGenerationTriggerResponse result) {
        String boatClassName = result.getBoatClassName();
        int index = 0;
        String name = "";
        do {
            index++;
            name = boatClassName + "-" + index;
        } while (nameIdMapping.containsKey(name));
        idNameMapping.put(result.getId(), name);
        nameIdMapping.put(name, result.getId());
    }

    protected void startPullingResults(final String id) {

        sailingService.getPolarSheetsGenerationResults(id, new AsyncCallback<PolarSheetsData>() {

            @Override
            public void onSuccess(PolarSheetsData result) {
                // TODO string messages
                setCompletionLabel("Generating...");
                dataCountLabel.setText("DataCount: " + result.getDataCount());
                chartPanel.setData(idNameMapping.get(id), result);
                if (!result.isComplete()) {
                    Timer timer = new Timer() {

                        @Override
                        public void run() {
                            startPullingResults(id);
                        }
                    };

                    timer.schedule(1500);

                } else {
                    // TODO string messages
                    setCompletionLabel("Generation finished!");
                    polarSheetsTrackedRacesList.changeGenerationButtonState(true);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getLocalizedMessage());
            }

        });

    }

    protected void setCompletionLabel(String string) {
        polarSheetsGenerationLabel.setText(string);
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
