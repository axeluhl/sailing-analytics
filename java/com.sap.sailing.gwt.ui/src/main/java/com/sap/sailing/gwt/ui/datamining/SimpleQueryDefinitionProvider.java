package com.sap.sailing.gwt.ui.datamining;

import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.datamining.shared.AggregatorType;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.SharedDimension;
import com.sap.sailing.datamining.shared.SimpleQueryDefinition;
import com.sap.sailing.datamining.shared.StatisticType;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class SimpleQueryDefinitionProvider extends AbstractQueryDefinitionProvider {

    private FlowPanel mainPanel;

    private SelectionTablesPanel selectionTablesPanel;
    
    private StatisticsProvider statisticsProvider;
    private ValueListBox<StatisticAndAggregatorType> statisticsListBox;

    private GroupBySelectionPanel groupBySelectionPanel;

    public SimpleQueryDefinitionProvider(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter) {
        super(stringMessages, sailingService, errorReporter);
        mainPanel = new FlowPanel();
        statisticsProvider = new SimpleStatisticsProvider();

        selectionTablesPanel = new SelectionTablesPanel(stringMessages, sailingService, errorReporter);
        selectionTablesPanel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                notifyQueryDefinitionChanged();
            }
        });
        mainPanel.add(selectionTablesPanel);
        mainPanel.add(createFunctionsPanel());
    }
    
    @Override
    public QueryDefinition getQueryDefinition() {
        SimpleQueryDefinition queryDTO = new SimpleQueryDefinition(groupBySelectionPanel.getGrouperType(), getStatisticType(), getAggregatorType());
        switch (queryDTO.getGrouperType()) {
        case Custom:
            queryDTO.setCustomGrouperScriptText(groupBySelectionPanel.getCustomGrouperScriptText());
            break;
        case Dimensions:
        default:
            for (SharedDimension dimension : groupBySelectionPanel.getDimensionsToGroupBy()) {
                queryDTO.appendDimensionToGroupBy(dimension);
            }
            break;
        }
        for (Entry<SharedDimension, Collection<?>> selectionEntry : selectionTablesPanel.getSelection().entrySet()) {
            queryDTO.setSelectionFor(selectionEntry.getKey(), selectionEntry.getValue());
        }
        return queryDTO;
    }
    
    @Override
    public void applyQueryDefinition(QueryDefinition queryDefinition) {
        setBlockChangeNotification(true);
        selectionTablesPanel.applySelection(queryDefinition);
        groupBySelectionPanel.apply(queryDefinition);
        applyStatistic(queryDefinition);
        setBlockChangeNotification(false);
        
        notifyQueryDefinitionChanged();
    }

    private void applyStatistic(QueryDefinition queryDefinition) {
        statisticsListBox.setValue(statisticsProvider.getStatistic(queryDefinition.getStatisticType(), queryDefinition.getAggregatorType()), false);
    }

    private StatisticType getStatisticType() {
        return statisticsListBox.getValue().getStatisticType();
    }

    private AggregatorType getAggregatorType() {
        return statisticsListBox.getValue().getAggregatorType();
    }

    private HorizontalPanel createFunctionsPanel() {
        HorizontalPanel functionsPanel = new HorizontalPanel();
        functionsPanel.setSpacing(5);

        Button clearSelectionButton = new Button(this.getStringMessages().clearSelection());
        clearSelectionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                selectionTablesPanel.clearSelection();
            }
        });
        functionsPanel.add(clearSelectionButton);

        groupBySelectionPanel = new GroupBySelectionPanel(getStringMessages()) {
            @Override
            protected void finishValueChangedHandling() {
                notifyQueryDefinitionChanged();
            }
        };
        functionsPanel.add(groupBySelectionPanel);

        functionsPanel.add(new Label(getStringMessages().statisticToCalculate() + ": "));
        statisticsListBox = new ValueListBox<StatisticAndAggregatorType>(new Renderer<StatisticAndAggregatorType>() {
            @Override
            public String render(StatisticAndAggregatorType statisticAndAggregatorType) {
                if (statisticAndAggregatorType == null) {
                    return "";
                }
                return statisticAndAggregatorType.toString();
            }

            @Override
            public void render(StatisticAndAggregatorType statisticAndAggregatorType, Appendable appendable)
                    throws IOException {
                appendable.append(render(statisticAndAggregatorType));
            }
        });
        statisticsListBox.addValueChangeHandler(new ValueChangeHandler<StatisticAndAggregatorType>() {
            @Override
            public void onValueChange(ValueChangeEvent<StatisticAndAggregatorType> event) {
                notifyQueryDefinitionChanged();
            }
        });
        statisticsProvider.addStatistic(StatisticType.DataAmount, AggregatorType.Sum);
        statisticsProvider.addStatistic(StatisticType.Speed, AggregatorType.Average);
        statisticsListBox.setValue(statisticsProvider.getStatistic(StatisticType.DataAmount, AggregatorType.Sum), false);
        statisticsListBox.setAcceptableValues(statisticsProvider.getAllStatistics());
        functionsPanel.add(statisticsListBox);

        return functionsPanel;
    }

    @Override
    public Widget getWidget() {
        return mainPanel;
    }

}
