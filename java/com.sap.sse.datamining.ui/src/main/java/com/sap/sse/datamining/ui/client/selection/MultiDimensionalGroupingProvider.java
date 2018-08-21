package com.sap.sse.datamining.ui.client.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.ui.client.AbstractDataMiningComponent;
import com.sap.sse.datamining.ui.client.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.client.DataRetrieverChainDefinitionProvider;
import com.sap.sse.datamining.ui.client.GroupingChangedListener;
import com.sap.sse.datamining.ui.client.GroupingProvider;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.controls.AbstractObjectRenderer;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class MultiDimensionalGroupingProvider extends AbstractDataMiningComponent<SerializableSettings>
        implements GroupingProvider {

    private static final String GroupingProviderStyle = "groupingProvider";
    private static final String GroupingProviderLabelStyle = "groupingProviderLabel";
    private static final String GroupingProviderElementStyle = "groupingProviderElement";
    
    private static final NaturalComparator NaturalComparator = new NaturalComparator();
    private static final Comparator<FunctionDTO> DimensionComparator = (d1, d2) -> {
        // Null values (to deselect the dimension) on top
        if (d1 == null || d2 == null) {
            if (d1 == d2) return 0;
            if (d1 == null) return -1;
            if (d2 == null) return 1;
        }
        return NaturalComparator.compare(d1.getDisplayName(), d2.getDisplayName());
    };

    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<GroupingChangedListener> listeners;

    private final FlowPanel mainPanel;
    private final List<ValueListBox<FunctionDTO>> dimensionToGroupByBoxes;

    private boolean isAwaitingReload;
    private boolean isUpdating;
    private DataRetrieverChainDefinitionDTO currentRetrieverChainDefinition;
    private final List<FunctionDTO> availableDimensions;
    private Iterable<FunctionDTO> dimensionsToSelect;
    private Consumer<Iterable<String>> selectionCallback;

    public MultiDimensionalGroupingProvider(Component<?> parent, ComponentContext<?> context,
            DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
            DataRetrieverChainDefinitionProvider retrieverChainProvider) {
        super(parent, context);
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        listeners = new HashSet<GroupingChangedListener>();
        currentRetrieverChainDefinition = null;
        availableDimensions = new ArrayList<>();
        isAwaitingReload = false;
        dimensionToGroupByBoxes = new ArrayList<ValueListBox<FunctionDTO>>();

        mainPanel = new FlowPanel();
        mainPanel.addStyleName(GroupingProviderStyle);
        
        Label groupByLabel = new Label(this.getDataMiningStringMessages().groupBy());
        groupByLabel.addStyleName(GroupingProviderLabelStyle);
        groupByLabel.addStyleName("emphasizedLabel");
        mainPanel.add(groupByLabel);

        ValueListBox<FunctionDTO> firstDimensionToGroupByBox = createDimensionToGroupByBox();
        addDimensionToGroupByBoxAndUpdateAcceptableValues(firstDimensionToGroupByBox);
        retrieverChainProvider.addDataRetrieverChainDefinitionChangedListener(this);
    }

    @Override
    public void awaitReloadComponents() {
        isAwaitingReload = true;
    }

    @Override
    public boolean isAwaitingReload() {
        return isAwaitingReload;
    }

    @Override
    public void reloadComponents() {
        isAwaitingReload = false;
        updateAvailableDimensions();
    }

    @Override
    public void dataRetrieverChainDefinitionChanged(DataRetrieverChainDefinitionDTO newRetrieverChainDefinition) {
        if (!Objects.equals(currentRetrieverChainDefinition, newRetrieverChainDefinition)) {
            currentRetrieverChainDefinition = newRetrieverChainDefinition;
            if (!isAwaitingReload) {
                updateAvailableDimensions();
            }
        }
    }

    private void updateAvailableDimensions() {
        if (currentRetrieverChainDefinition != null) {
            isUpdating = true;
            dataMiningService.getDimensionsFor(currentRetrieverChainDefinition,
                    LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<HashSet<FunctionDTO>>() {
                        @Override
                        public void onSuccess(HashSet<FunctionDTO> dimensions) {
                            clear();
                            for (FunctionDTO dimension : dimensions) {
                                availableDimensions.add(dimension);
                            }
                            ValueListBox<FunctionDTO> firstDimensionToGroupByBox = createDimensionToGroupByBox();
                            addDimensionToGroupByBoxAndUpdateAcceptableValues(firstDimensionToGroupByBox);
                            if (!availableDimensions.isEmpty()) {
                                Collections.sort(availableDimensions, DimensionComparator);
                                if (dimensionsToSelect != null) {
                                    setSelectedDimensions(dimensionsToSelect, selectionCallback);
                                } else {
                                    Optional<FunctionDTO> dimensionWithLowestOrdinal = availableDimensions.stream().min((d1, d2) -> {
                                        int ordinal1 = d1 == null ? Integer.MAX_VALUE : d1.getOrdinal();
                                        int ordinal2 = d2 == null ? Integer.MAX_VALUE : d2.getOrdinal();
                                        return Integer.compare(ordinal1, ordinal2);
                                    });
                                    dimensionWithLowestOrdinal.ifPresent(d -> firstDimensionToGroupByBox.setValue(d, true));
                                }
                                dimensionsToSelect = null;
                                selectionCallback = null;
                            } else {
                                notifyListeners();
                            }
                            isUpdating = false;
                        }
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError("Error fetching the dimensions from the server: " + caught.getMessage());
                            dimensionsToSelect = null;
                            selectionCallback = null;
                            isUpdating = false;
                        }
                    });
        } else {
            clear();
            ValueListBox<FunctionDTO> firstDimensionToGroupByBox = createDimensionToGroupByBox();
            addDimensionToGroupByBoxAndUpdateAcceptableValues(firstDimensionToGroupByBox);
            dimensionsToSelect = null;
            selectionCallback = null;
        }
    }

    private void clear() {
        clearDimensionBoxes();
        availableDimensions.clear();
    }

    private void clearDimensionBoxes() {
        for (ValueListBox<FunctionDTO> dimensionBox : dimensionToGroupByBoxes) {
            dimensionBox.removeFromParent();
        }
        dimensionToGroupByBoxes.clear();
    }

    @Override
    public Iterable<FunctionDTO> getAvailableDimensions() {
        return Collections.unmodifiableList(availableDimensions);
    }

    @Override
    public void setDimensionToGroupBy(int i, FunctionDTO dimensionToGroupBy) {
        dimensionToGroupByBoxes.get(i).setValue(dimensionToGroupBy, /* fireEvents */ true);
    }

    private ValueListBox<FunctionDTO> createDimensionToGroupByBox() {
        ValueListBox<FunctionDTO> dimensionToGroupByBox = createDimensionToGroupByBoxWithoutEventHandler();
        dimensionToGroupByBox.addStyleName("dataMiningListBox");
        dimensionToGroupByBox.addValueChangeHandler(new ValueChangeHandler<FunctionDTO>() {
            private boolean firstChange = true;

            @Override
            public void onValueChange(ValueChangeEvent<FunctionDTO> event) {
                if (firstChange && event.getValue() != null) {
                    ValueListBox<FunctionDTO> newDimensionToGroupByBox = createDimensionToGroupByBox();
                    addDimensionToGroupByBox(newDimensionToGroupByBox);
                    firstChange = false;
                } else if (event.getValue() == null) {
                    mainPanel.remove(dimensionToGroupByBox);
                    dimensionToGroupByBoxes.remove(dimensionToGroupByBox);
                }
                updateAcceptableValues();
                notifyListeners();
            }
        });
        return dimensionToGroupByBox;
    }

    @Override
    public ValueListBox<FunctionDTO> createDimensionToGroupByBoxWithoutEventHandler() {
        ValueListBox<FunctionDTO> dimensionToGroupByBox = new ValueListBox<FunctionDTO>(
                new AbstractObjectRenderer<FunctionDTO>() {
                    @Override
                    protected String convertObjectToString(FunctionDTO function) {
                        return function.getDisplayName();
                    }

                });
        dimensionToGroupByBox.addStyleName(GroupingProviderElementStyle);
        return dimensionToGroupByBox;
    }

    private void addDimensionToGroupByBoxAndUpdateAcceptableValues(ValueListBox<FunctionDTO> dimensionToGroupByBox) {
        addDimensionToGroupByBox(dimensionToGroupByBox);
        updateAcceptableValues();
    }

    private void addDimensionToGroupByBox(ValueListBox<FunctionDTO> dimensionToGroupByBox) {
        mainPanel.add(dimensionToGroupByBox);
        dimensionToGroupByBoxes.add(dimensionToGroupByBox);
    }

    private void updateAcceptableValues() {
        for (ValueListBox<FunctionDTO> dimensionToGroupByBox : dimensionToGroupByBoxes) {
            List<FunctionDTO> acceptableValues = new ArrayList<FunctionDTO>(availableDimensions);
            acceptableValues.removeAll(getDimensionsToGroupBy());
            if (dimensionToGroupByBox.getValue() != null) {
                acceptableValues.add(dimensionToGroupByBox.getValue());
            }
            if (!acceptableValues.isEmpty()) {
                acceptableValues.add(null);
                Collections.sort(acceptableValues, DimensionComparator);
            } else {
                dimensionToGroupByBox.setEnabled(false);
            }
            dimensionToGroupByBox.setAcceptableValues(acceptableValues);
        }
    }

    @Override
    public Collection<FunctionDTO> getDimensionsToGroupBy() {
        Collection<FunctionDTO> dimensionsToGroupBy = new ArrayList<>();
        for (ValueListBox<FunctionDTO> dimensionListBox : dimensionToGroupByBoxes) {
            if (dimensionListBox.getValue() != null) {
                dimensionsToGroupBy.add(dimensionListBox.getValue());
            }
        }
        return dimensionsToGroupBy;
    }

    @Override
    public void removeDimensionToGroupBy(FunctionDTO dimension) {
        for (final Iterator<ValueListBox<FunctionDTO>> i = dimensionToGroupByBoxes.iterator(); i.hasNext();) {
            final ValueListBox<FunctionDTO> dimensionListBox = i.next();
            if (Util.equalsWithNull(dimension, dimensionListBox.getValue())) {
                i.remove();
                dimensionListBox.removeFromParent();
                updateAcceptableValues();
                notifyListeners();
            }
        }
    }

    @Override
    public void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition, Consumer<Iterable<String>> callback) {
        DataRetrieverChainDefinitionDTO newRetrieverChain = queryDefinition.getDataRetrieverChainDefinition();
        dimensionsToSelect = queryDefinition.getDimensionsToGroupBy();
        selectionCallback = callback;
        if (!isAwaitingReload && !isUpdating && currentRetrieverChainDefinition.equals(newRetrieverChain)) {
            clearDimensionBoxes();
            ValueListBox<FunctionDTO> firstDimensionToGroupByBox = createDimensionToGroupByBox();
            addDimensionToGroupByBoxAndUpdateAcceptableValues(firstDimensionToGroupByBox);
            
            setSelectedDimensions(dimensionsToSelect, selectionCallback);
            dimensionsToSelect = null;
            selectionCallback = null;
        }
    }

    private void setSelectedDimensions(Iterable<FunctionDTO> dimensions, Consumer<Iterable<String>> callback) {
        Collection<FunctionDTO> missingDimensions = new ArrayList<>();
        int boxIndex = 0;
        for (FunctionDTO dimension : dimensions) {
            int index = availableDimensions.indexOf(dimension);
            if (index != -1) {
                setDimensionToGroupBy(boxIndex, availableDimensions.get(index));
                boxIndex++;
            } else {
                missingDimensions.add(dimension);
            }
        }
        
        if (!missingDimensions.isEmpty()) {
            String listedDimensions = missingDimensions.stream().map(FunctionDTO::getDisplayName)
                                                                .collect(Collectors.joining(", "));
            callback.accept(Collections
                    .singleton(getDataMiningStringMessages().groupingDimensionsAreNotAvailable(listedDimensions)));
        } else {
            callback.accept(Collections.emptySet());
        }
    }

    @Override
    public void addGroupingChangedListener(GroupingChangedListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (GroupingChangedListener listener : listeners) {
            listener.groupingChanged();
        }
    }

    @Override
    public String getLocalizedShortName() {
        return getDataMiningStringMessages().groupingProvider();
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
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<SerializableSettings> getSettingsDialogComponent(SerializableSettings settings) {
        return null;
    }

    @Override
    public void updateSettings(SerializableSettings newSettings) {
        // no-op
    }

    @Override
    public String getDependentCssClassName() {
        return "multiDimensionalGroupingProvider";
    }

    @Override
    public SerializableSettings getSettings() {
        return null;
    }

    @Override
    public String getId() {
        return "MultiDimensionalGroupingProvider";
    }
}
