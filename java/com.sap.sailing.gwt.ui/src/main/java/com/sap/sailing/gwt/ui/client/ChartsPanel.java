package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.ajaxloader.client.ArrayHelper;
import com.google.gwt.core.client.JsArray;
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
import com.google.gwt.user.client.ui.CheckBox;
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
import com.google.gwt.visualization.client.Selection;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.events.SelectHandler;
import com.google.gwt.visualization.client.formatters.NumberFormat;
import com.google.gwt.visualization.client.visualizations.corechart.AxisOptions;
import com.google.gwt.visualization.client.visualizations.corechart.LineChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.CompetitorAndTimePointsDAO;
import com.sap.sailing.gwt.ui.shared.DetailType;
import com.sap.sailing.gwt.ui.shared.Pair;
import com.sap.sailing.gwt.ui.shared.RaceIdentifier;

public class ChartsPanel extends FormPanel {
    private List<Pair<CompetitorDAO, Double[]>> chartData = null;
    private CompetitorAndTimePointsDAO competitorAndTimePointsDAO = null;
    private LineChart chart;
    private final SailingServiceAsync sailingService;
    private HorizontalPanel mainPanel;
    private VerticalPanel chartPanel;
    private final DeckPanel deckPanel;
    private final RaceIdentifier[] races;
    private int selectedRace = 0;
    private int stepsToLoad = 100;
    private int startPoint = 0;
    private int endPoint = stepsToLoad;
    private final StringConstants stringConstants;
    private boolean chartLoaded = false;
    private boolean dataLoaded = false;
    private int chartWidth;
    private int chartHeight;
    private HashMap<String, Boolean> competitorVisible = new HashMap<String, Boolean>();
    private VerticalPanel selectCompetitors;
    private NumberFormat chartNumberFormat;
    
    public static final int DECK_PANEL_INDEX_LOADING = 0;
    public static final int DECK_PANEL_INDEX_CHART = 1;
    
    private DetailType dataToShow = DetailType.WINDWARD_DISTANCE_TO_OVERALL_LEADER;
    private AbsolutePanel loadingPanel;

