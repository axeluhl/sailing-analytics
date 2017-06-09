package com.sap.sailing.gwt.ui.client.shared.panels;

import com.google.gwt.user.client.ui.FlowPanel;

/**
 * A WelcomeWidget displays a short message to the user to show him the functionality (or whatever) of the component on which it's displayed.
 * @author Lennart Hensler (D054527)
 */
public abstract class WelcomeWidget extends FlowPanel {
    
    final static String STYLE_NAME_PREFIX = "welcomeWidget-";

    /**
     * Sets the text of the header component.
     * @param headerText The new header text
     */
    abstract void setWelcomeHeaderText(String headerText);
    
    /**
     * Sets the text of the component under the header
     * @param welcomeText The new welcome text
     */
    abstract void setWelcomeText(String welcomeText);
    
}
