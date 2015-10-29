package com.sap.sailing.gwt.home.desktop.app;

import com.sap.sailing.gwt.home.shared.app.ResettableNavigationPathDisplay;
import com.sap.sse.gwt.client.mvp.TopLevelView;

public interface ApplicationTopLevelView extends TopLevelView {
    void showLoading(boolean visibile);
    ResettableNavigationPathDisplay getNavigationPathDisplay();
}
