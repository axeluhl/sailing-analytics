package com.sap.sailing.gwt.ui.datamining.presentation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Color;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Exporting;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;

public class ResultsChart extends AbstractResultsPresenter<Number> {
    
    private final Comparator<GroupKey> standardKeyComparator = new Comparator<GroupKey>() {
        @Override
        public int compare(GroupKey key1, GroupKey key2) {
            return key1.compareTo(key2);
        }
        @Override
        public String toString() {
            return getStringMessages().group();
        };
    };
    private final Comparator<GroupKey> ascendingByValueKeyComparator = new Comparator<GroupKey>() {
        @Override
        public int compare(GroupKey key1, GroupKey key2) {
            Map<GroupKey, ? extends Number> resultData = getCurrentResult().getResults();
            double doubleValue1 = resultData.get(key1).doubleValue();
            double doubleValue2 = resultData.get(key2).doubleValue();
            return Double.compare(doubleValue1, doubleValue2);
        }
        @Override
        public String toString() {
            return getStringMessages().valueAscending();
        };
    };
    private final Comparator<GroupKey> descendingByValueKeyComparator = new Comparator<GroupKey>() {
        @Override
        public int compare(GroupKey key1, GroupKey key2) {
            return -1 * ascendingByValueKeyComparator.compare(key1, key2);
        }
        @Override
        public String toString() {
            return getStringMessages().valueDescending();
        };
    };
    
    private final HorizontalPanel sortByPanel;
    private final ValueListBox<Comparator<GroupKey>> keyComparatorListBox;

    private final SimpleLayoutPanel chartPanel;
    private final Chart chart;
    private Map<GroupKey, Series> seriesMappedByGroupKey;
    private final GroupKey simpleResultSeriesKey;
    private Map<GroupKey, Integer> mainKeyToXValueMap;

