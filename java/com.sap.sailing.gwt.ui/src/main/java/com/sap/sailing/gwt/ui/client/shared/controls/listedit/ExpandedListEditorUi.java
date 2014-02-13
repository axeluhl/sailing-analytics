package com.sap.sailing.gwt.ui.client.shared.controls.listedit;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;

public abstract class ExpandedListEditorUi<ValueType> extends ListEditorUi<ValueType> {
    private final ImageResource removeImage;

    private Grid expandedValuesGrid;
    private final boolean canRemoveItems;

    public ExpandedListEditorUi(StringMessages stringMessages, ImageResource removeImage, boolean canRemoveItems) {
        super(stringMessages);
        this.removeImage = removeImage;
        this.canRemoveItems = canRemoveItems;
    }

    protected abstract Widget createAddWidget();

    protected abstract Widget createValueWidget(int rowIndex, ValueType newValue);

    @Override
    public Widget initWidget() {
        expandedValuesGrid = new Grid(0, 2);

        VerticalPanel panel = new VerticalPanel();
        panel.add(createAddWidget());
        panel.add(expandedValuesGrid);
        return panel;
    }

    @Override
    public void refresh() {
        expandedValuesGrid.clear();
        for (ValueType value : context.getValue()) {
            addRow(value);
        }
    }

    @Override
    public boolean isCollapsed() {
        return false;
    }

    private void addRow(ValueType newValue) {
        int rowIndex = expandedValuesGrid.insertRow(expandedValuesGrid.getRowCount());

        if (canRemoveItems) {
            PushButton removeButton = new PushButton(new Image(removeImage));
            removeButton.setTitle(stringMessages.remove());
            removeButton.addClickHandler(new ClickHandler() {
    
                @Override
                public void onClick(ClickEvent event) {
                    int rowToRemove = expandedValuesGrid.getCellForEvent(event).getRowIndex();
                    removeRow(rowToRemove);
                }
            });
            expandedValuesGrid.setWidget(rowIndex, 1, removeButton);
        }

        expandedValuesGrid.setWidget(rowIndex, 0, createValueWidget(rowIndex, newValue));
    }

    private void removeRow(int rowIndexToRemove) {
        expandedValuesGrid.removeRow(rowIndexToRemove);
        context.getValue().remove(rowIndexToRemove);
        context.onChange();
        
        onRowRemoved();
    }
    
    protected void setValueFromValueWidget(ValueBoxBase<ValueType> valueWidget, ValueType newValue, boolean fireEvents) {
        for(int i = 0; i < expandedValuesGrid.getRowCount(); i++) {
            Widget gridWidget = expandedValuesGrid.getWidget(i, 0);
            if(gridWidget.getElement() == valueWidget.getElement()) {
                context.getValue().set(i, newValue);
                if (fireEvents) {
                    context.onChange();
                }
                break;
            }
        }
    }

    protected void addValue(ValueType newValue) {
        context.getValue().add(newValue);
        addRow(newValue);
        context.onChange();
        
        onRowAdded();
    }

    @Override
    public void onRowAdded() {
    }

    @Override
    public void onRowRemoved() {
    }
}
