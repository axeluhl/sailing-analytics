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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionChangedListener;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionProvider;
import com.sap.sailing.gwt.ui.datamining.SelectionChangedListener;
import com.sap.sailing.gwt.ui.datamining.SelectionProvider;
import com.sap.sse.datamining.shared.QueryDefinition;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.LocalizedTypeDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class RetrieverLevelSpecificSelectionProvider implements SelectionProvider<Object>, DataRetrieverChainDefinitionChangedListener {
    
    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<SelectionChangedListener> listeners;
    private final SelectionChangedListener singleRetrieverLevelSelectionProvidersListener;
    
    private DataRetrieverChainDefinitionDTO retrieverChain;
    
    private final ScrollPanel mainPanel;
    private final VerticalPanel contentPanel;
    private final Collection<SingleRetrieverLevelSelectionProviderPrototype> singleRetrieverLevelSelectionProviders;
    
    public RetrieverLevelSpecificSelectionProvider(StringMessages stringMessages, DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
                                                   DataRetrieverChainDefinitionProvider dataRetrieverChainDefinitionProvider) {
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        listeners = new HashSet<>();
        singleRetrieverLevelSelectionProvidersListener = new SelectionChangedListener() {
            @Override
            public void selectionChanged() {
                for (SelectionChangedListener listener : listeners) {
                    listener.selectionChanged();
                }
            }
        };
        
        singleRetrieverLevelSelectionProviders = new ArrayList<>();
        
        contentPanel = new VerticalPanel();
        mainPanel = new ScrollPanel(contentPanel);
        
        dataRetrieverChainDefinitionProvider.addDataRetrieverChainDefinitionChangedListener(this);
    }
    
    @Override
    public void dataRetrieverChainDefinitionChanged(DataRetrieverChainDefinitionDTO newDataRetrieverChainDefinition) {
        if (!Objects.equals(retrieverChain, newDataRetrieverChainDefinition)) {
            retrieverChain = newDataRetrieverChainDefinition;
            updateRetrievalLevels();
        }
    }

    private void updateRetrievalLevels() {
        contentPanel.clear();
        singleRetrieverLevelSelectionProviders.clear();
        
        int retrieverLevel = 0;
        boolean first = true;
        for (LocalizedTypeDTO retrievedDataType : retrieverChain.getRetrievedDataTypesChain()) {
            if (!first) {
                contentPanel.add(new Label("|"));
            }
            first = false;
            
            SingleRetrieverLevelSelectionProviderPrototype singleRetrieverLevelSelectionProvider = new SingleRetrieverLevelSelectionProviderPrototype(stringMessages, dataMiningService, errorReporter,
                                                                                                                                    retrieverChain, retrievedDataType, retrieverLevel );
            singleRetrieverLevelSelectionProvider.addSelectionChangedListener(singleRetrieverLevelSelectionProvidersListener);
            
            contentPanel.add(singleRetrieverLevelSelectionProvider);
            singleRetrieverLevelSelectionProviders.add(singleRetrieverLevelSelectionProvider);
            
            retrieverLevel++;
        }
        
        udpateAvailableDimensions();
    }

    private void udpateAvailableDimensions() {
        dataMiningService.getDimensionsFor(retrieverChain, LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<Collection<FunctionDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error fetching the dimensions of the retrieval chain from the server: " + caught.getMessage());
            }
            @Override
            public void onSuccess(Collection<FunctionDTO> dimensions) {
                Map<String, Collection<FunctionDTO>> dimensionsMappedBySourceType = mapBySourceType(dimensions);
                
                for (SingleRetrieverLevelSelectionProviderPrototype singleRetrieverLevelSelectionProvider : singleRetrieverLevelSelectionProviders) {
                    String sourceType = singleRetrieverLevelSelectionProvider.getRetrievedDataType().getTypeName();
                    if (dimensionsMappedBySourceType.containsKey(sourceType)) {
                        singleRetrieverLevelSelectionProvider.setAvailableDimensions(dimensionsMappedBySourceType.get(sourceType));
                    }
                }
            }
        });
    }

    private Map<String, Collection<FunctionDTO>> mapBySourceType(Collection<FunctionDTO> dimensions) {
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
        for (SingleRetrieverLevelSelectionProviderPrototype singleRetrieverLevelSelectionProvider : singleRetrieverLevelSelectionProviders) {
            Map<FunctionDTO, Collection<? extends Serializable>> levelFilterSelection = singleRetrieverLevelSelectionProvider.getFilterSelection();
            if (!levelFilterSelection.isEmpty()) {
                filterSelection.put(singleRetrieverLevelSelectionProvider.getRetrieverLevel(),
                        new HashMap<FunctionDTO, Collection<? extends Serializable>>(levelFilterSelection));
            }
        }
        return filterSelection;
    }

    @Override
    public void applySelection(QueryDefinition queryDefinition) {
        // TODO
    }

    @Override
    public void clearSelection() {
        for (SingleRetrieverLevelSelectionProviderPrototype singleRetrieverLevelSelectionProvider : singleRetrieverLevelSelectionProviders) {
            singleRetrieverLevelSelectionProvider.clearSelection();
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

}
