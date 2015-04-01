package com.sap.sailing.gwt.ui.datamining.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionChangedListener;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionProvider;
import com.sap.sailing.gwt.ui.datamining.SelectionChangedListener;
import com.sap.sailing.gwt.ui.datamining.SelectionProvider;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.LocalizedTypeDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class RetrieverLevelSpecificSelectionProvider implements SelectionProvider<Object>, DataRetrieverChainDefinitionChangedListener,
                                                                SelectionChangedListener {
    
    private final DataMiningSession session;
    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<SelectionChangedListener> listeners;
    
    private DataRetrieverChainDefinitionDTO retrieverChain;
    
    private final ScrollPanel mainPanel;
    private final FlowLayoutPanel contentPanel;
    private final Collection<SingleRetrieverLevelSelectionProviderPrototype> SingleRetrieverLevelSelectionProviders;
    
    public RetrieverLevelSpecificSelectionProvider(DataMiningSession session, StringMessages stringMessages,
                                                   DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
                                                   DataRetrieverChainDefinitionProvider dataRetrieverChainDefinitionProvider) {
        this.session = session;
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        listeners = new HashSet<>();
        
        SingleRetrieverLevelSelectionProviders = new ArrayList<>();
        
        contentPanel = new FlowLayoutPanel();
        mainPanel = new ScrollPanel(contentPanel);
        
        dataRetrieverChainDefinitionProvider.addDataRetrieverChainDefinitionChangedListener(this);
    }
    
    @Override
    public void dataRetrieverChainDefinitionChanged(DataRetrieverChainDefinitionDTO newDataRetrieverChainDefinition) {
        if (!Objects.equals(retrieverChain, newDataRetrieverChainDefinition)) {
            retrieverChain = newDataRetrieverChainDefinition;
            if (retrieverChain != null) {
                updateRetrievalLevels();
            } else {
                contentPanel.clear();
                SingleRetrieverLevelSelectionProviders.clear();
            }
        }
    }

    private void updateRetrievalLevels() {
        dataMiningService.getDimensionsFor(retrieverChain, LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<Iterable<FunctionDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error fetching the dimensions of the retrieval chain from the server: " + caught.getMessage());
            }
            @Override
            public void onSuccess(Iterable<FunctionDTO> dimensions) {
                contentPanel.clear();
                SingleRetrieverLevelSelectionProviders.clear();

                Map<String, Collection<FunctionDTO>> dimensionsMappedBySourceType = mapBySourceType(dimensions);
                int retrieverLevel = 0;
                boolean first = true;
                for (LocalizedTypeDTO retrievedDataType : retrieverChain.getRetrievedDataTypesChain()) {
                    if (!first) {
                        Label retrieverLevelSeparatorLabel = new Label("|");
                        contentPanel.add(retrieverLevelSeparatorLabel);
                    }
                    first = false;

                    String sourceTypeName = retrievedDataType.getTypeName();
                    if (dimensionsMappedBySourceType.containsKey(sourceTypeName) &&
                        !dimensionsMappedBySourceType.get(sourceTypeName).isEmpty()) {
                        SingleRetrieverLevelSelectionProviderPrototype singleRetrieverLevelSelectionProvider =
                                new SingleRetrieverLevelSelectionProviderPrototype(session, stringMessages, dataMiningService, errorReporter,
                                                                                   retrieverChain, retrievedDataType, retrieverLevel );
                        singleRetrieverLevelSelectionProvider.addSelectionChangedListener(RetrieverLevelSpecificSelectionProvider.this);
                        contentPanel.add(singleRetrieverLevelSelectionProvider);
                        SingleRetrieverLevelSelectionProviders.add(singleRetrieverLevelSelectionProvider);
                        singleRetrieverLevelSelectionProvider.setAvailableDimensions(dimensionsMappedBySourceType.get(sourceTypeName));
                    } else {
                        contentPanel.add(new Label(retrievedDataType.getDisplayName()));
                    }
                    
                    retrieverLevel++;
                }
            }
        });
    }

    private Map<String, Collection<FunctionDTO>> mapBySourceType(Iterable<FunctionDTO> dimensions) {
        Map<String, Collection<FunctionDTO>> dimensionsMappedBySourceType = new HashMap<>();
        for (FunctionDTO dimension : dimensions) {
            if (!dimensionsMappedBySourceType.containsKey(dimension.getSourceTypeName())) {
                dimensionsMappedBySourceType.put(dimension.getSourceTypeName(), new HashSet<FunctionDTO>());
            }
            dimensionsMappedBySourceType.get(dimension.getSourceTypeName()).add(dimension);
        }
        return dimensionsMappedBySourceType;
    }

    @Override
    public Map<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> getFilterSelection() {
        Map<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> filterSelection = new HashMap<>();
        for (SingleRetrieverLevelSelectionProviderPrototype singleRetrieverLevelSelectionProvider : SingleRetrieverLevelSelectionProviders) {
            Map<FunctionDTO, Collection<? extends Serializable>> levelFilterSelection = singleRetrieverLevelSelectionProvider.getFilterSelection();
            if (!levelFilterSelection.isEmpty()) {
                filterSelection.put(singleRetrieverLevelSelectionProvider.getRetrieverLevel(),
                        new HashMap<FunctionDTO, Collection<? extends Serializable>>(levelFilterSelection));
            }
        }
        return filterSelection;
    }

    @Override
    public void applySelection(QueryDefinitionDTO queryDefinition) {
        for (SingleRetrieverLevelSelectionProviderPrototype singleRetrieverLevelSelectionProvider : SingleRetrieverLevelSelectionProviders) {
            Map<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> filterSelection = queryDefinition.getFilterSelection();
            int retrieverLevel = singleRetrieverLevelSelectionProvider.getRetrieverLevel();
            if (filterSelection.containsKey(retrieverLevel)) {
                singleRetrieverLevelSelectionProvider.applySelection(filterSelection.get(retrieverLevel));
            }
        }
    }

    @Override
    public void clearSelection() {
        for (SingleRetrieverLevelSelectionProviderPrototype singleRetrieverLevelSelectionProvider : SingleRetrieverLevelSelectionProviders) {
            singleRetrieverLevelSelectionProvider.clearSelection();
        }
    }
    
    @Override
    public void selectionChanged() {
        for (SelectionChangedListener listener : listeners) {
            listener.selectionChanged();
        }
    }

    @Override
    public void addSelectionChangedListener(SelectionChangedListener listener) {
        listeners.add(listener);
    }

    @Override
    public String getLocalizedShortName() {
        return RetrieverLevelSpecificSelectionProvider.class.getSimpleName();
    }

    @Override
    public Widget getEntryWidget() {
        return mainPanel;
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
    public String getDependentCssClassName() {
        return "retrieverLevelSpecificSelectionProvider";
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
    public void updateSettings(Object newSettings) {
    }
    
    private class FlowLayoutPanel extends FlowPanel implements RequiresResize, ProvidesResize {
        @Override
        public void onResize() {
            for (Widget child : getChildren()) {
                if (child instanceof RequiresResize)
                    ((RequiresResize) child).onResize();
            }
        }
    }

}
