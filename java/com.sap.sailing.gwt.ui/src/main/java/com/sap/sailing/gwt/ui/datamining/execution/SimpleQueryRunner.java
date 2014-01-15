package com.sap.sailing.gwt.ui.datamining.execution;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.datamining.QueryDefinitionProvider;
import com.sap.sailing.gwt.ui.datamining.QueryRunner;
import com.sap.sailing.gwt.ui.datamining.ResultsPresenter;
import com.sap.sailing.gwt.ui.datamining.settings.QueryRunnerSettings;
import com.sap.sailing.gwt.ui.datamining.settings.QueryRunnerSettingsDialogComponent;

public class SimpleQueryRunner implements QueryRunner {

    private final StringMessages stringMessages;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;

    private QueryRunnerSettings settings;
    private final QueryDefinitionProvider queryDefinitionProvider;
    private final ResultsPresenter<Number> resultsPresenter;
    
    private final Button runButton;

    public SimpleQueryRunner(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter, QueryDefinitionProvider queryDefinitionProvider,
            ResultsPresenter<Number> resultsPresenter) {
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        
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
    public void run(QueryDefinition queryDefinition) {
        Iterable<String> errorMessages = queryDefinitionProvider.validateQueryDefinition(queryDefinition);
        if (errorMessages == null || !errorMessages.iterator().hasNext()) {
//            queryStatusLabel.setText(" | " + stringMessages.running());
            sailingService.runQuery(queryDefinition, new AsyncCallback<QueryResult<Number>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error running the query: " + caught.getMessage());
                    resultsPresenter.showError(stringMessages.errorRunningDataMiningQuery() + ".");
                }
            
                @Override
                public void onSuccess(QueryResult<Number> result) {
//                    queryStatusLabel.setText(" | " + stringMessages.done());
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
    public void queryDefinitionChanged(QueryDefinition newQueryDefinition) {
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

}
