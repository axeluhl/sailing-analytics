package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.corechart.AxisOptions;
import com.google.gwt.visualization.client.visualizations.corechart.LineChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.CompetitorWithRaceDAO;
import com.sap.sailing.gwt.ui.shared.RaceIdentifier;

public class CompareCompetitorsPanel extends FormPanel {
    private final List<CompetitorDAO> competitors;
    private CompetitorWithRaceDAO[][] chartData = null;
    private LineChart chart;
    private final SailingServiceAsync sailingService;
    private HorizontalPanel mainPanel;
    private VerticalPanel chartPanel;
    private final DeckPanel deckPanel;
    private final RaceIdentifier[] races;
    private int selectedRace = 0;
    private int stepsToLoad = 100;
    private final StringConstants stringConstants;
    private boolean chartLoaded = false;
    private boolean dataLoaded = false;
    private int chartWidth;
    private int chartHeight;
    
    public static final int DECK_PANEL_INDEX_LOADING = 0;
    public static final int DECK_PANEL_INDEX_CHART = 1;
    
    public static final int SHOW_CURRENT_SPEED_OVER_GROUND = 0;
    public static final int SHOW_VELOCITY_MADE_GOOD = 1;
    public static final int SHOW_GAP_TO_LEADER = 2;
    public static final int SHOW_WINDWARD_DISTANCE_TO_GO = 3;
    public static final int SHOW_DISTANCE_TRAVELED = 4;
    private int dataToShow = SHOW_CURRENT_SPEED_OVER_GROUND;
    private AbsolutePanel loadingPanel;

