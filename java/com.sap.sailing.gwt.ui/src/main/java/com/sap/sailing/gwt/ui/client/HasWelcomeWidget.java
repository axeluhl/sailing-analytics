package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.ui.client.shared.panels.SimpleWelcomeWidget;
import com.sap.sailing.gwt.ui.client.shared.panels.WelcomeWidget;

/**
 * Classes which implement this interface have the ability to display a {@link WelcomeWidget}.
 * 
 * @author Lennart Hensler (D054527)
 */
public interface HasWelcomeWidget {

    /**
     * Sets the welcome widget of the component.
     * @param welcome The new welcome widget
     * @see {@link WelcomeWidget}, {@link SimpleWelcomeWidget}
     */
    void setWelcomeWidget(WelcomeWidget welcome);
    void setWelcomeWidgetVisible(boolean isVisible);
    
}
