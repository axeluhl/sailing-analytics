package com.sap.sailing.gwt.ui.datamining.selection.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.LocalizedTypeDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class RetrieverLevelFilterSelectionProvider implements Component<AbstractSettings> {

    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    
    private final DataMiningSession session;
    private final ListRetrieverChainFilterSelectionProvider retrieverChainSelectionProvider;
    private final DataRetrieverChainDefinitionDTO retrieverChain;
    private final LocalizedTypeDTO retrievedDataType;
    private final int retrieverLevel;
    private final Collection<FunctionDTO> availableDimensions;
    
    private final HorizontalLayoutPanel mainPanel;
    private final Collection<DimensionFilterSelectionProvider> dimensionSelectionProviders;
    private final SizeProvider sizeProvider;

    public RetrieverLevelFilterSelectionProvider(DataMiningSession session, DataMiningServiceAsync dataMiningService,
            ErrorReporter errorReporter, ListRetrieverChainFilterSelectionProvider retrieverChainSelectionProvider, DataRetrieverChainDefinitionDTO retrieverChain,
            LocalizedTypeDTO retrievedDataType, int retrieverLevel, SizeProvider sizeProvider) {
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        
        this.session = session;
        this.retrieverChainSelectionProvider = retrieverChainSelectionProvider;
        this.retrieverChain = retrieverChain;
        this.retrievedDataType = retrievedDataType;
        this.retrieverLevel = retrieverLevel;
        availableDimensions = new ArrayList<>();
        
        mainPanel = new HorizontalLayoutPanel();
        dimensionSelectionProviders = new ArrayList<>();

        this.sizeProvider = sizeProvider;
    }

    public void setAvailableDimensions(Collection<FunctionDTO> dimensions) {
        availableDimensions.clear();
        availableDimensions.addAll(dimensions);
        initializeDimensionSelectionProviders();
    }

    private void initializeDimensionSelectionProviders() {
        DimensionFilterSelectionProvider dimensionFilter = createDimensionSelectionProvider();
        List<FunctionDTO> availableDimensionsList = new ArrayList<>(availableDimensions);
        Collections.sort(availableDimensionsList);
        dimensionFilter.setAvailableDimensions(availableDimensionsList);
        addDimensionSelectionProvider(dimensionFilter);
    }
    
    boolean canAddDimensionSelectionProvider() {
        return availableDimensions.size() != getSelectedDimensions().size();
    }
    
    void createAndAddDimensionSelectionProvider() {
        addDimensionSelectionProvider(createDimensionSelectionProvider());
    }

    private DimensionFilterSelectionProvider createDimensionSelectionProvider() {
        DimensionFilterSelectionProvider dimensionFilter = new DimensionFilterSelectionProvider(dataMiningService, errorReporter, session, this);
        return dimensionFilter;
    }
    
    private void addDimensionSelectionProvider(DimensionFilterSelectionProvider dimensionFilter) {
        dimensionSelectionProviders.add(dimensionFilter);
        mainPanel.add(dimensionFilter.getEntryWidget());
        mainPanel.onResize();
    }

    boolean shouldRemoveDimensionSelectionProvider() {
        return availableDimensions.size() - getSelectedDimensions().size() > 1;
    }
    
    void removeDimensionFilter(DimensionFilterSelectionProvider dimensionFilter) {
        dimensionFilter.clearSelection(); // Notifies the listeners, if values were selected
        dimensionSelectionProviders.remove(dimensionFilter);
        mainPanel.remove(dimensionFilter.getEntryWidget());
        mainPanel.onResize();
    }
    
    void updateAvailableDimensions() {
        Collection<FunctionDTO> remainingDimensionsBase = new ArrayList<FunctionDTO>(availableDimensions);
        remainingDimensionsBase.removeAll(getSelectedDimensions());
        for (DimensionFilterSelectionProvider dimensionFilter : dimensionSelectionProviders) {
            List<FunctionDTO> remainingDimensions = new ArrayList<>(remainingDimensionsBase);
            FunctionDTO selectedDimension = dimensionFilter.getSelectedDimension();
            if (selectedDimension != null) {
                remainingDimensions.add(selectedDimension);
            }
            Collections.sort(remainingDimensions);
            remainingDimensions.add(null);
            dimensionFilter.setAvailableDimensions(remainingDimensions);
        }
        mainPanel.onResize();
    }

    private Collection<FunctionDTO> getSelectedDimensions() {
        Collection<FunctionDTO> selectedDimensions = new ArrayList<>();
        for (DimensionFilterSelectionProvider dimensionFilter : dimensionSelectionProviders) {
            FunctionDTO selectedDimension = dimensionFilter.getSelectedDimension();
            if (selectedDimension != null) {
                selectedDimensions.add(selectedDimension);
            }
        }
        return selectedDimensions;
    }
    
    void updateAvailableData(FunctionDTO exceptForDimension) {
        for (DimensionFilterSelectionProvider selectionProvider : dimensionSelectionProviders) {
            FunctionDTO selectedDimension = selectionProvider.getSelectedDimension();
            if (selectedDimension != null && !selectedDimension.equals(exceptForDimension)) {
                selectionProvider.fetchAndDisplayAvailableData(true);
            }
        }
    }

    void dimensionFilterSelectionChanged(DimensionFilterSelectionProvider dimensionFilterSelectionProvider) {
        retrieverChainSelectionProvider.retrieverLevelFilterSelectionChanged(this, dimensionFilterSelectionProvider);
    }

    Map<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> getCompleteFilterSelection() {
        return retrieverChainSelectionProvider.getSelection();
    }

    public Map<FunctionDTO, Collection<? extends Serializable>> getFilterSelection() {
        HashMap<FunctionDTO, Collection<? extends Serializable>> filterSelection = new HashMap<>();
        for (DimensionFilterSelectionProvider dimensionFilter : dimensionSelectionProviders) {
            Collection<? extends Serializable> dimensionFilterSelection = dimensionFilter.getSelection();
            if (!dimensionFilterSelection.isEmpty()) {
                filterSelection.put(dimensionFilter.getSelectedDimension(), dimensionFilterSelection);
            }
        }
        return filterSelection;
    }

    public void applySelection(Map<FunctionDTO, Collection<? extends Serializable>> filterSelection) {
        dimensionSelectionProviders.clear();
        mainPanel.clear();
        
        List<FunctionDTO> sortedDimensions = new ArrayList<>(filterSelection.keySet());
        Collections.sort(sortedDimensions);
        
        for (FunctionDTO functionDTO : sortedDimensions) {
            DimensionFilterSelectionProvider dimensionFilter = createDimensionSelectionProvider();
            addDimensionSelectionProvider(dimensionFilter);
            updateAvailableDimensions();
            dimensionFilter.setSelectedDimensionAndValues(functionDTO, filterSelection.get(functionDTO));
        }
    }

    public void clearSelection() {
        mainPanel.clear();
        for (DimensionFilterSelectionProvider dimensionFilter : dimensionSelectionProviders) {
            dimensionFilter.clearSelection();
        }
        dimensionSelectionProviders.clear();
        initializeDimensionSelectionProviders();
    }

    DataRetrieverChainDefinitionDTO getDataRetrieverChain() {
        return retrieverChain;
    }
    
    public int getRetrieverLevel() {
        return retrieverLevel;
    }
    
    public LocalizedTypeDTO getRetrievedDataType() {
        return retrievedDataType;
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<AbstractSettings> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(AbstractSettings newSettings) {
    }

    @Override
    public String getLocalizedShortName() {
        return getClass().getSimpleName();
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
        return "singleRetrieverLevelSelectionPanel";
    }
    
    interface SizeProvider {
        
        public int getFreeWidthInPX();
        public int getFreeHeightInPX();

    }
    
    private class HorizontalLayoutPanel extends HorizontalPanel implements RequiresResize, ProvidesResize {
        @Override
        public void onResize() {
            int heightInPX = sizeProvider.getFreeHeightInPX();
            setHeight(heightInPX + "px");
            for (DimensionFilterSelectionProvider selectionProvider : dimensionSelectionProviders) {
                selectionProvider.resizeToHeight(heightInPX);
            }
        }
    }

}
