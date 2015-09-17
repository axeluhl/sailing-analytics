package com.sap.sailing.gwt.ui.polarmining;

import java.util.Map;
import java.util.Map.Entry;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Exporting;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.presentation.AbstractResultsPresenter;
import com.sap.sailing.polars.datamining.shared.PolarAggregation;
import com.sap.sailing.polars.datamining.shared.PolarDataMiningSettings;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;


public class PolarResultsPresenter extends AbstractResultsPresenter<Object, Settings> {

    private final Chart polarChart;
    private final SimpleLayoutPanel wrapperPanel;

    public PolarResultsPresenter(StringMessages stringMessages) {
        super(stringMessages);
        polarChart = createPolarChart();
        wrapperPanel = new SimpleLayoutPanel() {
            @Override
            public void onResize() {
                polarChart.setSizeToMatchContainer();
                polarChart.redraw();
            }
        };
        wrapperPanel.add(polarChart);
    }

    private Chart createPolarChart() {
        Chart polarSheetChart = new Chart().setType(Series.Type.LINE)
                .setLinePlotOptions(new LinePlotOptions().setLineWidth(1).setMarker(new Marker().setEnabled(false)))
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
        return wrapperPanel;
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
                Series series = polarChart.createSeries();
                for (int i = 0; i < 360; i++) {
                    double speed = speedsPerAngle[i];
                    if (countPerAngle[i] >= settings.getMinimumDataCountPerAngle() && speed > 0) {
                        series.addPoint(i > 180 ? i - 360 : i, speed, false, false, false);
                    }
                }
                series.setName(entry.getKey().asString());
                polarChart.addSeries(series, false, false);
            }
        }
        //Initially resize the chart. Otherwise it's too big. FIXME with a better solution
        Timer timer = new Timer() {

            @Override
            public void run() {
                polarChart.setSizeToMatchContainer();
            }
        };
        timer.schedule(200);
    }

    @Override
    public String getLocalizedShortName() {
        return getStringMessages().polarResultsPresenter();
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<Settings> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(Settings newSettings) {
    }

    @Override
    public String getDependentCssClassName() {
        return "polarResultsPresenter";
    }

}
