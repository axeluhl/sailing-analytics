package com.sap.sailing.gwt.ui.datamining;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.datamining.shared.AggregatorType;
import com.sap.sailing.datamining.shared.Components.GrouperType;
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
    private ValueListBox<GrouperType> grouperTypeListBox;
    private TextArea customGrouperScriptTextBox;
    private HorizontalPanel dimensionsToGroupByPanel;
    private List<ValueListBox<SharedDimension>> dimensionsToGroupByBoxes;
    
    private StatisticsProvider statisticsProvider;
    private ValueListBox<StatisticAndAggregatorType> statisticsListBox;

    public SimpleQueryDefinitionProvider(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter) {
        super(stringMessages, sailingService, errorReporter);
        mainPanel = new FlowPanel();
        dimensionsToGroupByBoxes = new ArrayList<ValueListBox<SharedDimension>>();
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
        SimpleQueryDefinition queryDTO = new SimpleQueryDefinition(getGrouperType(), getStatisticType(), getAggregatorType());
        switch (queryDTO.getGrouperType()) {
        case Custom:
            queryDTO.setCustomGrouperScriptText(getCustomGrouperScriptText());
            break;
        case Dimensions:
        default:
            for (SharedDimension dimension : getDimensionsToGroupBy()) {
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
        applySelection(queryDefinition);
        applyGrouping(queryDefinition);
        applyStatistic(queryDefinition);
        setBlockChangeNotification(false);
        
        notifyQueryDefinitionChanged();
    }

    private void applySelection(QueryDefinition queryDefinition) {
        selectionTablesPanel.applySelection(queryDefinition);
    }

    private void applyGrouping(QueryDefinition queryDefinition) {
        grouperTypeListBox.setValue(queryDefinition.getGrouperType(), true);
        
        switch (queryDefinition.getGrouperType()) {
        case Custom:
            customGrouperScriptTextBox.setText(queryDefinition.getCustomGrouperScriptText());
            break;
        case Dimensions:
            applyDimensionsToGroupBy(queryDefinition);
            break;
        default:
            throw new IllegalArgumentException("Not yet implemented for the given data type: " + queryDefinition.getGrouperType().toString());
        }
    }

    private void applyDimensionsToGroupBy(QueryDefinition queryDefinition) {
        int index = 0;
        for (SharedDimension dimension : queryDefinition.getDimensionsToGroupBy()) {
            dimensionsToGroupByBoxes.get(index).setValue(dimension, true);
            index++;
        }
    }

    private void applyStatistic(QueryDefinition queryDefinition) {
        statisticsListBox.setValue(statisticsProvider.getStatistic(queryDefinition.getStatisticType(), queryDefinition.getAggregatorType()), false);
    }

    private GrouperType getGrouperType() {
        return grouperTypeListBox.getValue();
    }

    private String getCustomGrouperScriptText() {
        return getGrouperType() == GrouperType.Custom ? customGrouperScriptTextBox.getText() : "";
    }

    private Collection<SharedDimension> getDimensionsToGroupBy() {
        Collection<SharedDimension> dimensionsToGroupBy = new ArrayList<SharedDimension>();
        if (getGrouperType() == GrouperType.Dimensions) {
            for (ValueListBox<SharedDimension> dimensionToGroupByBox : dimensionsToGroupByBoxes) {
                if (dimensionToGroupByBox.getValue() != null) {
                    dimensionsToGroupBy.add(dimensionToGroupByBox.getValue());
                }
            }
        }
        return dimensionsToGroupBy;
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

        functionsPanel.add(createGroupByPanel());

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

    private FlowPanel createGroupByPanel() {
        FlowPanel groupByPanel = new FlowPanel();

        HorizontalPanel selectGroupByPanel = new HorizontalPanel();
        selectGroupByPanel.setSpacing(5);
        selectGroupByPanel.add(new Label(getStringMessages().groupBy() + ": "));
        groupByPanel.add(selectGroupByPanel);

        grouperTypeListBox = new ValueListBox<GrouperType>(new Renderer<GrouperType>() {
            @Override
            public String render(GrouperType grouperType) {
                if (grouperType == null) {
                    return "";
                }
                return grouperType.toString();
            }

            @Override
            public void render(GrouperType grouperType, Appendable appendable) throws IOException {
                appendable.append(render(grouperType));
            }
        });
        grouperTypeListBox.setValue(GrouperType.Dimensions, false);
        grouperTypeListBox.setAcceptableValues(Arrays.asList(GrouperType.values()));
        selectGroupByPanel.add(grouperTypeListBox);

        final DeckPanel groupByOptionsPanel = new DeckPanel();
        groupByPanel.add(groupByOptionsPanel);
        grouperTypeListBox.addValueChangeHandler(new ValueChangeHandler<GrouperType>() {
            @Override
            public void onValueChange(ValueChangeEvent<GrouperType> event) {
                if (event.getValue() != null) {
                    switch (event.getValue()) {
                    case Custom:
                        groupByOptionsPanel.showWidget(1);
                        break;
                    case Dimensions:
                        groupByOptionsPanel.showWidget(0);
                        break;
                    }
                }
                notifyQueryDefinitionChanged();
            }
        });

        dimensionsToGroupByPanel = new HorizontalPanel();
        dimensionsToGroupByPanel.setSpacing(5);
        groupByOptionsPanel.add(dimensionsToGroupByPanel);
        
        ValueListBox<SharedDimension> dimensionToGroupByBox = createDimensionToGroupByBox();
        dimensionsToGroupByPanel.add(dimensionToGroupByBox);
        dimensionsToGroupByBoxes.add(dimensionToGroupByBox);

        FlowPanel dynamicGroupByPanel = new FlowPanel();
        groupByOptionsPanel.add(dynamicGroupByPanel);
        dynamicGroupByPanel.add(new Label("public Object getValueToGroupByFrom(GPSFix data) {"));
        customGrouperScriptTextBox = new TextArea();
        customGrouperScriptTextBox.setCharacterWidth(100);
        customGrouperScriptTextBox.setVisibleLines(1);
        dynamicGroupByPanel.add(customGrouperScriptTextBox);
        dynamicGroupByPanel.add(new Label("}"));

        groupByOptionsPanel.showWidget(0);
        return groupByPanel;
    }

    private ValueListBox<SharedDimension> createDimensionToGroupByBox() {
        ValueListBox<SharedDimension> dimensionToGroupByBox = new ValueListBox<SharedDimension>(
                new Renderer<SharedDimension>() {
                    @Override
                    public String render(SharedDimension gpsFixDimension) {
                        if (gpsFixDimension == null) {
                            return "";
                        }
                        return gpsFixDimension.toString();
                    }

                    @Override
                    public void render(SharedDimension gpsFixDimension, Appendable appendable)
                            throws IOException {
                        appendable.append(render(gpsFixDimension));

                    }
                });
        dimensionToGroupByBox.addValueChangeHandler(new ValueChangeHandler<SharedDimension>() {
            private boolean firstChange = true;

            @Override
            public void onValueChange(ValueChangeEvent<SharedDimension> event) {
                if (firstChange && event.getValue() != null) {
                    ValueListBox<SharedDimension> newBox = createDimensionToGroupByBox();
                    dimensionsToGroupByPanel.add(newBox);
                    dimensionsToGroupByBoxes.add(newBox);
                    firstChange = false;
                } else if (event.getValue() == null) {
                    dimensionsToGroupByPanel.remove((Widget) event.getSource());
                    dimensionsToGroupByBoxes.remove(event.getSource());
                }
                notifyQueryDefinitionChanged();
            }
        });
        dimensionToGroupByBox.setAcceptableValues(Arrays.asList(SharedDimension.values()));
        return dimensionToGroupByBox;
    }

    @Override
    public Widget getWidget() {
        return mainPanel;
    }

}
