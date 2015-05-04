package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;

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
