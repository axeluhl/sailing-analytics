package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.AggregatorType;
import com.sap.sailing.datamining.shared.Components.GrouperType;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.datamining.shared.SharedDimension;
import com.sap.sailing.datamining.shared.SimpleQueryDefinition;
import com.sap.sailing.datamining.shared.StatisticType;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;

public class QueryResultsPanel extends FlowPanel implements QueryDefinitionChangedListener, Component<DataMiningSettings> {

    private static DataMiningResources resources = GWT.create(DataMiningResources.class);

    private StringMessages stringMessages;
    private SailingServiceAsync sailingService;
    private ErrorReporter errorReporter;
    private DataMiningSettings settings;
    private QueryDefinitionProvider queryDefinitionProvider;

    private Button runQueryButton;
    private Label queryStatusLabel;
    private ResultsPresenter<Number> presenter;

    public QueryResultsPanel(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter, QueryDefinitionProvider queryDefinitionProvider, ResultsPresenter<Number> presenter) {
        super();
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.settings = new DataMiningSettings();
        this.queryDefinitionProvider = queryDefinitionProvider;
        this.presenter = presenter;
        
        add(createFunctionsPanel());
        add(this.presenter.getWidget());
        
        updateSettings(settings);
        this.queryDefinitionProvider.applyQueryDefinition(getStandardQueryDefinition());
        runQuery(this.queryDefinitionProvider.getQueryDefinition());
    }

    @Override
    public void queryDefinitionChanged(QueryDefinition newQueryDefinition) {
        runQuery(newQueryDefinition);
    }

    private void runQuery(QueryDefinition queryDefinition) {
        Iterable<String> errorMessages = queryDefinitionProvider.validateQueryDefinition(queryDefinition);
        if (errorMessages == null || !errorMessages.iterator().hasNext()) {
            queryStatusLabel.setText(" | " + stringMessages.running());
            getSailingService().runGPSFixQuery(queryDefinition, new AsyncCallback<QueryResult<Number>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error running the query: " + caught.getMessage());
                    presenter.showError(stringMessages.errorRunningDataMiningQuery() + ".");
                }
            
                @Override
                public void onSuccess(QueryResult<Number> result) {
                    queryStatusLabel.setText(" | " + stringMessages.done());
                    presenter.showResult(result);
                }
            });
        } else {
            presenter.showError(stringMessages.queryNotValidBecause(), errorMessages);
        }
    }

    protected SailingServiceAsync getSailingService() {
        return sailingService;
    }

    private HorizontalPanel createFunctionsPanel() {
        HorizontalPanel functionsPanel = new HorizontalPanel();
        functionsPanel.setSpacing(5);
        
        runQueryButton = new Button(stringMessages.run());
        runQueryButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                runQuery(queryDefinitionProvider.getQueryDefinition());
            }
        });
        functionsPanel.add(runQueryButton);
        
        queryStatusLabel = new Label();
        functionsPanel.add(queryStatusLabel);
        

        Anchor settingsAnchor = new Anchor(AbstractImagePrototype.create(resources.settingsIcon()).getSafeHtml());
        settingsAnchor.setTitle(stringMessages.settings());
        settingsAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new SettingsDialog<DataMiningSettings>(QueryResultsPanel.this, stringMessages).show();
            }
        });
        functionsPanel.add(settingsAnchor);
        
        return functionsPanel;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<DataMiningSettings> getSettingsDialogComponent() {
        DataMiningSettings dataMiningSettings = new DataMiningSettings(settings);
        return new DataMiningSettingsDialogComponent(dataMiningSettings, stringMessages);
    }

    @Override
    public void updateSettings(DataMiningSettings newSettings) {
        settings.setRunAutomatically(newSettings.isRunAutomatically());
        
        if (settings.isRunAutomatically()) {
            queryDefinitionProvider.addListener(QueryResultsPanel.this);
        } else {
            queryDefinitionProvider.removeListener(QueryResultsPanel.this);
        }
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.dataMining();
    }

    @Override
    public Widget getEntryWidget() {
        return this;
    }

    private static QueryDefinition getStandardQueryDefinition() {
        SimpleQueryDefinition standardDefinition = new SimpleQueryDefinition(GrouperType.Dimensions, StatisticType.Speed, AggregatorType.Average);
        standardDefinition.appendDimensionToGroupBy(SharedDimension.RegattaName);
        standardDefinition.appendDimensionToGroupBy(SharedDimension.RaceName);
        return standardDefinition;
    }

}