    public CompareCompetitorsPanel(SailingServiceAsync sailingService, final List<CompetitorDAO> competitors,
            RaceIdentifier[] races, StringConstants stringConstants, int chartWidth, int chartHeight) {
        this.sailingService = sailingService;
        this.competitors = competitors;
        this.races = races;
        this.stringConstants = stringConstants;
        this.chartWidth = chartWidth;
        this.chartHeight = chartHeight;
        mainPanel = new HorizontalPanel();
        mainPanel.setSpacing(5);
        chartPanel = new VerticalPanel();
        HorizontalPanel raceChooserPanel = new HorizontalPanel();
        raceChooserPanel.setSpacing(5);
        for (int i = 0; i < races.length; i++){
            RadioButton r = new RadioButton("chooseRace");
            r.setText(races[i].toString());
            raceChooserPanel.add(r);
            if (i == 0){
                r.setValue(true);
            }
            final int index = i;
            r.addClickHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    selectedRace = index;
                    loadData();
                }
            });
        }
        chartPanel.add(raceChooserPanel);
        
        loadingPanel = new AbsolutePanel ();
        loadingPanel.setSize(chartWidth + "px", chartHeight + "px");
        
        Anchor a = new Anchor(new SafeHtmlBuilder().appendHtmlConstant(
                "<img src=\"/images/ajax-loader.gif\"/>").toSafeHtml());
        loadingPanel.add(a,chartWidth/2-32/2,chartHeight/2-32-2);
        chartPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        chartPanel.setSpacing(5);
        deckPanel = new DeckPanel();
        
        deckPanel.add(loadingPanel);
        final CaptionPanel configCaption = new CaptionPanel(stringConstants.configuration());
        configCaption.setHeight("100%");
        configCaption.setVisible(false);
        VerticalPanel configPanel = new VerticalPanel();
        configCaption.setContentWidget(configPanel);
        configPanel.setSpacing(5);
        
        Label lblChart = new Label(stringConstants.chooseChart());
        configPanel.add(lblChart);
        final ListBox dataSelection = new ListBox();
        dataSelection.addItem(stringConstants.speedOverGroundLong(), "" + SHOW_CURRENT_SPEED_OVER_GROUND);
        dataSelection.addItem(stringConstants.distanceTraveled(), "" + SHOW_DISTANCE_TRAVELED);
        dataSelection.addItem(stringConstants.velocityMadeGoodLong(), "" + SHOW_VELOCITY_MADE_GOOD);
        dataSelection.addItem(stringConstants.gapToLeaderLong(), "" + SHOW_GAP_TO_LEADER);
        dataSelection.addItem(stringConstants.windwardDistanceToGoInMeters(), "" + SHOW_WINDWARD_DISTANCE_TO_GO);
        dataSelection.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                dataToShow = Integer.parseInt(dataSelection.getValue(dataSelection.getSelectedIndex()));
                if (chart != null) {
                    chart.draw(prepareTableData(), getOptions());
                }
            }
        });
        configPanel.add(dataSelection);
        Label lblSteps = new Label("Points to load:");
        configPanel.add(lblSteps);
        final TextBox txtbSteps = new TextBox();
        txtbSteps.setText("" + stepsToLoad);
        configPanel.add(txtbSteps);
        Button bttSteps = new Button(stringConstants.refresh());
        bttSteps.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                stepsToLoad = Integer.parseInt(txtbSteps.getText());
                loadData();
            }
        });
        configPanel.add(bttSteps);

        final Runnable onLoadCallback = new Runnable() {

            @Override
            public void run() {
                chart = new LineChart(prepareTableData(), getOptions());
                deckPanel.add(chart);
                if (chartLoaded && dataLoaded){
                    deckPanel.showWidget(DECK_PANEL_INDEX_CHART);
                }
                chartLoaded = true;
                fireEvent(new DataLoadedEvent());
                
            }
        };
        VisualizationUtils.loadVisualizationApi(onLoadCallback, LineChart.PACKAGE);
        loadData();
        chartPanel.add(deckPanel);
        mainPanel.add(chartPanel);
        Anchor showConfigAnchor = new Anchor(new SafeHtmlBuilder().appendHtmlConstant(
                "<img class=\"linkNoBorder\" src=\"/images/settings.png\"/>").toSafeHtml());
        showConfigAnchor.setTitle(stringConstants.configuration());
        showConfigAnchor.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                configCaption.setVisible(!configCaption.isVisible());
            }
        });
        mainPanel.add(showConfigAnchor);
        mainPanel.add(configCaption);
        this.add(mainPanel);
    }

    private Options getOptions() {
        Options opt = Options.create();
        opt.setWidth(chartWidth);
        opt.setHeight(chartHeight);
        switch (dataToShow) {
        case SHOW_VELOCITY_MADE_GOOD:
            opt.setTitle(stringConstants.velocityMadeGoodLong());
            break;
        case SHOW_GAP_TO_LEADER:
            opt.setTitle(stringConstants.gapToLeaderLong());
            break;
        case SHOW_WINDWARD_DISTANCE_TO_GO:
            opt.setTitle(stringConstants.windwardDistanceToGoInMeters());
            break;
        case SHOW_DISTANCE_TRAVELED:
            opt.setTitle(stringConstants.distanceTraveled());
            break;
        default:
            opt.setTitle(stringConstants.speedOverGroundLong());
        }
        AxisOptions hAxisOptions = AxisOptions.create();
        hAxisOptions.setTitle("time");
        opt.setHAxisOptions(hAxisOptions);

        AxisOptions vAxisOptions = AxisOptions.create();
        switch (dataToShow) {
        case SHOW_VELOCITY_MADE_GOOD:
            vAxisOptions.setTitle(stringConstants.speed() + " " + stringConstants.in() + " " + stringConstants.velocityMadeGoodInKnotsUnit());
            break;
        case SHOW_GAP_TO_LEADER:
            vAxisOptions.setTitle(stringConstants.time() + " " + stringConstants.in() + " " + stringConstants.secondsUnit());
            break;
        case SHOW_WINDWARD_DISTANCE_TO_GO:
            vAxisOptions.setTitle(stringConstants.distance() + " " + stringConstants.in() + " " + stringConstants.metersUnit());
            break;
        case SHOW_DISTANCE_TRAVELED:
            vAxisOptions.setTitle(stringConstants.distance() + " " + stringConstants.in() + " " + stringConstants.metersUnit());
            break;
        default:
            vAxisOptions.setTitle(stringConstants.speed() + " " + stringConstants.in() + " " + stringConstants.currentSpeedOverGroundInKnotsUnit());
            break;
        }
        opt.setVAxisOptions(vAxisOptions);
        return opt;
    }
    
    private void loadData(){
        deckPanel.showWidget(DECK_PANEL_INDEX_LOADING);
        dataLoaded = false;
        this.sailingService.getCompetitorRaceData(races[selectedRace], stepsToLoad,
                new AsyncCallback<CompetitorWithRaceDAO[][]>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert(stringConstants.failedToLoadRaceData());
                    }

                    @Override
                    public void onSuccess(CompetitorWithRaceDAO[][] result) {
                        fireEvent(new DataLoadedEvent());
                        chartData = result;
                        dataLoaded = true;
                        if (chartLoaded && dataLoaded){
                            deckPanel.showWidget(DECK_PANEL_INDEX_CHART);
                            chart.draw(prepareTableData(), getOptions());
                        }
                    }
                });
    }

    private AbstractDataTable prepareTableData() {
        DataTable data = DataTable.create();
        data.addColumn(ColumnType.STRING, stringConstants.time());
        for (int i = 0; i < competitors.size(); i++) {
            data.addColumn(ColumnType.NUMBER, competitors.get(i).name);
        }

        if (chartData == null) {
            return data;
        }
        if (chartData[0] != null) {
            int length = 0;
            for (int i = 0; i < chartData.length; i++) {
                length = (length < chartData[i].length) ? chartData[i].length : length;
            }
            data.addRows(length);
            long startTime = chartData[0][0].getStartTime();
            for (int n = 0; n < chartData[0].length; n++) {
                long time = chartData[0][n].getLegEntry().timeInMilliseconds - startTime;
                String minutes = "" + Math.abs((time/60000));
                if (minutes.length() < 2){
                    minutes = ((time < 0)? "-" : "") +"0" + minutes;
                }
                String seconds = "" + Math.abs((time/1000)%60);
                if (seconds.length() < 2){
                    seconds= "0" + seconds;
                }
                data.setValue(n, 0, minutes + ":" + seconds);
            }
            for (int i = 0; i < chartData.length; i++) {
                double distanceTraveledOnPreviousLegs = 0;
                double lastTraveledDistance = 0;
                for (int j = 0; j < chartData[i].length; j++) {
                    Double value = null;
                    switch (dataToShow) {
                    case SHOW_GAP_TO_LEADER:
                        value = chartData[i][j].getLegEntry().gapToLeaderInSeconds;
                        break;
                    case SHOW_VELOCITY_MADE_GOOD:
                        value = chartData[i][j].getLegEntry().velocityMadeGoodInKnots;
                        break;
                    case SHOW_WINDWARD_DISTANCE_TO_GO:
                        value = chartData[i][j].getLegEntry().windwardDistanceToGoInMeters;
                        break;
                    case SHOW_DISTANCE_TRAVELED:
                        value = chartData[i][j].getLegEntry().distanceTraveledInMeters;
                        if (value != null){
                            if (lastTraveledDistance > value){
                                distanceTraveledOnPreviousLegs += lastTraveledDistance;
                            }
                            lastTraveledDistance = value;
                            value += distanceTraveledOnPreviousLegs;
                        }
                        break;
                    default:
                        value = chartData[i][j].getLegEntry().currentSpeedOverGroundInKnots;
                    }
                    if (value != null) {
                        data.setValue(j, (i + 1), value);
                    }
                }
            }
        }

        return data;
    }

    // DataLoaded event handling.
    public void addDataLoadedHandler(DataLoadedHandler handler) {
        this.addHandler(handler, DataLoadedEvent.TYPE);
    }

    interface DataLoadedHandler extends com.google.gwt.event.shared.EventHandler {
        public void onDataLoaded(DataLoadedEvent event);
    }

    public static class DataLoadedEvent extends GwtEvent<DataLoadedHandler> {
        public static Type<DataLoadedHandler> TYPE = new Type<DataLoadedHandler>();

        public DataLoadedEvent() {
            super();
        }

        @Override
        protected void dispatch(DataLoadedHandler handler) {
            handler.onDataLoaded(this);
        }

        @Override
        public com.google.gwt.event.shared.GwtEvent.Type<DataLoadedHandler> getAssociatedType() {
            return TYPE;
        }

    }

}
