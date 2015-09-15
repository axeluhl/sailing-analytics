package com.sap.sailing.gwt.ui.polarsheets;

import java.util.Map;
import java.util.Map.Entry;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Exporting;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;

import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.presentation.AbstractResultsPresenter;
import com.sap.sailing.polars.datamining.shared.PolarAggregation;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;


public class PolarResultsPresenter extends AbstractResultsPresenter<Object> {

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
                .setLinePlotOptions(new LinePlotOptions().setLineWidth(1))
                .setPolar(true).setHeight100().setWidth100();
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
            Series series = polarChart.createSeries();
            PolarAggregation aggregation = (PolarAggregation) entry.getValue();
            double[] speedsPerAngle = aggregation.getAverageSpeedsPerAngle();
            for (int i = 0; i < 360; i++) {
                double speed = speedsPerAngle[i];
                if (speed > 0) {
                    series.addPoint(i - 179, speed, false, false, false);
                }
            }
            series.setName(entry.getKey().asString());
            polarChart.addSeries(series);
        }
        wrapperPanel.onResize();
    }


}
