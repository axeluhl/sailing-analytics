package com.sap.sailing.gwt.ui.shared.components;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class ClosableWelcomeWidget extends SimpleWelcomeWidget {

    private Button closeButton;
    private boolean closable;
    
    /**
     * Creates a new closable welcome widget. It has the same components as the {@link SimpleWelcomeWidget} and a Button
     * at the top right corner to close the welcome widget (it sets the whole component visible = <code>false</code>).
     * 
     * @param closable Sets the visibility of the close Button
     * @param headerText The text of the header component
     * @param welcomeText The text of the component under the header (welcome component)
     * @param horizontalAlignment The alignment of the header and the welcomen component
     */
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
