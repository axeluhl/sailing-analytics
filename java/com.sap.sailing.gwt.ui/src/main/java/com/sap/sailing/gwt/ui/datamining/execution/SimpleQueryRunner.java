package com.sap.sailing.gwt.ui.datamining.execution;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.ManagedDataMiningQueriesCounter;
import com.sap.sailing.gwt.ui.datamining.QueryDefinitionProvider;
import com.sap.sailing.gwt.ui.datamining.QueryRunner;
import com.sap.sailing.gwt.ui.datamining.ResultsPresenter;
import com.sap.sailing.gwt.ui.datamining.settings.QueryRunnerSettings;
import com.sap.sailing.gwt.ui.datamining.settings.QueryRunnerSettingsDialogComponent;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class SimpleQueryRunner implements QueryRunner {

    private final DataMiningSession session;
    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final ManagedDataMiningQueriesCounter counter;

    private QueryRunnerSettings settings;
    private final QueryDefinitionProvider queryDefinitionProvider;
    private final ResultsPresenter<Number> resultsPresenter;
    private final Button runButton;

    public SimpleQueryRunner(DataMiningSession session, StringMessages stringMessages, DataMiningServiceAsync dataMiningService,
            ErrorReporter errorReporter, QueryDefinitionProvider queryDefinitionProvider,
            ResultsPresenter<Number> resultsPresenter) {
        this.session = session;
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        counter = new SimpleManagedDataMiningQueriesCounter();
        
        this.settings = new QueryRunnerSettings();
        this.queryDefinitionProvider = queryDefinitionProvider;
        this.resultsPresenter = resultsPresenter;
        
        runButton = new Button(this.stringMessages.runAsSubstantive());
        runButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                run(SimpleQueryRunner.this.queryDefinitionProvider.getQueryDefinition());
            }
        });
        
        if (this.settings.isRunAutomatically()) {
            queryDefinitionProvider.addQueryDefinitionChangedListener(this);
        }
    }

    @Override
    public void run(StatisticQueryDefinitionDTO queryDefinition) {
        Iterable<String> errorMessages = queryDefinitionProvider.validateQueryDefinition(queryDefinition);
        if (errorMessages == null || !errorMessages.iterator().hasNext()) {
            counter.increase();
            resultsPresenter.showBusyIndicator();
            dataMiningService.runQuery(session, queryDefinition, new ManagedDataMiningQueryCallback<Number>(counter) {
                @Override
                protected void handleFailure(Throwable caught) {
                    errorReporter.reportError("Error running the query: " + caught.getMessage());
                    resultsPresenter.showError(stringMessages.errorRunningDataMiningQuery() + ".");
                }
                @Override
                protected void handleSuccess(QueryResult<Number> result) {
                    resultsPresenter.showResult(result);
                }
            });
        } else {
            resultsPresenter.showError(stringMessages.queryNotValidBecause(), errorMessages);
        }
    }

    @Override
    public void updateSettings(QueryRunnerSettings newSettings) {
        if (settings.isRunAutomatically() != newSettings.isRunAutomatically()) {
            settings = newSettings;
            if (settings.isRunAutomatically()) {
                queryDefinitionProvider.addQueryDefinitionChangedListener(this);
            } else {
                queryDefinitionProvider.removeQueryDefinitionChangedListener(this);
            }
        }
    }

    @Override
    public void queryDefinitionChanged(StatisticQueryDefinitionDTO newQueryDefinition) {
        run(newQueryDefinition);
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.queryRunner();
    }

    @Override
    public Widget getEntryWidget() {
        return runButton;
    }

    @Override
    public boolean isVisible() {
        return runButton.isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        runButton.setVisible(visibility);
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<QueryRunnerSettings> getSettingsDialogComponent() {
        QueryRunnerSettings dataMiningSettings = new QueryRunnerSettings(settings);
        return new QueryRunnerSettingsDialogComponent(dataMiningSettings, stringMessages);
    }

    @Override
    public String getDependentCssClassName() {
        return "simpleQueryRunner";
    }

}