    public ChartsPanel(SailingServiceAsync sailingService, final List<CompetitorDAO> competitors,
            RaceIdentifier[] races, StringConstants stringConstants, int chartWidth, int chartHeight) {
        this.sailingService = sailingService;
        this.races = races;
        this.stringConstants = stringConstants;
        this.chartWidth = chartWidth;
        this.chartHeight = chartHeight;
        for (CompetitorDAO competitor : competitors){
            setCompetitorVisible(competitor, true);
        }
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
                    competitorAndTimePointsDAO = null;
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
        dataSelection.addItem(DetailType.WINDWARD_DISTANCE_TO_OVERALL_LEADER.toString());
        dataSelection.addItem(DetailType.DISTANCE_TRAVELED.toString());
        dataSelection.addItem(DetailType.VELOCITY_MADE_GOOD_IN_KNOTS.toString());
        dataSelection.addItem(DetailType.GAP_TO_LEADER_IN_SECONDS.toString());
        dataSelection.addItem(DetailType.CURRENT_SPEED_OVER_GROUND_IN_KNOTS.toString());
        dataSelection.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                for (DetailType dt : DetailType.values()){
                    if (dt.toString().equals(dataSelection.getItemText(dataSelection.getSelectedIndex()))){
                        dataToShow = dt;
                    }
                }
                
                loadData();
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
                competitorAndTimePointsDAO = null;
                loadData();
            }
        });
        configPanel.add(bttSteps);
        HorizontalPanel startPointPanel = new HorizontalPanel();
        Label lblStartPoint = new Label("Start point:");
        startPointPanel.add(lblStartPoint);
        final TextBox txtbStartPoint = new TextBox();
        txtbStartPoint.setText(""+ startPoint);
        startPointPanel.add(txtbStartPoint);
        configPanel.add(startPointPanel);
        HorizontalPanel endPointPanel = new HorizontalPanel();
        Label lblEndPoint = new Label("End point:");
        endPointPanel.add(lblEndPoint);
        final TextBox txtbEndPoint = new TextBox();
        txtbEndPoint.setText(""+endPoint);
        endPointPanel.add(txtbEndPoint);
        configPanel.add(endPointPanel);
        selectCompetitors = new VerticalPanel();
        configPanel.add(selectCompetitors);
        Button bttSetPoints = new Button("Set Points");
        bttSetPoints.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                startPoint = Integer.parseInt(txtbStartPoint.getText());
                endPoint = Integer.parseInt(txtbEndPoint.getText());
                loadData();
            }
        });
        configPanel.add(bttSetPoints);

        final Runnable onLoadCallback = new Runnable() {

            @Override
            public void run() {
                chart = new LineChart(prepareTableData(), getOptions());
                deckPanel.add(chart);
                chartLoaded = true;
                if (chartLoaded && dataLoaded){
                    deckPanel.showWidget(DECK_PANEL_INDEX_CHART);
                    chart.draw(prepareTableData(), getOptions());
//                    setMarkPassingSelection();
//                    chart.addSelectHandler(new SelectHandler() {
//                        
//                        @Override
//                        public void onSelect(SelectEvent event) {
//                            setMarkPassingSelection();
//                        }
//                    });
                }
                fireEvent(new DataLoadedEvent());
                
            }
        };
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
        VisualizationUtils.loadVisualizationApi(onLoadCallback, LineChart.PACKAGE);
        loadData();
    }

    private Options getOptions() {
        Options opt = Options.create();
        opt.setWidth(chartWidth);
        opt.setHeight(chartHeight);
        opt.setTitle(dataToShow.toString());
        AxisOptions hAxisOptions = AxisOptions.create();
        hAxisOptions.setTitle("time");
        opt.setHAxisOptions(hAxisOptions);

        AxisOptions vAxisOptions = AxisOptions.create();
        switch (dataToShow) {
        case VELOCITY_MADE_GOOD_IN_KNOTS:
            vAxisOptions.setTitle(stringConstants.speed() + " " + stringConstants.in() + " " + stringConstants.velocityMadeGoodInKnotsUnit());
            break;
        case GAP_TO_LEADER_IN_SECONDS:
            vAxisOptions.setTitle(stringConstants.time() + " " + stringConstants.in() + " " + stringConstants.secondsUnit());
            break;
        case WINDWARD_DISTANCE_TO_OVERALL_LEADER:
            vAxisOptions.setTitle(stringConstants.distance() + " " + stringConstants.in() + " " + stringConstants.metersUnit());
            break;
        case DISTANCE_TRAVELED:
            vAxisOptions.setTitle(stringConstants.distance() + " " + stringConstants.in() + " " + stringConstants.metersUnit());
            break;
        case CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
            vAxisOptions.setTitle(stringConstants.speed() + " " + stringConstants.in() + " " + stringConstants.currentSpeedOverGroundInKnotsUnit());
            break;
        }
        opt.setVAxisOptions(vAxisOptions);
        return opt;
    }
    
    private void loadData(){
        deckPanel.showWidget(DECK_PANEL_INDEX_LOADING);
        dataLoaded = false;
        final Runnable loadData = new Runnable() {
            
            @Override
            public void run() {
                ChartsPanel.this.sailingService.getCompetitorRaceData(races[selectedRace], competitorAndTimePointsDAO, dataToShow,
                        new AsyncCallback<List<Pair<CompetitorDAO, Double[]>>>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                Window.alert(stringConstants.failedToLoadRaceData());
                            }

                            @Override
                            public void onSuccess(List<Pair<CompetitorDAO, Double[]>> result) {
                                fireEvent(new DataLoadedEvent());
                                chartData = result;
                                dataLoaded = true;
                                if (chartLoaded && dataLoaded){
                                    deckPanel.showWidget(DECK_PANEL_INDEX_CHART);
                                    chart.draw(prepareTableData(), getOptions());
                                    setMarkPassingSelection();
                                    chart.addSelectHandler(new SelectHandler() {
                                        
                                        @Override
                                        public void onSelect(SelectEvent event) {
                                            setMarkPassingSelection();
                                        }
                                    });
                                }
                            }
                        });
            }
        };
        if (competitorAndTimePointsDAO != null){
            loadData.run();
        }
        else {
            this.sailingService.getCompetitorAndTimePoints(races[selectedRace], stepsToLoad, new AsyncCallback<CompetitorAndTimePointsDAO>() {

                @Override
                public void onFailure(Throwable caught) {
                    
                }

                @Override
                public void onSuccess(CompetitorAndTimePointsDAO result) {
                    competitorAndTimePointsDAO = result;
                    selectCompetitors.clear();
                    for (int i = 0; i < result.getCompetitor().length; i++){
                        final CheckBox cb = new CheckBox(result.getCompetitor()[i].name);
                        final CompetitorDAO c = result.getCompetitor()[i];
                        if (isCompetitorVisible(c)){
                            cb.setValue(true);
                        }
                        cb.addClickHandler(new ClickHandler() {
                            
                            @Override
                            public void onClick(ClickEvent event) {
                                setCompetitorVisible(c, cb.getValue());
                                setMarkPassingSelection();
                            }
                        });
                        selectCompetitors.add(cb);
                    }
                    loadData.run();
                }
            });
        }
    }

    private AbstractDataTable prepareTableData() {
        DataTable data = DataTable.create();
        data.addColumn(ColumnType.STRING, stringConstants.time());
        if (competitorAndTimePointsDAO != null){
            for (CompetitorDAO c : competitorAndTimePointsDAO.getCompetitor()) {
                data.addColumn(ColumnType.NUMBER, c.name);
            }
        }
        if (chartData != null && chartData.get(0) != null) {
            int length = 0;
            for (int i = 0; i < chartData.size(); i++) {
                length = (length < chartData.get(i).getB().length) ? chartData.get(i).getB().length : length;
            }
            data.addRows(endPoint-startPoint);
            for (int i = 0; i < data.getNumberOfRows(); i++) {
                long time = competitorAndTimePointsDAO.getTimePoints()[startPoint+i] - competitorAndTimePointsDAO.getStartTime();
                String minutes = "" + Math.abs((time/60000));
                if (minutes.length() < 2){
                    minutes = ((time < 0)? "-" : "") +"0" + minutes;
                }
                String seconds = "" + Math.abs((time/1000)%60);
                if (seconds.length() < 2){
                    seconds= "0" + seconds;
                }
                data.setValue(i, 0, minutes + ":" + seconds + " min");
            }
            String suffix = "";
            switch (dataToShow){
            case CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
                suffix = stringConstants.currentSpeedOverGroundInKnotsUnit();
                break;
            case DISTANCE_TRAVELED:
                suffix = stringConstants.metersUnit();
                break;
            case GAP_TO_LEADER_IN_SECONDS:
                suffix = stringConstants.secondsUnit();
                break;
            case VELOCITY_MADE_GOOD_IN_KNOTS:
                suffix = stringConstants.currentSpeedOverGroundInKnotsUnit();
                break;
            case WINDWARD_DISTANCE_TO_OVERALL_LEADER:
                suffix = stringConstants.metersUnit();
                break;
            }
            chartNumberFormat = NumberFormat.create(createNumberFormatOptions(suffix));
            for (int i = 0; i < chartData.size(); i++) {
                for (int j = 0; j < endPoint-startPoint; j++) {
                    if (chartData.get(i).getB()[startPoint+j] != null && isCompetitorVisible(chartData.get(i).getA())) {
                        data.setValue(j, (i + 1), chartData.get(i).getB()[startPoint+j]);
                        
                    }
                }
                chartNumberFormat.format(data, i+1);
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
    
    private void setCompetitorVisible (CompetitorDAO competitor, boolean isVisible){
        boolean changed = isCompetitorVisible(competitor) != isVisible;
        competitorVisible.put(competitor.id, isVisible);
        if (changed && chart != null){
            chart.draw(prepareTableData(), getOptions());
        }
    }
    
    private boolean isCompetitorVisible(CompetitorDAO competitor){
        Boolean isVisible = competitorVisible.get(competitor.id);
        return (isVisible != null) ? isVisible : false;
    }

    private com.google.gwt.visualization.client.formatters.NumberFormat.Options createNumberFormatOptions(String suffix){
        com.google.gwt.visualization.client.formatters.NumberFormat.Options options = com.google.gwt.visualization.client.formatters.NumberFormat.Options.create();
        options.setFractionDigits(dataToShow.getPrecision());
        options.setSuffix(suffix);
        return options;
    }
    
    private void setMarkPassingSelection(){
        ArrayList<Selection> selections = new ArrayList<Selection>();
        Long[] timePoints = competitorAndTimePointsDAO.getTimePoints();
        for (int column = 0; column < competitorAndTimePointsDAO.getCompetitor().length; column++) {
            int currentMarkPassing = 0;
            Long[] markPassing = competitorAndTimePointsDAO.getMarkPassings(competitorAndTimePointsDAO.getCompetitor()[column]);
            
            for (int row = 0; currentMarkPassing < markPassing.length; row++){
                if (startPoint+row > 0 && timePoints[startPoint+row-1] < markPassing[currentMarkPassing] && timePoints[startPoint+row] > markPassing[currentMarkPassing]){
                    selections.add(Selection.createCellSelection(row, column+1));
                    currentMarkPassing++;
                }
                else if (startPoint + row == 0 && timePoints[startPoint+row] > markPassing[currentMarkPassing]){
                    selections.add(Selection.createCellSelection(row, column+1));
                    currentMarkPassing++;
                }
                if (endPoint-startPoint <= row-1){
                    currentMarkPassing++;
                    row = 0;
                }
            }
        }
        JsArray<Selection> sel = ArrayHelper.toJsArray(selections.toArray(new Selection[0]));
        if (chartLoaded && dataLoaded){
            chart.setSelections(sel);
        }
    }
}