    public ResultsChart(StringMessages stringMessages) {
        super(stringMessages);
        
        sortByPanel = new HorizontalPanel();
        sortByPanel.setSpacing(5);
        sortByPanel.add(new Label(stringMessages.sortBy()));
        keyComparatorListBox = new ValueListBox<>(new AbstractRenderer<Comparator<?>>() {
            @Override
            public String render(Comparator<?> object) {
                return object.toString();
            }
        });
        keyComparatorListBox.addValueChangeHandler(new ValueChangeHandler<Comparator<GroupKey>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Comparator<GroupKey>> event) {
                resetChartSeries();
                showResultData();
            }
        });
        sortByPanel.add(keyComparatorListBox);
        sortByPanel.setVisible(false);
        addControlWidget(sortByPanel);
        
        chartPanel = new SimpleLayoutPanel() {
            @Override
            public void onResize() {
                chart.setSizeToMatchContainer();
                chart.redraw();
            }
        };
        chart = createChart();
        chartPanel.setWidget(chart);
        
        seriesMappedByGroupKey = new HashMap<GroupKey, Series>();
        simpleResultSeriesKey = new GenericGroupKey<String>(stringMessages.results());
    }
    
    @Override
    protected Widget getPresentationWidget() {
        return chartPanel;
    }
    
    @Override
    protected Iterable<Widget> getControlWidgets() {
        Collection<Widget> controlWidgets = new ArrayList<>();
        controlWidgets.add(sortByPanel);
        return controlWidgets;
    }

    @Override
    public void internalShowResult() {
        if (getCurrentResult() != null && !getCurrentResult().isEmpty()) {
            updateKeyComparatorListBox();
            resetChartSeries();
            updateYAxisLabels();
            updateChartSubtitleAndSetChartAsWidget();
            showResultData();
        } else {
            showError(getStringMessages().noDataFound() + ".");
        }
    }

    private void updateKeyComparatorListBox() {
        boolean visible = false;
        Comparator<GroupKey> valueToBeSelected = standardKeyComparator;
        Collection<Comparator<GroupKey>> acceptableValues = new ArrayList<>();
        acceptableValues.add(valueToBeSelected);
        if (getCurrentResult() != null && isCurrentResultSimple()) {
            valueToBeSelected = getKeyComparator() != null ? getKeyComparator() : valueToBeSelected;
            acceptableValues.add(ascendingByValueKeyComparator);
            acceptableValues.add(descendingByValueKeyComparator);
            visible = true;
        }
        keyComparatorListBox.setValue(valueToBeSelected);
        keyComparatorListBox.setAcceptableValues(acceptableValues);
        sortByPanel.setVisible(visible);
    }

    private boolean isCurrentResultSimple() {
        for (GroupKey groupKey : getCurrentResult().getResults().keySet()) {
            if (groupKey.hasSubKey()) {
                return false;
            }
        }
        return true;
    }

    private void resetChartSeries() {
        chart.removeAllSeries(false);
        seriesMappedByGroupKey = new HashMap<GroupKey, Series>();
    }

    private void updateYAxisLabels() {
        chart.getYAxis().setAxisTitleText(getCurrentResult().getResultSignifier());
        chart.setToolTip(new ToolTip().setValueDecimals(getCurrentResult().getValueDecimals()).setValueSuffix(
                getCurrentResult().getUnitSignifier()));
    }

    private void updateChartSubtitleAndSetChartAsWidget() {
        chart.setChartSubtitle(new ChartSubtitle().setText(getStringMessages().queryResultsChartSubtitle(
                getCurrentResult().getRetrievedDataAmount(), getCurrentResult().getCalculationTimeInSeconds())));
    }

    private void showResultData() {
        buildMainKeyMapAndSetXAxisCategories();
        createAndAddSeriesToChart();
        
        for (Entry<GroupKey, ? extends Number> resultEntry : getCurrentResult().getResults().entrySet()) {
            GroupKey mainKey = resultEntry.getKey().getMainKey();
            Point point = new Point(mainKeyToXValueMap.get(mainKey), resultEntry.getValue());
            point.setName(mainKey.asString());
            seriesMappedByGroupKey.get(groupKeyToSeriesKey(resultEntry.getKey()))
                .addPoint(point, false, false, false);
        }
        
        chart.redraw();
    }

    private void buildMainKeyMapAndSetXAxisCategories() {
        List<GroupKey> sortedMainKeys = getSortedMainKeys();
        String[] categories = new String[sortedMainKeys.size()];
        mainKeyToXValueMap = new HashMap<>();
        for (int i = 0; i < sortedMainKeys.size(); i++) {
            GroupKey mainKey = sortedMainKeys.get(i);
            categories[i] = mainKey.asString();
            mainKeyToXValueMap.put(mainKey, i);
        }
        chart.getXAxis().setCategories(false, categories);
    }

    private List<GroupKey> getSortedMainKeys() {
        Collection<GroupKey> mainKeySet = new HashSet<>();
        for (GroupKey groupKey : getCurrentResult().getResults().keySet()) {
            mainKeySet.add(groupKey.getMainKey());
        }
        List<GroupKey> sortedKeys = new ArrayList<>(mainKeySet);
        Collections.sort(sortedKeys, getKeyComparator());
        return sortedKeys;
    }

    private Comparator<GroupKey> getKeyComparator() {
        return keyComparatorListBox.getValue();
    }

    private void createAndAddSeriesToChart() {
        for (GroupKey groupKey : getCurrentResult().getResults().keySet()) {
            GroupKey seriesKey = groupKeyToSeriesKey(groupKey);
            if (!seriesMappedByGroupKey.containsKey(seriesKey)) {
                seriesMappedByGroupKey.put(seriesKey, chart.createSeries().setName(seriesKey.asString()));
            }
        }
        List<GroupKey> sortedSeriesKeys = new ArrayList<>(seriesMappedByGroupKey.keySet());
        Collections.sort(sortedSeriesKeys);
        for (GroupKey seriesKey : sortedSeriesKeys) {
            chart.addSeries(seriesMappedByGroupKey.get(seriesKey), false, false);
        }
    }
    
    private GroupKey groupKeyToSeriesKey(GroupKey groupKey) {
        return groupKey.hasSubKey() ? groupKey.getSubKey() : simpleResultSeriesKey;
    }

    private Chart createChart() {
        Chart chart = new Chart()
                .setType(Series.Type.COLUMN)
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

}
