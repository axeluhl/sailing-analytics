package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.StatisticType;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.client.shared.components.SimpleValueListBox;
import com.sap.sailing.gwt.ui.datamining.StatisticChangedListener;
import com.sap.sailing.gwt.ui.datamining.StatisticProvider;
import com.sap.sailing.gwt.ui.datamining.StatisticsManager;

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
        
        mainPanel.add(new Label(this.stringMessages.calculate() + ":"));
        statisticListBox = new SimpleValueListBox<StatisticType>();
        Collection<StatisticType> registeredStatisticTypes = this.statisticsManager.getRegisteredStatisticTypes();
        ComplexStatistic standardStatistic = this.statisticsManager.getStatistic(registeredStatisticTypes.iterator().next());
        statisticListBox.setValue(standardStatistic.getStatisticType(), false);
        statisticListBox.setAcceptableValues(registeredStatisticTypes);
        statisticListBox.addValueChangeHandler(new ValueChangeHandler<StatisticType>() {
            @Override
            public void onValueChange(ValueChangeEvent<StatisticType> event) {
                applyStatistic(ComplexStatisticProvider.this.statisticsManager.getStatistic(event.getValue()));
                notifyHandlers();
            }
        });
        mainPanel.add(statisticListBox);
        
        HorizontalPanel aggregatorPanel = new HorizontalPanel();
        aggregatorPanel.setSpacing(1);
        aggregatorPanel.add(new Label("("));
        aggregatorListBox = new SimpleValueListBox<AggregatorType>();
        aggregatorListBox.addValueChangeHandler(new ValueChangeHandler<AggregatorType>() {
            @Override
            public void onValueChange(ValueChangeEvent<AggregatorType> event) {
                notifyHandlers();
            }
        });
        aggregatorPanel.add(aggregatorListBox);
        aggregatorPanel.add(new Label(")"));
        mainPanel.add(aggregatorPanel);
        
        mainPanel.add(new Label(this.stringMessages.basedOn()));
        dataTypeListBox = new SimpleValueListBox<DataTypes>();
        dataTypeListBox.addValueChangeHandler(new ValueChangeHandler<DataTypes>() {
            @Override
            public void onValueChange(ValueChangeEvent<DataTypes> event) {
                notifyHandlers();
            }
        });
        mainPanel.add(dataTypeListBox);
        
        applyStatistic(standardStatistic);
    }

    @Override
    public void applyQueryDefinition(QueryDefinition queryDefinition) {
        applyStatistic(statisticsManager.getStatistic(queryDefinition.getStatisticType()));
        aggregatorListBox.setValue(queryDefinition.getAggregatorType(), false);
        dataTypeListBox.setValue(queryDefinition.getDataType(), false);
    }

    private void applyStatistic(ComplexStatistic statistic) {
        statisticListBox.setValue(statistic.getStatisticType(), false);
        
        aggregatorListBox.setAcceptableValues(Arrays.asList(AggregatorType.values()));
        aggregatorListBox.setValue(statistic.getPossibleAggregators().iterator().next(), false);
        aggregatorListBox.setAcceptableValues(statistic.getPossibleAggregators());
        
        dataTypeListBox.setAcceptableValues(Arrays.asList(DataTypes.values()));
        dataTypeListBox.setValue(statistic.getPossibleDataTypes().iterator().next(), false);
        dataTypeListBox.setAcceptableValues(statistic.getPossibleDataTypes());
    }

    private void notifyHandlers() {
        for (StatisticChangedListener listener : listeners) {
            listener.statisticChanged();
        }
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
