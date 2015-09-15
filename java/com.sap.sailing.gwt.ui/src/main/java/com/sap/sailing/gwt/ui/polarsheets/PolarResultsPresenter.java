package com.sap.sailing.gwt.ui.polarsheets;

import org.moxieapps.gwt.highcharts.client.BaseChart;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.presentation.AbstractResultsPresenter;
import com.sap.sailing.polars.datamining.shared.PolarAggregation;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;


public class PolarResultsPresenter extends AbstractResultsPresenter<PolarAggregation> {

    private final Widget polarChart;

    public PolarResultsPresenter(StringMessages stringMessages) {
        super(stringMessages);
        polarChart = createPolarChart();
    }

    private Widget createPolarChart() {
        Chart polarSheetChart = new Chart().setType(Series.Type.LINE)
                .setLinePlotOptions(new LinePlotOptions().setLineWidth(1)).setZoomType(BaseChart.ZoomType.X_AND_Y)
                .setPolar(true).setHeight100().setWidth100();
        polarSheetChart.getYAxis().setMin(0);
        polarSheetChart.getXAxis().setMin(-179).setMax(180).setTickInterval(45);
        polarSheetChart.setOption("/pane/startAngle", 180);
        return polarSheetChart;
    }

    @Override
    protected Widget getPresentationWidget() {
        return polarChart;
    }

    @Override
    protected void onDataSelectionValueChange() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void internalShowResults(QueryResultDTO<PolarAggregation> result) {
    }


}
