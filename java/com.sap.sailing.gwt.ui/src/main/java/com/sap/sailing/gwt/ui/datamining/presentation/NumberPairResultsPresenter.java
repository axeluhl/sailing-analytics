package com.sap.sailing.gwt.ui.datamining.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Color;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Exporting;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.Series.Type;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;

import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.data.AveragePairWithStats;
import com.sap.sse.datamining.shared.impl.CompoundGroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
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
        chart.getYAxis().setAxisTitleText("Result").setLabels(new YAxisLabels().setFormatter(new AxisLabelsFormatter() {
            @Override
            public String format(AxisLabelsData axisLabelsData) {
                try {
                    return axisLabelsData.getValueAsDouble() + "";
                } catch (Exception e) {
                    return "";
                }
            }
        }));
        return chart;
    }

    @Override
    protected Widget getPresentationWidget() {
        return chartPanel;
    }


    @Override
    protected void internalShowResults(QueryResultDTO<?> r) {
        result = r;
//        createAndAddSeriesToChart();
        for (Entry<GroupKey, ?> resultEntry : result.getResults().entrySet()) {
            AveragePairWithStats<Number> value = (AveragePairWithStats<Number>) resultEntry.getValue();
            
            Point point = new Point(value.getAverage().getA(), value.getAverage().getB());
            series.add(point);
        }
        
        chart.addSeries(chart.createSeries().setType(Type.SCATTER).setPoints(series.toArray(new Point[series.size()])));
        chart.redraw();
    }


    @Override
    public String getLocalizedShortName() {
        return "NumberPair";
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
