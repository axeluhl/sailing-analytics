package com.sap.sailing.gwt.ui.shared.charts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Color;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker.Symbol;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class LeaderboardRankChart extends SimplePanel implements RequiresResize, TimeListener {
    private static final int LINE_WIDTH = 1;
    private final CompetitorSelectionProvider competitorSelectionProvider;
    private final Map<CompetitorDTO, Series> competitorSeries;
    private final Chart chart;
    private final List<String> raceColumnNames;
    private final Timer timer;
    private final String leaderboardName;
    private final StringMessages stringMessages;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;

    public LeaderboardRankChart(SailingServiceAsync sailingService, String leaderboardName,
            CompetitorSelectionProvider competitorSelectionProvider, Timer timer,
            final StringMessages stringMessages, ErrorReporter errorReporter, boolean compactChart) {
        this.competitorSelectionProvider = competitorSelectionProvider;
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.timer = timer;
        timer.addTimeListener(this);
        this.leaderboardName = leaderboardName;
        competitorSeries = new HashMap<CompetitorDTO, Series>();
        raceColumnNames = new ArrayList<String>();
        chart = new Chart()
                .setPersistent(true)
                .setMarginLeft(65)
                .setMarginRight(65)
                .setWidth100()
                .setHeight100()
                .setBorderColor(new Color("#CACACA"))
                .setBorderWidth(1)
                .setBorderRadius(0)
                .setBackgroundColor(new Color("#EBEBEB"))
                .setPlotBorderWidth(0)
                .setCredits(new Credits().setEnabled(false))
                .setChartTitle(new ChartTitle().setText(stringMessages.rank()))
                .setLinePlotOptions(new LinePlotOptions().setLineWidth(LINE_WIDTH).setMarker(
                        new Marker().setEnabled(true).setHoverState(
                                new Marker().setEnabled(true).setRadius(4))).setShadow(false)
                                    .setHoverStateLineWidth(LINE_WIDTH));
        ChartUtil.useCheckboxesToShowAndHide(chart);
        chart.setToolTip(new ToolTip().setEnabled(true).setFormatter(new ToolTipFormatter() {
            @Override
            public String format(ToolTipData toolTipData) {
                String seriesName = toolTipData.getSeriesName();
                return "<b>" + seriesName + ":</b> " + stringMessages.rankNAfterRace(-toolTipData.getYAsLong(),
                        raceColumnNames.get((int) toolTipData.getXAsLong()));
            }
        }));
        chart.getXAxis().setType(Axis.Type.LINEAR).setAxisTitleText(stringMessages.afterRace());
        chart.getXAxis().setTickInterval(1).setLabels(new XAxisLabels().setFormatter(new AxisLabelsFormatter() {
            @Override
            public String format(AxisLabelsData axisLabelsData) {
                return raceColumnNames.get((int) axisLabelsData.getValueAsLong());
            }
        }));
        chart.getYAxis().setAllowDecimals(false).setStartOnTick(true).setEndOnTick(true).setAxisTitleText(stringMessages.rank())
        .setShowFirstLabel(false).setShowLastLabel(false)
        .setLabels(new YAxisLabels().setFormatter(new AxisLabelsFormatter() {
            @Override
            public String format(AxisLabelsData axisLabelsData) {
                // invert signum to ensure best are at the top
                return "" + (-axisLabelsData.getValueAsLong());
            }
        }));
        if (compactChart) {
            chart.setSpacingBottom(10).setSpacingLeft(10).setSpacingRight(10).setSpacingTop(2)
                 .setOption("legend/margin", 2)
                 .setOption("title/margin", 5)
                 .setChartSubtitle(null)
                 .getXAxis().setAxisTitle(null);
        }
        setWidget(chart);
        setSize("100%", "100%");
        // show the loading message only upon initial load
        chart.showLoading(stringMessages.loadingCompetitorData());
        timeChanged(timer.getTime());
    }

    private void loadChartData(Date date) {
        sailingService.getRankedCompetitorsFromBestToWorstAfterEachRaceColumn(leaderboardName,
                /* date: null means "now" or "live" */ null, new AsyncCallback<List<Pair<String,List<CompetitorDTO>>>>() {
                    @Override
                    public void onSuccess(List<Pair<String, List<CompetitorDTO>>> result) {
                        List<Series> chartSeries = new ArrayList<Series>(Arrays.asList(chart.getSeries()));
                        chart.hideLoading();
                        raceColumnNames.clear();
                        Set<Series> unusedSeries = new HashSet<Series>(competitorSeries.values());
                        for (Series series : competitorSeries.values()) {
                            for (Point p : new ArrayList<Point>(Arrays.asList(series.getPoints()))) {
                                series.removePoint(p, /* redraw */ false, /* animation */ false);
                            }
                        }
                        int raceNumber = 0;
                        int maxCompetitorCount = 0;
                        for (Pair<String, List<CompetitorDTO>> entry : result) {
                            raceColumnNames.add(entry.getA());
                            int rank = 1;
                            maxCompetitorCount = Math.max(maxCompetitorCount, entry.getB().size());
                            for (CompetitorDTO competitor : entry.getB()) {
                                if (Util.isEmpty(competitorSelectionProvider.getSelectedCompetitors()) ||
                                        competitorSelectionProvider.isSelected(competitor)) {
                                    Series series = getOrCreateSeries(competitor);
                                    series.addPoint(raceNumber, -rank, /* redraw */ false, /* shift */ false, /* animation */ false);
                                    
                                    unusedSeries.remove(series);
                                    if (!chartSeries.contains(series)) {
                                        chart.addSeries(series);
                                        chartSeries.add(series);
                                    }
                                }
                                rank++;
                            }
                            raceNumber++;
                        }
                        chart.getYAxis().setMaxPadding(0.5/maxCompetitorCount).setMinPadding(0.5/maxCompetitorCount);
                        chart.setSizeToMatchContainer();
                        // it's important here to recall the redraw method, otherwise the bug fix for wrong checkbox positions (nativeAdjustCheckboxPosition)
                        // in the BaseChart class would not be called 
                        chart.redraw();
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        chart.hideLoading();
                        errorReporter.reportError(stringMessages.errorFetchingRankingChartData(caught.getMessage()));
                    }
                });
    }

    private Series getOrCreateSeries(CompetitorDTO competitor) {
        Series result = competitorSeries.get(competitor);
        if (result == null) {
            result = chart.createSeries().setType(Series.Type.LINE).setName(competitor.name);
            result.setPlotOptions(new LinePlotOptions()
            .setLineWidth(LINE_WIDTH)
            .setMarker(new Marker().setEnabled(true).setRadius(4).setSymbol(Symbol.DIAMOND))
            .setShadow(true).setHoverStateLineWidth(LINE_WIDTH)
            .setColor(competitorSelectionProvider.getColor(competitor)).setSelected(true));
            competitorSeries.put(competitor, result);
        }
        return result;
    }

    @Override
    public void onResize() {
        if (!competitorSeries.isEmpty()) {
            chart.setSizeToMatchContainer();
            // it's important here to recall the redraw method, otherwise the bug fix for wrong checkbox positions (nativeAdjustCheckboxPosition)
            // in the BaseChart class would not be called 
            chart.redraw();
        }
    }

    @Override
    public void timeChanged(Date date) {
        loadChartData(timer.getPlayMode() == PlayModes.Live ? null : date);
    }
}
