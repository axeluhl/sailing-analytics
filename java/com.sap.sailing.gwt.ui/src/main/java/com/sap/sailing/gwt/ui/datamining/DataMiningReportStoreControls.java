package com.sap.sailing.gwt.ui.datamining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestBox.DefaultSuggestionDisplay;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.dto.DataMiningReportDTO;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.dto.StoredDataMiningReportDTO;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.CompositeResultsPresenter;
import com.sap.sse.datamining.ui.client.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.client.DataRetrieverChainDefinitionProvider;
import com.sap.sse.datamining.ui.client.ReportProvider;
import com.sap.sse.datamining.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

/**
 * UI Panel with the three buttons to load, store and remove and the suggest box to select a stored data mining report by
 * name. A {@link StoredDataMiningReportsProvider} that is passed to the constructor acts as the local client-side
 * repository of reports.
 */
public class DataMiningReportStoreControls extends Composite {

    private static PersistDataMiningReportStoreControlsUiBinder uiBinder = GWT
            .create(PersistDataMiningReportStoreControlsUiBinder.class);

    interface PersistDataMiningReportStoreControlsUiBinder extends UiBinder<Widget, DataMiningReportStoreControls> {
    }
    
    private final StringMessages stringMessages = StringMessages.INSTANCE;

    @UiField
    Button saveReportButtonUi;
    @UiField
    Button loadReportButtonUi;
    @UiField
    Button removeReportButtonUi;
    @UiField(provided = true)
    SuggestBox suggestBoxUi;

    private final ErrorReporter errorReporter;
    private final DataMiningSession session;
    private final DataMiningServiceAsync dataMiningService;
    private final Panel dataminingContentPanel;
    private final StoredDataMiningReportsProvider reportsProvider;
    private final CompositeResultsPresenter<?> resultsPresenter;
    private final MultiWordSuggestOracle oracle;
    private final Panel applyReportBusyIndicator;
    private final ReportProvider reportProvider;

    public DataMiningReportStoreControls(ErrorReporter errorReporter, DataMiningSession session, DataMiningServiceAsync dataMiningService,
            StoredDataMiningReportsProvider reportsProvider, Panel dataminingContentPanel, DataRetrieverChainDefinitionProvider retrieverChainProvider,
            CompositeResultsPresenter<?> resultsPresenter, ReportProvider reportProvider) {
        this.reportProvider = reportProvider;
        this.errorReporter = errorReporter;
        this.session = session;
        this.dataMiningService = dataMiningService;
        this.reportsProvider = reportsProvider;
        this.reportsProvider.addReportsChangedListener(
                reports -> updateOracle(reports.stream().map(r -> r.getName()).collect(Collectors.toList())));
        this.reportsProvider.reloadReports();
        this.dataminingContentPanel = dataminingContentPanel;
        this.resultsPresenter = resultsPresenter;
        oracle = new MultiWordSuggestOracle();
        suggestBoxUi = new SuggestBox(oracle, new TextBox(), new DefaultSuggestionDisplay() {
            @Override
            public void hideSuggestions() {
                updateSaveLoadButtons();
                super.hideSuggestions();
            }
        });
        initWidget(uiBinder.createAndBindUi(this));
        saveReportButtonUi.setText(StringMessages.INSTANCE.save());
        loadReportButtonUi.setText(StringMessages.INSTANCE.load());
        removeReportButtonUi.setText(StringMessages.INSTANCE.remove());
        suggestBoxUi.getValueBox().getElement().setPropertyString("placeholder",
                StringMessages.INSTANCE.dataMiningStoredReportPlaceholder());
        suggestBoxUi.getValueBox().addClickHandler(e -> suggestBoxUi.showSuggestionList());
        suggestBoxUi.getValueBox().addKeyUpHandler(e -> updateSaveLoadButtons());
        suggestBoxUi.getValueBox().addBlurHandler(e -> updateSaveLoadButtons());
        Widget glass = new SimplePanel();
        glass.addStyleName("whiteGlass");
        HTML labeledBusyIndicator = new HTML(SafeHtmlUtils.fromString(stringMessages.applyingReport()));
        labeledBusyIndicator.setStyleName("applyQueryBusyMessage");
        applyReportBusyIndicator = new LayoutPanel();
        applyReportBusyIndicator.add(glass);
        applyReportBusyIndicator.add(labeledBusyIndicator);
    }

    private void updateSaveLoadButtons() {
        String name = suggestBoxUi.getValueBox().getText();
        saveReportButtonUi.setEnabled(name != null && !"".equals(name.trim()));
        loadReportButtonUi.setEnabled(reportsProvider.hasReportWithName(name));
        removeReportButtonUi.setEnabled(reportsProvider.hasReportWithName(name));
    }

    @UiHandler("saveReportButtonUi")
    void onSaveClick(ClickEvent e) {
        final String name = suggestBoxUi.getValue().trim();
        final DataMiningReportDTO report = buildReport();
        if (report == null) {
            Notification.notify(StringMessages.INSTANCE.dataMiningStoredReportNoQueriesWereFound(),
                    NotificationType.ERROR);
        } else if (reportsProvider.addOrUpdateReport(name, report)) {
            Notification.notify(StringMessages.INSTANCE.dataMiningStoredReportUpdateSuccessful(name),
                    NotificationType.SUCCESS);
        } else {
            Notification.notify(StringMessages.INSTANCE.dataMiningStoredReportCreationSuccessful(name),
                    NotificationType.SUCCESS);
        }
    }

