package com.sap.sailing.gwt.ui.polarsheets;

import java.util.ArrayList;
import java.util.List;

import org.moxieapps.gwt.highcharts.client.events.PointSelectEvent;
import org.moxieapps.gwt.highcharts.client.events.PointSelectEventHandler;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.PolarSheetGenerationResponse;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.PolarSheetsHistogramData;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.WindStepping;
import com.sap.sailing.domain.common.impl.PolarSheetGenerationSettingsImpl;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.ErrorReporter;

/**
 * A panel containing a list of tracked races (not the ones without gps or wind data), a polar chart and a histogram chart. 
 * Plus some buttons and settings to allow the user generate polar diagrams for the selected races and view
 * histograms about the underlying data distribution of the points in the polar diagrams.
 * 
 * @author d054528 Frederik Petersen
 *
 */
public class PolarSheetsPanel extends SplitLayoutPanel implements RaceSelectionChangeListener, RegattasDisplayer, Component<PolarSheetGenerationSettings> {

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

    private Label polarSheetsGenerationLabel;
    private Label dataCountLabel;

    private final ScrollPanel leftScrollPanel;

    private PolarSheetGenerationSettings settings;
    
    private final List<PolarSheetListChangeListener> polarSheetListChangeListeners = new ArrayList<PolarSheetListChangeListener>();

    private final TextBox namingBox;

    public PolarSheetsPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            final StringMessages stringMessages, PolarSheetsEntryPoint polarSheetsEntryPoint) {
        this.polarSheetsEntryPoint = polarSheetsEntryPoint;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
     
        setSize("100%", "100%");

        VerticalPanel leftPanel = addFilteredTrackedRacesList();
        leftScrollPanel = new ScrollPanel(leftPanel);
        addWest(leftScrollPanel, Window.getClientWidth() * 0.3);
        polarSheetsGenerationLabel = createPolarSheetGenerationStatusLabel();
        leftPanel.add(polarSheetsGenerationLabel);
        dataCountLabel = new Label();
        leftPanel.add(dataCountLabel);
        HorizontalPanel namingContainer = new HorizontalPanel();
        Label namingLabel = new Label(stringMessages.sheetName() + ": ");
        namingLabel.setTitle(stringMessages.sheetNameTooltip());
        namingContainer.add(namingLabel);
        namingBox = new TextBox();
        namingContainer.add(namingBox);
        leftPanel.add(namingContainer);
        DockLayoutPanel rightPanel = new DockLayoutPanel(Unit.PCT);
        PolarSheetsChartPanel polarSheetsChartPanel = createPolarSheetsChartPanel();
        DockLayoutPanel polarChartAndControlPanel = new DockLayoutPanel(Unit.PCT);
        PolarChartControlPanel polarChartControlPanel = new PolarChartControlPanel(stringMessages,
                polarSheetsChartPanel);
        polarSheetListChangeListeners.add(polarChartControlPanel);
        polarChartAndControlPanel.setWidth("100%");
        polarChartAndControlPanel.setHeight("100%");
        polarChartAndControlPanel.addWest(polarSheetsChartPanel, 80);
        polarChartAndControlPanel.addEast(polarChartControlPanel, 20);
        
        
        rightPanel.addNorth(polarChartAndControlPanel, 70);
        histogramPanel = new PolarSheetsHistogramPanel(stringMessages);
        histogramPanel.getElement().setAttribute("align", "top");
        rightPanel.addSouth(histogramPanel, 30);

        
        add(rightPanel);
        setEventListenersForPolarSheetChart();

        PolarSheetGenerationSettings initialSettings = PolarSheetGenerationSettingsImpl.createStandardPolarSettings();
        settings = initialSettings;
        chartPanel.setSettings(initialSettings);
    }
    
    private void setEventListenersForPolarSheetChart() {
        PointSelectEventHandler pointSelectEventHandler = new PointSelectEventHandler() {
            @Override
            public boolean onSelect(PointSelectEvent pointSelectEvent) {
                int angle = (int) pointSelectEvent.getXAsLong();
                String polarSheetNameWithWind = pointSelectEvent.getSeriesName();
                String[] split = polarSheetNameWithWind.split("-");
                String polarSheetName = split[0];
                int windSpeed = Integer.parseInt(split[1]); 
                PolarSheetsData currentPolarSheetsData = chartPanel.getPolarSheetsDataMap().get(polarSheetName);
                WindStepping stepping = currentPolarSheetsData.getStepping();
                PolarSheetsHistogramData histogramData = currentPolarSheetsData.getHistogramDataMap()
                        .get(stepping.getLevelIndexForValue(windSpeed)).get(angle);
                histogramPanel.setData(histogramData);
                return true;
            }
        };
        chartPanel.setPointSelectHandler(pointSelectEventHandler);
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
        polarSheetsTrackedRacesList.setSettingsHandler(this);
    }

    protected void startPolarSheetGeneration() {
        // List conversion, to make List serializable
        final List<RegattaAndRaceIdentifier> selectedRacesInArrayList = new ArrayList<RegattaAndRaceIdentifier>();
        selectedRacesInArrayList.addAll(selectedRaces);
        polarSheetsTrackedRacesList.changeGenerationButtonState(false);
        setCompletionLabel(stringMessages.generating() + "...");
        chartPanel.showLoadingInfo();
        sailingService.generatePolarSheetForRaces(selectedRacesInArrayList, settings, namingBox.getValue(),
                new AsyncCallback<PolarSheetGenerationResponse>() {

            @Override
            public void onSuccess(PolarSheetGenerationResponse result) {
                String name = createNameForPolarSheet(result);
                dataCountLabel.setText(stringMessages.dataCount() + ": " + result.getData().getDataCount());
                chartPanel.getPolarSheetsDataMap().put(result.getId(), result.getData());
                chartPanel.setData(name, result.getData());
                setCompletionLabel(stringMessages.generationFinished());
                chartPanel.hideLoadingInfo();
                polarSheetsTrackedRacesList.changeGenerationButtonState(true);
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getLocalizedMessage());
            }
        });
        namingBox.setValue("");
    }

    private String createNameForPolarSheet(PolarSheetGenerationResponse result) {
        String sheetName = result.getName();
        int index = 0;
        String name = sheetName;
        while (chartPanel.getPolarSheetsDataMap().containsKey(name)) {
            index++;
            name = sheetName + "_" + index;
        } 
        for (PolarSheetListChangeListener listener : polarSheetListChangeListeners) {
            listener.polarSheetAdded(name);
        }
        return name;
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

    
    @Override
    protected void onLoad() {
        Timer timer = new Timer() {
            
            @Override
            public void run() {
                setWidgetSize(leftScrollPanel, Window.getClientWidth() * 0.3);
                Timer timer = new Timer() {
                    
                    @Override
                    public void run() {
                        chartPanel.onResize();
                        histogramPanel.onResize();
                    }
                };
                timer.schedule(200);
            }
        };
        timer.schedule(600);
        super.onLoad();
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<PolarSheetGenerationSettings> getSettingsDialogComponent() {
        return new PolarSheetGenerationSettingsDialogComponent(settings, stringMessages);
    }

    @Override
    public void updateSettings(PolarSheetGenerationSettings newSettings) {
        settings = newSettings;
        chartPanel.setSettings(newSettings);
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.polars();
    }

    @Override
    public Widget getEntryWidget() {
        return this;
    }

    @Override
    public String getDependentCssClassName() {
        return "polarSheet";
    }
}
