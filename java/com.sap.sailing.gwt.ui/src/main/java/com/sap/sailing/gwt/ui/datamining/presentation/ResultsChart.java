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
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.panels.ResizingSimplePanel;
import com.sap.sailing.gwt.ui.datamining.ResultsPresenter;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;

public class ResultsChart implements ResultsPresenter<Number> {
    
    private final Comparator<GroupKey> standardKeyComparator = new Comparator<GroupKey>() {
        @Override
        public int compare(GroupKey key1, GroupKey key2) {
            return key1.compareTo(key2);
        }
        @Override
        public String toString() {
            return stringMessages.group();
        };
    };
    private final Comparator<GroupKey> ascendingByValueKeyComparator = new Comparator<GroupKey>() {
        @Override
        public int compare(GroupKey key1, GroupKey key2) {
            Map<GroupKey, ? extends Number> resultData = currentResult.getResults();
            double doubleValue1 = resultData.get(key1).doubleValue();
            double doubleValue2 = resultData.get(key2).doubleValue();
            return Double.compare(doubleValue1, doubleValue2);
        }
        @Override
        public String toString() {
            return stringMessages.valueAscending();
        };
    };
    private final Comparator<GroupKey> descendingByValueKeyComparator = new Comparator<GroupKey>() {
        @Override
        public int compare(GroupKey key1, GroupKey key2) {
            return -1 * ascendingByValueKeyComparator.compare(key1, key2);
        }
        @Override
        public String toString() {
            return stringMessages.valueDescending();
        };
    };
    
    private final StringMessages stringMessages;
    private final DockLayoutPanel mainPanel;

    private final HorizontalPanel sortByPanel;
    private final ValueListBox<Comparator<GroupKey>> keyComparatorListBox;

    private final ResizingSimplePanel presentationPanel;
    private final Chart chart;
    private final HTML errorLabel;
    private final HTML labeledBusyIndicator;

    private Map<GroupKey, Series> seriesMappedByGroupKey;
    private final GroupKey simpleResultSeriesKey;
    private QueryResult<? extends Number> currentResult;
    private Map<GroupKey, Integer> mainKeyToXValueMap;

    public ResultsChart(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
        
        mainPanel = new DockLayoutPanel(Unit.PX);
        
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
        mainPanel.addNorth(sortByPanel, 40);
        mainPanel.setWidgetHidden(sortByPanel, true);
        
        presentationPanel = new ResizingSimplePanel() {
            @Override
            public void onResize() {
                chart.setSizeToMatchContainer();
                chart.redraw();
            }
        };
        mainPanel.add(presentationPanel);
        
        chart = createChart();
        seriesMappedByGroupKey = new HashMap<GroupKey, Series>();
        simpleResultSeriesKey = new GenericGroupKey<String>(stringMessages.results());
        
        errorLabel = new HTML();
        errorLabel.setStyleName("chart-importantMessage");
        
        labeledBusyIndicator = new HTML(stringMessages.runningQuery());
        labeledBusyIndicator.setStyleName("chart-busyMessage");

        showError(this.stringMessages.invalidSelection());
    }

    @Override
    public void showError(String error) {
        currentResult = null;
        errorLabel.setHTML(error);
        presentationPanel.setWidget(errorLabel);
    }
    
    @Override
    public void showError(String mainError, Iterable<String> detailedErrors) {
        StringBuilder errorBuilder = new StringBuilder(mainError + ":<br /><ul>");
        for (String detailedError : detailedErrors) {
            errorBuilder.append("<li>" + detailedError + "</li>");
        }
        errorBuilder.append("</ul>");
        showError(errorBuilder.toString());
    }
    
    @Override
    public void showBusyIndicator() {
        currentResult = null;
        presentationPanel.setWidget(labeledBusyIndicator);
    }

    @Override
    public void showResult(QueryResult<Number> result) {
        if (result != null && !result.isEmpty()) {
            currentResult = result;
            updateKeyComparatorListBox();
            resetChartSeries();
            updateYAxisLabels();
            updateChartSubtitleAndSetChartAsWidget();
            showResultData();
        } else {
            showError(stringMessages.noDataFound() + ".");
        }
    }

    private void updateKeyComparatorListBox() {
        boolean hidden = true;
        Comparator<GroupKey> valueToBeSelected = standardKeyComparator;
        Collection<Comparator<GroupKey>> acceptableValues = new ArrayList<>();
        acceptableValues.add(valueToBeSelected);
        if (currentResult != null && isCurrentResultSimple()) {
            valueToBeSelected = getKeyComparator() != null ? getKeyComparator() : valueToBeSelected;
            acceptableValues.add(ascendingByValueKeyComparator);
            acceptableValues.add(descendingByValueKeyComparator);
            hidden = false;
        }
        keyComparatorListBox.setValue(valueToBeSelected);
        keyComparatorListBox.setAcceptableValues(acceptableValues);
        mainPanel.setWidgetHidden(sortByPanel, hidden);
        mainPanel.forceLayout();
    }

    private boolean isCurrentResultSimple() {
        for (GroupKey groupKey : currentResult.getResults().keySet()) {
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
        chart.getYAxis().setAxisTitleText(currentResult.getResultSignifier());
        chart.setToolTip(new ToolTip().setValueDecimals(currentResult.getValueDecimals()).setValueSuffix(
                currentResult.getUnitSignifier()));
    }

    private void updateChartSubtitleAndSetChartAsWidget() {
        chart.setChartSubtitle(new ChartSubtitle().setText(stringMessages.queryResultsChartSubtitle(
                currentResult.getRetrievedDataAmount(), currentResult.getCalculationTimeInSeconds())));
        // This is needed, so that the subtitle is updated. Otherwise the text would stay empty
        presentationPanel.setWidget(null);
        presentationPanel.setWidget(chart);
    }

    private void showResultData() {
        buildMainKeyMapAndSetXAxisCategories();
        createAndAddSeriesToChart();
        
        for (Entry<GroupKey, ? extends Number> resultEntry : currentResult.getResults().entrySet()) {
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

    public List<GroupKey> getSortedMainKeys() {
        Collection<GroupKey> mainKeySet = new HashSet<>();
        for (GroupKey groupKey : currentResult.getResults().keySet()) {
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
        for (GroupKey groupKey : currentResult.getResults().keySet()) {
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
                .setChartTitle(new ChartTitle().setText(stringMessages.dataMiningResult()));

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
    public Widget getWidget() {
        return mainPanel;
    }

}