    @UiHandler("loadReportButtonUi")
    void onLoadClick(ClickEvent e) {
        String name = suggestBoxUi.getValue().trim();
        this.reportsProvider.findReportByName(name).map(this::applyReport).orElseGet(()->{
            Notification.notify(StringMessages.INSTANCE.dataMiningStoredReportLoadedFailed(name),
                    NotificationType.ERROR);
            return null;
        });
    }

    @UiHandler("removeReportButtonUi")
    void onRemoveClick(ClickEvent e) {
        if (reportsProvider.removeReport(suggestBoxUi.getValue())) {
            suggestBoxUi.setValue("");
            Notification.notify(
                    StringMessages.INSTANCE.dataMiningStoredReportRemovedSuccessful(suggestBoxUi.getValue()),
                    NotificationType.SUCCESS);
        } else {
            Notification.notify(StringMessages.INSTANCE.dataMiningStoredReportRemovedFailed(suggestBoxUi.getValue()),
                    NotificationType.ERROR);
        }
    }
    
    /**
     * Compiles a new {@link DataMiningReportDTO} report based on a the queries from all available tabs in the
     * {@link #resultsPresenter}.
     */
    private DataMiningReportDTO buildReport() {
        return reportProvider.getCurrentReport();
    }

    private StoredDataMiningReportDTO applyReport(StoredDataMiningReportDTO storedReport) {
        showBusyIndicator(true);
        final DataMiningReportDTO report = storedReport.getReport();
        reportProvider.setCurrentReport(report);
        final Iterable<StatisticQueryDefinitionDTO> reportQueries = report.getQueryDefinitions();
        final SequentialQueryExecutor executor = new SequentialQueryExecutor(reportQueries);
        executor.run(results -> {
            resultsPresenter.showResults(results);
            showBusyIndicator(false);
            if (!executor.hasErrorOccurred()) {
                Notification.notify(
                        StringMessages.INSTANCE.dataMiningStoredReportLoadedSuccessful(storedReport.getName()),
                        NotificationType.SUCCESS);
            } else {
                Notification.notify(
                        StringMessages.INSTANCE.dataMiningStoredReportLoadedWithErrors(storedReport.getName()),
                        NotificationType.WARNING);
            }
        });
        return storedReport;
    }
    
    private void showBusyIndicator(boolean show) {
        if (show) {
            dataminingContentPanel.add(applyReportBusyIndicator);
        } else {
            dataminingContentPanel.remove(applyReportBusyIndicator);
        }
    }

    /** Updates the oracle of the suggest box with the names of the stored queries. */
    private void updateOracle(Collection<String> collection) {
        oracle.clear();
        oracle.addAll(collection);
        oracle.setDefaultSuggestionsFromText(collection);
        loadReportButtonUi.setEnabled(!collection.isEmpty());
        removeReportButtonUi.setEnabled(!collection.isEmpty());
        updateSaveLoadButtons();
    }

    private class SequentialQueryExecutor {
        private final List<StatisticQueryDefinitionDTO> queryDefinitions;
        private final Collection<Pair<StatisticQueryDefinitionDTO, QueryResultDTO<?>>> queriesWithResults = new ArrayList<>();
        private boolean errorOccurred = false;
        private Consumer<Collection<Pair<StatisticQueryDefinitionDTO, QueryResultDTO<?>>>> callback;

        public SequentialQueryExecutor(Iterable<StatisticQueryDefinitionDTO> queryDefinitions) {
            this.queryDefinitions = new ArrayList<>();
            Util.addAll(queryDefinitions, this.queryDefinitions);
        }

        public void run(Consumer<Collection<Pair<StatisticQueryDefinitionDTO, QueryResultDTO<?>>>> callback) {
            executeQuery(this.queryDefinitions.get(0));
            this.callback = callback;
        }

        private void executeQuery(StatisticQueryDefinitionDTO queryDefinition) {
            dataMiningService.runQuery(session, (ModifiableStatisticQueryDefinitionDTO) queryDefinition,
                    new AsyncCallback<QueryResultDTO<Serializable>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError("Error running the query: " + caught.getMessage());
                            errorOccurred = true;
                            handleQueryResult(null);
                        }

                        @Override
                        public void onSuccess(QueryResultDTO<Serializable> result) {
                            handleQueryResult(result);
                        }
                    });
        }

        private void handleQueryResult(QueryResultDTO<?> result) {
            int executedIndex = queriesWithResults.size();
            queriesWithResults.add(new Pair<>(queryDefinitions.get(executedIndex), result));
            if (queriesWithResults.size() < queryDefinitions.size()) {
                executeQuery(queryDefinitions.get(executedIndex + 1));
            } else {
                this.callback.accept(queriesWithResults);
            }
        }

        public boolean hasErrorOccurred() {
            return errorOccurred;
        }
    }
}
