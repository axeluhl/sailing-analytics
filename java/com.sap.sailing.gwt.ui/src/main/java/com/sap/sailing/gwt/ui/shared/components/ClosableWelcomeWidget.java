package com.sap.sailing.gwt.ui.shared.components;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class ClosableWelcomeWidget extends SimpleWelcomeWidget {

    private Button closeButton;
    private boolean closable;
    
    public ClosableWelcomeWidget(boolean closable, String headerText, String welcomeText, HorizontalAlignmentConstant horizontalAlignment, StringMessages stringConstants) {
        super(headerText, welcomeText, horizontalAlignment, stringConstants);
        this.closable = closable;
        
        closeButton = new Button(stringConstants.close());
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ClosableWelcomeWidget.this.setVisible(false);
            }
        });
        closeButton.setVisible(this.closable);
        setHorizontalAlignment(ALIGN_RIGHT);
        insert(closeButton, 0);
    }
    
    public boolean isClosable() {
        return closable;
    }
    
    public void setIsClosable(boolean isClosable) {
        closable = isClosable;
        closeButton.setVisible(closable);
    }

}
