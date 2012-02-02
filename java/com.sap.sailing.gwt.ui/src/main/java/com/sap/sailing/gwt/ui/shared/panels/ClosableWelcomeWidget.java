package com.sap.sailing.gwt.ui.shared.panels;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class ClosableWelcomeWidget extends SimpleWelcomeWidget {

    private StringMessages stringConstants;
    private boolean closable;
    private Button closeButton;
    
    /**
     * Creates a new closable welcome widget. It has the same components as the {@link SimpleWelcomeWidget} and a Button
     * at the top right corner to close the welcome widget (it sets the whole component visible = <code>false</code>).
     * 
     * @param closable Sets the visibility of the close Button
     * @param headerText The text of the header component
     * @param welcomeText The text of the component under the header (welcome component)
     */
    public ClosableWelcomeWidget(boolean closable, String headerText, String welcomeText, StringMessages stringConstants) {
        super(headerText, welcomeText);
        this.stringConstants = stringConstants;
        this.closable = closable;
        
        closeButton = new Button(this.stringConstants.close());
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ClosableWelcomeWidget.this.setVisible(false);
            }
        });
        closeButton.setVisible(this.closable);
        closeButton.setStyleName(STYLE_NAME_PREFIX + "CloseButton");
        mainPanel.insert(closeButton, 0);
    }
    
    /**
     * @return <code>true</code> if the welcome widget is closable (close button is visible) right now, else returns <code>false</code>
     */
    public boolean isClosable() {
        return closable;
    }
    
    /**
     * Sets the visibility of the close button and consequently the ability of the welcome widget to close itself.
     */
    public void setIsClosable(boolean isClosable) {
        closable = isClosable;
        closeButton.setVisible(closable);
    }

}
