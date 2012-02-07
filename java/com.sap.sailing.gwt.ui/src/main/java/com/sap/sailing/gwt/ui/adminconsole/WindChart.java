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
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
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
    private final Set<WindSource> windSourcesToDisplay;
    
    /**
     * After the constructor finishes, holds one series for each wind source.
     */
    private final Map<WindSource, Series> windSourceSeries;
    private final ErrorReporter errorReporter;
    private final SailingServiceAsync sailingService;
    private final Chart chart;
    private final Timer timer;
    
    /**
     * @param raceSelectionProvider
     *            if <code>null</code>, this chart won't update its contents automatically upon race selection change;
     *            otherwise, whenever the selection changes, the wind data of the race selected now is loaded from the
     *            server and displayed in this chart. If no race is selected, the chart is cleared.
     */
    public WindChart(SailingServiceAsync sailingService, RaceSelectionProvider raceSelectionProvider,
            Timer timer, WindChartSettings settings, StringMessages stringMessages, ErrorReporter errorReporter) {
        super();
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.windSourceSeries = new HashMap<WindSource, Series>();
        this.windSourcesToDisplay = new HashSet<WindSource>();
        this.timer = timer;
        chart = new Chart()
                .setZoomType(Chart.ZoomType.X)
                .setSpacingRight(20)
                .setChartTitle(new ChartTitle().setText(stringMessages.wind()))
                .setChartSubtitle(new ChartSubtitle().setText(stringMessages.clickAndDragToZoomIn()))
                .setToolTip(new ToolTip().setShared(true))
                .setLegend(new Legend().setEnabled(false))
                .setLinePlotOptions(new LinePlotOptions().setLineWidth(LINE_WIDTH).setMarker(new Marker().setEnabled(false).setHoverState(
                                                new Marker().setEnabled(true).setRadius(4))).setShadow(false)
                                .setHoverStateLineWidth(LINE_WIDTH));

        chart.getXAxis().setType(Axis.Type.DATE_TIME).setMaxZoom(10000) // ten seconds
                .setAxisTitleText(stringMessages.time());
        chart.getYAxis().setAxisTitleText(stringMessages.windSpeed()).setStartOnTick(false).setShowFirstLabel(false);
        for (WindSource windSource : WindSource.values()) {
            Series series = createSeries(windSource);
            windSourceSeries.put(windSource, series);
        }
        updateSettings(settings);
        if (raceSelectionProvider != null) {
            raceSelectionProvider.addRaceSelectionChangeListener(this);
            onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
        }
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.wind();
    }

    @Override
    public Widget getEntryWidget() {
        VerticalPanel vp = new VerticalPanel();
        showVisibleSeries();
        vp.add(chart);
        return vp;
    }

    private void showVisibleSeries() {
        Series[] currentlyVisible = chart.getSeries();
        Set<Series> visible = new HashSet<Series>();
        for (Series series : currentlyVisible) {
            visible.add(series);
        }
        for (WindSource windSource : windSourcesToDisplay) {
            Series series = windSourceSeries.get(windSource);
            if (!visible.contains(series)) {
                chart.addSeries(series);
            } else {
                visible.remove(series);
            }
        }
        for (Series seriesToRemove : visible) {
            chart.removeSeries(seriesToRemove);
        }
    }

    private Series createSeries(WindSource windSource) {
        Series newSeries = chart
                .createSeries()
                .setType(Series.Type.LINE)
                .setName(windSource.name());
        return newSeries;
    }

    public void updateStripChartSeries(WindInfoForRaceDTO result) {
        for (Map.Entry<WindSource, WindTrackInfoDTO> e : result.windTrackInfoByWindSource.entrySet()) {
            WindSource windSource = e.getKey();
            Series series = windSourceSeries.get(windSource);
            WindTrackInfoDTO windTrackInfo = e.getValue();
            Point[] points = new Point[windTrackInfo.windFixes.size()];
            int i=0;
            for (WindDTO wind : windTrackInfo.windFixes) {
                points[i++] = new Point(wind.timepoint, wind.dampenedTrueWindFromDeg);
            }
            series.setPoints(points);
        }
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
        chart.removeAllSeries(/* redraw */ false);
        for (WindSource windSourceToDisplay : windSourcesToDisplay) {
            Series series = windSourceSeries.get(windSourceToDisplay);
            chart.addSeries(series);
        }
    }

    private void loadData(final RaceIdentifier raceIdentifier) {
        sailingService.getWindInfo(raceIdentifier,
        // TODO Time interval should be determined by a selection in the chart but be at most 60s. See bug #121. Consider incremental updates for new data only.
                null, null, // use race start and time of newest event as default time period
                null, // retrieve data on all wind sources
                new AsyncCallback<WindInfoForRaceDTO>() {
                    @Override
                    public void onSuccess(WindInfoForRaceDTO result) {
                        if (result != null) {
                            updateStripChartSeries(result);
                        } else {
                            clearChart(); // no wind known for untracked race
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
            RaceIdentifier selectedRaceIdentifier = selectedRaces.iterator().next();
            loadData(selectedRaceIdentifier);
        } else {
            clearChart();
        }
    }

    @Override
    public void timeChanged(Date date) {
        if (timer.getPlayMode() == PlayModes.Live) {
            // TODO fetch missing pieces from cache
        } else {
            // assuming play mode is replay / non-live
            // TODO fetch all if not yet fetched
        }
        // TODO implement timeChanged by loading missing wind data and adding to series
    }
}
