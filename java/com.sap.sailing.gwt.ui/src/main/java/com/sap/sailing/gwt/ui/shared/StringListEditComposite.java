package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class StringListEditComposite extends ListEditComposite<String> {

    public StringListEditComposite(boolean isInplace, List<String> initialValues, StringMessages stringMessages, 
            ImageResource removeImage, String popupDialogTitle) {
        super(isInplace, initialValues, stringMessages, removeImage, popupDialogTitle);
    }

    public StringListEditComposite(List<String> initialValues, StringMessages stringMessages, ImageResource removeImage) {
        super(initialValues, stringMessages, removeImage);
    }

    @Override
    protected ListEditComposite<String> createInPlaceComposite(List<String> initialValues,
            StringMessages stringMessages, ImageResource removeImage) {
        return new StringListEditComposite(initialValues, stringMessages, removeImage);
    }
    
    @Override
    protected String getCondensedValueText() {
        StringBuilder valuesText = new StringBuilder();
        for (int i = 0; i < getValue().size(); i++) {
            if (i > 0) {
                valuesText.append(',');
            }
            valuesText.append(getValue().get(i));
        }
        String condensedValue = valuesText.toString();
        return condensedValue;
    }
    
    @Override
    protected Widget createAddWidget() {
        final TextBox valueBox = new TextBox();
        final Button addButton = new Button(stringMessages.add());
        addButton.setEnabled(false);
        addButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                addValue(valueBox.getValue());
                valueBox.setText("");
            }
        });
        valueBox.addKeyUpHandler(new KeyUpHandler() {
            
            @Override
            public void onKeyUp(KeyUpEvent event) {
                addButton.setEnabled(!valueBox.getValue().isEmpty());
            }
        });
        
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(valueBox);
        panel.add(addButton);
        return panel;
    }

    @Override
    protected Widget createValueWidget(String value) {
        return new Label(value);
    }
    
}
