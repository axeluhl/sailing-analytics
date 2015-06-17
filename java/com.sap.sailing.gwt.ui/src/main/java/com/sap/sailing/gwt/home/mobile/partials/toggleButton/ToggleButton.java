package com.sap.sailing.gwt.home.mobile.partials.toggleButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ToggleButton extends Composite {

    private static ToggleButtonUiBinder uiBinder = GWT.create(ToggleButtonUiBinder.class);

    interface ToggleButtonUiBinder extends UiBinder<Widget, ToggleButton> {
    }
    
    @UiField DivElement toggleButtonUi;

    public ToggleButton() {
        ToggleButtonResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

}
