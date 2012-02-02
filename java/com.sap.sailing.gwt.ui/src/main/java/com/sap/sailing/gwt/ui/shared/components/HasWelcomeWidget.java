package com.sap.sailing.gwt.ui.shared.components;

import com.google.gwt.user.client.ui.Widget;

public interface HasWelcomeWidget {

    /**
     * Sets the welcome widget of the component.
     * @param welcome The new welcome widget
     * @see {@link WelcomeWidget}, {@link SimpleWelcomeWidget}, {@link ClosableWelcomeWidget}, ...
     */
    void setWelcomeWidget(Widget welcome);
    void setWelcomeWidgetVisible(boolean isVisible);
    
}
