package com.sap.sailing.gwt.ui.polarmining;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Exporting;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.Series.Type;
import org.moxieapps.gwt.highcharts.client.events.SeriesHideEvent;
import org.moxieapps.gwt.highcharts.client.events.SeriesHideEventHandler;
import org.moxieapps.gwt.highcharts.client.events.SeriesShowEvent;
import org.moxieapps.gwt.highcharts.client.events.SeriesShowEventHandler;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.AreaPlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.presentation.AbstractResultsPresenter;
import com.sap.sailing.polars.datamining.shared.PolarAggregation;
import com.sap.sailing.polars.datamining.shared.PolarDataMiningSettings;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;


public class PolarResultsPresenter extends AbstractResultsPresenter<Object> {

    private final DockLayoutPanel dockLayoutPanel;
    
    private final Chart polarChart;
    private final SimpleLayoutPanel polarChartWrapperPanel;
    
    private final Chart dataCountHistogramChart;
    private final SimpleLayoutPanel dataCountHistogramChartWrapperPanel;
    
    private final Map<Series, Series> histogramSeriesForPolarSeries= new HashMap<>();

    public PolarResultsPresenter(StringMessages stringMessages) {
        super(stringMessages);
        
        polarChart = createPolarChart();
        polarChartWrapperPanel = new SimpleLayoutPanel() {
            @Override
            public void onResize() {
                polarChart.setSizeToMatchContainer();
                polarChart.redraw();
            }
        };
        polarChartWrapperPanel.add(polarChart);
        
        dataCountHistogramChart = createDataCountHistogramChart();
        dataCountHistogramChartWrapperPanel = new SimpleLayoutPanel() {
            @Override
            public void onResize() {
                dataCountHistogramChart.setSizeToMatchContainer();
                dataCountHistogramChart.redraw();
            }
        };
        dataCountHistogramChartWrapperPanel.add(dataCountHistogramChart);
        
        dockLayoutPanel = new DockLayoutPanel(Unit.PCT);
        dockLayoutPanel.addWest(polarChartWrapperPanel, 40);
        dockLayoutPanel.addEast(dataCountHistogramChart, 60);
        
        setSeriesShowAndHideHandler();
    }
    
    private void setSeriesShowAndHideHandler() {
        
        SeriesPlotOptions seriesPlotOptions = new SeriesPlotOptions();
        seriesPlotOptions.setSeriesShowEventHandler(createSeriesShowEventHandler());
        seriesPlotOptions.setSeriesHideEventHandler(createSeriesHideEventHandler());
        polarChart.setSeriesPlotOptions(seriesPlotOptions );
    }

    private SeriesShowEventHandler createSeriesShowEventHandler() {
        return new SeriesShowEventHandler() {
            
            @Override
            public boolean onShow(SeriesShowEvent seriesShowEvent) {
                String id = seriesShowEvent.getSeriesId();
                Series shownSeries = polarChart.getSeries(id);
                Series histogramSeries = histogramSeriesForPolarSeries.get(shownSeries);
                histogramSeries.setVisible(true, true);
                return true;
            }
        };
    }
    
    private SeriesHideEventHandler createSeriesHideEventHandler() {
        return new SeriesHideEventHandler() {
            
            @Override
            public boolean onHide(SeriesHideEvent seriesHideEvent) {
                String id = seriesHideEvent.getSeriesId();
                Series hiddenSeries = polarChart.getSeries(id);
                Series histogramSeries = histogramSeriesForPolarSeries.get(hiddenSeries);
                histogramSeries.setVisible(false, true);
                return true;
            }
        };
    }

    private Chart createDataCountHistogramChart() {
        Chart histogramChart = new Chart().setType(Type.AREA).setHeight100().setWidth100();
        histogramChart.setTitle(new ChartTitle().setText(""), new ChartSubtitle().setText(""));
        histogramChart.getYAxis().setMin(0).setAxisTitle(new AxisTitle().setText(stringMessages.numberOfDataPoints()));
        histogramChart.getXAxis().setLabels(new XAxisLabels().setRotation(-90f).setY(30))
                .setAxisTitle(new AxisTitle().setText(stringMessages.beatAngle()));
        histogramChart.setAreaPlotOptions(new AreaPlotOptions().setLineColor("#666666")
                .setLineWidth(1).setMarker(new Marker().setLineWidth(1).setLineColor("#666666")));
        histogramChart.setLegend(new Legend().setEnabled(false));
        histogramChart.setExporting(new Exporting().setEnabled(false));
        return histogramChart;
    }

    private Chart createPolarChart() {
        LinePlotOptions linePlotOptions = new LinePlotOptions().setLineWidth(1).setMarker(new Marker().setEnabled(false));
        Chart polarSheetChart = new Chart().setType(Series.Type.LINE)
                .setLinePlotOptions(linePlotOptions)
                .setPolar(true).setHeight100().setWidth100();
        polarSheetChart.setTitle(new ChartTitle().setText(""), new ChartSubtitle().setText(""));
        polarSheetChart.getYAxis().setMin(0);
        polarSheetChart.getXAxis().setMin(-179).setMax(180).setTickInterval(45);
        polarSheetChart.setOption("/pane/startAngle", 180);
        polarSheetChart.setExporting(new Exporting().setEnabled(false));
        return polarSheetChart;
    }

    @Override
    protected Widget getPresentationWidget() {
        return dockLayoutPanel;
    }

    @Override
    protected void onDataSelectionValueChange() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void internalShowResults(QueryResultDTO<Object> result) {
        Map<GroupKey, Object> results = result.getResults();
        for (Entry<GroupKey, Object> entry : results.entrySet()) {
            PolarAggregation aggregation = (PolarAggregation) entry.getValue();
            double[] speedsPerAngle = aggregation.getAverageSpeedsPerAngle();
            int count = aggregation.getCount();
            int[] countPerAngle = aggregation.getCountPerAngle();
            PolarDataMiningSettings settings = aggregation.getSettings();
            if (settings.getMinimumDataCountPerGraph() < count) {
                Series polarSeries = polarChart.createSeries();
                Series histogramSeries = dataCountHistogramChart.createSeries();
                for (int i = 0; i < 360; i++) {
                    int convertedAngle = i > 180 ? i - 360 : i;
                    double speed = speedsPerAngle[i];
                    if (countPerAngle[i] >= settings.getMinimumDataCountPerAngle() && speed > 0) {
                        polarSeries.addPoint(convertedAngle, speed, false, false, false);
                    }  
                    histogramSeries.addPoint(convertedAngle, countPerAngle[i]);
                }
                polarSeries.setName(entry.getKey().asString());
                histogramSeries.setName(entry.getKey().asString());
                polarChart.addSeries(polarSeries, false, false);
                histogramSeries.setVisible(false, false);
                histogramSeriesForPolarSeries.put(polarSeries, histogramSeries);
                dataCountHistogramChart.addSeries(histogramSeries);
            }
        }
        //Initially resize the chart. Otherwise it's too big. FIXME with a better solution
        Timer timer = new Timer() {

            @Override
            public void run() {
                polarChart.setSizeToMatchContainer();
                dataCountHistogramChart.setSizeToMatchContainer();
            }
        };
        timer.schedule(200);
    }


}
