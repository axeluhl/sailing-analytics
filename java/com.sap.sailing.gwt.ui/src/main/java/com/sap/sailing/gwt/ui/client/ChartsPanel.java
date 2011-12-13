package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import ca.nanometrics.gflot.client.Axis;
import ca.nanometrics.gflot.client.DataPoint;
import ca.nanometrics.gflot.client.PlotItem;
import ca.nanometrics.gflot.client.PlotModelStrategy;
import ca.nanometrics.gflot.client.PlotPosition;
import ca.nanometrics.gflot.client.PlotWithOverview;
import ca.nanometrics.gflot.client.PlotWithOverviewModel;
import ca.nanometrics.gflot.client.SeriesHandler;
import ca.nanometrics.gflot.client.SeriesType;
import ca.nanometrics.gflot.client.event.PlotHoverListener;
import ca.nanometrics.gflot.client.event.SelectionListener;
import ca.nanometrics.gflot.client.jsni.Plot;
import ca.nanometrics.gflot.client.options.AxisOptions;
import ca.nanometrics.gflot.client.options.GridOptions;
import ca.nanometrics.gflot.client.options.LegendOptions;
import ca.nanometrics.gflot.client.options.LineSeriesOptions;
import ca.nanometrics.gflot.client.options.PlotOptions;
import ca.nanometrics.gflot.client.options.PointsSeriesOptions;
import ca.nanometrics.gflot.client.options.SelectionOptions;
import ca.nanometrics.gflot.client.options.TickFormatter;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.CompetitorInRaceDAO;
import com.sap.sailing.gwt.ui.shared.CompetitorsAndTimePointsDAO;
import com.sap.sailing.server.api.DetailType;
import com.sap.sailing.server.api.RaceIdentifier;

public class ChartsPanel extends FormPanel {
    private CompetitorInRaceDAO chartData;
    private CompetitorsAndTimePointsDAO competitorsAndTimePointsDAO = null;
    private final SailingServiceAsync sailingService;
    private DateTimeFormat dateFormat;
    private HorizontalPanel mainPanel;
    private VerticalPanel chartPanel;
    private FlowPanel legendPanel;
    private VerticalPanel configPanel;
    private DeckPanel chart;
    private final RaceIdentifier[] races;
    private int colorCounter = 0;
    private HashMap<Integer, String> idColor;
    private int selectedRace = 0;
    private int stepsToLoad = 100;
    private final StringConstants stringConstants;
    private HashMap<CompetitorDAO, Boolean> competitorVisible;
    private VerticalPanel selectCompetitors;
    private PlotWithOverview plot;
    private PlotWithOverviewModel model;
    private PlotOptions plotOptions;
    private HashMap<Integer, SeriesHandler> seriesID;
    private HashMap<CompetitorDAO, Integer> competitorID;
    private HashMap<Integer, SeriesHandler> markSeriesID;
    private HashMap<CompetitorDAO, Widget> competitorLabels;
    private int width = 800, height = 600;
    private int competitorNr = 0;

    private DetailType dataToShow = DetailType.WINDWARD_DISTANCE_TO_OVERALL_LEADER;
    private AbsolutePanel loadingPanel;

