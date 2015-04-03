package com.sap.sailing.gwt.ui.datamining.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.SelectionChangedListener;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.LocalizedTypeDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class RetrieverLevelFilterSelectionProvider implements Component<Object> {

    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<SelectionChangedListener> listeners;
    
    private final DataMiningSession session;
    private final DataRetrieverChainDefinitionDTO retrieverChain;
    private final LocalizedTypeDTO retrievedDataType;
    private final int retrieverLevel;
    private final Collection<FunctionDTO> availableDimensions;
    
    private final HorizontalLayoutPanel mainPanel;
    private final Collection<DimensionFilterSelectionProvider> dimensionFilters;
    private final SelectionChangeEvent.Handler selectionTablesChangedListener;
    private final SizeProvider sizeProvider;

    public RetrieverLevelFilterSelectionProvider(DataMiningSession session, DataMiningServiceAsync dataMiningService,
            ErrorReporter errorReporter, DataRetrieverChainDefinitionDTO retrieverChain,
            LocalizedTypeDTO retrievedDataType, int retrieverLevel, SizeProvider sizeProvider) {
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        listeners = new HashSet<>();
        
        this.session = session;
        this.retrieverChain = retrieverChain;
        this.retrievedDataType = retrievedDataType;
        this.retrieverLevel = retrieverLevel;
        availableDimensions = new ArrayList<>();
        
        mainPanel = new HorizontalLayoutPanel();
        dimensionFilters = new ArrayList<>();
        selectionTablesChangedListener = new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                notifyListeners();
            }
        };

        this.sizeProvider = sizeProvider;
    }

    public void setAvailableDimensions(Collection<FunctionDTO> dimensions) {
        availableDimensions.clear();
        availableDimensions.addAll(dimensions);
        initializeDimensionFilters();
    }

    private void initializeDimensionFilters() {
        DimensionFilterSelectionProvider dimensionFilter = createDimensionFilter();
        List<FunctionDTO> availableDimensionsList = new ArrayList<>(availableDimensions);
        Collections.sort(availableDimensionsList);
        dimensionFilter.setAvailableDimensions(availableDimensionsList);
        addDimensionFilter(dimensionFilter);
    }
    
    boolean canAddDimensionFilter() {
        return availableDimensions.size() != getSelectedDimensions().size();
    }
    
    void createAndAddDimensionFilter() {
        addDimensionFilter(createDimensionFilter());
    }

    private DimensionFilterSelectionProvider createDimensionFilter() {
        DimensionFilterSelectionProvider dimensionFilter = new DimensionFilterSelectionProvider(dataMiningService, errorReporter, session, this);
        dimensionFilter.addSelectionChangeHandler(selectionTablesChangedListener);
        return dimensionFilter;
    }
    
    private void addDimensionFilter(DimensionFilterSelectionProvider dimensionFilter) {
        dimensionFilters.add(dimensionFilter);
        mainPanel.add(dimensionFilter.getEntryWidget());
        mainPanel.onResize();
    }

    boolean shouldRemoveDimensionFilter() {
        return availableDimensions.size() - getSelectedDimensions().size() > 1;
    }
    
    void removeDimensionFilter(DimensionFilterSelectionProvider dimensionFilter) {
        dimensionFilter.clearSelection(); // Notifies the listeners, if values were selected
        dimensionFilters.remove(dimensionFilter);
        mainPanel.remove(dimensionFilter.getEntryWidget());
        mainPanel.onResize();
    }
    
    void updateAvailableDimensions() {
        Collection<FunctionDTO> remainingDimensionsBase = new ArrayList<FunctionDTO>(availableDimensions);
        remainingDimensionsBase.removeAll(getSelectedDimensions());
        for (DimensionFilterSelectionProvider dimensionFilter : dimensionFilters) {
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
        for (DimensionFilterSelectionProvider dimensionFilter : dimensionFilters) {
            FunctionDTO selectedDimension = dimensionFilter.getSelectedDimension();
            if (selectedDimension != null) {
                selectedDimensions.add(selectedDimension);
            }
        }
        return selectedDimensions;
    }

    public Map<FunctionDTO, Collection<? extends Serializable>> getFilterSelection() {
        HashMap<FunctionDTO, Collection<? extends Serializable>> filterSelection = new HashMap<>();
        for (DimensionFilterSelectionProvider dimensionFilter : dimensionFilters) {
            Collection<? extends Serializable> dimensionFilterSelection = dimensionFilter.getSelection();
            if (!dimensionFilterSelection.isEmpty()) {
                filterSelection.put(dimensionFilter.getSelectedDimension(), dimensionFilterSelection);
            }
        }
        return filterSelection;
    }

    public void applySelection(Map<FunctionDTO, Collection<? extends Serializable>> filterSelection) {
        dimensionFilters.clear();
        mainPanel.clear();
        
        List<FunctionDTO> sortedDimensions = new ArrayList<>(filterSelection.keySet());
        Collections.sort(sortedDimensions);
        
        for (FunctionDTO functionDTO : sortedDimensions) {
            DimensionFilterSelectionProvider dimensionFilter = createDimensionFilter();
            addDimensionFilter(dimensionFilter);
            updateAvailableDimensions();
            dimensionFilter.setSelectedDimensionAndValues(functionDTO, filterSelection.get(functionDTO));
        }
    }

    public void clearSelection() {
        dimensionFilters.clear();
        mainPanel.clear();
        initializeDimensionFilters();
        notifyListeners();
    }
    
    public void addSelectionChangedListener(SelectionChangedListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (SelectionChangedListener listener : listeners) {
            listener.selectionChanged();
        }
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
    public SettingsDialogComponent<Object> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(Object newSettings) {
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
    
    private class HorizontalLayoutPanel extends HorizontalPanel implements RequiresResize, ProvidesResize {
        @Override
        public void onResize() {
            int heightInPX = sizeProvider.getFreeHeightInPX();
            setHeight(heightInPX + "px");
            for (DimensionFilterSelectionProvider selectionProvider : dimensionFilters) {
                selectionProvider.resizeToHeight(heightInPX);
            }
        }
    }

}
