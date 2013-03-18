package com.sap.sailing.gwt.ui.shared.components;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.panels.BusyIndicator;

public interface IsEmbeddableComponent {

    Widget getHeaderWidget();

    Widget getContentWidget();

    Widget getToolbarWidget();
    
    Widget getLegendWidget();
    
    BusyIndicator getBusyIndicator();
    
    boolean isEmbedded();
    
    boolean hasToolbar();
    
    boolean hasBusyIndicator();
}
