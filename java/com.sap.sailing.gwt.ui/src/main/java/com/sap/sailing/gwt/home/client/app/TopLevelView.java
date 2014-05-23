package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;

public interface TopLevelView {
    void showLoading(boolean visibile);

    AcceptsOneWidget getStage();
    
    Widget asWidget();
}
