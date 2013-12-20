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
import com.sap.sailing.datamining.shared.Components.GrouperType;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.SharedDimension;
import com.sap.sailing.datamining.shared.SharedDimension.OrdinalComparator;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.client.shared.components.SimpleValueListBox;
import com.sap.sailing.gwt.ui.datamining.GroupingChangedListener;
import com.sap.sailing.gwt.ui.datamining.GroupingProvider;

public class RestrictedGroupingProvider implements GroupingProvider {
    
    private static final OrdinalComparator dimensionComparator = new SharedDimension.OrdinalComparator();
    
    private final StringMessages stringMessages;
    private final Set<GroupingChangedListener> listeners;
    
    private final HorizontalPanel mainPanel;
    private final List<ValueListBox<SharedDimension>> dimensionToGroupByBoxes;

    public RestrictedGroupingProvider(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        listeners = new HashSet<GroupingChangedListener>();
        dimensionToGroupByBoxes = new ArrayList<ValueListBox<SharedDimension>>();
        
        mainPanel = new HorizontalPanel();
        mainPanel.setSpacing(5);
        mainPanel.add(new Label(this.stringMessages.groupBy() + ":"));
        mainPanel.add(createDimensionToGroupByBox());
    }

    private ValueListBox<SharedDimension> createDimensionToGroupByBox() {
        ValueListBox<SharedDimension> dimensionToGroupByBox = new SimpleValueListBox<SharedDimension>();
        dimensionToGroupByBoxes.add(dimensionToGroupByBox);
        dimensionToGroupByBox.addValueChangeHandler(new ValueChangeHandler<SharedDimension>() {
            private boolean firstChange = true;

            @Override
            public void onValueChange(ValueChangeEvent<SharedDimension> event) {
                if (firstChange && event.getValue() != null) {
                    ValueListBox<SharedDimension> newBox = createDimensionToGroupByBox();
                    mainPanel.add(newBox);
                    dimensionToGroupByBoxes.add(newBox);
                    firstChange = false;
                } else if (event.getValue() == null) {
                    mainPanel.remove((Widget) event.getSource());
                    dimensionToGroupByBoxes.remove(event.getSource());
                    updateAcceptableValues();
                }
                notifyListeners();
            }
        });
        updateAcceptableValues();
        return dimensionToGroupByBox;
    }

    private void updateAcceptableValues() {
        for (ValueListBox<SharedDimension> dimensionToGroupByBox : dimensionToGroupByBoxes) {
            List<SharedDimension> acceptableValues = new ArrayList<SharedDimension>(Arrays.asList(SharedDimension.values()));
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
    public Collection<SharedDimension> getDimensionsToGroupBy() {
        Collection<SharedDimension> dimensionsToGroupBy = new ArrayList<SharedDimension>();
        for (ValueListBox<SharedDimension> dimensionListBox : dimensionToGroupByBoxes) {
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
    public void applyQueryDefinition(QueryDefinition queryDefinition) {
        if (queryDefinition.getGrouperType() == GrouperType.Dimensions) {
            int index = 0;
            for (SharedDimension dimension : queryDefinition.getDimensionsToGroupBy()) {
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
