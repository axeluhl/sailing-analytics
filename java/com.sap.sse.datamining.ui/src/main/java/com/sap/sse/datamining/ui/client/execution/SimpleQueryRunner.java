package com.sap.sse.datamining.ui.client.execution;

import java.io.Serializable;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.AbstractDataMiningComponent;
import com.sap.sse.datamining.ui.client.DataMiningService;
import com.sap.sse.datamining.ui.client.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.client.ManagedDataMiningQueriesCounter;
import com.sap.sse.datamining.ui.client.QueryDefinitionProvider;
import com.sap.sse.datamining.ui.client.QueryRunner;
import com.sap.sse.datamining.ui.client.ResultsPresenter;
import com.sap.sse.datamining.ui.client.settings.QueryRunnerSettings;
import com.sap.sse.datamining.ui.client.settings.QueryRunnerSettingsDialogComponent;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class SimpleQueryRunner extends AbstractDataMiningComponent<QueryRunnerSettings> implements QueryRunner {

    /**
     * The delay before a query is sent to the {@link DataMiningService}.
     * 
     * @see #queryReleaseTimer
     */
    private static final int queryBufferTimeInMillis = 200;

    private final DataMiningSession session;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final ManagedDataMiningQueriesCounter counter;

    /**
     * Timer to prevent the execution of unnecessary queries, when they're run automatically (see
     * {@link QueryRunnerSettings}). This can be caused by a change of the used data type, that then causes a change of
     * the dimension to group by and the data retriever chain. Or caused by quick changes of the filter selection.
     * 
     * @see #queryBufferTimeInMillis
     */
    private final Timer queryReleaseTimer;
    private QueryRunnerSettings settings;
    private final QueryDefinitionProvider<?> queryDefinitionProvider;
    private final ResultsPresenter<?> resultsPresenter;
    private final Button runButton;

    public SimpleQueryRunner(Component<?> parent, ComponentContext<?> context, DataMiningSession session,
           DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
            QueryDefinitionProvider<?> queryDefinitionProvider, ResultsPresenter<?> resultsPresenter) {
        super(parent, context);
        this.session = session;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        counter = new SimpleManagedDataMiningQueriesCounter();

        this.settings = new QueryRunnerSettings();
        this.queryDefinitionProvider = queryDefinitionProvider;
        this.resultsPresenter = resultsPresenter;

        runButton = new Button(getDataMiningStringMessages().runAsSubstantive());
        runButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                runQuery();
            }
        });

        queryReleaseTimer = new Timer() {
            @Override
            public void run() {
                runQuery();
            }
        };
        if (this.settings.isRunAutomatically()) {
            queryDefinitionProvider.addQueryDefinitionChangedListener(this);
        }
    }

    @Override
    public void runQuery() {
        run(queryDefinitionProvider.getQueryDefinition());
    }

    @Override
    public void run(StatisticQueryDefinitionDTO queryDefinition) {
        Iterable<String> errorMessages = queryDefinitionProvider.validateQueryDefinition(queryDefinition);
        if (errorMessages == null || !errorMessages.iterator().hasNext()) {
            counter.increase();
            resultsPresenter.showBusyIndicator();
            dataMiningService.runQuery(session, queryDefinition, new ManagedDataMiningQueryCallback<Serializable>(counter) {
                        @Override
                        protected void handleFailure(Throwable caught) {
                            errorReporter.reportError("Error running the query: " + caught.getMessage());
                            resultsPresenter
                                    .showError(getDataMiningStringMessages().errorRunningDataMiningQuery() + ".");
                        }

                        @Override
                        protected void handleSuccess(QueryResultDTO<Serializable> result) {
                            resultsPresenter.showResult(result);
                        }
                    });
        } else {
            resultsPresenter.showError(getDataMiningStringMessages().queryNotValidBecause(), errorMessages);
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
        // See javadoc of queryReleaseTimer
        queryReleaseTimer.schedule(queryBufferTimeInMillis);
    }

    @Override
    public String getLocalizedShortName() {
        return getDataMiningStringMessages().queryRunner();
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
    public QueryRunnerSettings getSettings() {
        return settings;
    }

    @Override
    public SettingsDialogComponent<QueryRunnerSettings> getSettingsDialogComponent(QueryRunnerSettings settings) {
        return new QueryRunnerSettingsDialogComponent(settings, getDataMiningStringMessages());
    }

    @Override
    public String getDependentCssClassName() {
        return "simpleQueryRunner";
    }

    @Override
    public String getId() {
        return "SimpleQueryRunner";
    }

}
