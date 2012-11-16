package com.sap.sailing.gwt.ui.shared.charts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Color;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker.Symbol;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class LeaderboardRankChart extends SimplePanel {
    private static final int LINE_WIDTH = 1;
    private final CompetitorSelectionProvider competitorSelectionProvider;
    private final Map<CompetitorDTO, Series> competitorSeries;
    private final Chart chart;
    private final List<String> raceColumnNames;

    public LeaderboardRankChart(SailingServiceAsync sailingService, String leaderboardName,
            CompetitorSelectionProvider competitorSelectionProvider, final StringMessages stringMessages, ErrorReporter errorReporter, boolean compactChart) {
        this.competitorSelectionProvider = competitorSelectionProvider;
        competitorSeries = new HashMap<CompetitorDTO, Series>();
        raceColumnNames = new ArrayList<String>();
        chart = new Chart()
                .setPersistent(true)
                .setMarginLeft(65)
                .setMarginRight(65)
                .setWidth100()
                .setHeight100()
                .setBorderColor(new Color("#A6A6A6"))
                .setBorderWidth(1)
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
                return "<b>" + seriesName + ":</b> " + stringMessages.rank() + toolTipData.getYAsLong()+
                        stringMessages.afterRace() + toolTipData.getXAsString();
            }
        }));
        chart.getXAxis().setType(Axis.Type.LINEAR).setAxisTitleText(stringMessages.afterRace());
        chart.getXAxis().setLabels(new XAxisLabels().setFormatter(new AxisLabelsFormatter() {
            @Override
            public String format(AxisLabelsData axisLabelsData) {
                return raceColumnNames.get((int) axisLabelsData.getValueAsLong());
            }
        }));
        chart.getYAxis(0).setAxisTitleText(stringMessages.rank()).setStartOnTick(true).setShowFirstLabel(false)
        /* TODO do we need a specific formatter if we don't really format anything?
                .setLabels(new YAxisLabels().setFormatter(new AxisLabelsFormatter() {
                    @Override
                    public String format(AxisLabelsData axisLabelsData) {
                        return axisLabelsData.getValueAsString();
                    }
                })) */;
        if (compactChart) {
            chart.setSpacingBottom(10).setSpacingLeft(10).setSpacingRight(10).setSpacingTop(2)
                 .setOption("legend/margin", 2)
                 .setOption("title/margin", 5)
                 .setChartSubtitle(null)
                 .getXAxis().setAxisTitle(null);
        }
        setSize("100%", "100%");
        setWidget(chart);
        loadChartData(leaderboardName, stringMessages, errorReporter, sailingService);
    }

    private void loadChartData(String leaderboardName, final StringMessages stringMessages,
            final ErrorReporter errorReporter, SailingServiceAsync sailingService) {
        chart.showLoading(stringMessages.loadingCompetitorData());
        sailingService.getRankedCompetitorsFromBestToWorstAfterEachRaceColumn(leaderboardName,
                /* date: null means "now" or "live" */ null, new AsyncCallback<List<Pair<String,List<CompetitorDTO>>>>() {
                    @Override
                    public void onSuccess(List<Pair<String, List<CompetitorDTO>>> result) {
                        List<Series> chartSeries = Arrays.asList(chart.getSeries());
                        chart.hideLoading();
                        raceColumnNames.clear();
                        int raceNumber = 0;
                        for (Pair<String, List<CompetitorDTO>> entry : result) {
                            raceColumnNames.add(entry.getA());
                            int rank = 1;
                            for (CompetitorDTO competitor : entry.getB()) {
                                Series series = getOrCreateSeries(competitor);
                                if (!chartSeries.contains(series)) {
                                    chart.addSeries(series);
                                }
                                series.addPoint(raceNumber, rank);
                                rank++;
                            }
                            raceNumber++;
                        }
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
}
