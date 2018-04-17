package com.sap.sailing.gwt.ui.datamining.presentation;

import java.util.ArrayList;
import java.util.Map.Entry;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Color;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Exporting;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.Series.Type;

import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.data.AveragePairWithStats;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class NumberPairResultsPresenter extends AbstractResultsPresenter<Settings> {
    private QueryResultDTO<?> result;
    private final SimpleLayoutPanel chartPanel;
    private final Chart chart;
    private final ArrayList<Point> series;


    public NumberPairResultsPresenter(Component<?> parent, ComponentContext<?> context, StringMessages stringMessages) {
        super(parent, context, stringMessages);
  
        chartPanel = new SimpleLayoutPanel() {
            @Override
            public void onResize() {
                chart.setSizeToMatchContainer();
                chart.redraw();
            }
        };
        chart = createChart();
        chartPanel.setWidget(chart);
        series = new ArrayList<>();

    }
    
    private Chart createChart() {
        Chart chart = new Chart()
                .setType(Series.Type.SCATTER)
                .setMarginLeft(100)
                .setMarginRight(45)
                .setWidth100()
                .setHeight100()
                .setBorderColor(new Color("#F0AB00"))
                .setPlotBorderWidth(0)
                .setCredits(new Credits().setEnabled(false))
                .setChartTitle(new ChartTitle().setText(getStringMessages().dataMiningResult()));
        chart.setExporting(new Exporting().setEnabled(false));
        chart.getXAxis().setAllowDecimals(false);
        chart.getYAxis().setAxisTitleText("");
        return chart;
    }

    @Override
    protected Widget getPresentationWidget() {
        return chartPanel;
    }


    @Override
    protected void internalShowResults(QueryResultDTO<?> r) {
        result = r;
        for (Entry<GroupKey, ?> resultEntry : result.getResults().entrySet()) {
            @SuppressWarnings("unchecked")
            AveragePairWithStats<Number> value = (AveragePairWithStats<Number>) resultEntry.getValue();
            
            Point point = new Point(value.getAverage().getA(), value.getAverage().getB());
            point.setName(resultEntry.getKey().asString());
            series.add(point);
        }
        chart.getXAxis().setAxisTitleText(result.getResultSignifier());
        
        chart.addSeries(chart.createSeries().setName("Test").setType(Type.SCATTER).setPoints(series.toArray(new Point[series.size()])));
    }


    @Override
    public String getLocalizedShortName() {
        return getStringMessages().numberPairResultsPresenter();
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
    public Settings getSettings() {
        return null;
    }

    @Override
    public String getId() {
        return "NumberPairResultsPresenter";
    }

    @Override
    public String getDependentCssClassName() {
        // TODO Auto-generated method stub
        return null;
    }

}
