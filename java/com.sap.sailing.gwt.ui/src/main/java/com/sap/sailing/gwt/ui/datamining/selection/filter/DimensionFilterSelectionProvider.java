package com.sap.sailing.gwt.ui.datamining.selection.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.datamining.DataMiningResources;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionProvider;
import com.sap.sailing.gwt.ui.datamining.FilterSelectionChangedListener;
import com.sap.sailing.gwt.ui.datamining.FilterSelectionProvider;
import com.sap.sailing.gwt.ui.datamining.ManagedDataMiningQueriesCounter;
import com.sap.sailing.gwt.ui.datamining.execution.ManagedDataMiningQueryCallback;
import com.sap.sailing.gwt.ui.datamining.execution.SimpleManagedDataMiningQueriesCounter;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.shared.components.AbstractComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class DimensionFilterSelectionProvider extends AbstractComponent<SerializableSettings> {

    private static final DataMiningResources resources = GWT.create(DataMiningResources.class);
    private static final NaturalComparator NaturalComparator = new NaturalComparator();
    
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final DataMiningSession session;
    private final ManagedDataMiningQueriesCounter counter;
    private final Set<FilterSelectionChangedListener> listeners;

    private final DataRetrieverChainDefinitionProvider retrieverChainProvider;
    private final FilterSelectionProvider filterSelectionProvider;
    
    private final DataRetrieverLevelDTO retrieverLevel;
    private final FunctionDTO dimension;
    
    private final FlowPanel mainPanel;
    private final SimpleBusyIndicator busyIndicator;
    private final FilterableSelectionTable<?> selectionTable;

    public DimensionFilterSelectionProvider(Component<?> parent, ComponentContext<?> componentContext, DataMiningServiceAsync dataMiningService,
            ErrorReporter errorReporter, DataMiningSession session, DataRetrieverChainDefinitionProvider retrieverChainProvider,
            FilterSelectionProvider filterSelectionProvider, DataRetrieverLevelDTO retrieverLevel, FunctionDTO dimension) {
        super(parent, componentContext);
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        this.session = session;
        this.retrieverChainProvider = retrieverChainProvider;
        this.filterSelectionProvider = filterSelectionProvider;
        this.retrieverLevel = retrieverLevel;
        this.dimension = dimension;

        counter = new SimpleManagedDataMiningQueriesCounter();
        listeners = new HashSet<>();
        
        busyIndicator = new SimpleBusyIndicator(true, 0.7f);
        busyIndicator.setVisible(false);
        
        selectionTable = new FilterableSelectionTable<>();
        selectionTable.addSelectionChangeHandler(this::notifyListeners);
        selectionTable.setVisible(false);
        
        ToggleButton toggleFilterButton = new ToggleButton(new Image(resources.searchIcon()));
        toggleFilterButton.addClickHandler(e -> selectionTable.setFilteringEnabled(toggleFilterButton.isDown()));
        
        HorizontalPanel headerPanel = new HorizontalPanel();
        headerPanel.setSpacing(2);
        headerPanel.add(new Label(dimension.getDisplayName()));
        headerPanel.add(toggleFilterButton);
        
        mainPanel = new FlowPanel();
        mainPanel.add(headerPanel);
        mainPanel.add(busyIndicator);
        mainPanel.add(selectionTable.getWidget());
        
        updateContent();
    }

    private void updateContent() {
        HashMap<DataRetrieverLevelDTO,SerializableSettings> retrieverSettings = retrieverChainProvider.getRetrieverSettings();
        HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> filterSelection = filterSelectionProvider.getSelection();
        if (filterSelection.containsKey(retrieverLevel)) {
            filterSelection.get(retrieverLevel).remove(dimension);
        }
        HashSet<FunctionDTO> dimensions = new HashSet<>();
        dimensions.add(dimension);
        
        counter.increase();
        busyIndicator.setVisible(true);
        dataMiningService.getDimensionValuesFor(session, retrieverChainProvider.getDataRetrieverChainDefinition(), retrieverLevel, dimensions,
                retrieverSettings, filterSelection, LocaleInfo.getCurrentLocale().getLocaleName(), new ManagedDataMiningQueryCallback<HashSet<Object>>(counter) {
                    @Override
                    protected void handleSuccess(QueryResultDTO<HashSet<Object>> result) {
                        Map<GroupKey, HashSet<Object>> results = result.getResults();
                        List<Object> content = new ArrayList<>();
                        
                        if (!results.isEmpty()) {
                            GroupKey contentKey = new GenericGroupKey<FunctionDTO>(dimension);
                            content.addAll(results.get(contentKey));
                            content.sort((o1, o2) -> NaturalComparator.compare(o1.toString(), o2.toString()));
                        }
                        
                        busyIndicator.setVisible(false);
                        selectionTable.setContent(content, true);
                        selectionTable.setVisible(true);
                    }
                    @Override
                    protected void handleFailure(Throwable caught) {
                        errorReporter.reportError("Error fetching the dimension values of " + dimension + ": " + caught.getMessage());
                    }
                });
    }

    public HashSet<? extends Serializable> getSelection() {
        return selectionTable.getSelection();
    }

    public void clearSelection() {
        selectionTable.clearSelection();
    }

    public void addListener(FilterSelectionChangedListener listener) {
        listeners.add(listener);
    }
    
    private void notifyListeners() {
        for (FilterSelectionChangedListener listener : listeners) {
            listener.selectionChanged();
        }
    }

    @Override
    public Widget getEntryWidget() {
        return mainPanel;
    }

    @Override
    public String getId() {
        return "DimensionFilterSelectionProvider";
    }

    @Override
    public String getLocalizedShortName() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean isVisible() {
        return getEntryWidget().isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        getEntryWidget().setVisible(visibility);
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<SerializableSettings> getSettingsDialogComponent(SerializableSettings settings) {
        return null;
    }

    @Override
    public SerializableSettings getSettings() {
        return null;
    }

    @Override
    public void updateSettings(SerializableSettings newSettings) {
        // no-op
    }

    @Override
    public String getDependentCssClassName() {
        return "dimensionFilterSelectionProvider";
    }

}
