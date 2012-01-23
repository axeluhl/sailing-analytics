package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.DetailTypeFormatter;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorInRaceDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorsAndTimePointsDTO;
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
 * @author Benjamin Ebling (D056866), Axel Uhl (d043530)
 * 
 */
public abstract class AbstractChartPanel<SettingsType extends ChartSettings> extends SimplePanel implements CompetitorSelectionChangeListener {
    private CompetitorInRaceDTO chartData;
    private CompetitorsAndTimePointsDTO competitorsAndTimePointsDTO = null;
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
    private PlotWithOverview plot;
    private final List<SeriesHandler> seriesID;
    private final Set<SeriesHandler> seriesIsUsed;
    private final List<CompetitorDTO> competitorID;
    private final List<SeriesHandler> markSeriesID;
    private final HashMap<CompetitorDTO, Widget> competitorLabels;
    private final HashMap<String, String> markPassingBuoyName;
    private int width, height;

    private DetailType dataToShow;
    private AbsolutePanel loadingPanel;
    private final CompetitorSelectionProvider competitorSelectionProvider;

    public AbstractChartPanel(SailingServiceAsync sailingService,
            CompetitorSelectionProvider competitorSelectionProvider, RaceIdentifier[] races,
            final StringMessages stringMessages, int chartWidth, int chartHeight, ErrorReporter errorReporter,
            DetailType dataToShow) {
        width = chartWidth;
    	height = chartHeight;
    	this.dataToShow = dataToShow;
    	this.competitorSelectionProvider = competitorSelectionProvider;
    	competitorSelectionProvider.addCompetitorSelectionChangeListener(this);
    	this.errorReporter = errorReporter;
    	chartData = new CompetitorInRaceDTO();
    	seriesID = new ArrayList<SeriesHandler>();
    	seriesIsUsed = new HashSet<SeriesHandler>();
    	competitorID = new ArrayList<CompetitorDTO>();
    	markSeriesID = new ArrayList<SeriesHandler>();
    	colorMap = new ColorMap<Integer>();
    	competitorLabels = new HashMap<CompetitorDTO, Widget>();
    	markPassingBuoyName = new HashMap<String, String>();
        this.sailingService = sailingService;
        this.races = races;
        this.stringMessages = stringMessages;
        dateFormat = DateTimeFormat.getFormat("HH:mm:ss");
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
                    setCompetitorsAndTimePointsDTO(null);
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
                final List<CompetitorDTO> competitorsToLoad = new ArrayList<CompetitorDTO>();
                for (CompetitorDTO competitor : competitorSelectionProvider.getAllCompetitors()) {
                    if (isCompetitorVisible(competitor) && !competitorID.contains(competitor)) {
                        competitorsToLoad.add(competitor);
                    }
                }
                final CompetitorsAndTimePointsDTO competitorsAndTimePointsToLoad = new CompetitorsAndTimePointsDTO(
                        getStepsToLoad());
                competitorsAndTimePointsToLoad.setStartTime(getCompetitorsAndTimePointsDTO().getStartTime());
                competitorsAndTimePointsToLoad.setTimePointOfNewestEvent(getCompetitorsAndTimePointsDTO()
                        .getTimePointOfNewestEvent());
                for (CompetitorDTO competitor : competitorsToLoad) {
                    competitorsAndTimePointsToLoad.setMarkPassings(competitor,
                            getCompetitorsAndTimePointsDTO().getMarkPassings(competitor));
                }
                competitorsAndTimePointsToLoad.setCompetitors(competitorsToLoad.toArray(new CompetitorDTO[0]));
                AbstractChartPanel.this.sailingService.getCompetitorRaceData(races[selectedRace],
                        competitorsAndTimePointsToLoad, dataToShow, new AsyncCallback<CompetitorInRaceDTO>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(getStringMessages().failedToLoadRaceData() + ": "
                                        + caught.toString());
                            }

                            @Override
                            public void onSuccess(CompetitorInRaceDTO result) {
                                fireEvent(new DataLoadedEvent());
                                for (CompetitorDTO competitor : competitorsToLoad) {
                                    chartData.setRaceData(competitor, result.getRaceData(competitor));
                                    chartData.setMarkPassingData(competitor, result.getMarkPassings(competitor));
                                }
                                updateTableData(competitorsAndTimePointsToLoad.getCompetitors());
                                chart.showWidget(1);
                            }
                        });
            }
        };
        if (getCompetitorsAndTimePointsDTO() != null) {
            loadData.run();
        } else {
            this.sailingService.getCompetitorsAndTimePoints(races[selectedRace], getStepsToLoad(),
                    new AsyncCallback<CompetitorsAndTimePointsDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(stringMessages.failedToLoadRaceInformation(caught.toString()));
                        }

                        @Override
                        public void onSuccess(CompetitorsAndTimePointsDTO result) {
                            setCompetitorsAndTimePointsDTO(result);
                            if (chart.getWidgetCount() == 1){
                            	chart.add(createChart());
                            }
                            loadData.run();
                        }
                    });
        }
    }

    @Override
    public void addedToSelection(CompetitorDTO competitor) {
        loadData();
    }
    
    @Override
    public void removedFromSelection(CompetitorDTO competitor) {
        if (competitorID.contains(competitor)) {
            setLegendVisible(competitor, false);
            getCompetitorSeries(competitor).setVisible(false);
            getCompetitorMarkPassingSeries(competitor).setVisible(false);
        }
        plot.redraw();
    }
    
    private synchronized void updateTableData(CompetitorDTO[] competitorDTOs) {
    	List<SeriesHandler> series = new ArrayList<SeriesHandler>();
    	for (SeriesHandler sh : seriesID){
    		series.add(sh);
    	}
    	for (SeriesHandler sh : markSeriesID){
    		series.add(sh);
    	}
    	CompetitorDTO firstCompetitor = null;
        if (getCompetitorsAndTimePointsDTO() != null && chartData != null) {
            for (CompetitorDTO competitor : competitorDTOs) {
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
                	Pair<String,Long>[] markPassingTimes = getCompetitorsAndTimePointsDTO().getMarkPassings(competitor);
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
                    long[] timepoints = getCompetitorsAndTimePointsDTO().getTimePoints();
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
                        long[] timepoints = getCompetitorsAndTimePointsDTO().getTimePoints();
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
                if (competitorDTOs != null && competitorDTOs.length > 0) {
                    plot.setLinearSelection(0, 1);
                }
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

    private boolean isCompetitorVisible(CompetitorDTO competitor) {
        return competitorSelectionProvider.isSelected(competitor);
    }

    private Widget createChart() {
        final Label selectedPointLabel = new Label(getStringMessages().hoverOverAPoint());
        final PlotWithOverviewModel model = new PlotWithOverviewModel(PlotModelStrategy.defaultStrategy());
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
        for (int i = 0; i <  getCompetitorsAndTimePointsDTO().getCompetitors().length; i++){
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
                // FIXME can the indexOf ever find anything? seriesID is a List<SeriesHandler>; item.getSeries() returns a Series object
//                CompetitorDTO competitor = competitorID.get(seriesID.indexOf(item.getSeries()));
                Integer index = item.getSeriesIndex() / 2;
                CompetitorDTO competitor = competitorID.get(index);
                if (item != null && competitor != null) {
                    if (item.getSeries().getLabel().toLowerCase().contains("mark")) {
                        selectedPointLabel.setText(stringMessages.competitorPassedMarkAtDate(competitor.name,
                                markPassingBuoyName.get(competitor.id + (long) item.getDataPoint().getX()),
                                dateFormat.format(new Date((long) item.getDataPoint().getX()))));
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
                		selectedPointLabel.setText(stringMessages.valueForCompetitorAt(competitor.name,
                		        dateFormat.format(new Date((long) item.getDataPoint().getX())),
                		        numberFormat.format(item.getDataPoint().getY()) + unit));
                	}
                } else {
                    selectedPointLabel.setText(getStringMessages().noSelection());
                }
            }
        }, true);
        plot.addSelectionListener(new SelectionListener() {
            public void selected(double x1, double y1, double x2, double y2) {
            	//Remove not visible buoys from the series when user is zooming in or add them if he is zooming out.
                    //Make series invisible didn't work, so they're removed from the Model instead
                    //TODO How to re-add the series to the model? How to modify the selection, if the 'overview-bar' is used?
//            	for (CompetitorDTO competitor : competitorID){
//            	    Pair<String,Long>[] markPassingTimes = competitorsAndTimePointsDTO.getMarkPassings(competitor);
//                    Double[] markPassingValues = chartData.getMarkPassings(competitor);
//                    boolean markPassingsInRange = false;
//                    
//                    SeriesHandler markSeries = getCompetitorMarkPassingSeries(competitor);
//                    markSeries.clear();
//                    
//                    for (int j = 0; j < markPassingTimes.length; j++){
//                        double passingTime = markPassingTimes[j].getB().doubleValue();
//                        if (markPassingValues[j] != null && x1 < passingTime && passingTime < x2) {
//                            markSeries.add(new DataPoint(passingTime ,markPassingValues[j]));
//                            markPassingsInRange = true;
//                        }
//                    }
//                    
//                    if (!markPassingsInRange){
//                        model.removeSeries(markSeries);
//                        markSeriesID.remove(markSeries);
//                    } else {
//                        //Here you should re-add the series. The code below does'nt work
//                        if (!markSeriesID.contains(markSeries)) {
//                            CompetitorDTO[] competitors = competitorsAndTimePointsDTO.getCompetitor();
//                            int index = -1;
//                            for (CompetitorDTO competitorDTO : competitors) {
//                                if (competitor.equals(competitorDTO)) {
//                                    break;
//                                }
//                                index++;
//                            }
//                            SeriesHandler series = model
//                                    .addSeries(index + " passed mark", colorMap.getColorByID(index));
//                            series.setOptions(SeriesType.LINES, new LineSeriesOptions().setLineWidth(0).setShow(false));
//                            series.setOptions(SeriesType.POINTS, new PointsSeriesOptions().setLineWidth(3)
//                                    .setShow(true));
//                            series.setVisible(true);
//                            markSeriesID.add(index, series);
//                        }
//                    }
//            	}
                //Instead of making the mark passings invisible, refactor the selection range, so that there is no blank space in the plot
                ArrayList<Double> x1Values = new ArrayList<Double>();
                ArrayList<Double> x2Values = new ArrayList<Double>();
                boolean markPassingInRange = false;
                
                competitorLoop:
                for (CompetitorDTO competitor : competitorID) {
                    ArrayList<Pair<Double, Double>> negativeDeltas = new ArrayList<Pair<Double, Double>>();
                    ArrayList<Pair<Double, Double>> positiveDeltas = new ArrayList<Pair<Double, Double>>();
                    Pair<String, Long>[] markPassingTimes = competitorsAndTimePointsDTO.getMarkPassings(competitor);
                    Double[] markPassingValues = chartData.getMarkPassings(competitor);
                    
                    for (int i = 0; i < markPassingValues.length; i++) {
                        double markPassing = markPassingTimes[i].getB().doubleValue();
                        if (markPassingValues[i] != null && (markPassing < x1 || markPassing > x2)) {
                            double delta = markPassing < x1 ? markPassing - x1 : markPassing - x2;
                            Pair<Double, Double> p = new Pair<Double, Double>(delta, markPassing);
                            if (delta < 0) {
                                negativeDeltas.add(p);
                            } else {
                                positiveDeltas.add(p);
                            }
                        } else if (markPassingValues[i] != null) {
                            markPassingInRange = true;
                            break competitorLoop;
                        }
                    }
                    
                    Comparator<Pair<Double, Double>> comp = new Comparator<Pair<Double,Double>>() {
                        @Override
                        public int compare(Pair<Double, Double> p1, Pair<Double, Double> p2) {
                            return p1.getA().compareTo(p2.getA());
                        }
                    };
                    
                    Collections.sort(negativeDeltas, comp);
                    x1Values.add(negativeDeltas.get(negativeDeltas.size() - 1).getB());
                    Collections.sort(positiveDeltas, comp);
                    x2Values.add(positiveDeltas.get(0).getB());
                }
                
                //If there are mark passings between x1 and x2 no refactoring is needed
                if (!markPassingInRange) {
                    Collections.sort(x1Values);
                    x1 = x1Values.get(0) - 2000; //Increase x1 by 2 seconds prevent small blank space
                    Collections.sort(x2Values);
                    x2 = x2Values.get(x2Values.size() - 1) + 2000; //Increase x2 by 2 seconds prevent small blank space
                }
                
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
    private SeriesHandler getCompetitorSeries(CompetitorDTO competitor){
    	return seriesID.get(competitorID.indexOf(competitor));
    }
    
    /**
     * 
     * @param competitor
     * @return A series in the chart, that can be used to show the mark passings.
     */
    private SeriesHandler getCompetitorMarkPassingSeries(CompetitorDTO competitor){
    	return markSeriesID.get(competitorID.indexOf(competitor));
    }
    
    /**
     * Decide if the legend for a specific competitor should be shown or not.
     */
    private void setLegendVisible(CompetitorDTO competitor, boolean visible) {
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

    /**
     * Updates the settings known to be contained in {@link ChartSettings}. Subclasses have to update settings provided
     * by subclasses thereof. Subclasses also need to call {@link #clearChart(boolean)} and {@link #loadData()} after
     * updating all settings.
     */
    public void updateSettingsOnly(ChartSettings newSettings) {
        setStepsToLoad(newSettings.getStepsToLoad());
        setCompetitorsAndTimePointsDTO(null);
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
    
    /**
     * Updates the {@link #dataToShow} field but does not yet {@link #clearChart(boolean) clear the chart} nor
     * {@link #loadData load the data}.
     */
    protected void setDataToShow(DetailType dataToShow) {
        this.dataToShow = dataToShow;
    }

    protected CompetitorsAndTimePointsDTO getCompetitorsAndTimePointsDTO() {
        return competitorsAndTimePointsDTO;
    }

    protected void setCompetitorsAndTimePointsDTO(CompetitorsAndTimePointsDTO competitorsAndTimePointsDTO) {
        this.competitorsAndTimePointsDTO = competitorsAndTimePointsDAO;
    }
}
