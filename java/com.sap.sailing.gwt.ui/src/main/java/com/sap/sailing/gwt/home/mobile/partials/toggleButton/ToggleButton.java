package com.sap.sailing.gwt.home.mobile.partials.toggleButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class ToggleButton extends BigButton {

    private static final StringMessages I18N = StringMessages.INSTANCE;
    private final ToggleButtonCommand toggleCommand;
    private String label;

    public ToggleButton(final ToggleButtonCommand toggleCommand) {
        this.toggleCommand = toggleCommand;
        this.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                toggleCommand.execute();
                buttonUi.setInnerText(toggleCommand.expanded ? I18N.collapseX(label) : I18N.showAllX(label));
            }
        });
    }
    
    @Override
    public void setLabel(String label) {
        buttonUi.setInnerText(toggleCommand.expanded ? I18N.collapseX(label) : I18N.showAllX(label));
        this.label = label;
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
