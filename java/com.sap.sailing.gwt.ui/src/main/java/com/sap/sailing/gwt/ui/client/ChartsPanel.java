package com.sap.sailing.gwt.ui.client;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
import ca.nanometrics.gflot.client.options.Markings;
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
import com.sap.sailing.gwt.ui.shared.CompetitorAndTimePointsDAO;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.CompetitorInRaceDAO;
import com.sap.sailing.gwt.ui.shared.DetailType;
import com.sap.sailing.gwt.ui.shared.RaceIdentifier;

public class ChartsPanel extends FormPanel {
    private CompetitorInRaceDAO chartData = null;
    private CompetitorAndTimePointsDAO competitorAndTimePointsDAO = null;
    private final SailingServiceAsync sailingService;
    private DateTimeFormat dateFormat;
    private HorizontalPanel mainPanel;
    private VerticalPanel chartPanel;
    private HorizontalPanel legendPanel;
    private Widget chart;
    private final RaceIdentifier[] races;
    private int selectedRace = 0;
    private int stepsToLoad = 100;
    private final StringConstants stringConstants;
    private HashMap<String, Boolean> competitorVisible = new HashMap<String, Boolean>();
    private VerticalPanel selectCompetitors;
    private PlotWithOverview plot;
    private PlotOptions plotOptions;
    private HashMap<CompetitorDAO, SeriesHandler> competitorSeries;
    private HashMap<CompetitorDAO, SeriesHandler> competitorMarkPassingSeries;

    private DetailType dataToShow = DetailType.WINDWARD_DISTANCE_TO_OVERALL_LEADER;
    private AbsolutePanel loadingPanel;

