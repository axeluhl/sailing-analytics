package com.sap.sailing.gwt.ui.datamining.client.selection;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.StatisticType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.client.shared.components.SimpleValueListBox;
import com.sap.sailing.gwt.ui.datamining.client.StatisticChangedListener;
import com.sap.sailing.gwt.ui.datamining.client.StatisticProvider;
import com.sap.sailing.gwt.ui.datamining.client.StatisticsManager;
import com.sap.sse.datamining.shared.components.AggregatorType;

public class ComplexStatisticProvider implements StatisticProvider {
    
    private final StringMessages stringMessages;
    private final StatisticsManager statisticsManager;
    private Set<StatisticChangedListener> listeners;
    
    private final HorizontalPanel mainPanel;
    private final ValueListBox<StatisticType> statisticListBox;
    private final ValueListBox<AggregatorType> aggregatorListBox;
    private final ValueListBox<DataTypes> dataTypeListBox;

    public ComplexStatisticProvider(StringMessages stringMessages, StatisticsManager statisticsManager) {
        this.stringMessages = stringMessages;
        this.statisticsManager = statisticsManager;
        listeners = new HashSet<StatisticChangedListener>();
        
        mainPanel = new HorizontalPanel();
        mainPanel.setSpacing(5);
        
        Label dataTypeLabel = new Label(this.stringMessages.use());
        dataTypeLabel.addStyleName("dataTypeLabel");
        mainPanel.add(dataTypeLabel);
        dataTypeListBox = createDataTypeListBox();
        mainPanel.add(dataTypeListBox);

        mainPanel.add(new Label(this.stringMessages.toCalculateThe()));
        statisticListBox = createStatisticToCalculateListBox();
        mainPanel.add(statisticListBox);
        
        aggregatorListBox = createAggregatorListBox();
        HorizontalPanel aggregatorPanel = surroundAggregatorListBoxWithBraces(aggregatorListBox);
        mainPanel.add(aggregatorPanel);
        
        applyStatistic(getStandardStatistic());
    }

    private ValueListBox<DataTypes> createDataTypeListBox() {
        ValueListBox<DataTypes> dataTypeListBox = new SimpleValueListBox<DataTypes>();
        dataTypeListBox.setValue(getStandardStatistic().getBaseDataType(), false);
        dataTypeListBox.setAcceptableValues(this.statisticsManager.getRegisteredBaseDataTypes());
        dataTypeListBox.addValueChangeHandler(new ValueChangeHandler<DataTypes>() {
            @Override
            public void onValueChange(ValueChangeEvent<DataTypes> event) {
                applyStatistic(ComplexStatisticProvider.this.statisticsManager.getStatistic(event.getValue()));
                notifyHandlers();
            }
        });
        return dataTypeListBox;
    }

    private HorizontalPanel surroundAggregatorListBoxWithBraces(ValueListBox<AggregatorType> aggregatorListBox) {
        HorizontalPanel aggregatorPanel = new HorizontalPanel();
        aggregatorPanel.setSpacing(1);
        aggregatorPanel.add(new Label("("));
        
        aggregatorPanel.add(aggregatorListBox);
        aggregatorPanel.add(new Label(")"));
        return aggregatorPanel;
    }
    
    private ValueListBox<AggregatorType> createAggregatorListBox() {
        ValueListBox<AggregatorType> aggregatorListBox = new SimpleValueListBox<AggregatorType>();
        aggregatorListBox.addValueChangeHandler(new ValueChangeHandler<AggregatorType>() {
            @Override
            public void onValueChange(ValueChangeEvent<AggregatorType> event) {
                notifyHandlers();
            }
        });
        return aggregatorListBox;
    }

    private ValueListBox<StatisticType> createStatisticToCalculateListBox() {
        ValueListBox<StatisticType> statisticListBox = new SimpleValueListBox<StatisticType>();
        statisticListBox.addValueChangeHandler(new ValueChangeHandler<StatisticType>() {
            @Override
            public void onValueChange(ValueChangeEvent<StatisticType> event) {
                notifyHandlers();
            }
        });
        return statisticListBox;
    }

    private ComplexStatistic getStandardStatistic() {
        return this.statisticsManager.getAllStatistics().iterator().next();
    }

    @Override
    public void applyQueryDefinition(QueryDefinition queryDefinition) {
        applyStatistic(statisticsManager.getStatistic(queryDefinition.getDataType()));
        aggregatorListBox.setValue(queryDefinition.getAggregatorType(), false);
        dataTypeListBox.setValue(queryDefinition.getDataType(), false);
    }

    private void applyStatistic(ComplexStatistic statistic) {
        dataTypeListBox.setValue(statistic.getBaseDataType(), false);
        
        aggregatorListBox.setAcceptableValues(Arrays.asList(AggregatorType.values()));
        aggregatorListBox.setValue(statistic.getPossibleAggregators().iterator().next(), false);
        aggregatorListBox.setAcceptableValues(statistic.getPossibleAggregators());
        
        statisticListBox.setAcceptableValues(Arrays.asList(StatisticType.values()));
        statisticListBox.setValue(statistic.getPossibleStatistics().iterator().next(), false);
        statisticListBox.setAcceptableValues(statistic.getPossibleStatistics());
    }

    private void notifyHandlers() {
        SimpleStatistic newStatistic = getStatistic();
        for (StatisticChangedListener listener : listeners) {
            listener.statisticChanged(newStatistic);
        }
    }

    @Override
    public SimpleStatistic getStatistic() {
        return new SimpleStatistic(getDataType(), getStatisticType(), getAggregatorType());
    }

    @Override
    public void addStatisticChangedListener(StatisticChangedListener listener) {
        listeners.add(listener);
    }

    @Override
    public StatisticType getStatisticType() {
        return statisticListBox.getValue();
    }

    @Override
    public AggregatorType getAggregatorType() {
        return aggregatorListBox.getValue();
    }

    @Override
    public DataTypes getDataType() {
        return dataTypeListBox.getValue();
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.statisticProvider();
    }

    @Override
    public Widget getEntryWidget() {
        return mainPanel;
    }

    @Override
    public boolean isVisible() {
        return mainPanel.isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        mainPanel.setVisible(visibility);
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<Object> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(Object newSettings) { }

}
