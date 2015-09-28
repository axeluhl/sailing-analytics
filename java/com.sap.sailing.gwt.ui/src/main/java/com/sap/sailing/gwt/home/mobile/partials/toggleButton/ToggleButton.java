package com.sap.sailing.gwt.home.mobile.partials.toggleButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class ToggleButton extends Widget {

    private static final StringMessages I18N = StringMessages.INSTANCE;
    private static ToggleButtonUiBinder uiBinder = GWT.create(ToggleButtonUiBinder.class);

    interface ToggleButtonUiBinder extends UiBinder<Element, ToggleButton> {
    }
    
    @UiField DivElement toggleButtonUi;
    private final ToggleButtonCommand toggleCommand;
    private String label;

    public ToggleButton(ToggleButtonCommand toggleCommand) {
        this.toggleCommand = toggleCommand;
        ToggleButtonResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
        sinkEvents(Event.ONCLICK);
    }
    
    public void setLabel(String label) {
        toggleButtonUi.setInnerText(toggleCommand.expanded ? I18N.collapseX(label) : I18N.showAllX(label));
        this.label = label;
    }
    
    @Override
    public void onBrowserEvent(Event event) {
        if (event.getTypeInt() == Event.ONCLICK) {
            toggleCommand.execute();
            toggleButtonUi.setInnerText(toggleCommand.expanded ? I18N.collapseX(label) : I18N.showAllX(label));
            return;
        }
        super.onBrowserEvent(event);
    }
    
    public static abstract class ToggleButtonCommand implements Command {
        private boolean expanded = false;
        @Override
        public final void execute() {
            this.expanded = !expanded;
            this.execute(expanded);
        }
        protected abstract void execute(boolean expanded);
    }
    
}
