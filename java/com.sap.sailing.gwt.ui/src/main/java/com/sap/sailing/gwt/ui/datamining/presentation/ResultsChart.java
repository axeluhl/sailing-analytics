package com.sap.sailing.gwt.ui.datamining.presentation;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.sap.sailing.gwt.ui.client.shared.controls.SimpleObjectRenderer;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.CompoundGroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class ResultsChart extends AbstractNumericResultsPresenter<Settings> {
    
    private final Comparator<GroupKey> standardKeyComparator = new Comparator<GroupKey>() {
        @Override
        public int compare(GroupKey key1, GroupKey key2) {
            return key1.compareTo(key2);
        }
        @Override
        public String toString() {
            return getStringMessages().groupName();
        };
    };
    private final Comparator<GroupKey> ascendingByValueKeyComparator = new Comparator<GroupKey>() {
        @Override
        public int compare(GroupKey key1, GroupKey key2) {
            double doubleValue1 = currentResultValues.get(key1).doubleValue();
            double doubleValue2 = currentResultValues.get(key2).doubleValue();
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
    private final Comparator<GroupKey> ascendingByGroupAverageComparator = new Comparator<GroupKey>() {
        @Override
        public int compare(GroupKey key1, GroupKey key2) {
            GroupKey mainKey1 = GroupKey.Util.getMainKey(key1);
            GroupKey mainKey2 = GroupKey.Util.getMainKey(key2);
            return Double.compare(averagePerMainKey.get(mainKey1), averagePerMainKey.get(mainKey2));
        }
        public String toString() {
            return getStringMessages().groupAverageAscending();
        };
    };
    private final Comparator<GroupKey> descendingByGroupAverageComparator = new Comparator<GroupKey>() {
        @Override
        public int compare(GroupKey key1, GroupKey key2) {
            return -1 * ascendingByGroupAverageComparator.compare(key1, key2);
        }
        public String toString() {
            return getStringMessages().groupAverageDescending();
        };
    };
    private final Comparator<GroupKey> ascendingByGroupMedianComparator = new Comparator<GroupKey>() {
        @Override
        public int compare(GroupKey key1, GroupKey key2) {
            GroupKey mainKey1 = GroupKey.Util.getMainKey(key1);
            GroupKey mainKey2 = GroupKey.Util.getMainKey(key2);
            return Double.compare(medianPerMainKey.get(mainKey1), medianPerMainKey.get(mainKey2));
        }
        public String toString() {
            return getStringMessages().groupMedianAscending();
        };
    };
    private final Comparator<GroupKey> descendingByGroupMedianComparator = new Comparator<GroupKey>() {
        @Override
        public int compare(GroupKey key1, GroupKey key2) {
            return -1 * ascendingByGroupMedianComparator.compare(key1, key2);
        }
        public String toString() {
            return getStringMessages().groupMedianDescending();
        };
    };
    
    private final HorizontalPanel sortByPanel;
    private final ValueListBox<Comparator<GroupKey>> keyComparatorListBox;
    private final ValueListBox<Integer> decimalsListBox;

    private final SimpleLayoutPanel chartPanel;
    private final Chart chart;
    private final Map<GroupKey, Series> seriesMappedByGroupKey;
    private final GroupKey simpleResultSeriesKey;
    private final Map<GroupKey, Integer> mainKeyToXValueMap;
    private Map<GroupKey, Number> currentResultValues;

    private final Map<GroupKey, Double> averagePerMainKey;
    private final Map<GroupKey, Double> medianPerMainKey;

    public ResultsChart(Component<?> parent, ComponentContext<?> context, StringMessages stringMessages) {
        super(parent, context, stringMessages);
        
        sortByPanel = new HorizontalPanel();
        sortByPanel.setSpacing(5);
        sortByPanel.add(new Label(stringMessages.sortBy() + ":"));
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
        addControl(sortByPanel);
        
        HorizontalPanel decimalsPanel = new HorizontalPanel();
        decimalsPanel.setSpacing(5);
        decimalsPanel.add(new Label(getStringMessages().shownDecimals() + ":"));
        decimalsListBox = new ValueListBox<Integer>(new SimpleObjectRenderer<Integer>());
        decimalsPanel.add(decimalsListBox);
        decimalsListBox.setValue(0);
        decimalsListBox.setAcceptableValues(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        decimalsListBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                updateChartLabels();
                resetChartSeries();
                showResultData();
            }
        });
        addControl(decimalsPanel);
        
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
        mainKeyToXValueMap = new HashMap<>();
        averagePerMainKey = new HashMap<>();
        medianPerMainKey = new HashMap<>();
    }
    
    @Override
    protected Widget getPresentationWidget() {
        return chartPanel;
    }

    @Override
    protected void internalShowNumericResult(Map<GroupKey, Number> resultValues) {
        this.currentResultValues = resultValues;
        decimalsListBox.setValue(getCurrentResult().getValueDecimals(), false);
        updateKeyComparatorListBox();
        resetChartSeries();
        updateChartLabels();
        updateChartSubtitle();
        showResultData();
    }

    private void updateKeyComparatorListBox() {
        boolean visible = false;
        Comparator<GroupKey> valueToBeSelected = standardKeyComparator;
        Collection<Comparator<GroupKey>> acceptableValues = new ArrayList<>();
        acceptableValues.add(valueToBeSelected);
        if (getCurrentResult() != null) {
            if (isCurrentResultSimple()) {
                acceptableValues.add(ascendingByValueKeyComparator);
                acceptableValues.add(descendingByValueKeyComparator);
            } else {
                acceptableValues.add(ascendingByGroupAverageComparator);
                acceptableValues.add(descendingByGroupAverageComparator);
                acceptableValues.add(ascendingByGroupMedianComparator);
                acceptableValues.add(descendingByGroupMedianComparator);
            }
            visible = true;
        }
        keyComparatorListBox.setValue(valueToBeSelected);
        keyComparatorListBox.setAcceptableValues(acceptableValues);
        sortByPanel.setVisible(visible);
    }

    private void resetChartSeries() {
        chart.removeAllSeries(false);
        seriesMappedByGroupKey.clear();
    }

    private void updateChartLabels() {
        chart.getYAxis().setAxisTitleText(getCurrentResult().getResultSignifier());
        chart.setToolTip(new ToolTip().setValueDecimals(decimalsListBox.getValue()));
    }

    private void updateChartSubtitle() {
        chart.setChartSubtitle(new ChartSubtitle().setText(getStringMessages().queryResultsChartSubtitle(
                getCurrentResult().getRetrievedDataAmount(), getCurrentResult().getCalculationTimeInSeconds())));
    }

    private void showResultData() {
        buildMainKeyMapAndSetXAxisCategories();
        createAndAddSeriesToChart();
        Map<GroupKey, List<Number>> valuesPerMainKey = new HashMap<>();
        for (Entry<GroupKey, ? extends Number> resultEntry : currentResultValues.entrySet()) {
            GroupKey mainKey = GroupKey.Util.getMainKey(resultEntry.getKey());
            Number value = resultEntry.getValue();
            if (!isCurrentResultSimple()) {
                if (!valuesPerMainKey.containsKey(mainKey)) {
                    valuesPerMainKey.put(mainKey, new ArrayList<Number>());
                }
                valuesPerMainKey.get(mainKey).add(value);
            }
            Point point = new Point(mainKeyToXValueMap.get(mainKey), value);
            point.setName(mainKey.asString());
            seriesMappedByGroupKey.get(groupKeyToSeriesKey(resultEntry.getKey()))
                .addPoint(point, false, false, false);
        }
        averagePerMainKey.clear();
        medianPerMainKey.clear();
        if (!isCurrentResultSimple()) {
            for (GroupKey mainKey : valuesPerMainKey.keySet()) {
                List<Number> values = valuesPerMainKey.get(mainKey);
                averagePerMainKey.put(mainKey, getAverageFromValues(values));
                medianPerMainKey.put(mainKey, getMedianFromValues(values));
            }
        }
        chart.redraw();
    }

    private Double getAverageFromValues(Collection<Number> values) {
        double sum = 0;
        for (Number value : values) {
            sum += value.doubleValue();
        }
        return sum / values.size();
    }

    private Double getMedianFromValues(List<Number> values) {
        Collections.sort(values, new Comparator<Number>() {
            @Override
            public int compare(Number n1, Number n2) {
                return Double.compare(n1.doubleValue(), n2.doubleValue());
            }
        });
        if (values.size() % 2 == 0) {
            int index1 = (values.size() / 2) - 1;
            int index2 = index1 + 1;
            return (values.get(index1).doubleValue() + values.get(index2).doubleValue()) / 2;
        } else {
            int index = ((values.size() + 1) / 2) - 1;
            return values.get(index).doubleValue();
        }
    }

    private void buildMainKeyMapAndSetXAxisCategories() {
        List<GroupKey> sortedMainKeys = getSortedMainKeys();
        String[] categories = new String[sortedMainKeys.size()];
        mainKeyToXValueMap.clear();
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
            mainKeySet.add(GroupKey.Util.getMainKey(groupKey));
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
        if (groupKey.hasSubKeys()) {
            List<? extends GroupKey> subKeys = GroupKey.Util.getSubKeys(groupKey);
            return subKeys.size() == 1 ? subKeys.get(0) : new CompoundGroupKey(subKeys);
        } else {
            return simpleResultSeriesKey;
        }
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

    @Override
    public String getLocalizedShortName() {
        return getStringMessages().resultsChart();
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
    public String getDependentCssClassName() {
        return "resultsChart";
    }

    @Override
    public Settings getSettings() {
        return null;
    }

    @Override
    public String getId() {
        return "rc";
    }
}
