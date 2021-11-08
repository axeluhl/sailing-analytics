package com.sap.sse.datamining.ui.client.execution;

import java.io.Serializable;
import java.util.Iterator;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.AbstractDataMiningComponent;
import com.sap.sse.datamining.ui.client.CompositeResultsPresenter;
import com.sap.sse.datamining.ui.client.DataMiningService;
import com.sap.sse.datamining.ui.client.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.client.ManagedDataMiningQueriesCounter;
import com.sap.sse.datamining.ui.client.QueryDefinitionProvider;
import com.sap.sse.datamining.ui.client.QueryRunner;
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
    private final CompositeResultsPresenter<?> resultsPresenter;
    private final Button runButton;

    public SimpleQueryRunner(Component<?> parent, ComponentContext<?> context, DataMiningSession session,
            DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
            QueryDefinitionProvider<?> queryDefinitionProvider, CompositeResultsPresenter<?> resultsPresenter) {
        super(parent, context);
        this.session = session;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        counter = new SimpleManagedDataMiningQueriesCounter();
        queryDefinitionProvider.addQueryDefinitionChangedListener(this);

        this.settings = new QueryRunnerSettings();
        this.queryDefinitionProvider = queryDefinitionProvider;
        this.resultsPresenter = resultsPresenter;

        runButton = new Button(getDataMiningStringMessages().run());
        runButton.ensureDebugId("RunDataminingQueryButton");
        runButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                run(queryDefinitionProvider.getQueryDefinition());
            }
        });

        queryReleaseTimer = new Timer() {
            @Override
            public void run() {
                SimpleQueryRunner.this.run(queryDefinitionProvider.getQueryDefinition());
            }
        };
        
        queryDefinitionChanged(queryDefinitionProvider.getQueryDefinition());
    }

    @Override
    public void run(StatisticQueryDefinitionDTO queryDefinition) {
        Iterable<String> errorMessages = queryDefinitionProvider.validateQueryDefinition(queryDefinition);
        String presenterId = resultsPresenter.getCurrentPresenterId();
        if (errorMessages == null || !errorMessages.iterator().hasNext()) {
            counter.increase();
            resultsPresenter.showBusyIndicator(presenterId);
            dataMiningService.runQuery(session, (ModifiableStatisticQueryDefinitionDTO) queryDefinition,
                    new ManagedDataMiningQueryCallback<Serializable>(counter) {
                        @Override
                        protected void handleSuccess(QueryResultDTO<Serializable> result) {
                            resultsPresenter.showResult(presenterId, queryDefinition, result);
                            queryDefinitionProvider.queryDefinitionChangesHaveBeenStored();
                        }
                        @Override
                        protected void handleFailure(Throwable caught) {
                            errorReporter.reportError("Error running the query: " + caught.getMessage());
                            resultsPresenter.showError(presenterId,
                                    getDataMiningStringMessages().errorRunningDataMiningQuery() + ".");
                        }
                    });
        } else {
            resultsPresenter.showError(presenterId, getDataMiningStringMessages().queryNotValidBecause(), errorMessages);
        }
    }

    @Override
    public void updateSettings(QueryRunnerSettings newSettings) {
        if (settings.isRunAutomatically() != newSettings.isRunAutomatically()) {
            settings = newSettings;
        }
    }

    @Override
    public void queryDefinitionChanged(StatisticQueryDefinitionDTO newQueryDefinition) {
        Iterable<String> errors = queryDefinitionProvider.validateQueryDefinition(newQueryDefinition);
        boolean isValid = Util.isEmpty(errors);
        runButton.setEnabled(isValid);
        if (isValid) {
            runButton.setTitle(null);
            if (settings.isRunAutomatically()) {
                queryReleaseTimer.schedule(queryBufferTimeInMillis);
            }
        } else {
            Iterator<String> errorsIterator = errors.iterator();
            StringBuilder tooltipBuilder = new StringBuilder(errorsIterator.next());
            errorsIterator.forEachRemaining(e -> tooltipBuilder.append("\n").append(e));
            runButton.setTitle(tooltipBuilder.toString());
        }
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