    public ChartsPanel(SailingServiceAsync sailingService, final List<CompetitorDAO> competitors,
            RaceIdentifier[] races, StringConstants stringConstants, int chartWidth, int chartHeight) {
    	width = chartWidth;
    	height = chartHeight;
    	chartData = new CompetitorInRaceDAO();
    	seriesID = new HashMap<Integer, SeriesHandler>();
    	competitorID = new HashMap<CompetitorDAO, Integer>();
    	markSeriesID = new HashMap<Integer, SeriesHandler>();
    	idColor = new HashMap<Integer, String>();
    	competitorVisible = new HashMap<CompetitorDAO, Boolean>();
    	competitorLabels = new HashMap<CompetitorDAO, Widget>();
        this.sailingService = sailingService;
        this.races = races;
        this.stringConstants = stringConstants;
        dateFormat = DateTimeFormat.getFormat("HH:mm:ss");

        for (CompetitorDAO competitor : competitors) {
            setCompetitorVisible(competitor, true);
        }
        mainPanel = new HorizontalPanel();
        mainPanel.setSpacing(5);
        chartPanel = new VerticalPanel();
        HorizontalPanel raceChooserPanel = new HorizontalPanel();
        raceChooserPanel.setSpacing(5);
        for (int i = 0; i < races.length; i++) {
            RadioButton r = new RadioButton("chooseRace");
            r.setText(races[i].toString());
            raceChooserPanel.add(r);
            if (i == 0) {
                r.setValue(true);
            }
            final int index = i;
            r.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    selectedRace = index;
                    competitorsAndTimePointsDAO = null;
                    colorCounter = 0;
                    loadData();
                }
            });
        }
        chartPanel.add(raceChooserPanel);

        loadingPanel = new AbsolutePanel();
        loadingPanel.setSize(width + "px", height + "px");

        Anchor a = new Anchor(new SafeHtmlBuilder().appendHtmlConstant("<img src=\"/images/ajax-loader.gif\"/>")
                .toSafeHtml());
        loadingPanel.add(a, width / 2 - 32 / 2, height / 2 - 32 - 2);
        chartPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        chartPanel.setSpacing(5);
        final CaptionPanel configCaption = new CaptionPanel(stringConstants.configuration());
        configCaption.setHeight("100%");
        configCaption.setVisible(false);
        initConfigPanel();
        configCaption.setContentWidget(configPanel);
        
        legendPanel = new FlowPanel();
        legendPanel.setWidth("100%");
        chartPanel.add(legendPanel);
        chart = new DeckPanel();
        chart.add(loadingPanel);
        chart.showWidget(0);
        chartPanel.add(chart);
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
        loadData();
    }
    
    private void initConfigPanel(){
    	configPanel = new VerticalPanel();
        configPanel.setSpacing(5);

        Label lblChart = new Label(stringConstants.chooseChart());
        configPanel.add(lblChart);
        final ListBox dataSelection = new ListBox();
        dataSelection.addItem(DetailTypeFormatter.format(DetailType.WINDWARD_DISTANCE_TO_OVERALL_LEADER, stringConstants),DetailType.WINDWARD_DISTANCE_TO_OVERALL_LEADER.toString());
        dataSelection.addItem(DetailTypeFormatter.format(DetailType.DISTANCE_TRAVELED, stringConstants),DetailType.DISTANCE_TRAVELED.toString());
        dataSelection.addItem(DetailTypeFormatter.format(DetailType.VELOCITY_MADE_GOOD_IN_KNOTS, stringConstants),DetailType.VELOCITY_MADE_GOOD_IN_KNOTS.toString());
        dataSelection.addItem(DetailTypeFormatter.format(DetailType.GAP_TO_LEADER_IN_SECONDS, stringConstants),DetailType.GAP_TO_LEADER_IN_SECONDS.toString());
        dataSelection.addItem(DetailTypeFormatter.format(DetailType.CURRENT_SPEED_OVER_GROUND_IN_KNOTS, stringConstants),DetailType.CURRENT_SPEED_OVER_GROUND_IN_KNOTS.toString());
        dataSelection.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                for (DetailType dt : DetailType.values()){
                    if (dt.toString().equals(dataSelection.getValue(dataSelection.getSelectedIndex()))){
                        dataToShow = dt;
                    }
                }
                competitorID.clear();
                competitorNr = 0;
                loadData();
            }
        });
        configPanel.add(dataSelection);
        Label lblSteps = new Label(stringConstants.pointsToLoad());
        configPanel.add(lblSteps);
        final TextBox txtbSteps = new TextBox();
        txtbSteps.setText("" + stepsToLoad);
        configPanel.add(txtbSteps);
        Button bttSteps = new Button(stringConstants.refresh());
        bttSteps.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                stepsToLoad = Integer.parseInt(txtbSteps.getText());
                competitorsAndTimePointsDAO = null;
                loadData();
            }
        });
        configPanel.add(bttSteps);
        selectCompetitors = new VerticalPanel();
        configPanel.add(selectCompetitors);
    }

    private void loadData() {
        final Runnable loadData = new Runnable() {
            @Override
            public void run() {
            	final List<CompetitorDAO> competitorsToLoad = new ArrayList<CompetitorDAO>();
            	for (CompetitorDAO competitor : competitorsAndTimePointsDAO.getCompetitor()){
            		if (isCompetitorVisible(competitor) && competitorID.get(competitor) == null){
            			competitorsToLoad.add(competitor);
            			
            		}
            	}
            	final CompetitorsAndTimePointsDAO competitorsAndTimePointsToLoad = new CompetitorsAndTimePointsDAO(stepsToLoad);
            	competitorsAndTimePointsToLoad.setStartTime(competitorsAndTimePointsDAO.getStartTime());
            	competitorsAndTimePointsToLoad.setTimePointOfNewestEvent(competitorsAndTimePointsDAO.getTimePointOfNewestEvent());
            	for (CompetitorDAO competitor : competitorsToLoad){
            		competitorsAndTimePointsToLoad.setMarkPassings(competitor, competitorsAndTimePointsDAO.getMarkPassings(competitor));
            	}
            	competitorsAndTimePointsToLoad.setCompetitor(competitorsToLoad.toArray(new CompetitorDAO[0]));
                ChartsPanel.this.sailingService.getCompetitorRaceData(races[selectedRace], competitorsAndTimePointsToLoad,
                        dataToShow, new AsyncCallback<CompetitorInRaceDAO>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                Window.alert(stringConstants.failedToLoadRaceData() + ": " + caught.toString());
                            }

                            @Override
                            public void onSuccess(CompetitorInRaceDAO result) {
                                fireEvent(new DataLoadedEvent());
                                for (CompetitorDAO competitor : competitorsToLoad){
                                	chartData.setRaceData(competitor, result.getRaceData(competitor));
                                	chartData.setMarkPassingData(competitor, result.getMarkPassings(competitor));
                                }
                                updateTableData(competitorsAndTimePointsToLoad.getCompetitor());
                            }
                        });
            }
        };
        if (competitorsAndTimePointsDAO != null) {
            loadData.run();
        } else {
            this.sailingService.getCompetitorsAndTimePoints(races[selectedRace], stepsToLoad,
                    new AsyncCallback<CompetitorsAndTimePointsDAO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                        	Window.alert("Failed to load race information: " + caught.toString());
                        }

                        @Override
                        public void onSuccess(CompetitorsAndTimePointsDAO result) {
                            competitorsAndTimePointsDAO = result;
                            selectCompetitors.clear();
                            legendPanel.clear();
                            competitorLabels.clear();
                            for (int i = 0; i < result.getCompetitor().length; i++) {
                                final CheckBox cb = new CheckBox(result.getCompetitor()[i].name);
                                final CompetitorDAO c = result.getCompetitor()[i];
                                if (isCompetitorVisible(c)) {
                                    cb.setValue(true);
                                }
                                cb.addClickHandler(new ClickHandler() {
                                    @Override
                                    public void onClick(ClickEvent event) {
                                    	if (cb.getValue() == true && chartData.getRaceData(c) == null){
                                    		setCompetitorVisible(c, cb.getValue());
                                    		loadData();
                                    	}
                                    	else {
                                    		setCompetitorVisible(c, cb.getValue());
                                            if (competitorID.get(c) != null){
                                            	setLegendVisible(c,cb.getValue());
                                            	getCompetitorSeries(c).setVisible(cb.getValue());
                                                getCompetitorMarkPassingSeries(c).setVisible(cb.getValue());
                                            }
                                            plot.redraw();
                                    	}
                                    }
                                });
                                selectCompetitors.add(cb);
                            }
                            if (chart.getWidgetCount() == 1){
                            	chart.add(createChart());
                            }
                            chart.showWidget(1);
                            loadData.run();
                        }
                    });
        }
    }

    private synchronized void updateTableData(CompetitorDAO[] competitorDAOs) {
        if (competitorsAndTimePointsDAO != null && chartData != null) {
            for (CompetitorDAO competitor : competitorDAOs) {
            	competitorID.put(competitor, competitorNr++);
                SeriesHandler compSeries = getCompetitorSeries(competitor);
                SeriesHandler markSeries = getCompetitorMarkPassingSeries(competitor);
                compSeries.clear();
                markSeries.clear();
                if (isCompetitorVisible(competitor) && chartData.getRaceData(competitor) != null){
                	long[] markPassingTimes = competitorsAndTimePointsDAO.getMarkPassings(competitor);
                    Double[] markPassingValues = chartData.getMarkPassings(competitor);
                    for (int j = 0; j < markPassingTimes.length; j++){
                        if (markPassingValues[j] != null && markPassingTimes != null) {
                            markSeries.add(new DataPoint(markPassingTimes[j],markPassingValues[j]));
                        }
                    }
                    for (int j = 0; j < stepsToLoad; j++) {
                        long time = competitorsAndTimePointsDAO.getTimePoints()[j];
                        if (chartData.getRaceData(competitor)[j] != null) {
                            compSeries.add(new DataPoint(time, chartData.getRaceData(competitor)[j]));
                        }
                    }
                }
                else {
                	compSeries.add(new DataPoint(0,0));
                	markSeries.add(new DataPoint(0,0));
                }
                setLegendVisible(competitor,isCompetitorVisible(competitor));
                compSeries.setVisible(isCompetitorVisible(competitor));
                markSeries.setVisible(isCompetitorVisible(competitor));
            }
        }
        if (plot != null && plot.isAttached()){
        	try {
        		plot.setLinearSelection(1, 2);
                plot.redraw();
        	}
        	catch (Exception e){
        		
        	}
        }
        return;
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

    private void setCompetitorVisible(CompetitorDAO competitor, boolean isVisible) {
        competitorVisible.put(competitor, isVisible);
    }

    private boolean isCompetitorVisible(CompetitorDAO competitor) {
        Boolean isVisible = competitorVisible.get(competitor);
        return (isVisible != null) ? isVisible : false;
    }

    private Widget createChart() {
        final Label selectedPointLabel = new Label(stringConstants.hoverOverAPoint());
        model = new PlotWithOverviewModel(PlotModelStrategy.defaultStrategy());
        plotOptions = new PlotOptions();
        plotOptions.setDefaultLineSeriesOptions(new LineSeriesOptions().setLineWidth(1).setShow(true));
        plotOptions.setDefaultPointsOptions(new PointsSeriesOptions().setShow(false));
        plotOptions.setDefaultShadowSize(2);
        AxisOptions hAxisOptions = new AxisOptions();
        hAxisOptions.setTickFormatter(new TickFormatter() {

            @Override
            public String formatTickValue(double tickValue, Axis axis) {
                return dateFormat.format(new Date((long) tickValue));
            }
        });
        plotOptions.setXAxisOptions(hAxisOptions);
        plotOptions.setLegendOptions(new LegendOptions().setShow(false));
        plotOptions.setGridOptions(new GridOptions().setHoverable(true).setMouseActiveRadius(5).setAutoHighlight(true));

        plotOptions.setSelectionOptions(new SelectionOptions().setDragging(true).setMode("x"));
        for (int i = 0; i <  competitorsAndTimePointsDAO.getCompetitor().length; i++){
        	SeriesHandler series = model.addSeries(""+i, getColorByID(i));
    		series.setOptions(SeriesType.LINES, new LineSeriesOptions().setLineWidth(2.5).setShow(true));
    		series.setOptions(SeriesType.POINTS, new PointsSeriesOptions().setLineWidth(0).setShow(false));
    		series.setVisible(false);
    		seriesID.put(i, series);
    		series = model.addSeries(i + " passed mark", getColorByID(i));
    		series.setOptions(SeriesType.LINES, new LineSeriesOptions().setLineWidth(0).setShow(false));
    		series.setOptions(SeriesType.POINTS, new PointsSeriesOptions().setLineWidth(3).setShow(true));
    		series.setVisible(false);
    		markSeriesID.put(i, series);
        }
        plot = new PlotWithOverview(model, plotOptions);
        // add hover listener
        plot.addHoverListener(new PlotHoverListener() {
            public void onPlotHover(Plot plot, PlotPosition position, PlotItem item) {
            	CompetitorDAO competitor = null;
            	for (Entry<CompetitorDAO, Integer> competitorWithID : competitorID.entrySet()) {
            		if (item != null && item.getSeries().getLabel().contains(""+competitorWithID.getValue())){
            			competitor = competitorWithID.getKey();
            		}
				}
                if (item != null && competitor != null) {
                	if (item.getSeries().getLabel().toLowerCase().contains("mark")){
                		selectedPointLabel.setText(competitor.name + " passed mark at " + dateFormat.format(new Date((long) item.getDataPoint().getX())));
                	}
                	else {
                		selectedPointLabel.setText(competitor.name + " at " + dateFormat.format(new Date((long) item.getDataPoint().getX()))
                                + ": " + item.getDataPoint().getY() + "[Unit]");
                	}
                } else {
                    selectedPointLabel.setText(stringConstants.noSelection());
                }
            }
        }, true);
        plot.addSelectionListener(new SelectionListener() {

            public void selected(double x1, double y1, double x2, double y2) {
            	/*
            	for (CompetitorDAO competitor : competitorsAndTimePointsDAO.getCompetitor()){
            		long[] markPassingTimes = competitorsAndTimePointsDAO.getMarkPassings(competitor);
                    Double[] markPassingValues = chartData.getMarkPassings(competitor);
                    SeriesHandler markSeries = getCompetitorMarkPassingSeries(competitor);
                    markSeries.clear();
                    int visibleMarkPassings = 0;
                    for (int j = 0; j < markPassingTimes.length; j++){
                        if (markPassingValues[j] != null && markPassingTimes[j] > x1 && markPassingTimes[j] < x2) {
                            markSeries.add(new DataPoint(markPassingTimes[j],markPassingValues[j]));
                            visibleMarkPassings++;
                        }
                    }
                    if (visibleMarkPassings == 0){
                    	markSeries.setVisible(false);
                    }
            	}*/
                plot.setLinearSelection(x1, x2);
            }
        });
        plot.setHeight(height- 60);
        plot.setWidth(width);
        plot.setOverviewHeight(60);
        FlowPanel panel = new FlowPanel() {
            @Override
            protected void onLoad() {
                super.onLoad();
                plot.redraw();
            }
        };
        panel.add(selectedPointLabel);
        panel.add(plot);
        return panel;
    }
    
    private String getColorByID(int id){
    	String color = idColor.get(id);
    	if (color == null || color.isEmpty()){
    		color = createHexColor(colorCounter++);
    		idColor.put(id, color);
    	}
    	return color;
    }
    
    private String createHexColor(int index){
        String rs, gs, bs;
        int r = 0, g = 0, b = 0;
        double factor = 1 - ((index/6)/6.0);
        if (index%6 < 2 || index%6 > 4){
            r = (int) (255*factor);
        }
        rs = Integer.toHexString(r);
        while(rs.length() < 2){
            rs = "0" + rs;
        }
        if (index%6 > 0 && index%6 < 4){
            g = (int) (220*factor);
        }
        gs = Integer.toHexString(g);
        while(gs.length() < 2){
            gs = "0" + gs;
        }
        if (index%6 > 2){
            b = (int) (255*factor);
        }
        bs = Integer.toHexString(b);
        while(bs.length() < 2){
            bs = "0" + bs;
        }
        return "#" + rs + gs + bs;
    }
    
    private SeriesHandler getCompetitorSeries(CompetitorDAO competitor){
    	return seriesID.get(competitorID.get(competitor));
    }
    
    private SeriesHandler getCompetitorMarkPassingSeries(CompetitorDAO competitor){
    	return markSeriesID.get(competitorID.get(competitor));
    }
    
    private void setLegendVisible(CompetitorDAO competitor, Boolean visible){
    	Widget label = competitorLabels.get(competitor);
    	if (label == null){
    		label = createCompetitorLabel(competitor.name, getColorByID(competitorID.get(competitor)));
    		competitorLabels.put(competitor, label);
    		legendPanel.add(label);
    	}
    	label.setVisible(visible);
    }

    private Widget createCompetitorLabel(String competitor, String color){
    	HorizontalPanel competitorLabel = new HorizontalPanel();
    	competitorLabel.setStyleName("chartLegend");
    	competitorLabel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    	competitorLabel.setSpacing(3);
    	Label colorSquare = new Label();
    	colorSquare.getElement().setAttribute("style", "background-color: " + color + ";");
    	colorSquare.setSize("10px", "10px");
    	competitorLabel.add(colorSquare);
    	Label legendLabel = new Label(competitor);
    	legendLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
    	competitorLabel.add(legendLabel);
    	return competitorLabel;
    }
    
    public void resize(int width, int height){
    	this.width = width;
    	this.height = height;
    	this.setSize(width + "px", height + "px");
    }
}
