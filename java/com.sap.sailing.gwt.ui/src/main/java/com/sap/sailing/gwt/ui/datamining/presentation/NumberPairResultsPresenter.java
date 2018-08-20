package com.sap.sailing.gwt.ui.datamining.presentation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Color;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Exporting;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker.Symbol;
import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;

import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.data.PairWithStats;
import com.sap.sse.datamining.shared.impl.CompoundGroupKey;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class NumberPairResultsPresenter extends AbstractSailingResultsPresenter<Settings> {
    private QueryResultDTO<?> result;
    private final SimpleLayoutPanel chartPanel;
    private final Chart chart;
    private final Map<GroupKey, Series> seriesMappedByGroupKey;

    public NumberPairResultsPresenter(Component<?> parent, ComponentContext<?> context, StringMessages stringMessages) {
        super(parent, context, stringMessages);
        seriesMappedByGroupKey = new HashMap<>();
        chartPanel = new SimpleLayoutPanel() {
            @Override
            public void onResize() {
                chart.setSizeToMatchContainer();
                chart.redraw();
            }
        };
        chart = createChart();
        chartPanel.setWidget(chart);
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
                .setChartTitle(new ChartTitle().setText(getDataMiningStringMessages().dataMiningResult()));
        chart.setExporting(new Exporting().setEnabled(false));
        chart.getXAxis().setAllowDecimals(false);
        chart.getYAxis().setAxisTitleText("");
        
        chart.setToolTip(new ToolTip().setFormatter(new ToolTipFormatter() {
            
            @Override
            public String format(ToolTipData toolTipData) {
                return "<b>"+toolTipData.getPointName()+"</b><br>X: "+toolTipData.getPoint().getX()+"<br>Y: "+toolTipData.getYAsString();
            }
        }));
        
        return chart;
    }

    @Override
    protected Widget getPresentationWidget() {
        return chartPanel;
    }

    @Override
    protected void internalShowResults(QueryResultDTO<?> res) {
        result = res;
        createAndAddSeriesToChart();
        for (Entry<GroupKey, ?> resultEntry : result.getResults().entrySet()) {
            @SuppressWarnings("unchecked")
            PairWithStats<Number> value = (PairWithStats<Number>) resultEntry.getValue();
            if (value.getIndividualPairs() != null) {
                for (Pair<Number, Number> pair : value.getIndividualPairs()) {
                    createAndAddPoint(resultEntry.getKey(), pair.getA(), pair.getB());
                }
            } else {
                createAndAddPoint(resultEntry.getKey(), value.getAverage().getA(), value.getAverage().getB());
            }
        }
        chart.getXAxis().setAxisTitleText(result.getResultSignifier());
    }
    
    private void createAndAddPoint(GroupKey key, Number x, Number y) {
        Point point = new Point(x, y);
        point.setName(key.asString());
        seriesMappedByGroupKey.get(groupKeyToSeriesKey(key)).addPoint(point, false, false, false);     
    }
    
    private void createAndAddSeriesToChart() {
        for (GroupKey groupKey : result.getResults().keySet()) {
            GroupKey seriesKey = groupKeyToSeriesKey(groupKey);
            if (!seriesMappedByGroupKey.containsKey(seriesKey)) {
                seriesMappedByGroupKey.put(seriesKey, chart.createSeries().setPlotOptions(new SeriesPlotOptions().setMarker(new Marker().setSymbol(Symbol.CIRCLE))).setName(seriesKey.asString()));
                chart.addSeries(seriesMappedByGroupKey.get(seriesKey), false, false);
            }
        }
    }
    
    private GroupKey groupKeyToSeriesKey(GroupKey groupKey) {
        return new CompoundGroupKey(groupKey.getKeys());
    }


    @Override
    public String getLocalizedShortName() {
        return getDataMiningStringMessages().numberPairResultsPresenter();
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
        return null;
    }

}
