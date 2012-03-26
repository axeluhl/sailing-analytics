package com.sap.sailing.gwt.ui.adminconsole;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.events.SeriesCheckboxClickEvent;
import org.moxieapps.gwt.highcharts.client.events.SeriesCheckboxClickEventHandler;
import org.moxieapps.gwt.highcharts.client.events.SeriesLegendItemClickEvent;
import org.moxieapps.gwt.highcharts.client.events.SeriesLegendItemClickEventHandler;
import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;

import com.google.gwt.user.client.ui.SimplePanel;

public abstract class SimpleChartPanel extends SimplePanel {
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