    public ChartsPanel(SailingServiceAsync sailingService, final List<CompetitorDAO> competitors,
            RaceIdentifier[] races, StringConstants stringConstants, int chartWidth, int chartHeight) {
    	competitorSeries = new HashMap<CompetitorDAO, SeriesHandler>();
    	competitorMarkPassingSeries = new HashMap<CompetitorDAO, SeriesHandler>();
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
                    competitorAndTimePointsDAO = null;
                    loadData();
                }
            });
        }
        chartPanel.add(raceChooserPanel);

        loadingPanel = new AbsolutePanel();
        loadingPanel.setSize(chartWidth + "px", chartHeight + "px");

        Anchor a = new Anchor(new SafeHtmlBuilder().appendHtmlConstant("<img src=\"/images/ajax-loader.gif\"/>")
                .toSafeHtml());
        loadingPanel.add(a, chartWidth / 2 - 32 / 2, chartHeight / 2 - 32 - 2);
        chartPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        chartPanel.setSpacing(5);
        final CaptionPanel configCaption = new CaptionPanel(stringConstants.configuration());
        configCaption.setHeight("100%");
        configCaption.setVisible(false);
        VerticalPanel configPanel = new VerticalPanel();
        configCaption.setContentWidget(configPanel);
        configPanel.setSpacing(5);

        Label lblChart = new Label(stringConstants.chooseChart());
        configPanel.add(lblChart);
        final ListBox dataSelection = new ListBox();
        dataSelection.addItem(DetailType.WINDWARD_DISTANCE_TO_OVERALL_LEADER.toString(stringConstants),
                DetailType.WINDWARD_DISTANCE_TO_OVERALL_LEADER.toString());
        dataSelection.addItem(DetailType.DISTANCE_TRAVELED.toString(stringConstants),
                DetailType.DISTANCE_TRAVELED.toString());
        dataSelection.addItem(DetailType.VELOCITY_MADE_GOOD_IN_KNOTS.toString(stringConstants),
                DetailType.VELOCITY_MADE_GOOD_IN_KNOTS.toString());
        dataSelection.addItem(DetailType.GAP_TO_LEADER_IN_SECONDS.toString(stringConstants),
                DetailType.GAP_TO_LEADER_IN_SECONDS.toString());
        dataSelection.addItem(DetailType.CURRENT_SPEED_OVER_GROUND_IN_KNOTS.toString(stringConstants),
                DetailType.CURRENT_SPEED_OVER_GROUND_IN_KNOTS.toString());
        dataSelection.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                for (DetailType dt : DetailType.values()) {
                    if (dt.toString().equals(dataSelection.getValue(dataSelection.getSelectedIndex()))) {
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
                updateTableData();
            }
        });
        configPanel.add(bttSteps);
        selectCompetitors = new VerticalPanel();
        configPanel.add(selectCompetitors);
        
        legendPanel = new HorizontalPanel();
        legendPanel.setSpacing(3);
        legendPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        legendPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        chartPanel.add(legendPanel);
        
        chart = createChart();
        chartPanel.add(chart);
        mainPanel.add(chartPanel);
        Anchor showConfigAnchor = new Anchor(new SafeHtmlBuilder().appendHtmlConstant(
                "<img class=\"linkNoBorder\" src=\"/images/settings.png\"/>").toSafeHtml());
        showConfigAnchor.setTitle(stringConstants.configuration());
        showConfigAnchor.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                configCaption.setVisible(!configCaption.isVisible());
                updateTableData();
                plot.redraw();
            }
        });

        mainPanel.add(showConfigAnchor);
        mainPanel.add(configCaption);
        this.add(mainPanel);
        loadData();
    }

    private void loadData() {
        final Runnable loadData = new Runnable() {

            @Override
            public void run() {
                ChartsPanel.this.sailingService.getCompetitorRaceData(races[selectedRace], competitorAndTimePointsDAO,
                        dataToShow, new AsyncCallback<CompetitorInRaceDAO>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                Window.alert(stringConstants.failedToLoadRaceData());
                            }

                            @Override
                            public void onSuccess(CompetitorInRaceDAO result) {
                                fireEvent(new DataLoadedEvent());
                                chartData = result;
                                chartPanel.remove(chart);
                                chart = createChart();
                                chartPanel.add(chart);
                            }
                        });
            }
        };
        if (competitorAndTimePointsDAO != null) {
            loadData.run();
        } else {
            this.sailingService.getCompetitorAndTimePoints(races[selectedRace], stepsToLoad,
                    new AsyncCallback<CompetitorAndTimePointsDAO>() {

                        @Override
                        public void onFailure(Throwable caught) {

                        }

                        @Override
                        public void onSuccess(CompetitorAndTimePointsDAO result) {
                            competitorAndTimePointsDAO = result;
                            selectCompetitors.clear();
                            for (int i = 0; i < result.getCompetitor().length; i++) {
                                final CheckBox cb = new CheckBox(result.getCompetitor()[i].name);
                                final CompetitorDAO c = result.getCompetitor()[i];
                                if (isCompetitorVisible(c)) {
                                    cb.setValue(true);
                                }
                                cb.addClickHandler(new ClickHandler() {

                                    @Override
                                    public void onClick(ClickEvent event) {
                                        setCompetitorVisible(c, cb.getValue());
                                        updateTableData();
                                    }
                                });
                                selectCompetitors.add(cb);
                            }
                            loadData.run();
                        }
                    });
        }
    }

    private void updateTableData() {
        if (plot == null || plotOptions == null) {
            return;
        }
        if (competitorAndTimePointsDAO != null && chartData != null) {
            Markings ms = new Markings();
            //plotOptions.setLegendOptions(new LegendOptions().setShow(true).setNumOfColumns(8).setPosition(LegendOptions.NORTH_EAST));
            legendPanel.clear();
            for (int i = 0; i < competitorAndTimePointsDAO.getCompetitor().length; i++) {
                CompetitorDAO competitor = competitorAndTimePointsDAO.getCompetitor()[i];
                SeriesHandler compSeries = getCompetitorSeries(competitor, i);
                compSeries.setVisible(isCompetitorVisible(competitor));
                SeriesHandler markSeries = getCompetitorMarkPassingSeries(competitor, i);
                if (isCompetitorVisible(competitor)){
                	addLegend(competitor.name, i);
                }
                markSeries.setVisible(isCompetitorVisible(competitor));
                Long[] markPassingTimes = competitorAndTimePointsDAO.getMarkPassings(competitor);
                Double[] markPassingValues = chartData.getMarkPassings(competitor);
                for (int j = 0; j < markPassingTimes.length; j++){
                    if (markPassingValues[j] != null) {
                        markSeries.add(new DataPoint(markPassingTimes[j],markPassingValues[j]));
                    }
                }
                for (int j = 0; j < stepsToLoad; j++) {
                    long time = competitorAndTimePointsDAO.getTimePoints()[j];
                    if (chartData.getRaceData(competitor)[j] != null) {
                        compSeries.add(new DataPoint(time, chartData.getRaceData(competitor)[j]));
                    }
                }
            }
            plotOptions.setGridOptions(new GridOptions().setHoverable(true).setMarkings(ms).setClickable(true)
                    .setAutoHighlight(true));
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
        //boolean changed = isCompetitorVisible(competitor) != isVisible;
        competitorVisible.put(competitor.id, isVisible);
        /*
         * if (changed && chart != null){ chart.draw(prepareTableData(), getOptions()); }
         */
    }

    private boolean isCompetitorVisible(CompetitorDAO competitor) {
        Boolean isVisible = competitorVisible.get(competitor.id);
        return (isVisible != null) ? isVisible : false;
    }

    public Widget createChart() {

        final Label selectedPointLabel = new Label("Hover over a point!");

        PlotWithOverviewModel model = new PlotWithOverviewModel(PlotModelStrategy.defaultStrategy());
        plotOptions = new PlotOptions();
        plotOptions.setDefaultLineSeriesOptions(new LineSeriesOptions().setLineWidth(1).setShow(true));
        plotOptions.setDefaultPointsOptions(new PointsSeriesOptions().setShow(false));
        plotOptions.setDefaultShadowSize(1);
        AxisOptions hAxisOptions = new AxisOptions();
        hAxisOptions.setTickFormatter(new TickFormatter() {

            @Override
            public String formatTickValue(double tickValue, Axis axis) {
                return dateFormat.format(new Date((long) tickValue));
            }
        });
        plotOptions.setXAxisOptions(hAxisOptions);
        plotOptions.setDefaultColors(new String[]{createHexColor(0),createHexColor(0),createHexColor(1),createHexColor(1),createHexColor(2),createHexColor(2)});
        plotOptions.setLegendOptions(new LegendOptions().setShow(false));

        plotOptions.setSelectionOptions(new SelectionOptions().setDragging(true).setMode("x"));
        plot = new PlotWithOverview(model, plotOptions);
        // add hover listener
        plot.addHoverListener(new PlotHoverListener() {
            public void onPlotHover(Plot plot, PlotPosition position, PlotItem item) {
                if (item != null) {
                    selectedPointLabel.setText(item.getSeries().getLabel() + " x: " + item.getDataPoint().getX()
                            + ", y: " + item.getDataPoint().getY());
                } else {
                    selectedPointLabel.setText("No selection!");
                }
            }
        }, false);
        plot.addSelectionListener(new SelectionListener() {

            public void selected(double x1, double y1, double x2, double y2) {
                plot.setLinearSelection(x1, x2);
            }
        });
        updateTableData();

        // addMarkPassingMarkers(plotOptions);

        plot.setHeight(500);
        plot.setWidth(800);
        plot.setOverviewHeight(60);

        FlowPanel panel = new FlowPanel() {
            @Override
            protected void onLoad() {
                super.onLoad();
                plot.setLinearSelection(0, stepsToLoad);
                plot.redraw();
            }
        };
        panel.add(selectedPointLabel);
        panel.add(plot);
        return panel;
    }
    
    private String createHexColor(int index){
        String rs, gs, bs;
        int r = 0, g = 0, b = 0;
        double factor = 1 - ((index/6)/6);
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
    
    private SeriesHandler getCompetitorSeries(CompetitorDAO competitor, int index){
    	SeriesHandler series  = competitorSeries.get(competitor);
    	if (series == null){
    		series = plot.getModel().addSeries(competitor.name);
    		series.setOptions(SeriesType.LINES, new LineSeriesOptions().setLineWidth(2.5).setShow(true).setFillColor(createHexColor(index)));
    		series.setOptions(SeriesType.POINTS, new PointsSeriesOptions().setLineWidth(0).setShow(false));
    		competitorSeries.put(competitor, series);
    	}
    	return series;
    }
    
    private SeriesHandler getCompetitorMarkPassingSeries(CompetitorDAO competitor, int index){
    	SeriesHandler series  = competitorMarkPassingSeries.get(competitor);
    	if (series == null){
    		series = plot.getModel().addSeries(competitor.name + " mark passing");
    		series.setOptions(SeriesType.LINES, new LineSeriesOptions().setLineWidth(0).setShow(false));
    		series.setOptions(SeriesType.POINTS, new PointsSeriesOptions().setLineWidth(3).setShow(true).setFillColor(createHexColor(index)));
    		competitorMarkPassingSeries.put(competitor, series);
    	}
    	return series;
    }
    
    private void addLegend(String competitor, int index){
    	Label colorSquare = new Label();
    	colorSquare.getElement().setAttribute("style", "background-color: " + createHexColor(index) + ";");
    	colorSquare.setSize("10px", "10px");
    	legendPanel.add(colorSquare);
    	Label legendLabel = new Label(competitor);
        legendPanel.add(legendLabel);
    }

}
