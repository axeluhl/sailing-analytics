package com.sap.sailing.gwt.home.mobile.partials.toggleButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

public class ToggleButton extends Widget {

    private static ToggleButtonUiBinder uiBinder = GWT.create(ToggleButtonUiBinder.class);

    interface ToggleButtonUiBinder extends UiBinder<Element, ToggleButton> {
    }
    
    @UiField DivElement toggleButtonUi;
    private final Command toggleCommand;

    public ToggleButton(Command toggleCommand) {
        this.toggleCommand = toggleCommand;
        ToggleButtonResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
        sinkEvents(Event.ONCLICK);
    }
    
    public void setButtonText(String text) {
        toggleButtonUi.setInnerText(text);
    }
    
    @Override
    public void onBrowserEvent(Event event) {
        if (event.getTypeInt() == Event.ONCLICK) {
            toggleCommand.execute();
            return;
        }
        super.onBrowserEvent(event);
    }
    
}
