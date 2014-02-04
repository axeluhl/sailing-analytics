package com.sap.sailing.gwt.ui.client.shared.controls.listedit;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;

public abstract class ExpandedListEditorUi<ValueType> extends ListEditorUi<ValueType> {
    private final ImageResource removeImage;

    private Grid expandedValuesGrid;

    public ExpandedListEditorUi(StringMessages stringMessages, ImageResource removeImage) {
        super(stringMessages);
        this.removeImage = removeImage;
    }

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

        PushButton removeButton = new PushButton(new Image(removeImage));
        removeButton.setTitle(stringMessages.remove());
        removeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                int rowToRemove = expandedValuesGrid.getCellForEvent(event).getRowIndex();
                expandedValuesGrid.removeRow(rowToRemove);
                context.getValue().remove(rowToRemove);
                context.onChange();
            }
        });

        expandedValuesGrid.setWidget(rowIndex, 0, createValueWidget(newValue));
        expandedValuesGrid.setWidget(rowIndex, 1, removeButton);
    }

    protected void addValue(ValueType newValue) {
        context.getValue().add(newValue);
        addRow(newValue);
        context.onChange();
    }

    protected abstract Widget createAddWidget();

    protected abstract Widget createValueWidget(ValueType newValue);
}
