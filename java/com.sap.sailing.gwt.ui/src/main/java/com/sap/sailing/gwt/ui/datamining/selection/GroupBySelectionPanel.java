package com.sap.sailing.gwt.ui.datamining.selection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.SharedDimension;
import com.sap.sailing.datamining.shared.Components.GrouperType;
import com.sap.sailing.gwt.ui.client.StringMessages;

public abstract class GroupBySelectionPanel extends FlowPanel {

    private StringMessages stringMessages;

    private ValueListBox<GrouperType> grouperTypeListBox;
    private DeckPanel groupByOptionsPanel;
    private TextArea customGrouperScriptTextBox;
    private HorizontalPanel dimensionsToGroupByPanel;
    private List<ValueListBox<SharedDimension>> dimensionsToGroupByBoxes;

    public GroupBySelectionPanel(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        dimensionsToGroupByBoxes = new ArrayList<ValueListBox<SharedDimension>>();

        add(createGrouperTypeSelectionPanel());

        groupByOptionsPanel = new DeckPanel();
        add(groupByOptionsPanel);

        dimensionsToGroupByPanel = new HorizontalPanel();
        dimensionsToGroupByPanel.setSpacing(5);
        groupByOptionsPanel.add(dimensionsToGroupByPanel);
        
        ValueListBox<SharedDimension> dimensionToGroupByBox = createDimensionToGroupByBox();
        dimensionsToGroupByPanel.add(dimensionToGroupByBox);
        dimensionsToGroupByBoxes.add(dimensionToGroupByBox);

        FlowPanel dynamicGroupByPanel = new FlowPanel();
        groupByOptionsPanel.add(dynamicGroupByPanel);
        dynamicGroupByPanel.add(new Label("public Object getValueToGroupByFrom(GPSFix data) {"));
        customGrouperScriptTextBox = new TextArea();
        customGrouperScriptTextBox.setCharacterWidth(100);
        customGrouperScriptTextBox.setVisibleLines(1);
        dynamicGroupByPanel.add(customGrouperScriptTextBox);
        dynamicGroupByPanel.add(new Label("}"));

        groupByOptionsPanel.showWidget(0);
    }

    public void apply(QueryDefinition queryDefinition) {
        grouperTypeListBox.setValue(queryDefinition.getGrouperType(), true);
        
        switch (queryDefinition.getGrouperType()) {
        case Custom:
            customGrouperScriptTextBox.setText(queryDefinition.getCustomGrouperScriptText());
            break;
        case Dimensions:
            applyDimensionsToGroupBy(queryDefinition);
            break;
        default:
            throw new IllegalArgumentException("Not yet implemented for the given data type: " + queryDefinition.getGrouperType().toString());
        }
    }

    private void applyDimensionsToGroupBy(QueryDefinition queryDefinition) {
        int index = 0;
        for (SharedDimension dimension : queryDefinition.getDimensionsToGroupBy()) {
            dimensionsToGroupByBoxes.get(index).setValue(dimension, true);
            index++;
        }
    }

    public GrouperType getGrouperType() {
        return grouperTypeListBox.getValue();
    }

    public String getCustomGrouperScriptText() {
        return getGrouperType() == GrouperType.Custom ? customGrouperScriptTextBox.getText() : "";
    }

    public Collection<SharedDimension> getDimensionsToGroupBy() {
        Collection<SharedDimension> dimensionsToGroupBy = new ArrayList<SharedDimension>();
        if (getGrouperType() == GrouperType.Dimensions) {
            for (ValueListBox<SharedDimension> dimensionToGroupByBox : dimensionsToGroupByBoxes) {
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
        grouperTypeListBox.setAcceptableValues(Arrays.asList(GrouperType.values()));
        selectGroupByPanel.add(grouperTypeListBox);
        
        grouperTypeListBox.addValueChangeHandler(new ValueChangeHandler<GrouperType>() {
            @Override
            public void onValueChange(ValueChangeEvent<GrouperType> event) {
                if (event.getValue() != null) {
                    switch (event.getValue()) {
                    case Custom:
                        groupByOptionsPanel.showWidget(1);
                        break;
                    case Dimensions:
                        groupByOptionsPanel.showWidget(0);
                        break;
                    }
                }
                finishValueChangedHandling();
            }
        });
        
        return selectGroupByPanel;
    }

    private ValueListBox<SharedDimension> createDimensionToGroupByBox() {
        ValueListBox<SharedDimension> dimensionToGroupByBox = new ValueListBox<SharedDimension>(
                new Renderer<SharedDimension>() {
                    @Override
                    public String render(SharedDimension gpsFixDimension) {
                        if (gpsFixDimension == null) {
                            return "";
                        }
                        return gpsFixDimension.toString();
                    }

                    @Override
                    public void render(SharedDimension gpsFixDimension, Appendable appendable)
                            throws IOException {
                        appendable.append(render(gpsFixDimension));

                    }
                });
        dimensionToGroupByBox.addValueChangeHandler(new ValueChangeHandler<SharedDimension>() {
            private boolean firstChange = true;

            @Override
            public void onValueChange(ValueChangeEvent<SharedDimension> event) {
                if (firstChange && event.getValue() != null) {
                    ValueListBox<SharedDimension> newBox = createDimensionToGroupByBox();
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
        dimensionToGroupByBox.setAcceptableValues(Arrays.asList(SharedDimension.values()));
        return dimensionToGroupByBox;
    }
    
    protected abstract void finishValueChangedHandling();
    
}
