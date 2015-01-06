package com.sap.sailing.gwt.ui.polarsheets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.Series.Type;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.AreaPlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.sap.sailing.domain.common.PolarSheetsHistogramData;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * Shows the underlying distribution for any point in the polar diagrams
 * of the {@link PolarSheetsChartPanel}.
 * 
 * @author d054528 Frederik Petersen
 *
 */
public class AngleOverDataSizeHistogramPanel extends DockLayoutPanel {
    
    private final Chart chart;
    private final StringMessages stringMessages;
    
    private final Map<String, Set<Series>> seriesForId;
    
    private final Map<String, Series> seriesForName;

    public AngleOverDataSizeHistogramPanel(StringMessages stringMessages) {
        super(Unit.PX);
        
        this.seriesForId = new HashMap<>();
        this.seriesForName = new HashMap<>();
        this.stringMessages = stringMessages;
        setSize("100%", "100%");
        
        chart = createHistogramChart();
        chart.getElement().setAttribute("align", "top");
        add(chart);
    }

    private Chart createHistogramChart() {
        Chart histogramChart = new Chart().setType(Type.AREA).setHeight100().setWidth100();
        histogramChart.setChartTitleText(stringMessages.histogram());
        histogramChart.getYAxis().setMin(0).setAxisTitle(new AxisTitle().setText(stringMessages.numberOfDataPoints()));
        histogramChart.getXAxis().setLabels(new XAxisLabels().setRotation(-90f).setY(10))
                .setAxisTitle(new AxisTitle().setText(stringMessages.beatAngle()));
        histogramChart.setAreaPlotOptions(new AreaPlotOptions().setLineColor("#666666")
                .setLineWidth(1).setMarker(new Marker().setLineWidth(1).setLineColor("#666666")));
        histogramChart.setLegend(new Legend().setEnabled(false));
        return histogramChart;
    }

    public void addData(Map<Integer, PolarSheetsHistogramData> histogramDataPerAngle, String seriesId, String actualSeriesName, boolean setVisible) {
        chart.setTitle("");
        Series series = chart.createSeries();
        
        Point[] points = toPoints(histogramDataPerAngle);
        series.setPoints(points, false);
        series.setName(actualSeriesName);
        chart.addSeries(series, false, false);
        series.setVisible(setVisible, false);
        
        if (!seriesForId.containsKey(seriesId)) {
            seriesForId.put(seriesId, new HashSet<Series>());
        }
        Set<Series> seriesSet = seriesForId.get(seriesId);
        seriesSet.add(series);
        seriesForName.put(actualSeriesName, series);
    }

    private Point[] toPoints(Map<Integer, PolarSheetsHistogramData> histogramDataPerAngle) {
        List<Number> xValues = new ArrayList<>();
        List<Number> yValues = new ArrayList<>();
        for (int i = 0; i < 360; i++) {
            if (histogramDataPerAngle.get(i) != null) {
                xValues.add(i);
                yValues.add(histogramDataPerAngle.get(i).getDataCount());
            }
        }
        return toPoints(xValues, yValues);
    }

    private Point[] toPoints(Iterable<Number> xValues, Iterable<Number> yValues) {
        List<Point> points = new ArrayList<>();
        Iterator<Number> x=xValues.iterator();
        Iterator<Number> y=yValues.iterator();
        while (x.hasNext()) {
            points.add(new Point(x.next(), y.next()));
        }
        return points.toArray(new Point[points.size()]);
    }
    
    @Override
    protected void onLoad() {
        Timer timer = new Timer() {
            
            @Override
            public void run() {
                chart.setSizeToMatchContainer();
            }
        };
        timer.schedule(200);
        super.onLoad();
    }
    
    @Override
    public void onResize() {
        chart.setSizeToMatchContainer();
        super.onResize();
    }

    public void removeSeries(String seriesId) {
        for (Series series : seriesForId.get(seriesId)) {
            chart.removeSeries(series);
            seriesForName.remove(series.getName());
        }
        seriesForId.remove(seriesId);
    }
    
    public void removeAllSeries() {
        chart.removeAllSeries();
        seriesForId.clear();
    }

    public void redrawChart() {
        chart.redraw();
    }

    public void showSeries(String name) {
        if (seriesForName.containsKey(name)) {
            seriesForName.get(name).show();
        }
    }

    public void hideSeries(String name) {
        if (seriesForName.containsKey(name)) {
            seriesForName.get(name).hide();
        }
    }
    
    
    

}
