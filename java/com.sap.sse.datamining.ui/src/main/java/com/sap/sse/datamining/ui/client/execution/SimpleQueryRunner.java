package com.sap.sse.datamining.ui.client.execution;

import java.io.Serializable;
import java.util.Iterator;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.data.ReportParameterToDimensionFilterBindings;
import com.sap.sse.datamining.shared.dto.DataMiningReportDTO;
import com.sap.sse.datamining.shared.dto.FilterDimensionParameter;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.AbstractDataMiningComponent;
import com.sap.sse.datamining.ui.client.CompositeResultsPresenter;
import com.sap.sse.datamining.ui.client.DataMiningService;
import com.sap.sse.datamining.ui.client.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.client.QueryDefinitionProvider;
import com.sap.sse.datamining.ui.client.QueryRunner;
import com.sap.sse.datamining.ui.client.ReportProvider;
import com.sap.sse.datamining.ui.client.settings.QueryRunnerSettings;
import com.sap.sse.datamining.ui.client.settings.QueryRunnerSettingsDialogComponent;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

/**
 * The "Run" button with settings and the event handler for the "Run" button which triggers the
 * {@link #run(StatisticQueryDefinitionDTO, ReportParameterToDimensionFilterBindings)} method where the query to run is obtained from the
 * {@link QueryDefinitionProvider} passed to the constructor. Results received from successful query execution through
 * the {@link DataMiningServiceAsync} are shown in the {@link CompositeResultsPresenter} that was passed to the
 * constructor, in the currently active tab.<p>
 * 
 * When the result is displayed, the {@link CompositeResultsPresenter#showResult(String, StatisticQueryDefinitionDTO, QueryResultDTO)}
 * method binds the query and result to the presenter ID managed by the composite results presenter.
 * 
 * @see QueryDefinitionProvider#getQueryDefinition()
 */
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

    /**
     * Timer to prevent the execution of unnecessary queries, when they're run automatically (see
     * {@link QueryRunnerSettings}). This can be caused by a change of the used data type, that then causes a change of
     * the dimension to group by and the data retriever chain. Or caused by quick changes of the filter selection.
     * 
     * @see #queryBufferTimeInMillis
     */
    private final Timer queryReleaseTimer;
    private QueryRunnerSettings settings;
    
    /**
     * The query and its optional {@link ReportParameterToDimensionFilterBindings use} of
     * {@link FilterDimensionParameter report parameters} is fetched from here.
     */
    private final QueryDefinitionProvider<?> queryDefinitionProvider;
    
    /**
     * Query execution results are sent to the presenter within this result presenter that
     * was current (e.g., the selected tab) when running the query was triggered.
     */
    private final CompositeResultsPresenter<?> resultsPresenter;
    
    /**
     * That's where we can obtain the {@link DataMiningReportDTO} from that is considered the "current" report shown and
     * manipulated by the entry point in whose context this query runner is used. When a query and its parameterization
     * if fetched from the {@link #queryDefinitionProvider} for execution, it is updated in the
     * {@link ReportProvider#getCurrentReport() current report}, replacing the query previously shown in the
     * {@link #resultsPresenter}'s {@link CompositeResultsPresenter#getCurrentPresenterId() current presenter}.
     */
    private final ReportProvider reportProvider;
    
    private final Button runButton;


    public SimpleQueryRunner(Component<?> parent, ComponentContext<?> context, DataMiningSession session,
            DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
            QueryDefinitionProvider<?> queryDefinitionProvider, CompositeResultsPresenter<?> resultsPresenter,
            ReportProvider reportProvider) {
        super(parent, context);
        this.session = session;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        queryDefinitionProvider.addQueryDefinitionChangedListener(this);
        this.settings = new QueryRunnerSettings();
        this.queryDefinitionProvider = queryDefinitionProvider;
        this.resultsPresenter = resultsPresenter;
        this.reportProvider = reportProvider;
        runButton = new Button(getDataMiningStringMessages().run());
        runButton.ensureDebugId("RunDataminingQueryButton");
        runButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final Pair<ModifiableStatisticQueryDefinitionDTO, ReportParameterToDimensionFilterBindings> queryDefinitionAndParameterBindings = queryDefinitionProvider
                        .getQueryDefinitionAndReportParameterBinding();
                run(queryDefinitionAndParameterBindings.getA(), queryDefinitionAndParameterBindings.getB());
            }
        });
        queryReleaseTimer = new Timer() {
            @Override
            public void run() {
                final Pair<ModifiableStatisticQueryDefinitionDTO, ReportParameterToDimensionFilterBindings> queryDefinitionAndParameterBindings = queryDefinitionProvider
                        .getQueryDefinitionAndReportParameterBinding();
                SimpleQueryRunner.this.run(queryDefinitionAndParameterBindings.getA(), queryDefinitionAndParameterBindings.getB());
            }
        };
        queryDefinitionChanged(queryDefinitionProvider.getQueryDefinition());
    }

    @Override
    public void run(ModifiableStatisticQueryDefinitionDTO queryDefinition, ReportParameterToDimensionFilterBindings reportParameterBindings) {
        final Iterable<String> errorMessages = queryDefinitionProvider.validateQueryDefinition(queryDefinition);
        final String presenterId = resultsPresenter.getCurrentPresenterId();
        final StatisticQueryDefinitionDTO oldPresenterQuery = resultsPresenter.getCurrentQueryDefinition();
        reportProvider.getCurrentReport().getReport().replaceQueryDefinition(oldPresenterQuery, queryDefinition, reportParameterBindings);
        if (errorMessages == null || !errorMessages.iterator().hasNext()) {
            resultsPresenter.showBusyIndicator(presenterId);
            dataMiningService.runQuery(session, (ModifiableStatisticQueryDefinitionDTO) queryDefinition,
                    new AsyncCallback<QueryResultDTO<Serializable>>() {
                        @Override
                        public void onSuccess(QueryResultDTO<Serializable> result) {
                            resultsPresenter.showResult(presenterId, queryDefinition, result);
                            queryDefinitionProvider.queryDefinitionChangesHaveBeenStored();
                        }
                        
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError("Error running the query: " + caught.getMessage());
                            resultsPresenter.showError(presenterId, // this also clears the query in the result presenter
                                    queryDefinition, getDataMiningStringMessages().errorRunningDataMiningQuery() + ".");
                        }
                    });
        } else {
            resultsPresenter.showError(presenterId, queryDefinition, getDataMiningStringMessages().queryNotValidBecause(), errorMessages);
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
