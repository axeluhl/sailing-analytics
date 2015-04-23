package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.user.client.ui.Widget;

public interface ComponentViewer {

    Widget getViewerWidget();
    
    String getViewerName();

    Component<?> getRootComponent();
    
    void forceLayout();
}
