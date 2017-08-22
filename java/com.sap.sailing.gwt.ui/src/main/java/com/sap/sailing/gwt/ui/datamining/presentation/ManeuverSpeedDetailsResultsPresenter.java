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
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsAggregation;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
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
public class ManeuverSpeedDetailsResultsPresenter extends AbstractResultsPresenter<Settings> {

    private final DockLayoutPanel dockLayoutPanel;
    
    private final Chart polarChart;
    private final SimpleLayoutPanel polarChartWrapperPanel;
    
    private final Chart lineChart;
    private final Chart dataCountHistogramChart;
    private final DockLayoutPanel rightSideChartsWrapperPanel;

    private final ManeuverSpeedDetailsChartConfigurationPanel chartConfigPanel;

    private Double minValue;

    private Double maxValue;
    
    private boolean flipWindDirection = false;

    private QueryResultDTO<?> result;
    
    public ManeuverSpeedDetailsResultsPresenter(Component<?> parent, ComponentContext<?> context, StringMessages stringMessages) {
        super(parent, context, stringMessages);
        
        chartConfigPanel = new ManeuverSpeedDetailsChartConfigurationPanel(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                minValue = chartConfigPanel.getMinValue();
                maxValue = chartConfigPanel.getMaxValue();
                flipWindDirection = chartConfigPanel.isFlipWindDirection();
                redrawAllCharts();
            }

        }, stringMessages);
        chartConfigPanel.setMinValue(minValue);
        chartConfigPanel.setMaxValue(maxValue);
        
        polarChart = ChartFactory.createPolarChart();
        polarChartWrapperPanel = new SimpleLayoutPanel() {
            @Override
            public void onResize() {
                polarChart.setSizeToMatchContainer();
                polarChart.redraw();
            }
        };
        polarChartWrapperPanel.add(polarChart);
        
        lineChart = ChartFactory.createLineChartForPolarData(stringMessages);

        dataCountHistogramChart = ChartFactory.createDataCountHistogramChart(stringMessages.beatAngle() + " ("
                + stringMessages.degreesShort() + ")", stringMessages);
        rightSideChartsWrapperPanel = new DockLayoutPanel(Unit.PCT) {
            @Override
            public void onResize() {
                lineChart.setSizeToMatchContainer();
                dataCountHistogramChart.setSizeToMatchContainer();
                lineChart.redraw();
                dataCountHistogramChart.redraw();
            }
        };
        rightSideChartsWrapperPanel.addNorth(lineChart, 50);
        rightSideChartsWrapperPanel.addSouth(dataCountHistogramChart, 50);
        
        dockLayoutPanel = new DockLayoutPanel(Unit.PCT);
        dockLayoutPanel.addNorth(chartConfigPanel, 10);
        dockLayoutPanel.addWest(polarChartWrapperPanel, 40);
        dockLayoutPanel.addEast(rightSideChartsWrapperPanel, 60);
        
    }
    
    private void redrawAllCharts() {
        if(result != null) {
            polarChart.removeAllSeries();
            lineChart.removeAllSeries();
            dataCountHistogramChart.removeAllSeries();
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
            for (int convertedTWA = -179; convertedTWA <= 180; convertedTWA++) {
                int tempTWA = convertedTWA;
                if(flipWindDirection) {
                    tempTWA += convertedTWA <= 0 ? 180 : -180;
                }
                int i = tempTWA < 0 ? tempTWA + 360 : tempTWA;
                double value = valuePerTWA[i];
                if ((minValue == null || value >= minValue) && (maxValue == null || value <= maxValue)) {
                    polarSeries.addPoint(convertedTWA, value, false, false, false);
                    valueSeries.addPoint(convertedTWA, value, false, false, false);
                }
                histogramSeries.addPoint(convertedTWA, countPerTWA[i], false, false, false);
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
        //Initially resize the chart. Otherwise it's too big. FIXME with a better solution
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
        return getStringMessages().maneuverSpeedDetailsResultsPresenter();
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
