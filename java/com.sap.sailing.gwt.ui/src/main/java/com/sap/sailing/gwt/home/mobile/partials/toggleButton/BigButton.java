package com.sap.sailing.gwt.home.mobile.partials.toggleButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FocusWidget;

public class BigButton extends FocusWidget {

    private static StyledButtonUiBinder uiBinder = GWT.create(StyledButtonUiBinder.class);

    interface StyledButtonUiBinder extends UiBinder<Element, BigButton> {
    }
    
    @UiField DivElement buttonUi;

    public BigButton() {
        this(null);
    }
    
    public BigButton(String label) {
        BigButtonResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
        if(label != null) {
            setLabel(label);
        }
    }
    
    public void setLabel(String label) {
        buttonUi.setInnerText(label);
    }
}
