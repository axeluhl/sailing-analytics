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

public class SingleRetrieverLevelSelectionProvider implements Component<Object> {

    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<SelectionChangedListener> listeners;
    
    private final DataMiningSession session;
    private final DataRetrieverChainDefinitionDTO retrieverChain;
    private final LocalizedTypeDTO retrievedDataType;
    private final int retrieverLevel;
    private final Collection<FunctionDTO> availableDimensions;
    
    private final HorizontalLayoutPanel mainPanel;
    private final Collection<SingleDimensionFilter> dimensionFilters;
    private final SelectionChangeEvent.Handler selectionTablesChangedListener;
    private final Widget sizeProvider;

    public SingleRetrieverLevelSelectionProvider(DataMiningSession session, DataMiningServiceAsync dataMiningService,
            ErrorReporter errorReporter, DataRetrieverChainDefinitionDTO retrieverChain,
            LocalizedTypeDTO retrievedDataType, int retrieverLevel, Widget sizeProvider) {
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
        
        SingleDimensionFilter dimensionFilter = createDimensionFilter();
        List<FunctionDTO> availableDimensionsList = new ArrayList<>(availableDimensions);
        Collections.sort(availableDimensionsList);
        dimensionFilter.setAvailableDimensions(availableDimensionsList);
        addDimensionFilter(dimensionFilter);
    }
    
    void createAndAddDimensionFilter() {
        addDimensionFilter(createDimensionFilter());
    }

    private SingleDimensionFilter createDimensionFilter() {
        SingleDimensionFilter dimensionFilter = new SingleDimensionFilter(dataMiningService, errorReporter, session, this);
        dimensionFilter.addSelectionChangeHandler(selectionTablesChangedListener);
        return dimensionFilter;
    }
    
    private void addDimensionFilter(SingleDimensionFilter dimensionFilter) {
        dimensionFilters.add(dimensionFilter);
        mainPanel.add(dimensionFilter.getEntryWidget());
        mainPanel.onResize();
    }

    public void removeDimensionFilter(SingleDimensionFilter dimensionFilter) {
        dimensionFilter.clearSelection(); // Notifies the listeners, if values were selected
        dimensionFilters.remove(dimensionFilter);
        mainPanel.remove(dimensionFilter.getEntryWidget());
        mainPanel.onResize();
    }
    
    void updateAvailableDimensions() {
        Collection<FunctionDTO> remainingDimensionsBase = new ArrayList<FunctionDTO>(availableDimensions);
        remainingDimensionsBase.removeAll(getSelectedDimensions());
        for (SingleDimensionFilter dimensionFilter : dimensionFilters) {
            List<FunctionDTO> remainingDimensions = new ArrayList<>(remainingDimensionsBase);
            FunctionDTO selectedDimension = dimensionFilter.getSelectedDimension();
            if (selectedDimension != null) {
                remainingDimensions.add(selectedDimension);
            }
            Collections.sort(remainingDimensions);
            remainingDimensions.add(null);
            dimensionFilter.setAvailableDimensions(remainingDimensions);
        }
    }

    private Collection<FunctionDTO> getSelectedDimensions() {
        Collection<FunctionDTO> selectedDimensions = new ArrayList<>();
        for (SingleDimensionFilter dimensionFilter : dimensionFilters) {
            FunctionDTO selectedDimension = dimensionFilter.getSelectedDimension();
            if (selectedDimension != null) {
                selectedDimensions.add(selectedDimension);
            }
        }
        return selectedDimensions;
    }

    public Map<FunctionDTO, Collection<? extends Serializable>> getFilterSelection() {
        HashMap<FunctionDTO, Collection<? extends Serializable>> filterSelection = new HashMap<>();
        for (SingleDimensionFilter dimensionFilter : dimensionFilters) {
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
            SingleDimensionFilter dimensionFilter = createDimensionFilter();
            addDimensionFilter(dimensionFilter);
            updateAvailableDimensions();
            dimensionFilter.setSelectedDimensionAndValues(functionDTO, filterSelection.get(functionDTO));
        }
    }

    public void clearSelection() {
        for (SingleDimensionFilter dimensionFilter : dimensionFilters) {
            dimensionFilter.clearSelection();
        }
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
            setSize(sizeProvider.getOffsetWidth() + "px", sizeProvider.getOffsetHeight() + "px");
            
            int heightInPX = sizeProvider.getOffsetHeight();
            for (SingleDimensionFilter selectionProvider : dimensionFilters) {
                selectionProvider.resizeToHeight(heightInPX);
            }
        }
    }

}
