package com.sap.sailing.gwt.home.mobile.partials.placeholder;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class PlaceHolderMessage extends Composite {
    
    private final Label label;

    public PlaceHolderMessage() {
        label = new Label(StringMessages.INSTANCE.noResults());
        label.getElement().getStyle().setPadding(1, Unit.EM);
        label.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        label.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
        label.getElement().getStyle().setDisplay(Display.NONE);
        initWidget(label);
    }
    
    public PlaceHolderMessage(String message) {
        this();
        setMessage(message);
    }
    
    private void setMessage(String message) {
        if(message == null) {
            label.getElement().getStyle().setDisplay(Display.NONE);
        } else {
            label.getElement().getStyle().clearDisplay();
            label.setText(message);
        }
    }

}
