package com.sap.sailing.gwt.ui.raceboard;

import com.sap.sse.gwt.client.formfactor.DeviceDetector;
import com.sap.sse.security.ui.authentication.view.AbstractFlyoutAuthenticationView;

public class RaceBoardAuthenticationView extends AbstractFlyoutAuthenticationView {
    private static final RaceBoardResources res = RaceBoardResources.INSTANCE;

    public RaceBoardAuthenticationView() {
        super(res);
        popupPanel.setStyleName(res.mainCss().usermanagement_view());
        popupPanel.setStyleName(res.mainCss().usermanagement_mobile(), DeviceDetector.isMobile());
        popupPanel.getWidget().addStyleName(res.mainCss().usermanagement_view_content_wrapper());
        flyoverContentUi.addClassName(res.mainCss().usermanagement_view_content());
    }
    
    public void show() {
        popupPanel.show();
        getPresenter().onVisibilityChanged(true);
    }
}
