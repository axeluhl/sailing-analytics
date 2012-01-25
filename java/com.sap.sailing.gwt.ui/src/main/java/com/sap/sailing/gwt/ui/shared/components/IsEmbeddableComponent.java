package com.sap.sailing.gwt.ui.shared.components;

import com.google.gwt.user.client.ui.Widget;

public interface IsEmbeddableComponent {

    Widget getHeaderWidget();

    Widget getContentWidget();

    Widget getToolbarWidget();
    
    boolean isEmbedded();
}
