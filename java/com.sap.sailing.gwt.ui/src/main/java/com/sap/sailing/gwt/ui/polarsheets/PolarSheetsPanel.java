package com.sap.sailing.gwt.ui.polarsheets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.events.PointMouseOverEvent;
import org.moxieapps.gwt.highcharts.client.events.PointMouseOverEventHandler;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
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

public class PolarSheetsPanel extends DockLayoutPanel implements RaceSelectionChangeListener, RegattaDisplayer {

    // TODO UI stuff
    public static final String POLARSHEETS_STYLE = "polarSheets";

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

    private ListBox nameListBox;

    public PolarSheetsPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages, PolarSheetsEntryPoint polarSheetsEntryPoint) {
        super(Unit.PCT);
        this.polarSheetsEntryPoint = polarSheetsEntryPoint;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        
        idNameMapping = new HashMap<String, String>();
        nameIdMapping = new HashMap<String, String>();
        setSize("100%", "100%");

        VerticalPanel leftPanel = addFilteredTrackedRacesList();
        ScrollPanel leftScrollPanel = new ScrollPanel(leftPanel);
        addWest(leftScrollPanel, 40);
        polarSheetsGenerationLabel = createPolarSheetGenerationStatusLabel();
        leftPanel.add(polarSheetsGenerationLabel);
        dataCountLabel = new Label();
        leftPanel.add(dataCountLabel);
        DockLayoutPanel rightPanel = new DockLayoutPanel(Unit.PCT);
        PolarSheetsChartPanel polarSheetsChartPanel = createPolarSheetsChartPanel();
        rightPanel.addNorth(polarSheetsChartPanel, 70);
        histogramPanel = new PolarSheetsHistogramPanel(stringMessages);
        histogramPanel.getElement().setAttribute("align", "top");
        rightPanel.addSouth(histogramPanel, 30);
        nameListBox = new ListBox();
        leftPanel.add(nameListBox);
        Button exportButton = new Button("Export");
        setExportButtonListener(exportButton);
        leftPanel.add(exportButton);
        add(rightPanel);

        asyncActionsExecutor = new AsyncActionsExecutor();
        setEventListenersForPolarSheetChart();
    }

    private void setExportButtonListener(Button exportButton) {
        ClickHandler handler = new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                String name = nameListBox.getItemText(nameListBox.getSelectedIndex());
                if (name!=null && !name.isEmpty()) {
                    StringBuffer exportData = new StringBuffer();
                    exportData.append(name + "\n");
                    Series[] seriesPerWindspeed = chartPanel.getSeriesPerWindspeedForName(name);
                    for (Series series : seriesPerWindspeed) {
                        if (series == null) {
                            continue;
                        }
                        String nameOfSeries = chartPanel.getNameForSeries(series);
                        String[] split = nameOfSeries.split("-");
                        int windSpeed = Integer.parseInt(split[2]);
                        Point[] points = series.getPoints();
                        if (windSpeed == 4) {
                            int[] degs = {0,50,60,110,130,180};
                            exportData.append("4 " + createStringForDegrees(points, degs) + "\n");
                        } else if (windSpeed == 6) {
                            int[] degs = {0,47,60,110,135,180};
                            exportData.append("6 " + createStringForDegrees(points, degs) + "\n");
                        } else if (windSpeed == 8) {
                            int[] degs = {0,43,60,110,135,180};
                            exportData.append("8 " + createStringForDegrees(points, degs) + "\n");
                        } else if (windSpeed == 10) {
                            int[] degs = {0,41,60,110,140,180};
                            exportData.append("10 " + createStringForDegrees(points, degs) + "\n");
                        } else if (windSpeed == 12) {
                            int[] degs = {0,40,60,110,145,180};
                            exportData.append("12 " + createStringForDegrees(points, degs) + "\n");
                        } else if (windSpeed == 14) {
                            int[] degs = {0,39,60,110,155,180};
                            exportData.append("14 " + createStringForDegrees(points, degs) + "\n");
                        } else if (windSpeed == 16) {
                            int[] degs = {0,38,60,110,155,180};
                            exportData.append("16 " + createStringForDegrees(points, degs) + "\n");
                        } else if (windSpeed == 20) {
                            int[] degs = {0,38,60,110,160,180};
                            exportData.append("20 " + createStringForDegrees(points, degs) + "\n");
                        } else if (windSpeed == 25) {
                            int[] degs = {0,39,60,110,168,180};
                            exportData.append("25 " + createStringForDegrees(points, degs) + "\n");
                        } else if (windSpeed == 30) {
                            int[] degs = {0,41,60,110,157,180};
                            exportData.append("30 " + createStringForDegrees(points, degs) + "\n");
                        }
                        
                    }
                    
                    Window.alert(exportData.toString());
                }
                
            }
        };
        
        exportButton.addClickHandler(handler);
        
    }

    private String createStringForDegrees(Point[] points, int[] degrees) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < degrees.length; i++) {
            int deg = degrees[i];
            double average;
            if (deg > 0 && deg < 180) {
                double windRight = points[deg].getY().doubleValue();
                double windLeft = points[360 - deg].getY().doubleValue();
                average = (windRight + windLeft) / 2;
            } else {
                average = points[deg].getY().doubleValue();
            }
            NumberFormat fmt = NumberFormat.getDecimalFormat();
            fmt.overrideFractionDigits(2);
            buffer.append(deg + " " + fmt.format(average) + " ");
        }
        return buffer.toString();
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

    private PolarSheetsChartPanel createPolarSheetsChartPanel() {
        chartPanel = new PolarSheetsChartPanel(stringMessages);
        return chartPanel;
    }

    private VerticalPanel addFilteredTrackedRacesList() {
        VerticalPanel trackedRacesPanel = new VerticalPanel();
        trackedRacesPanel.setWidth("100%");

        createPolarSheetsTrackedRacesList();
        trackedRacesPanel.add(polarSheetsTrackedRacesList);
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
        nameListBox.addItem(name);
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
