package com.sap.sse.datamining.ui.selection.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.ui.DataMiningServiceAsync;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.AbstractComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class RetrieverLevelFilterSelectionProvider extends AbstractComponent<AbstractSettings> {

    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;

    private final DataMiningSession session;
    private final ListRetrieverChainFilterSelectionProvider retrieverChainSelectionProvider;
    private final DataRetrieverChainDefinitionDTO retrieverChain;
    private final DataRetrieverLevelDTO retrieverLevel;
    private final Collection<FunctionDTO> availableDimensions;

    private final HorizontalPanel mainPanel;
    private final Collection<DimensionFilterSelectionProvider> dimensionSelectionProviders;

    public RetrieverLevelFilterSelectionProvider(Component<?> parent, ComponentContext<?> context,
            DataMiningSession session, DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
            ListRetrieverChainFilterSelectionProvider retrieverChainSelectionProvider,
            DataRetrieverChainDefinitionDTO retrieverChain, DataRetrieverLevelDTO retrieverLevel) {
        super(parent, context);
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;

        this.session = session;
        this.retrieverChainSelectionProvider = retrieverChainSelectionProvider;
        this.retrieverChain = retrieverChain;
        this.retrieverLevel = retrieverLevel;
        availableDimensions = new ArrayList<>();

        mainPanel = new HorizontalPanel();
        dimensionSelectionProviders = new ArrayList<>();
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
        DimensionFilterSelectionProvider dimensionFilter = new DimensionFilterSelectionProvider(dataMiningService,
                errorReporter, session, this);
        return dimensionFilter;
    }

    private void addDimensionSelectionProvider(DimensionFilterSelectionProvider dimensionFilter) {
        dimensionSelectionProviders.add(dimensionFilter);
        dimensionFilter.getEntryWidget().getElement().getStyle().setDisplay(Display.INLINE);
        mainPanel.add(dimensionFilter.getEntryWidget());
    }

    boolean shouldRemoveDimensionSelectionProvider() {
        return availableDimensions.size() - getSelectedDimensions().size() > 1;
    }

    void removeDimensionFilter(DimensionFilterSelectionProvider dimensionFilter) {
        dimensionFilter.clearSelection(); // Notifies the listeners, if values were selected
        dimensionSelectionProviders.remove(dimensionFilter);
        mainPanel.remove(dimensionFilter.getEntryWidget());
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
    }

    public boolean hasDimension(FunctionDTO dimension) {
        return availableDimensions.contains(dimension);
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

    /**
     * Updates the dimension values except for the given dimension.<br>
     * Aborts the update, if it caused a selection change.
     */
    void updateAvailableData(FunctionDTO exceptForDimension) {
        updateAvailableData(exceptForDimension, dimensionSelectionProviders.iterator());
    }

    void updateAvailableData(final FunctionDTO exceptForDimension,
            final Iterator<DimensionFilterSelectionProvider> selectionProviderIterator) {
        if (selectionProviderIterator.hasNext()) {
            DimensionFilterSelectionProvider selectionProvider = selectionProviderIterator.next();
            FunctionDTO selectedDimension = selectionProvider.getSelectedDimension();
            if (selectedDimension != null && !selectedDimension.equals(exceptForDimension)) {
                selectionProvider.fetchAndUpdateAvailableData(selectionProviderIterator);
            } else {
                updateAvailableData(exceptForDimension, selectionProviderIterator);
            }
        } else {
            // Update for this level is completed. Continue with the next level.
            retrieverChainSelectionProvider.updateFilterSelectionProviders(
                    retrieverChain.getNextRetrieverLevel(getRetrieverLevel()), exceptForDimension);
        }
    }

    void dimensionFilterSelectionChanged(DimensionFilterSelectionProvider dimensionFilterSelectionProvider) {
        retrieverChainSelectionProvider.retrieverLevelFilterSelectionChanged(this, dimensionFilterSelectionProvider);
    }

    HashMap<DataRetrieverLevelDTO, SerializableSettings> getRetrieverSettings() {
        return retrieverChainSelectionProvider.getRetrieverSettings();
    }

    HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> getCompleteFilterSelection() {
        return retrieverChainSelectionProvider.getSelection();
    }

    public Map<FunctionDTO, HashSet<? extends Serializable>> getFilterSelection() {
        HashMap<FunctionDTO, HashSet<? extends Serializable>> filterSelection = new HashMap<>();
        for (DimensionFilterSelectionProvider dimensionFilter : dimensionSelectionProviders) {
            HashSet<? extends Serializable> dimensionFilterSelection = dimensionFilter.getSelection();
            if (!dimensionFilterSelection.isEmpty()) {
                filterSelection.put(dimensionFilter.getSelectedDimension(), dimensionFilterSelection);
            }
        }
        return filterSelection;
    }

    /**
     * If there is already a filter set for {@code dimension}, replace its value selection by the {@code values}
     * provided. Otherwise, set the last (expectedly non-selected) {@link DimensionFilterSelectionProvider} to
     * {@code dimension} and set its filter values to {@code values}.
     * <p>
     * 
     * <em>Precondition:</em> {@link #hasDimension(FunctionDTO) hasDimension(dimension)}{@code == true}
     */
    public void addFilter(FunctionDTO dimension, Set<? extends Serializable> values) {
        DimensionFilterSelectionProvider dimensionFilter = null;
        for (final DimensionFilterSelectionProvider dimensionSelectionProvider : dimensionSelectionProviders) {
            dimensionFilter = dimensionSelectionProvider;
            if (Util.equalsWithNull(dimensionSelectionProvider.getSelectedDimension(), dimension)) {
                break;
            }
        }
        // now dimensionFilter will either be the "perfect match" with the correct dimension,
        // or it's the last DimensionFilterSelectionProvider found, expected to have an empty selection and
        // will be used to set the dimension and filter; or it's null, meaning there was no dimension filter
        // which is considered an error
        if (dimensionFilter == null) {
            throw new IllegalStateException(
                    "Internal error: must have at least one de-selected dimension filter per retriever level");
        }
        if (dimensionFilter.getSelectedDimension() == null) {
            // dimension not yet filtered for; use the last empty dimension filter box, set dimension and define filter
            // values:
            dimensionFilter.setSelectedDimensionAndValues(dimension, values);
            final DimensionFilterSelectionProvider newDimensionFilter = createDimensionSelectionProvider();
            addDimensionSelectionProvider(newDimensionFilter);
            updateAvailableDimensions();
        } else {
            // was filtered for that dimension already; replace filter, restricting to values requested:
            dimensionFilter.setSelectedDimensionAndValues(dimension, values);
        }
    }

    public void applySelection(HashMap<FunctionDTO, HashSet<? extends Serializable>> filterSelection) {
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

    public DataRetrieverLevelDTO getRetrieverLevel() {
        return retrieverLevel;
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<AbstractSettings> getSettingsDialogComponent(AbstractSettings settings) {
        return null;
    }

    @Override
    public void updateSettings(AbstractSettings newSettings) {
        // no-op
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

    @Override
    public AbstractSettings getSettings() {
        return null;
    }

    @Override
    public String getId() {
        return "RetrieverLevelFilterSelectionProvider";
    }
}
