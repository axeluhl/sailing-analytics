package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sailing.datamining.shared.DimensionIdentifier.OrdinalComparator;
import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.client.shared.components.SimpleObjectRenderer;
import com.sap.sailing.gwt.ui.datamining.GroupingChangedListener;
import com.sap.sailing.gwt.ui.datamining.GroupingProvider;
import com.sap.sse.datamining.shared.components.GrouperType;

public class RestrictedGroupingProvider implements GroupingProvider {
    
    private static final OrdinalComparator dimensionComparator = new DimensionIdentifier.OrdinalComparator();
    
    private final StringMessages stringMessages;
    private final Set<GroupingChangedListener> listeners;
    
    private final HorizontalPanel mainPanel;
    private final List<ValueListBox<DimensionIdentifier>> dimensionToGroupByBoxes;

    public RestrictedGroupingProvider(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        listeners = new HashSet<GroupingChangedListener>();
        dimensionToGroupByBoxes = new ArrayList<ValueListBox<DimensionIdentifier>>();
        
        mainPanel = new HorizontalPanel();
        mainPanel.setSpacing(5);
        mainPanel.add(new Label(this.stringMessages.groupBy() + ":"));
        
        ValueListBox<DimensionIdentifier> firstDimensionToGroupByBox = createDimensionToGroupByBox();
        addDimensionToGroupByBoxAndUpdateAcceptableValues(firstDimensionToGroupByBox);
    }

    private ValueListBox<DimensionIdentifier> createDimensionToGroupByBox() {
        ValueListBox<DimensionIdentifier> dimensionToGroupByBox = new ValueListBox<DimensionIdentifier>(new SimpleObjectRenderer<DimensionIdentifier>());
        dimensionToGroupByBox.addValueChangeHandler(new ValueChangeHandler<DimensionIdentifier>() {
            private boolean firstChange = true;

            @Override
            public void onValueChange(ValueChangeEvent<DimensionIdentifier> event) {
                if (firstChange && event.getValue() != null) {
                    ValueListBox<DimensionIdentifier> newDimensionToGroupByBox = createDimensionToGroupByBox();
                    addDimensionToGroupByBox(newDimensionToGroupByBox);
                    firstChange = false;
                } else if (event.getValue() == null) {
                    Widget changedDimensionToGroupByBox = (Widget) event.getSource();
                    mainPanel.remove(changedDimensionToGroupByBox);
                    dimensionToGroupByBoxes.remove(changedDimensionToGroupByBox);
                }
                updateAcceptableValues();
                notifyListeners();
            }
        });
        return dimensionToGroupByBox;
    }

    private void addDimensionToGroupByBoxAndUpdateAcceptableValues(ValueListBox<DimensionIdentifier> dimensionToGroupByBox) {
        addDimensionToGroupByBox(dimensionToGroupByBox);
        updateAcceptableValues();
    }

    private void addDimensionToGroupByBox(ValueListBox<DimensionIdentifier> dimensionToGroupByBox) {
        mainPanel.add(dimensionToGroupByBox);
        dimensionToGroupByBoxes.add(dimensionToGroupByBox);
    }

    private void updateAcceptableValues() {
        for (ValueListBox<DimensionIdentifier> dimensionToGroupByBox : dimensionToGroupByBoxes) {
            List<DimensionIdentifier> acceptableValues = new ArrayList<DimensionIdentifier>(Arrays.asList(DimensionIdentifier.values()));
            acceptableValues.removeAll(getDimensionsToGroupBy());
            if (dimensionToGroupByBox.getValue() != null) {
                acceptableValues.add(dimensionToGroupByBox.getValue());
            }
            Collections.sort(acceptableValues, dimensionComparator);
            acceptableValues.add(null);
            dimensionToGroupByBox.setAcceptableValues(acceptableValues);
        }
    }

    @Override
    public GrouperType getGrouperType() {
        return GrouperType.Dimensions;
    }

    @Override
    public Collection<DimensionIdentifier> getDimensionsToGroupBy() {
        Collection<DimensionIdentifier> dimensionsToGroupBy = new ArrayList<DimensionIdentifier>();
        for (ValueListBox<DimensionIdentifier> dimensionListBox : dimensionToGroupByBoxes) {
            if (dimensionListBox.getValue() != null) {
                dimensionsToGroupBy.add(dimensionListBox.getValue());
            }
        }
        return dimensionsToGroupBy;
    }

    @Override
    public String getCustomGrouperScriptText() {
        return "";
    }

    @Override
    public void applyQueryDefinition(QueryDefinitionDeprecated queryDefinition) {
        if (queryDefinition.getGrouperType() == GrouperType.Dimensions) {
            int index = 0;
            for (DimensionIdentifier dimension : queryDefinition.getDimensionsToGroupBy()) {
                dimensionToGroupByBoxes.get(index).setValue(dimension, true);
                index++;
            }
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
        return stringMessages.groupingProvider();
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
    public SettingsDialogComponent<Object> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(Object newSettings) { }

}
