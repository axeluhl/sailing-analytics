package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.ColorMap;
import com.sap.sailing.gwt.ui.client.DetailTypeFormatter;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.CompetitorInRaceDAO;
import com.sap.sailing.gwt.ui.shared.CompetitorsAndTimePointsDAO;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialog;
import com.sap.sailing.server.api.DetailType;
import com.sap.sailing.server.api.RaceIdentifier;

/**
 * ChartPanel is a GWT panel that can show one sort of competitor data (e.g. current speed over ground, windward distance to
 * leader) for different races in a chart.
 * 
 * When calling the constructor a chart is created that creates a final amount of series (so the maximum number of
 * competitors cannot be changed in one chart) which are connected to competitors, when the sailing service returns the
 * data. So {@code seriesID, competitorID and markSeriesID} are linked with the index. So if u know for example the
 * seriesID-index, you can get the competitor by calling competitorID.get(index).
 * 
 * @author Benjamin Ebling (D056866)
 * 
 */
public abstract class AbstractChartPanel<SettingsType extends ChartSettings> extends SimplePanel {
    private CompetitorInRaceDAO chartData;
    private CompetitorsAndTimePointsDAO competitorsAndTimePointsDAO = null;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final DateTimeFormat dateFormat;
    private final HorizontalPanel mainPanel;
    private final VerticalPanel chartPanel;
    private final FlowPanel legendPanel;
    private final Label title;
    private final DeckPanel chart;
    private final RaceIdentifier[] races;
    private final ColorMap<Integer> colorMap;
    private int selectedRace = 0;
    private int stepsToLoad = 100;
    private final StringMessages stringMessages;
    private final Set<CompetitorDAO> competitorVisible;
    private VerticalPanel selectCompetitors;
    private PlotWithOverview plot;
    private final List<SeriesHandler> seriesID;
    private final Set<SeriesHandler> seriesIsUsed;
    private final List<CompetitorDAO> competitorID;
    private final List<SeriesHandler> markSeriesID;
    private final HashMap<CompetitorDAO, Widget> competitorLabels;
    private final HashMap<String, String> markPassingBuoyName;
    private int width, height;

    private DetailType dataToShow = DetailType.WINDWARD_DISTANCE_TO_OVERALL_LEADER;
    private AbsolutePanel loadingPanel;

