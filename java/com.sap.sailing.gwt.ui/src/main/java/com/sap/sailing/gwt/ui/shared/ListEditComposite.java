package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.DialogCallback;
import com.sap.sailing.gwt.ui.client.StringMessages;

public abstract class ListEditComposite<ValueType> extends Composite 
    implements HasValue<List<ValueType>>, HasValueChangeHandlers<List<ValueType>> {

    private static final int condensedTitleMaxLength = 20;
    
    protected final StringMessages stringMessages;
    private final boolean isInplace;
    private final ImageResource removeImage;
    private final String popupDialogTitle;
    
    private List<ValueType> values;
    
    private TextBox valuesBox;
    private Button editButton;
    
    private Grid valuesGrid;

    protected abstract String getCondensedValueText();

    protected abstract Widget createValueWidget(ValueType value);
    
    protected abstract Widget createAddWidget();
    
    protected abstract ListEditComposite<ValueType> createInPlaceComposite(List<ValueType> initialValues, 
            StringMessages stringMessages, ImageResource removeImage);
    
    public ListEditComposite(boolean isInplace, List<ValueType> initialValues, StringMessages stringMessages, ImageResource removeImage,
            String popupDialogTitle) {
        this.isInplace = isInplace;
        this.values = new ArrayList<ValueType>(initialValues);
        this.stringMessages = stringMessages;
        this.removeImage = removeImage;
        this.popupDialogTitle = popupDialogTitle;
        initWidget(isInplace ? createInplaceWidget() : createCondensedWidget());
        refreshUi();
    }
    
    /**
     * Creates an inplace {@link ListEditComposite}
     */
    public ListEditComposite(List<ValueType> initialValues, StringMessages stringMessages, ImageResource removeImage) {
        this(true, initialValues, stringMessages, removeImage, "");
    }

    private void refreshUi() {
        if (isInplace) {
            refreshInplaceUi();
        } else {
            refreshCondensedUi();
        }
    }

    private Widget createCondensedWidget() {
        HorizontalPanel panel = new HorizontalPanel();
        valuesBox = new TextBox();
        valuesBox.setReadOnly(true);
        
        editButton = new Button(stringMessages.edit());
        editButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new PopupEditDialog(getValue(), new DialogCallback<List<ValueType>>() {

                    @Override
                    public void ok(List<ValueType> editedObject) {
                        setValue(editedObject);
                    }

                    @Override
                    public void cancel() {
                        
                    }
                }).show();
            }
        });
        panel.add(valuesBox);
        panel.add(editButton);
        return panel;
    }

    private void refreshCondensedUi() {
        String text = getCondensedValueText();
        String shortText = text;
        if (shortText.length() > condensedTitleMaxLength) {
            shortText = shortText.substring(0, condensedTitleMaxLength) + "...";
        }
        valuesBox.setText(shortText);
        valuesBox.setTitle(text);
    }

    private Widget createInplaceWidget() {
        valuesGrid = new Grid(0, 2);
        
        VerticalPanel panel = new VerticalPanel();
        panel.add(createAddWidget());
        panel.add(valuesGrid);
        return panel;
    }

    protected void addValue(ValueType newValue) {
        values.add(newValue);
        addRow(newValue);
        onChange();
    }
    
    private void addRow(ValueType newValue) {
        int rowIndex = valuesGrid.insertRow(valuesGrid.getRowCount());
        
        PushButton removeButton = new PushButton(new Image(removeImage));
        removeButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                int rowToRemove = valuesGrid.getCellForEvent(event).getRowIndex();
                values.remove(rowToRemove);
                valuesGrid.removeRow(rowToRemove);
            }
        });
        
        valuesGrid.setWidget(rowIndex, 0, createValueWidget(newValue));
        valuesGrid.setWidget(rowIndex, 1, removeButton);
    }

    private void refreshInplaceUi() {
        valuesGrid.clear();
        for (ValueType value : getValue()) {
            addRow(value);
        }
    }


    @Override
    public List<ValueType> getValue() {
        return values;
    }

    @Override
    public void setValue(List<ValueType> newValues) {
        setValue(newValues, true);
    }

    @Override
    public void setValue(List<ValueType> newValues, boolean fireEvents) {
        this.values = newValues;
        refreshUi();
        if (fireEvents) {
            onChange();
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<ValueType>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }    
    
    public void onChange() {
        ValueChangeEvent.fire(this, getValue());
    }
    
    private class PopupEditDialog extends DataEntryDialog<List<ValueType>> {

        private ListEditComposite<ValueType> inPlaceComposite;
        
        public PopupEditDialog(List<ValueType> initialValues, DataEntryDialog.DialogCallback<List<ValueType>> callback) {
            super(popupDialogTitle, "", stringMessages.save(), stringMessages.cancel(), null, callback);
            inPlaceComposite = createInPlaceComposite(initialValues, stringMessages, removeImage);
        }
        
        @Override
        protected Widget getAdditionalWidget() {
            return inPlaceComposite;
        }

        @Override
        protected List<ValueType> getResult() {
            return inPlaceComposite.getValue();
        }
        
    }

}
