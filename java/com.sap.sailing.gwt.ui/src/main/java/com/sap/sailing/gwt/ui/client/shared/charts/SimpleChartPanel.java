package com.sap.sailing.gwt.ui.client.shared.charts;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.events.SeriesCheckboxClickEvent;
import org.moxieapps.gwt.highcharts.client.events.SeriesCheckboxClickEventHandler;
import org.moxieapps.gwt.highcharts.client.events.SeriesLegendItemClickEvent;
import org.moxieapps.gwt.highcharts.client.events.SeriesLegendItemClickEventHandler;
import org.moxieapps.gwt.highcharts.client.plotOptions.PlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;

import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.gwt.ui.client.Timer;

public abstract class SimpleChartPanel extends SimplePanel {
    
    protected final Timer timer;
    
    public SimpleChartPanel(Timer timer) {
        this.timer = timer;
    }
    
    /**
     * When using this method to enable the use of checkboxes only for hiding / showing a series, callers need to ensure
     * that all series have {@link PlotOptions#setSelected(boolean)} set to <code>true</code> for all series that are
     * added to the chart and hence visible. Otherwise, the checkbox won't initially be in sync with the series'
     * visibility state.
     */
    protected void useCheckboxesToShowAndHide(final Chart chart) {
        chart.setLegend(new Legend().setEnabled(true).setBorderWidth(0).setSymbolPadding(20)); // make room for checkbox
        chart.setSeriesPlotOptions(new SeriesPlotOptions().setSeriesCheckboxClickEventHandler(new SeriesCheckboxClickEventHandler() {
                    @Override
                    public boolean onClick(SeriesCheckboxClickEvent seriesCheckboxClickEvent) {
                        if (seriesCheckboxClickEvent.isChecked()) {
                            chart.getSeries(seriesCheckboxClickEvent.getSeriesId()).show();
                        } else {
                            chart.getSeries(seriesCheckboxClickEvent.getSeriesId()).hide();
                        }
                        return false; // don't toggle the select state of the series
                    }
                }).setShowCheckbox(true).
                setSeriesLegendItemClickEventHandler(new SeriesLegendItemClickEventHandler() {
                    @Override
                    public boolean onClick(SeriesLegendItemClickEvent seriesLegendItemClickEvent) {
                        // disable toggling visibility by clicking the legend item; force user to use checkbox instead
                        return false;
                    }
                }));
    }
}