    public AbstractChartPanel(SailingServiceAsync sailingService, final List<CompetitorDAO> competitors,
            RaceIdentifier[] races, final StringMessages stringMessages, int chartWidth, int chartHeight, ErrorReporter errorReporter) {
    	width = chartWidth;
    	height = chartHeight;
    	this.errorReporter = errorReporter;
    	chartData = new CompetitorInRaceDAO();
    	seriesID = new ArrayList<SeriesHandler>();
    	seriesIsUsed = new HashSet<SeriesHandler>();
    	competitorID = new ArrayList<CompetitorDAO>();
    	markSeriesID = new ArrayList<SeriesHandler>();
    	colorMap = new ColorMap<Integer>();
    	competitorVisible = new HashSet<CompetitorDAO>();
    	competitorLabels = new HashMap<CompetitorDAO, Widget>();
    	markPassingBuoyName = new HashMap<String, String>();
        this.sailingService = sailingService;
        this.races = races;
        this.stringMessages = stringMessages;
        dateFormat = DateTimeFormat.getFormat("HH:mm:ss");

        for (CompetitorDAO competitor : competitors) {
            setCompetitorVisible(competitor, true);
        }
        mainPanel = new HorizontalPanel();
        mainPanel.setSpacing(5);
        chartPanel = new VerticalPanel();
        title = new Label(DetailTypeFormatter.format(dataToShow, stringMessages));
        title.setStyleName("chartTitle");
        chartPanel.add(title);
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
                    setCompetitorsAndTimePointsDAO(null);
                    clearChart(true);
                    loadData();
                }
            });
        }
        chartPanel.add(raceChooserPanel);
        loadingPanel = new AbsolutePanel();
        loadingPanel.setSize(width + "px", height + "px");
        Anchor a = new Anchor(new SafeHtmlBuilder().appendHtmlConstant("<img src=\"/gwt/images/ajax-loader.gif\"/>")
                .toSafeHtml());
        loadingPanel.add(a, width / 2 - 32 / 2, height / 2 - 32 - 2);
        chartPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        chartPanel.setSpacing(5);
        legendPanel = new FlowPanel();
        legendPanel.setWidth("100%");
        legendPanel.setHeight("21px");
        chartPanel.add(legendPanel);
        chart = new DeckPanel();
        chart.add(loadingPanel);
        chart.showWidget(0);
        chartPanel.add(chart);
        mainPanel.add(chartPanel);
        Anchor showConfigAnchor = new Anchor(new SafeHtmlBuilder().appendHtmlConstant(
                "<img class=\"linkNoBorder\" src=\"/gwt/images/settings.png\"/>").toSafeHtml());
        showConfigAnchor.setTitle(stringMessages.configuration());
        showConfigAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new SettingsDialog<SettingsType>(getComponent(), stringMessages).show();
            }
        });
        mainPanel.add(showConfigAnchor);
        this.add(mainPanel);
        loadData();
    }
    
    protected abstract Component<SettingsType> getComponent();
    
    protected void loadData() {
    	chart.showWidget(0);
        final Runnable loadData = new Runnable() {
            @Override
            public void run() {
                final List<CompetitorDAO> competitorsToLoad = new ArrayList<CompetitorDAO>();
                for (CompetitorDAO competitor : getCompetitorsAndTimePointsDAO().getCompetitor()) {
                    if (isCompetitorVisible(competitor) && !competitorID.contains(competitor)) {
                        competitorsToLoad.add(competitor);
                    }
                }
                final CompetitorsAndTimePointsDAO competitorsAndTimePointsToLoad = new CompetitorsAndTimePointsDAO(
                        getStepsToLoad());
                competitorsAndTimePointsToLoad.setStartTime(getCompetitorsAndTimePointsDAO().getStartTime());
                competitorsAndTimePointsToLoad.setTimePointOfNewestEvent(getCompetitorsAndTimePointsDAO()
                        .getTimePointOfNewestEvent());
                for (CompetitorDAO competitor : competitorsToLoad) {
                    competitorsAndTimePointsToLoad.setMarkPassings(competitor,
                            getCompetitorsAndTimePointsDAO().getMarkPassings(competitor));
                }
                competitorsAndTimePointsToLoad.setCompetitor(competitorsToLoad.toArray(new CompetitorDAO[0]));
                AbstractChartPanel.this.sailingService.getCompetitorRaceData(races[selectedRace],
                        competitorsAndTimePointsToLoad, dataToShow, new AsyncCallback<CompetitorInRaceDAO>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(getStringMessages().failedToLoadRaceData() + ": "
                                        + caught.toString());
                            }

                            @Override
                            public void onSuccess(CompetitorInRaceDAO result) {
                                fireEvent(new DataLoadedEvent());
                                for (CompetitorDAO competitor : competitorsToLoad) {
                                    chartData.setRaceData(competitor, result.getRaceData(competitor));
                                    chartData.setMarkPassingData(competitor, result.getMarkPassings(competitor));
                                }
                                updateTableData(competitorsAndTimePointsToLoad.getCompetitor());
                                chart.showWidget(1);
                            }
                        });
            }
        };
        if (getCompetitorsAndTimePointsDAO() != null) {
            loadData.run();
        } else {
            this.sailingService.getCompetitorsAndTimePoints(races[selectedRace], getStepsToLoad(),
                    new AsyncCallback<CompetitorsAndTimePointsDAO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                        	errorReporter.reportError("Failed to load race information: " + caught.toString());
                        }

                        @Override
                        public void onSuccess(CompetitorsAndTimePointsDAO result) {
                            setCompetitorsAndTimePointsDAO(result);
                            for (int i = 0; i < result.getCompetitor().length; i++) {
                                final CheckBox cb = new CheckBox(result.getCompetitor()[i].name);
                                final CompetitorDAO c = result.getCompetitor()[i];
                                if (isCompetitorVisible(c)) {
                                    cb.setValue(true);
                                }
                                cb.addClickHandler(new ClickHandler() {
                                    @Override
                                    public void onClick(ClickEvent event) {
                                    	if (cb.getValue() == true && !competitorID.contains(c)){
                                    		setCompetitorVisible(c, cb.getValue());
                                    		loadData();
                                    	}
                                    	else {
                                    		setCompetitorVisible(c, cb.getValue());
                                            if (competitorID.contains(c)){
                                            	setLegendVisible(c, cb.getValue());
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
                            loadData.run();
                        }
                    });
        }
    }

    private synchronized void updateTableData(CompetitorDAO[] competitorDAOs) {
    	List<SeriesHandler> series = new ArrayList<SeriesHandler>();
    	for (SeriesHandler sh : seriesID){
    		series.add(sh);
    	}
    	for (SeriesHandler sh : markSeriesID){
    		series.add(sh);
    	}
    	CompetitorDAO firstCompetitor = null;
        if (getCompetitorsAndTimePointsDAO() != null && chartData != null) {
            for (CompetitorDAO competitor : competitorDAOs) {
            	if (firstCompetitor == null){
            		firstCompetitor = competitor;
            	}
            	competitorID.add(competitor);
                SeriesHandler compSeries = getCompetitorSeries(competitor);
                seriesIsUsed.add(compSeries);
                SeriesHandler markSeries = getCompetitorMarkPassingSeries(competitor);
                seriesIsUsed.add(markSeries);
                compSeries.clear();
                markSeries.clear();
                if (isCompetitorVisible(competitor) && chartData.getRaceData(competitor) != null){
                	long starttime = System.currentTimeMillis();
                	Pair<String,Long>[] markPassingTimes = getCompetitorsAndTimePointsDAO().getMarkPassings(competitor);
                    Double[] markPassingValues = chartData.getMarkPassings(competitor);
                    for (int j = 0; j < markPassingTimes.length; j++){
                        if (markPassingValues[j] != null && markPassingTimes[j].getB() != null) {
                        	markPassingBuoyName.put(competitor.id + markPassingTimes[j].getB(), markPassingTimes[j].getA());
                            markSeries.add(new DataPoint(markPassingTimes[j].getB(),markPassingValues[j]));
                        }
                    }
                    GWT.log("Update mark passings time for " + competitor.name + ": " + (System.currentTimeMillis() - starttime));
                    starttime = System.currentTimeMillis();
                    Double[] data = chartData.getRaceData(competitor);
                    long[] timepoints = getCompetitorsAndTimePointsDAO().getTimePoints();
                    for (int j = 0; j < getStepsToLoad(); j++) {
                    	if (data[j] != null){
                    		compSeries.add(new DataPoint(timepoints[j], data[j]));
                    	}
                    }
                    GWT.log("Update data time for " + competitor.name + ": " + (System.currentTimeMillis() - starttime));
                }
                setLegendVisible(competitor, isCompetitorVisible(competitor));
                compSeries.setVisible(isCompetitorVisible(competitor));
                markSeries.setVisible(isCompetitorVisible(competitor));
                series.remove(compSeries);
                series.remove(markSeries);
            }
            if (firstCompetitor != null && chartData.getRaceData(firstCompetitor) != null) {
                for (SeriesHandler sh : series) {
                    if (!seriesIsUsed.contains(sh)) {
                        sh.clear();
                        Double[] data = chartData.getRaceData(firstCompetitor);
                        long[] timepoints = getCompetitorsAndTimePointsDAO().getTimePoints();
                        for (int j = 0; j < getStepsToLoad(); j++) {
                            if (data[j] != null) {
                                sh.add(new DataPoint(timepoints[j], data[j]));
                            }
                        }
                    }
                }
            }
        }
        if (plot != null && plot.isAttached()) {
            try {
                plot.setLinearSelection(0, 1);
                plot.redraw();
            } catch (Exception e) {

            }
        }
        return;
    }

    // DataLoaded event handling.
    public void addDataLoadedHandler(DataLoadedHandler handler) {
        this.addHandler(handler, DataLoadedEvent.TYPE);
    }

    public interface DataLoadedHandler extends com.google.gwt.event.shared.EventHandler {
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
		if (isVisible) {
			competitorVisible.add(competitor);
		} else {
			competitorVisible.remove(competitor);
		}
    }

    private boolean isCompetitorVisible(CompetitorDAO competitor) {
        return competitorVisible.contains(competitor);
    }

    private Widget createChart() {
        final Label selectedPointLabel = new Label(getStringMessages().hoverOverAPoint());
        PlotWithOverviewModel model = new PlotWithOverviewModel(PlotModelStrategy.defaultStrategy());
        PlotOptions plotOptions = new PlotOptions();
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
        for (int i = 0; i <  getCompetitorsAndTimePointsDAO().getCompetitor().length; i++){
        	SeriesHandler series = model.addSeries(""+i, colorMap.getColorByID(i));
    		series.setOptions(SeriesType.LINES, new LineSeriesOptions().setLineWidth(2.5).setShow(true));
    		series.setOptions(SeriesType.POINTS, new PointsSeriesOptions().setLineWidth(0).setShow(false));
    		series.setVisible(false);
    		seriesID.add(series);
    		series = model.addSeries(i + " passed mark", colorMap.getColorByID(i));
    		series.setOptions(SeriesType.LINES, new LineSeriesOptions().setLineWidth(0).setShow(false));
    		series.setOptions(SeriesType.POINTS, new PointsSeriesOptions().setLineWidth(3).setShow(true));
    		series.setVisible(false);
    		markSeriesID.add(series);
        }
        plot = new PlotWithOverview(model, plotOptions);
        // add hover listener
        plot.addHoverListener(new PlotHoverListener() {
            public void onPlotHover(Plot plot, PlotPosition position, PlotItem item) {
            	CompetitorDAO competitor = competitorID.get(seriesID.indexOf(item.getSeries()));
                if (item != null && competitor != null) {
                    if (item.getSeries().getLabel().toLowerCase().contains("mark")) {
                        selectedPointLabel.setText(competitor.name + " passed "
                                + markPassingBuoyName.get(competitor.id + (long) item.getDataPoint().getX()) + " at "
                                + dateFormat.format(new Date((long) item.getDataPoint().getX())));
                    } else {
                		String unit = "";
                		switch (dataToShow){
                		case CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
                			unit = getStringMessages().currentSpeedOverGroundInKnotsUnit();
                			break;
                		case DISTANCE_TRAVELED:
                			unit = getStringMessages().distanceInMetersUnit();
                			break;
                		case GAP_TO_LEADER_IN_SECONDS:
                			unit = getStringMessages().gapToLeaderInSecondsUnit();
                			break;
                		case VELOCITY_MADE_GOOD_IN_KNOTS:
                			unit = getStringMessages().velocityMadeGoodInKnotsUnit();
                			break;
                		case WINDWARD_DISTANCE_TO_OVERALL_LEADER:
                			unit = getStringMessages().windwardDistanceToGoInMetersUnit();
                		}
                		String decimalPlaces = "";
                		for (int i = 0; i < dataToShow.getPrecision(); i++){
                			if (i == 0){
                				decimalPlaces += ".";
                			}
                			decimalPlaces += "0";
                		}
                		NumberFormat numberFormat = NumberFormat.getFormat("0" +decimalPlaces);
                		selectedPointLabel.setText(competitor.name + " at " + dateFormat.format(new Date((long) item.getDataPoint().getX()))
                                + ": " + numberFormat.format(item.getDataPoint().getY()) + unit);
                	}
                } else {
                    selectedPointLabel.setText(getStringMessages().noSelection());
                }
            }
        }, true);
        plot.addSelectionListener(new SelectionListener() {

            public void selected(double x1, double y1, double x2, double y2) {
            	/* TODO Remove not visible buoys from the series when user is zooming in or add them if he is zooming out.
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
        plot.setHeight(height);
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
    
    /**
     * 
     * @param competitor
     * @return A series in the chart, that can be used to show the data of a specific competitor.
     */
    private SeriesHandler getCompetitorSeries(CompetitorDAO competitor){
    	return seriesID.get(competitorID.indexOf(competitor));
    }
    
    /**
     * 
     * @param competitor
     * @return A series in the chart, that can be used to show the mark passings.
     */
    private SeriesHandler getCompetitorMarkPassingSeries(CompetitorDAO competitor){
    	return markSeriesID.get(competitorID.indexOf(competitor));
    }
    
    /**
     * Decide if the legend for a specific competitor should be shown or not.
     */
    private void setLegendVisible(CompetitorDAO competitor, boolean visible) {
    	Widget label = competitorLabels.get(competitor);
    	if (label == null){
    		label = createCompetitorLabel(competitor.name, colorMap.getColorByID(competitorID.indexOf(competitor)));
    		competitorLabels.put(competitor, label);
    		legendPanel.add(label);
    	}
    	label.setVisible(visible);
    }

    /**
     * 
     * @param competitor The competitor for the legend.
     * @param color The color for the colored square next to the label.
     * @return A widget, that can be used for the legend of the chart.
     */
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
    
    /**
     * Clears the whole chart and empties cached data.
     * 
     * @param clearCheckBoxes Declares whether the checkboxes for the visibility of the competitors should be cleared too. Should be true, when you change the race to show.
     */
    protected void clearChart(boolean clearCheckBoxes) {
        if (clearCheckBoxes) {
            selectCompetitors.clear();
            seriesIsUsed.clear();
        }
        competitorID.clear();
        legendPanel.clear();
        competitorLabels.clear();
        for (SeriesHandler series : seriesID) {
            series.clear();
        }
        for (SeriesHandler series : markSeriesID) {
            series.clear();
        }
    }

    public String getLocalizedShortName() {
        return DetailTypeFormatter.format(dataToShow, getStringMessages());
    }

    public Widget getEntryWidget() {
        return this;
    }

    public boolean hasSettings() {
        return true;
    }
    
    public ChartSettings getAbstractSettings() {
        return new ChartSettings(getStepsToLoad());
    }
    
    public void updateSettings(ChartSettings newSettings) {
        setStepsToLoad(newSettings.getStepsToLoad());
        setCompetitorsAndTimePointsDAO(null);
        clearChart(true);
        loadData();
    }

    protected StringMessages getStringMessages() {
        return stringMessages;
    }

    protected int getStepsToLoad() {
        return stepsToLoad;
    }

    protected void setStepsToLoad(int stepsToLoad) {
        this.stepsToLoad = stepsToLoad;
    }
    
    protected DetailType getDataToShow() {
        return this.dataToShow;
    }

    protected CompetitorsAndTimePointsDAO getCompetitorsAndTimePointsDAO() {
        return competitorsAndTimePointsDAO;
    }

    protected void setCompetitorsAndTimePointsDAO(CompetitorsAndTimePointsDAO competitorsAndTimePointsDAO) {
        this.competitorsAndTimePointsDAO = competitorsAndTimePointsDAO;
    }
}
