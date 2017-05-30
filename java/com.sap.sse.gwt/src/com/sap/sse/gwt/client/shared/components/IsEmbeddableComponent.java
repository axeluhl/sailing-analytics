package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.user.client.ui.Widget;

public interface IsEmbeddableComponent {

    Widget getHeaderWidget();

    Widget getContentWidget();

    Widget getToolbarWidget();
    
    Widget getLegendWidget();
    
    boolean isEmbedded();
    
    boolean hasToolbar();
}
