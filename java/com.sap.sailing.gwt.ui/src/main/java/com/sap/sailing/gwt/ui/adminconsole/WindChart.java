package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import ca.nanometrics.gflot.client.options.SeriesOptions;
import ca.nanometrics.gflot.client.options.TickFormatter;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.gwt.ui.client.ColorMap;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class WindChart implements Component<WindChartSettings> {
    private final StringMessages stringMessages;
    private final DateTimeFormat dateFormat;
    private PlotWithOverviewModel model;
    private final Set<WindSource> windSourcesToDisplay;
    private final ColorMap<WindSource> colorMap;
    private final Map<WindSource, SeriesHandler> stripChartSeries;
    private final List<SeriesHandler> seriesHandlersInOrder;
    private final Label selectedPointLabel;
    private final ErrorReporter errorReporter;
    private PlotWithOverview plot;

    public WindChart(WindChartSettings settings, StringMessages stringMessages, ErrorReporter errorReporter) {
        super();
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.windSourcesToDisplay = new HashSet<WindSource>(settings.getWindSourcesToDisplay());
        dateFormat = DateTimeFormat.getFormat("HH:mm:ss");
        this.colorMap = new ColorMap<WindSource>();
        this.stripChartSeries = new HashMap<WindSource, SeriesHandler>();
        this.seriesHandlersInOrder = new ArrayList<SeriesHandler>();
        this.selectedPointLabel = new Label();
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.wind();
    }

    @Override
    public Widget getEntryWidget() {
        VerticalPanel vp = new VerticalPanel();
        HorizontalPanel hp = new HorizontalPanel();
        hp.add(selectedPointLabel);
        hp.setHeight("2em");
        vp.add(hp);
        model = new PlotWithOverviewModel(PlotModelStrategy.defaultStrategy());
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
        plotOptions.setSelectionOptions(new SelectionOptions().setDragging(true).setMode("x")); // select along x-axis only
        for (WindSource windSource : windSourcesToDisplay) {
            SeriesHandler series = addSeries(windSource);
            series.setOptions(SeriesType.LINES, getLinesOptions(/* visible */ windSourcesToDisplay.contains(windSource)));
            series.setOptions(SeriesType.POINTS, getPointsOptions(/* visible */ false));
            series.setVisible(false);
        }
        this.plot = new PlotWithOverview(model, plotOptions) {
            @Override
            protected void onLoad() {
                super.onLoad();
                // onLoad is called immediately after a widget becomes attached to the browser's document.
                // Use this time point to ask the chart to redraw
                redraw();
            }
        };
        // add hover listener
        plot.addHoverListener(new PlotHoverListener() {
            public void onPlotHover(Plot plot, PlotPosition position, PlotItem item) {
                if (item != null) {
                    SeriesHandler seriesHandler = seriesHandlersInOrder.get(item.getSeriesIndex());
                    WindSource windSource = getWindSource(seriesHandler);
                    selectedPointLabel.setText(windSource.name()+": "+position.getY().intValue());
                    selectedPointLabel.setVisible(true);
                } else {
                    selectedPointLabel.setVisible(false);
                }
            }
        }, /* only on data point */ false);
        plot.addSelectionListener(new SelectionListener() {
            public void selected(double x1, double y1, double x2, double y2) {
                /* TODO Remove not visible buoys from the series when user is zooming in or add them if he is zooming out.
                for (CompetitorDTO competitor : competitorsAndTimePointsDTO.getCompetitor()){
                        long[] markPassingTimes = competitorsAndTimePointsDTO.getMarkPassings(competitor);
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
        plot.setOverviewHeight(60);
        vp.add(plot);
        return vp;
    }

    private WindSource getWindSource(SeriesHandler seriesHandler) {
        for (Map.Entry<WindSource, SeriesHandler> e : stripChartSeries.entrySet()) {
            if (e.getValue() == seriesHandler) {
                return e.getKey();
            }
        }
        return null;
    }

    private SeriesHandler addSeries(WindSource windSource) {
        SeriesHandler series = model.addSeries(windSource.name(), colorMap.getColorByID(windSource));
        seriesHandlersInOrder.add(series);
        stripChartSeries.put(windSource, series);
        return series;
    }

    public void updateStripChartSeries(WindInfoForRaceDTO result) {
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (Map.Entry<WindSource, WindTrackInfoDTO> e : result.windTrackInfoByWindSource.entrySet()) {
            WindSource windSource = e.getKey();
            SeriesHandler seriesHandler = stripChartSeries.get(windSource);
            if (e.getValue().windFixes.isEmpty()) {
                if (seriesHandler != null) {
                    model.removeSeries(seriesHandler);
                    stripChartSeries.remove(windSource);
                }
            } else {
                if (seriesHandler == null) {
                    seriesHandler = addSeries(windSource);
                    seriesHandler.setOptions(SeriesType.LINES, getLinesOptions(/* visible */ windSourcesToDisplay.contains(windSource)));
                    seriesHandler.setOptions(SeriesType.POINTS, getPointsOptions(/* visible */ false));
                } else {
                    seriesHandler.clear();
                }
                for (WindDTO windFix : e.getValue().windFixes) {
                    seriesHandler.add(new DataPoint(windFix.timepoint, windFix.dampenedTrueWindFromDeg));
                    min = Math.min(min, windFix.timepoint);
                    max = Math.max(max, windFix.timepoint);
                }
                seriesHandler.setVisible(true);
            }
        }
        if (plot != null && plot.isAttached()) {
            try {
                plot.setLinearSelection(min, max); // TODO maintain previous selection
                plot.redraw();
            } catch (Exception ex) {
                errorReporter.reportError("Error trying to update strip chart: " + ex.getMessage());
            }
        }
    }

    private SeriesOptions getPointsOptions(boolean visible) {
        return new PointsSeriesOptions().setLineWidth(0).setShow(visible);
    }

    private SeriesOptions getLinesOptions(boolean visible) {
        return new LineSeriesOptions().setLineWidth(2.5).setShow(visible);
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<WindChartSettings> getSettingsDialogComponent() {
        return new WindChartSettingsDialogComponent(new WindChartSettings(windSourcesToDisplay));
    }

    /**
     * Sets the visibilities of the wind source series based on the new settings. Note that this does not
     * re-load any wind data. This has to happen by calling {@link #updateStripChartSeries(WindInfoForRaceDTO)}.
     */
    @Override
    public void updateSettings(WindChartSettings newSettings) {
        windSourcesToDisplay.clear();
        windSourcesToDisplay.addAll(newSettings.getWindSourcesToDisplay());
        for (Map.Entry<WindSource, SeriesHandler> e : stripChartSeries.entrySet()) {
            e.getValue().setVisible(windSourcesToDisplay.contains(e.getKey()));
        }
        plot.redraw();
    }

}
