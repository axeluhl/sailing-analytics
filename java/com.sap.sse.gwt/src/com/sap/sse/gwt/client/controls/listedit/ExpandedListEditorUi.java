package com.sap.sse.gwt.client.controls.listedit;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.StringMessages;

/**
 * A base class for an editing UI strategy for a list of objects of type <code>ValueType</code> for use in a
 * {@link ListEditorComposite} instance. The widget that renders this list editing UI strategy has a section at the top
 * that allows the user to add a new list entry. The widget used for this must be provided by implementing subclasses
 * through the {@link #createAddWidget()} method. Next to this widget, an "Add" button is displayed.
 * <p>
 * 
 * Beneath this section for adding new values, a grid with the existing values is displayed. Each value is rendered by a
 * widget provided by implementing subclasses through the {@link #createValueWidget(int, Object)} method. If the
 * <code>canRemoveItems</code> constructor parameter was set to <code>true</code> then next to each of these widgets, a
 * "Remove" button is shown.
 * <p>
 * 
 * Implementing subclasses may choose to override the {@link #onRowAdded()} and/or the {@link #onRowRemoved(int)} method(s)
 * to be notified of changes to the list. Alternatively or in addition, clients can
 * {@link ListEditorComposite#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler) add a value
 * change handler} to the enclosing {@link ListEditorComposite}.
 * 
 * @author Lukas Niemeier
 * @author Axel Uhl (D043530)
 * 
 * @param <ValueType>
 */
public abstract class ExpandedListEditorUi<ValueType> extends ListEditorUi<ValueType> {
    private final ImageResource removeImage;

    protected Grid expandedValuesGrid;
    private final boolean canRemoveItems;

    /**
     * @param removeImage
     *            may be <code>null</code> if <code>canRemoveItems==false</code>; otherwise, must contain a valid image
     *            to be used by the "Remove" button
     * @param canRemoveItems
     *            if <code>true</code>, next to each widget rendering one value in the value list a "Remove" push button will
     *            be shown using the <code>removeImage</code>
     */
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
        expandedValuesGrid.ensureDebugId("ExpandedValuesGrid");
        
        VerticalPanel panel = new VerticalPanel();
        panel.add(createAddWidget());
        panel.add(expandedValuesGrid);
        return panel;
    }

    @Override
    public void refresh() {
        expandedValuesGrid.clear();
        for (int i=expandedValuesGrid.getRowCount()-1; i>=0; i--) {
            expandedValuesGrid.removeRow(i);
        }
        for (ValueType value : context.getValue()) {
            addRow(value);
        }
    }

    protected void addRow(ValueType newValue) {
        int rowIndex = expandedValuesGrid.insertRow(expandedValuesGrid.getRowCount());
        if (canRemoveItems) {
            PushButton removeButton = new PushButton(new Image(removeImage));
            removeButton.ensureDebugId("RemoveButton");
            removeButton.setTitle(getStringMessages().remove());
            removeButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    final Cell cellForEvent = expandedValuesGrid.getCellForEvent(event);
                    int rowToRemove = cellForEvent.getRowIndex();
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
        
        onRowRemoved(rowIndexToRemove);
    }
    
    /**
     * @param valueWidget a widget returned previously from {@link #createValueWidget(int, Object)}
     */
    protected void setValueFromValueWidget(Widget valueWidget, ValueType newValue, boolean fireEvents) {
        for (int i = 0; i < expandedValuesGrid.getRowCount(); i++) {
            Widget gridWidget = expandedValuesGrid.getWidget(i, 0);
            if (gridWidget.getElement() == valueWidget.getElement()) {
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
    public void onRowRemoved(int rowIndex) {
    }
}
