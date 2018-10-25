package com.sap.sailing.gwt.ui.datamining;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.dto.StoredDataMiningQueryDTO;
import com.sap.sse.datamining.shared.impl.dto.StoredDataMiningQueryDTOImpl;
import com.sap.sse.datamining.ui.client.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.client.selection.QueryDefinitionProviderWithControls;

public class StoredDataMiningQueryDataProvider {
    private final Set<StoredDataMiningQueryDTO> queryDefinitions = new HashSet<>();

    private final QueryDefinitionProviderWithControls queryDefinitionProvider;
    private final DataMiningServiceAsync dataMiningService;

    private StoredDataMiningQueryPanel uiPanel;

    public StoredDataMiningQueryDataProvider(QueryDefinitionProviderWithControls queryDefinitionProvider,
            DataMiningServiceAsync dataMiningService) {
        this.queryDefinitionProvider = queryDefinitionProvider;
        this.dataMiningService = dataMiningService;
    }

    public StatisticQueryDefinitionDTO getCurrentQuery() {
        return queryDefinitionProvider.getQueryDefinition();
    }

    public boolean addOrUpdateQuery(String name, StatisticQueryDefinitionDTO query) {
        StoredDataMiningQueryDTOImpl storedQuery = new StoredDataMiningQueryDTOImpl(name, UUID.randomUUID(), query);
        boolean update = queryDefinitions.contains(storedQuery);

        dataMiningService.updateOrCreateStoredQuery(storedQuery, new AsyncCallback<StoredDataMiningQueryDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log(caught.getMessage(), caught);
            }

            @Override
            public void onSuccess(StoredDataMiningQueryDTO result) {
                queryDefinitions.add(result);
                updateUi();
            }
        });
        return update;
    }

    public boolean removeQuery(String name) {
        Optional<StoredDataMiningQueryDTO> query = findByName(name);
        if (query.isPresent()) {
            dataMiningService.removeStoredQuery(query.get(), new AsyncCallback<StoredDataMiningQueryDTO>() {

                @Override
                public void onFailure(Throwable caught) {
                    GWT.log(caught.getMessage(), caught);
                }

                @Override
                public void onSuccess(StoredDataMiningQueryDTO result) {
                    queryDefinitions.remove(result);
                    updateUi();
                }
            });
            return true;
        }
        return false;
    }

    public boolean applyQuery(String name) {
        Optional<StoredDataMiningQueryDTO> query = findByName(name);
        if (query.isPresent()) {
            queryDefinitionProvider.applyQueryDefinition(query.get().getQuery());
            return true;
        }
        return false;
    }

    public void setUiPanel(StoredDataMiningQueryPanel uiPanel) {
        this.uiPanel = uiPanel;
        refreshQueries();
    }

    private void updateUi() {
        if (uiPanel != null) {
            uiPanel.updateOracle(
                    queryDefinitions.stream().map(StoredDataMiningQueryDTO::getName).collect(Collectors.toList()));
        }
    }

    private void refreshQueries() {
        dataMiningService.retrieveStoredQueries(new AsyncCallback<ArrayList<StoredDataMiningQueryDTO>>() {
            @Override
            public void onSuccess(ArrayList<StoredDataMiningQueryDTO> result) {
                queryDefinitions.addAll(result);
                updateUi();
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log(caught.getMessage(), caught);
            }
        });
    }

    private Optional<StoredDataMiningQueryDTO> findByName(final String name) {
        return queryDefinitions.stream().filter(s -> s.getName().equals(name)).findAny();
    }
}
