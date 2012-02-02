package com.sap.sailing.gwt.ui.shared.components;

import com.google.gwt.user.client.ui.Widget;

public interface HasWelcomeWidget {

    void setWelcomeWidget(Widget welcome);
    Widget getWelcomeWidget();
    void setWelcomeWidgetVisible(boolean isVisible);
    
}
