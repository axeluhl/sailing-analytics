package com.sap.sailing.gwt.ui.datamining.client.selection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.datamining.shared.components.GrouperType;

public abstract class GroupBySelectionPanel extends FlowPanel {

    private StringMessages stringMessages;

    private ValueListBox<GrouperType> grouperTypeListBox;
    private HorizontalPanel dimensionsToGroupByPanel;
    private List<ValueListBox<DimensionIdentifier>> dimensionsToGroupByBoxes;

    public GroupBySelectionPanel(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        dimensionsToGroupByBoxes = new ArrayList<ValueListBox<DimensionIdentifier>>();

        add(createGrouperTypeSelectionPanel());

        dimensionsToGroupByPanel = new HorizontalPanel();
        dimensionsToGroupByPanel.setSpacing(5);
        add(dimensionsToGroupByPanel);
        
        ValueListBox<DimensionIdentifier> dimensionToGroupByBox = createDimensionToGroupByBox();
        dimensionsToGroupByPanel.add(dimensionToGroupByBox);
        dimensionsToGroupByBoxes.add(dimensionToGroupByBox);
    }

    public void apply(QueryDefinition queryDefinition) {
        grouperTypeListBox.setValue(queryDefinition.getGrouperType(), true);
        
        switch (queryDefinition.getGrouperType()) {
        case Dimensions:
            applyDimensionsToGroupBy(queryDefinition);
            break;
        default:
            throw new IllegalArgumentException("Not yet implemented for the given data type: " + queryDefinition.getGrouperType().toString());
        }
    }

    private void applyDimensionsToGroupBy(QueryDefinition queryDefinition) {
        int index = 0;
        for (DimensionIdentifier dimension : queryDefinition.getDimensionsToGroupBy()) {
            dimensionsToGroupByBoxes.get(index).setValue(dimension, true);
            index++;
        }
    }

    public GrouperType getGrouperType() {
        return grouperTypeListBox.getValue();
    }

    public Collection<DimensionIdentifier> getDimensionsToGroupBy() {
        Collection<DimensionIdentifier> dimensionsToGroupBy = new ArrayList<DimensionIdentifier>();
        if (getGrouperType() == GrouperType.Dimensions) {
            for (ValueListBox<DimensionIdentifier> dimensionToGroupByBox : dimensionsToGroupByBoxes) {
                if (dimensionToGroupByBox.getValue() != null) {
                    dimensionsToGroupBy.add(dimensionToGroupByBox.getValue());
                }
            }
        }
        return dimensionsToGroupBy;
    }

    private HorizontalPanel createGrouperTypeSelectionPanel() {
        HorizontalPanel selectGroupByPanel = new HorizontalPanel();
        selectGroupByPanel.setSpacing(5);
        
        selectGroupByPanel.add(new Label(this.stringMessages.groupBy() + ": "));
        grouperTypeListBox = new ValueListBox<GrouperType>(new Renderer<GrouperType>() {
            @Override
            public String render(GrouperType grouperType) {
                if (grouperType == null) {
                    return "";
                }
                return grouperType.toString();
            }

            @Override
            public void render(GrouperType grouperType, Appendable appendable) throws IOException {
                appendable.append(render(grouperType));
            }
        });
        grouperTypeListBox.setValue(GrouperType.Dimensions, false);
        selectGroupByPanel.add(grouperTypeListBox);
        
        grouperTypeListBox.addValueChangeHandler(new ValueChangeHandler<GrouperType>() {
            @Override
            public void onValueChange(ValueChangeEvent<GrouperType> event) {
                finishValueChangedHandling();
            }
        });
        
        return selectGroupByPanel;
    }

    private ValueListBox<DimensionIdentifier> createDimensionToGroupByBox() {
        ValueListBox<DimensionIdentifier> dimensionToGroupByBox = new ValueListBox<DimensionIdentifier>(
                new Renderer<DimensionIdentifier>() {
                    @Override
                    public String render(DimensionIdentifier gpsFixDimension) {
                        if (gpsFixDimension == null) {
                            return "";
                        }
                        return gpsFixDimension.toString();
                    }

                    @Override
                    public void render(DimensionIdentifier gpsFixDimension, Appendable appendable)
                            throws IOException {
                        appendable.append(render(gpsFixDimension));

                    }
                });
        dimensionToGroupByBox.addValueChangeHandler(new ValueChangeHandler<DimensionIdentifier>() {
            private boolean firstChange = true;

            @Override
            public void onValueChange(ValueChangeEvent<DimensionIdentifier> event) {
                if (firstChange && event.getValue() != null) {
                    ValueListBox<DimensionIdentifier> newBox = createDimensionToGroupByBox();
                    dimensionsToGroupByPanel.add(newBox);
                    dimensionsToGroupByBoxes.add(newBox);
                    firstChange = false;
                } else if (event.getValue() == null) {
                    dimensionsToGroupByPanel.remove((Widget) event.getSource());
                    dimensionsToGroupByBoxes.remove(event.getSource());
                }
                finishValueChangedHandling();
            }
        });
        dimensionToGroupByBox.setAcceptableValues(Arrays.asList(DimensionIdentifier.values()));
        return dimensionToGroupByBox;
    }
    
    protected abstract void finishValueChangedHandling();
    
}
