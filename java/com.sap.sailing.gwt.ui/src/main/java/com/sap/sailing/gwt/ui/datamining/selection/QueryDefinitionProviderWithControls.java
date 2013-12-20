package com.sap.sailing.gwt.ui.datamining.selection;

import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.StatisticType;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.SharedDimension;
import com.sap.sailing.datamining.shared.SimpleQueryDefinition;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.client.shared.panels.ResizingFlowPanel;
import com.sap.sailing.gwt.ui.datamining.DataMiningControls;
import com.sap.sailing.gwt.ui.datamining.SelectionChangedListener;
import com.sap.sailing.gwt.ui.datamining.SelectionProvider;
import com.sap.sailing.gwt.ui.datamining.StatisticsManager;

public class QueryDefinitionProviderWithControls extends AbstractQueryDefinitionProvider implements DataMiningControls {

    private FlowPanel mainPanel;
    private HorizontalPanel controlsPanel;

    private SelectionProvider<?> selectionProvider;
    
    private StatisticsManager statisticsProvider;
    private ValueListBox<ResultCalculationInformation> statisticsListBox;

    private GroupBySelectionPanel groupBySelectionPanel;

    public QueryDefinitionProviderWithControls(StringMessages stringMessages, SailingServiceAsync sailingService, ErrorReporter errorReporter) {
        super(stringMessages, sailingService, errorReporter);
        
        mainPanel = new ResizingFlowPanel() {
            @Override
            public void onResize() {
                Widget selectionWidget = selectionProvider.getEntryWidget();
                if (selectionWidget instanceof RequiresResize) {
                    ((RequiresResize) selectionWidget).onResize();
                }
            }
        };
        statisticsProvider = new SimpleStatisticsManager();

        mainPanel.add(createFunctionsPanel());

        selectionProvider = new RefreshingSelectionTablesPanel(stringMessages, sailingService, errorReporter);
        selectionProvider.addSelectionChangedListener(new SelectionChangedListener() {
            @Override
            public void selectionChanged() {
                notifyQueryDefinitionChanged();
            }
        });
        mainPanel.add(selectionProvider.getEntryWidget());
        
    }
    
    @Override
    public QueryDefinition getQueryDefinition() {
        SimpleQueryDefinition queryDTO = new SimpleQueryDefinition(LocaleInfo.getCurrentLocale(), groupBySelectionPanel.getGrouperType(), getStatisticType(), getAggregatorType(), getDataType());
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
        for (Entry<SharedDimension, Collection<?>> selectionEntry : selectionProvider.getSelection().entrySet()) {
            queryDTO.setSelectionFor(selectionEntry.getKey(), selectionEntry.getValue());
        }
        return queryDTO;
    }

    @Override
    public void applyQueryDefinition(QueryDefinition queryDefinition) {
        setBlockChangeNotification(true);
        selectionProvider.applySelection(queryDefinition);
        groupBySelectionPanel.apply(queryDefinition);
        applyStatistic(queryDefinition);
        setBlockChangeNotification(false);
        
        notifyQueryDefinitionChanged();
    }

    private void applyStatistic(QueryDefinition queryDefinition) {
        statisticsListBox.setValue(statisticsProvider.getStatistic(queryDefinition.getStatisticType(), queryDefinition.getAggregatorType(), queryDefinition.getDataType()), false);
    }

    private StatisticType getStatisticType() {
        return statisticsListBox.getValue().getStatisticType();
    }

    private AggregatorType getAggregatorType() {
        return statisticsListBox.getValue().getAggregatorType();
    }
    
    private DataTypes getDataType() {
        return statisticsListBox.getValue().getDataType();
    }

    private Widget createFunctionsPanel() {
        HorizontalPanel functionsPanel = new HorizontalPanel();
        functionsPanel.setSpacing(5);

        Button clearSelectionButton = new Button(this.getStringMessages().clearSelection());
        clearSelectionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                selectionProvider.clearSelection();
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
        statisticsListBox = new ValueListBox<ResultCalculationInformation>(new Renderer<ResultCalculationInformation>() {
            @Override
            public String render(ResultCalculationInformation statisticAndAggregatorType) {
                if (statisticAndAggregatorType == null) {
                    return "";
                }
                return statisticAndAggregatorType.toString();
            }

            @Override
            public void render(ResultCalculationInformation statisticAndAggregatorType, Appendable appendable)
                    throws IOException {
                appendable.append(render(statisticAndAggregatorType));
            }
        });
        statisticsListBox.addValueChangeHandler(new ValueChangeHandler<ResultCalculationInformation>() {
            @Override
            public void onValueChange(ValueChangeEvent<ResultCalculationInformation> event) {
                notifyQueryDefinitionChanged();
            }
        });
        statisticsProvider.addStatistic(StatisticType.Speed, AggregatorType.Average, DataTypes.GPSFix);
        statisticsProvider.addStatistic(StatisticType.Distance_TrackedLegOfCompetitor, AggregatorType.Sum, DataTypes.TrackedLegOfCompetitor);
        statisticsProvider.addStatistic(StatisticType.Distance_TrackedLegOfCompetitor, AggregatorType.Average, DataTypes.TrackedLegOfCompetitor);
        statisticsListBox.setValue(statisticsProvider.getStatistic(StatisticType.Speed, AggregatorType.Average, DataTypes.GPSFix), false);
        statisticsListBox.setAcceptableValues(statisticsProvider.getAllStatistics());
        functionsPanel.add(statisticsListBox);
        
        controlsPanel = new HorizontalPanel();
        controlsPanel.setSpacing(5);
        functionsPanel.add(controlsPanel);

        return functionsPanel;
    }

    @Override
    public void addControl(Widget controlWidget) {
        controlsPanel.add(controlWidget);
    }

    @Override
    public Widget getEntryWidget() {
        return mainPanel;
    }

    @Override
    public SelectionProvider<?> getSelectionProvider() {
        return selectionProvider;
    }

    @Override
    public String getLocalizedShortName() {
        return getStringMessages().queryDefinitionProvider();
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
