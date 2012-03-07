package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.PlotLine;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.events.ChartClickEvent;
import org.moxieapps.gwt.highcharts.client.events.ChartClickEventHandler;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.gwt.ui.client.ColorMap;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class WindChart implements Component<WindChartSettings>, RaceSelectionChangeListener, TimeListener {
    private static final int LINE_WIDTH = 1;
    private final StringMessages stringMessages;
    private final Set<WindSourceType> windSourceTypesToDisplay;
    
    /**
     * Holds one series for each wind source for which data has been received.
     */
    private final Map<WindSource, Series> windSourceDirectionSeries;
    private final Map<WindSource, Series> windSourceSpeedSeries;
    
    private final ErrorReporter errorReporter;
    private final SailingServiceAsync sailingService;
    private final Chart chart;
    private final Timer timer;
    private final DateTimeFormat dateFormat = DateTimeFormat.getFormat("HH:mm:ss");
    
    private Long timeOfEarliestRequestInMillis;
    private Long timeOfLatestRequestInMillis;
    private RaceIdentifier selectedRaceIdentifier;
    
    private final ColorMap<WindSource> colorMap;

    private final SimplePanel mainPanel;

    /**
     * @param raceSelectionProvider
     *            if <code>null</code>, this chart won't update its contents automatically upon race selection change;
     *            otherwise, whenever the selection changes, the wind data of the race selected now is loaded from the
     *            server and displayed in this chart. If no race is selected, the chart is cleared.
     */
    public WindChart(SailingServiceAsync sailingService, RaceSelectionProvider raceSelectionProvider,
            Timer timer, WindChartSettings settings, final StringMessages stringMessages, ErrorReporter errorReporter, int chartHeight) {
        super();
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.windSourceDirectionSeries = new HashMap<WindSource, Series>();
        this.windSourceSpeedSeries = new HashMap<WindSource, Series>();
        this.colorMap = new ColorMap<WindSource>();
        this.windSourceTypesToDisplay = new HashSet<WindSourceType>();
        this.timer = timer;
        chart = new Chart()
                .setZoomType(Chart.ZoomType.X)
                .setSpacingRight(20)
                .setWidth100()
                .setHeight(chartHeight)
                .setChartTitle(new ChartTitle().setText(stringMessages.wind()))
                .setChartSubtitle(new ChartSubtitle().setText(stringMessages.clickAndDragToZoomIn()))
                .setLegend(new Legend().setEnabled(true))
                .setLinePlotOptions(new LinePlotOptions().setLineWidth(LINE_WIDTH).setMarker(
                        new Marker().setEnabled(false).setHoverState(
                                new Marker().setEnabled(true).setRadius(4))).setShadow(false)
                                    .setHoverStateLineWidth(LINE_WIDTH));
        final NumberFormat numberFormat = NumberFormat.getFormat("0");
        chart.setToolTip(new ToolTip().setEnabled(true).setFormatter(new ToolTipFormatter() {
            @Override
            public String format(ToolTipData toolTipData) {
                final String unit = toolTipData.getSeriesName().startsWith(stringMessages.fromDeg()+" ") ? stringMessages.degreesShort() :
                    stringMessages.averageSpeedInKnotsUnit();
                return "<b>" + toolTipData.getSeriesName() + (toolTipData.getPointName() != null ? " "+toolTipData.getPointName() : "")
                        + "</b><br/>" +  
                        dateFormat.format(new Date(toolTipData.getXAsLong())) + ": " +
                        numberFormat.format(toolTipData.getYAsDouble()) + unit;
            }
        }));
        chart.setClickEventHandler(new ChartClickEventHandler() {
            @Override
            public boolean onClick(ChartClickEvent chartClickEvent) {
                WindChart.this.timer.setTime(chartClickEvent.getXAxisValueAsLong());
                return true;
            }
        });
        chart.getXAxis().setType(Axis.Type.DATE_TIME).setMaxZoom(10000) // ten seconds
                .setAxisTitleText(stringMessages.time());
        chart.getYAxis(0).setAxisTitleText(stringMessages.fromDeg()).setStartOnTick(false).setShowFirstLabel(false);
        chart.getYAxis(1).setOpposite(true).setAxisTitleText(stringMessages.speed()+" ("+stringMessages.averageSpeedInKnotsUnit()+")")
            .setStartOnTick(false).setShowFirstLabel(false).setGridLineWidth(0).setMinorGridLineWidth(0);
        
        mainPanel = new SimplePanel();
        mainPanel.setWidget(chart);
        updateSettings(settings);
        if (raceSelectionProvider != null) {
            raceSelectionProvider.addRaceSelectionChangeListener(this);
            onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
        }
        showVisibleSeries();
        timer.addTimeListener(this);
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.wind();
    }

    @Override
    public Widget getEntryWidget() {
        return mainPanel;
    }

    private void showVisibleSeries() {
        Series[] currentlyVisible = chart.getSeries();
        Set<Series> visible = new HashSet<Series>();
        for (Series series : currentlyVisible) {
            visible.add(series);
        }
        for (Map.Entry<WindSource, Series> e : windSourceDirectionSeries.entrySet()) {
            if (windSourceTypesToDisplay.contains(e.getKey().getType())) {
                if (!visible.contains(e.getValue())) {
                    chart.addSeries(e.getValue());
                } else {
                    visible.remove(e.getValue());
                }
            }
        }
        for (Map.Entry<WindSource, Series> e : windSourceSpeedSeries.entrySet()) {
            if (windSourceTypesToDisplay.contains(e.getKey().getType())) {
                if (!visible.contains(e.getValue())) {
                    chart.addSeries(e.getValue());
                } else {
                    visible.remove(e.getValue());
                }
            }
        }
        for (Series seriesToRemove : visible) {
            chart.removeSeries(seriesToRemove);
        }
    }

    /**
     * Creates the series for the <code>windSource</code> specified. If the series is created and needs to be visible
     * based on the {@link #windSourceTypesToDisplay}, it is added to the chart.
     */
    private Series getOrCreateSpeedSeries(WindSource windSource) {
        Series result = windSourceSpeedSeries.get(windSource);
        if (result == null) {
            result = createSpeedSeries(windSource);
            windSourceSpeedSeries.put(windSource, result);
            if (windSourceTypesToDisplay.contains(windSource.getType())) {
                chart.addSeries(result);
            }
        }
        return result;
    }

    /**
     * Only creates the series but doesn't add it to the chart. See also {@link #getOrCreateDirectionSeries(WindSource)} and
     * {@link #showVisibleSeries()}
     */
    private Series createDirectionSeries(WindSource windSource) {
        Series newSeries = chart
                .createSeries()
                .setType(Series.Type.LINE)
                .setName(stringMessages.fromDeg()+" "+windSource.name())
                .setYAxis(0)
                .setPlotOptions(new LinePlotOptions().setColor(colorMap.getColorByID(windSource)));
        return newSeries;
    }

    /**
     * Only creates the series but doesn't add it to the chart. See also {@link #getOrCreateSpeedSeries(WindSource)} and
     * {@link #showVisibleSeries()}
     */
    private Series createSpeedSeries(WindSource windSource) {
        Series newSeries = chart
                .createSeries()
                .setType(Series.Type.LINE)
                .setName(stringMessages.windSpeed()+" "+windSource.name())
                .setYAxis(1) // use the second Y-axis
                .setPlotOptions(new LinePlotOptions().setDashStyle(PlotLine.DashStyle.SHORT_DOT)
                        .setLineWidth(3).setHoverStateLineWidth(3)
                        .setColor(colorMap.getColorByID(windSource))); // show only the markers, not the connecting lines
        return newSeries;
    }

    /**
     * Updates the wind charts with the wind data from <code>result</code>. If <code>append</code> is <code>true</code>, previously
     * existing points in the chart are left unchanged. Otherwise, the existing wind series are replaced.
     */
    public void updateStripChartSeries(WindInfoForRaceDTO result, boolean append) {
        final NumberFormat numberFormat = NumberFormat.getFormat("0");
        for (Map.Entry<WindSource, WindTrackInfoDTO> e : result.windTrackInfoByWindSource.entrySet()) {
            WindSource windSource = e.getKey();
            Series directionSeries = getOrCreateDirectionSeries(windSource);
            Series speedSeries = getOrCreateSpeedSeries(windSource);
            // FIXME probably need to add the series to the chart...
            WindTrackInfoDTO windTrackInfo = e.getValue();
            Point[] directionPoints = new Point[windTrackInfo.windFixes.size()];
            Point[] speedPoints = new Point[windTrackInfo.windFixes.size()];
            int i=0;
            for (WindDTO wind : windTrackInfo.windFixes) {
                if (timeOfEarliestRequestInMillis == null || wind.timepoint<timeOfEarliestRequestInMillis) {
                    timeOfEarliestRequestInMillis = wind.timepoint;
                }
                if (timeOfLatestRequestInMillis == null || wind.timepoint>timeOfLatestRequestInMillis) {
                    timeOfLatestRequestInMillis = wind.timepoint;
                }
                Point newDirectionPoint = new Point(wind.timepoint, wind.dampenedTrueWindFromDeg);
                if (wind.dampenedTrueWindSpeedInKnots != null) {
                    newDirectionPoint.setName(numberFormat.format(wind.dampenedTrueWindSpeedInKnots)+stringMessages.averageSpeedInKnotsUnit());
                }
                directionPoints[i] = newDirectionPoint;
                Point newSpeedPoint = new Point(wind.timepoint, wind.dampenedTrueWindSpeedInKnots);
                speedPoints[i++] = newSpeedPoint;
            }
            Point[] newDirectionPoints;
            Point[] newSpeedPoints;
            if (append) {
                Point[] oldDirectionPoints = directionSeries.getPoints();
                newDirectionPoints = new Point[oldDirectionPoints.length + directionPoints.length];
                System.arraycopy(oldDirectionPoints, 0, newDirectionPoints, 0, oldDirectionPoints.length);
                System.arraycopy(directionPoints, 0, newDirectionPoints, oldDirectionPoints.length, directionPoints.length);
                Point[] oldSpeedPoints = speedSeries.getPoints();
                newSpeedPoints = new Point[oldSpeedPoints.length + speedPoints.length];
                System.arraycopy(oldSpeedPoints, 0, newSpeedPoints, 0, oldSpeedPoints.length);
                System.arraycopy(speedPoints, 0, newSpeedPoints, oldSpeedPoints.length, speedPoints.length);
            } else {
                newDirectionPoints = directionPoints;
                newSpeedPoints = speedPoints;
            }
            directionSeries.setPoints(newDirectionPoints);
            speedSeries.setPoints(newSpeedPoints);
        }
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<WindChartSettings> getSettingsDialogComponent() {
        return new WindChartSettingsDialogComponent(new WindChartSettings(windSourceTypesToDisplay));
    }

    /**
     * Sets the visibilities of the wind source series based on the new settings. Note that this does not
     * re-load any wind data. This has to happen by calling {@link #updateStripChartSeries(WindInfoForRaceDTO, boolean)}.
     */
    @Override
    public void updateSettings(WindChartSettings newSettings) {
        windSourceTypesToDisplay.clear();
        windSourceTypesToDisplay.addAll(newSettings.getWindSourceTypesToDisplay());
        chart.removeAllSeries(/* redraw */ false);
        for (Map.Entry<WindSource, Series> e : windSourceDirectionSeries.entrySet()) {
            if (windSourceTypesToDisplay.contains(e.getKey().getType())) {
                chart.addSeries(e.getValue());
            }
        }
        for (Map.Entry<WindSource, Series> e : windSourceDirectionSeries.entrySet()) {
            if (windSourceTypesToDisplay.contains(e.getKey().getType())) {
                chart.addSeries(e.getValue());
            }
        }
    }

    /**
     * Creates the series for the <code>windSource</code> specified. If the series is created and needs to be visible
     * based on the {@link #windSourceTypesToDisplay}, it is added to the chart.
     */
    private Series getOrCreateDirectionSeries(WindSource windSource) {
        Series result = windSourceDirectionSeries.get(windSource);
        if (result == null) {
            result = createDirectionSeries(windSource);
            windSourceDirectionSeries.put(windSource, result);
            if (windSourceTypesToDisplay.contains(windSource.getType())) {
                chart.addSeries(result);
            }
        }
        return result;
    }

    /**
     * @param append
     *            if <code>true</code>, the results retrieved from the server will be appended to the wind chart instead
     *            of overwriting the existing series.
     */
    private void loadData(final RaceIdentifier raceIdentifier, final Date from, final Date to, final boolean append) {
        sailingService.getWindInfo(raceIdentifier,
        // TODO Time interval should be determined by a selection in the chart but be at most 60s. See bug #121. Consider incremental updates for new data only.
                from, to, // use race start and time of newest event as default time period
                null, // retrieve data on all wind sources
                new AsyncCallback<WindInfoForRaceDTO>() {
                    @Override
                    public void onSuccess(WindInfoForRaceDTO result) {
                        if (result != null) {
                            updateStripChartSeries(result, append);
                        } else {
                            if (!append) {
                                clearChart(); // no wind known for untracked race
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.errorFetchingWindInformationForRace()+" " + raceIdentifier + ": "
                                + caught.getMessage());
                    }
                });
    }
    
    private void clearChart() {
        chart.removeAllSeries();
    }

    @Override
    public void onRaceSelectionChange(List<RaceIdentifier> selectedRaces) {
        if (selectedRaces != null && !selectedRaces.isEmpty()) {
            // show wind of first selected race
            selectedRaceIdentifier = selectedRaces.iterator().next();
            timeOfEarliestRequestInMillis = null;
            timeOfLatestRequestInMillis = null;
            loadData(selectedRaceIdentifier, /* from */null, /* to */
                    new Date(System.currentTimeMillis() - timer.getLivePlayDelayInMillis()), /* append */false);
        } else {
            clearChart();
        }
    }

    /**
     * If in live mode, fetches what's missing since the last fix and <code>date</code>. If nothing has been loaded yet,
     * loads from the beginning up to <code>date</code>. If in replay mode, checks if anything has been loaded at all. If not,
     * everything for the currently selected race is loaded; otherwise, no-op.
     */
    @Override
    public void timeChanged(Date date) {
        if (timer.getPlayMode() == PlayModes.Live) {
            // is date before first cache entry or is cache empty?
            if (timeOfEarliestRequestInMillis == null || timeOfEarliestRequestInMillis > date.getTime()) {
                loadData(selectedRaceIdentifier, null, date, /* append */ true);
            } else if (timeOfLatestRequestInMillis < date.getTime()) {
                loadData(selectedRaceIdentifier, new Date(timeOfLatestRequestInMillis), /* to */
                        new Date(System.currentTimeMillis() - timer.getLivePlayDelayInMillis()), /* append */true);
            }
            // otherwise the cache spans across date and so we don't need to load anything
        } else {
            // assuming play mode is replay / non-live
            if (timeOfLatestRequestInMillis == null) {
                loadData(selectedRaceIdentifier, /* from */null, /* to */
                        new Date(System.currentTimeMillis() - timer.getLivePlayDelayInMillis()), /* append */false); // replace old series
            }
        }
    }
}
