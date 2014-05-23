package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

public interface TopLevelView {
    void showLoading(boolean visibile);

    AcceptsOneWidget getStage();
}
