package com.sap.sailing.gwt.ui.datamining;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.HasAvailabilityCheck;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.dto.StoredDataMiningQueryDTO;
import com.sap.sse.datamining.shared.impl.dto.StoredDataMiningQueryDTOImpl;
import com.sap.sse.datamining.ui.client.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.client.DataMiningWriteServiceAsync;
import com.sap.sse.datamining.ui.client.QueryRunner;
import com.sap.sse.datamining.ui.client.selection.QueryDefinitionProviderWithControls;
import com.sap.sse.gwt.client.DefaultErrorReporter;

/**
 * Data provider and presenter for {@link StoredDataMiningQueryPanel}. holds the {@link #queryDefinitions} and handles
 * the backend calls to find/load/store/remove stored queries.
 */
public class StoredDataMiningQueryDataProvider {
    private final Set<StoredDataMiningQueryDTO> queryDefinitions = new HashSet<>();

    private final QueryDefinitionProviderWithControls queryDefinitionProvider;
    private final DataMiningServiceAsync dataMiningService;
    private final DataMiningWriteServiceAsync dataMiningWriteService;
    private final QueryRunner queryRunner;
    private final StringMessages stringMessages;

    private StoredDataMiningQueryPanel uiPanel;

    public StoredDataMiningQueryDataProvider(QueryDefinitionProviderWithControls queryDefinitionProvider,
            DataMiningServiceAsync dataMiningService, DataMiningWriteServiceAsync dataMiningWriteService, QueryRunner queryRunner, StringMessages stringMessages) {
        this.queryDefinitionProvider = queryDefinitionProvider;
        this.dataMiningService = dataMiningService;
        this.queryRunner = queryRunner;
        this.dataMiningWriteService = dataMiningWriteService;
        this.stringMessages = stringMessages;
    }

    /** @return the query currently selected in the query definition provider. */
    public StatisticQueryDefinitionDTO getCurrentQuery() {
        return queryDefinitionProvider.getQueryDefinition();
    }

    /**
     * Creates a new stored query with the {@link name} or updates the stored query if it already exists with the new
     * {@link #query}.
     * 
     * @param callback called when operation has completed. Provides true, if stored query was present and this is an update<br/>
     *         false, if a new stored query was created
     */
    public void addOrUpdateQuery(String name, StatisticQueryDefinitionDTO query, Consumer<Boolean> callback) {
        Optional<StoredDataMiningQueryDTO> findByName = findByName(name);

        StoredDataMiningQueryDTO storedQuery;
        boolean update;

        if (findByName.isPresent()) {
            // update if present
            StoredDataMiningQueryDTO existingQuery = findByName.get();
            storedQuery = new StoredDataMiningQueryDTOImpl(name, existingQuery.getId(), query);
            update = true;
        } else {
            // create if new
            storedQuery = new StoredDataMiningQueryDTOImpl(name, UUID.randomUUID(), query);
            update = false;
        }

        dataMiningWriteService.updateOrCreateStoredQuery((StoredDataMiningQueryDTOImpl) storedQuery,
                new AsyncCallback<StoredDataMiningQueryDTOImpl>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log(caught.getMessage(), caught);
                        if (HasAvailabilityCheck.has5xxResponse(caught)) {
                            DefaultErrorReporter.reportMasterTemporarilyUnavailable(stringMessages);
                        }
                    }

                    @Override
                    public void onSuccess(StoredDataMiningQueryDTOImpl result) {
                        queryDefinitions.remove(result);
                        queryDefinitions.add(result);
                        updateUi();
                        callback.accept(update);
                    }
                });
    }

    /**
     * Removes a query by {@link #name}.
     * @param callback executed when operation has completed. Provides true, if a stored query with the {@link #name} existed<br/>
     *         false, if no query with the corresponding name could be found
     */
    public void removeQuery(String name, Consumer<Boolean> callback) {
        Optional<StoredDataMiningQueryDTO> query = findByName(name);
        if (query.isPresent()) {
            dataMiningWriteService.removeStoredQuery((StoredDataMiningQueryDTOImpl) query.get(),
                    new AsyncCallback<StoredDataMiningQueryDTOImpl>() {

                @Override
                public void onFailure(Throwable caught) {
                    GWT.log(caught.getMessage(), caught);
                    if (HasAvailabilityCheck.has5xxResponse(caught)) {
                        DefaultErrorReporter.reportMasterTemporarilyUnavailable(stringMessages);
                    }
                }

                @Override
                        public void onSuccess(StoredDataMiningQueryDTOImpl result) {
                    queryDefinitions.remove(result);
                    updateUi();
                    callback.accept(true);
                }
            });
        } else {
            callback.accept(false);
        }
    }

    /**
     * Applies the stored data mining with the {@link #name} to the query definition provider and the query runner
     * without executing it.
     * 
     * @return true, if a stored query with the {@link #name} existed<br/>
     *         false, if no query with the corresponding name could be found
     */
    public boolean applyQuery(String name) {
        Optional<StoredDataMiningQueryDTO> query = findByName(name);
        if (query.isPresent()) {
            queryDefinitionProvider.applyQueryDefinition(query.get().getQuery());
            return true;
        }
        return false;
    }

    /** Sets the associated {@link StoredDataMiningQueryPanel} and refreshes the stored named queries to present. */
    public void setUiPanel(StoredDataMiningQueryPanel uiPanel) {
        this.uiPanel = uiPanel;
        refreshQueries();
    }

    /** Update the oracle in the {@link StoredDataMiningQueryPanel}. */
    private void updateUi() {
        if (uiPanel != null) {
            uiPanel.updateOracle(
                    queryDefinitions.stream().map(StoredDataMiningQueryDTO::getName).collect(Collectors.toList()));
        }
    }

    /** Refresh the stored queries and update the {@link StoredDataMiningQueryPanel}. */
    private void refreshQueries() {
        dataMiningService.retrieveStoredQueries(new AsyncCallback<ArrayList<StoredDataMiningQueryDTOImpl>>() {
            @Override
            public void onSuccess(ArrayList<StoredDataMiningQueryDTOImpl> result) {
                queryDefinitions.addAll(result);
                updateUi();
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log(caught.getMessage(), caught);
            }
        });
    }

    /** @return the {@link StoredDataMiningQueryDTO} associated with {@link #name} */
    private Optional<StoredDataMiningQueryDTO> findByName(final String name) {
        return queryDefinitions.stream().filter(s -> s.getName().equals(name)).findAny();
    }

    /**
     * @return true: if {@link #text} is a valid name of a stored query<br/>
     *         false: else
     */
    public boolean containsQueryName(String text) {
        return findByName(text).isPresent();
    }
}
