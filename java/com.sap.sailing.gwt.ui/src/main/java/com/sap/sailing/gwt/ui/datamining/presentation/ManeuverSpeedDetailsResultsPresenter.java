package com.sap.sailing.gwt.ui.datamining.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Series;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsAggregation;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.ChartToCsvExporter;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

/**
 * Allows presentation of {@link ManeuverSpeedDetailsAggregation} data.
 * 
 * Used in conjunction with the datamining framework.
 * 
 * @author D069712 (Vladislav Chumak)
 *
 */
public class ManeuverSpeedDetailsResultsPresenter extends AbstractSailingResultsPresenter<Settings> {

    private final DockLayoutPanel dockLayoutPanel;

    private Chart polarChart;
    private final SimpleLayoutPanel polarChartWrapperPanel;

    private Chart lineChart;
    private Chart dataCountHistogramChart;
    private final DockLayoutPanel rightSideChartsWrapperPanel;

    private final ManeuverSpeedDetailsChartConfigurationPanel chartConfigPanel;

    private Integer minDataCount;

    private Double minValue;

    private Double maxValue;

    private boolean zeroTo360AxisLabeling = false;

    private QueryResultDTO<?> result;

    public ManeuverSpeedDetailsResultsPresenter(Component<?> parent, ComponentContext<?> context,
            StringMessages stringMessages) {
        super(parent, context, stringMessages);

        chartConfigPanel = new ManeuverSpeedDetailsChartConfigurationPanel(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                minDataCount = chartConfigPanel.getMinDataCount();
                minValue = chartConfigPanel.getMinValue();
                maxValue = chartConfigPanel.getMaxValue();
                zeroTo360AxisLabeling = chartConfigPanel.isZeroTo360AxisLabeling();
                if (result != null) {
                    redrawAllCharts();
                }
            }

        }, stringMessages);

        addControl(chartConfigPanel);

        ChartToCsvExporter chartToCsvExporter = new ChartToCsvExporter(stringMessages.csvCopiedToClipboard());

        Button exportStatisticsCurveToCsvButton = new Button(stringMessages.exportStatisticsCurveToCsv(),
                new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        chartToCsvExporter.exportChartAsCsvToClipboard(lineChart);
                    }
                });
        addControl(exportStatisticsCurveToCsvButton);

        polarChartWrapperPanel = new SimpleLayoutPanel() {
            @Override
            public void onResize() {
                polarChart.setSizeToMatchContainer();
                polarChart.redraw();
            }
        };

        dataCountHistogramChart = ChartFactory.createDataCountHistogramChart(
                stringMessages.TWA() + " (" + stringMessages.degreesShort() + ")", stringMessages);
        dataCountHistogramChart.getXAxis().setMin(-179);
        dataCountHistogramChart.getXAxis().setMax(180);
        rightSideChartsWrapperPanel = new DockLayoutPanel(Unit.PCT) {
            @Override
            public void onResize() {
                lineChart.setSizeToMatchContainer();
                dataCountHistogramChart.setSizeToMatchContainer();
                lineChart.redraw();
                dataCountHistogramChart.redraw();
            }
        };
        dockLayoutPanel = new DockLayoutPanel(Unit.PCT);
        dockLayoutPanel.addWest(polarChartWrapperPanel, 40);
        dockLayoutPanel.addEast(rightSideChartsWrapperPanel, 60);

        redrawAllCharts();

    }

    private void redrawAllCharts() {
        if (polarChart != null) {
            polarChartWrapperPanel.remove(polarChart);
        }
        if (lineChart != null) {
            rightSideChartsWrapperPanel.remove(lineChart);
        }
        if (dataCountHistogramChart != null) {
            rightSideChartsWrapperPanel.remove(dataCountHistogramChart);
        }

        int xAxisMin = zeroTo360AxisLabeling ? 0 : -179;
        int xAxisMax = zeroTo360AxisLabeling ? 359 : 180;

        polarChart = ChartFactory.createPolarChart();
        lineChart = ChartFactory.createLineChartForPolarData(stringMessages);
        dataCountHistogramChart = ChartFactory.createDataCountHistogramChart(
                stringMessages.TWA() + " (" + stringMessages.degreesShort() + ")", stringMessages);
        polarChart.getXAxis().setMin(xAxisMin).setMax(xAxisMax);
        lineChart.getXAxis().setMin(xAxisMin).setMax(xAxisMax);
        dataCountHistogramChart.getXAxis().setMin(xAxisMin).setMax(xAxisMax);

        polarChartWrapperPanel.add(polarChart);
        rightSideChartsWrapperPanel.addNorth(lineChart, 50);
        rightSideChartsWrapperPanel.addSouth(dataCountHistogramChart, 50);

        if (result != null) {
            internalShowResults(result);
        }
    }

    @Override
    protected Widget getPresentationWidget() {
        return dockLayoutPanel;
    }

    @Override
    protected void internalShowResults(QueryResultDTO<?> result) {
        this.result = result;
        Map<GroupKey, ?> results = result.getResults();
        List<GroupKey> sortedNaturally = new ArrayList<GroupKey>(results.keySet());
        Collections.sort(sortedNaturally, new Comparator<GroupKey>() {
            @Override
            public int compare(GroupKey o1, GroupKey o2) {
                Comparator<String> naturalComparator = new NaturalComparator();
                return naturalComparator.compare(o1.asString(), o2.asString());
            }
        });
        for (GroupKey key : sortedNaturally) {
            ManeuverSpeedDetailsAggregation aggregation = (ManeuverSpeedDetailsAggregation) results.get(key);
            double[] valuePerTWA = aggregation.getValuePerTWA();
            int[] countPerTWA = aggregation.getCountPerTWA();
            Series polarSeries = polarChart.createSeries();
            Series histogramSeries = dataCountHistogramChart.createSeries();
            Series valueSeries = lineChart.createSeries();
            int xAxisMin = zeroTo360AxisLabeling ? 0 : -179;
            int xAxisMax = zeroTo360AxisLabeling ? 359 : 180;
            for (int convertedTWA = xAxisMin; convertedTWA <= xAxisMax; convertedTWA++) {
                int i = convertedTWA < 0 ? convertedTWA + 360 : convertedTWA;
                double value = valuePerTWA[i];
                int dataCount = countPerTWA[i];
                if (value != 0 && (minValue == null || value >= minValue) && (maxValue == null || value <= maxValue)
                        && (minDataCount == null || dataCount >= minDataCount)) {
                    polarSeries.addPoint(convertedTWA, value, false, false, false);
                    valueSeries.addPoint(convertedTWA, value, false, false, false);
                } else {
                    polarSeries.addPoint(convertedTWA, null, false, false, false);
                    valueSeries.addPoint(convertedTWA, null, false, false, false);
                }
                histogramSeries.addPoint(convertedTWA, dataCount, false, false, false);
            }
            polarSeries.setName(key.asString());
            valueSeries.setName(key.asString());
            histogramSeries.setName(key.asString());
            polarChart.addSeries(polarSeries, false, false);
            histogramSeries.setVisible(false, false);
            valueSeries.setVisible(false, false);
            lineChart.addSeries(valueSeries, false, false);
            dataCountHistogramChart.addSeries(histogramSeries);
        }
        // Initially resize the chart. Otherwise it's too big. FIXME with a better solution
        Timer timer = new Timer() {

            @Override
            public void run() {
                polarChart.setSizeToMatchContainer();
                lineChart.setSizeToMatchContainer();
                dataCountHistogramChart.setSizeToMatchContainer();
            }
        };
        timer.schedule(200);
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.maneuverSpeedDetailsResultsPresenter();
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<Settings> getSettingsDialogComponent(Settings settings) {
        return null;
    }

    @Override
    public void updateSettings(Settings newSettings) {
        // no-op
    }

    @Override
    public String getDependentCssClassName() {
        return "maneuverSpeedDetailsResultsPresenter";
    }

    @Override
    public Settings getSettings() {
        return null;
    }

    @Override
    public String getId() {
        return "ManeuverSpeedDetailsResultsPresenter";
    }

}
