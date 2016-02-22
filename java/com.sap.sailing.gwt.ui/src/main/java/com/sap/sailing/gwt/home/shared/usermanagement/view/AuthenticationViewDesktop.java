package com.sap.sailing.gwt.home.shared.usermanagement.view;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sse.security.ui.authentication.view.AbstractFlyoutAuthenticationView;

public class AuthenticationViewDesktop extends AbstractFlyoutAuthenticationView {

    public AuthenticationViewDesktop() {
        super(SharedResources.INSTANCE);
    }
    
    public void show() {
        popupPanel.setPopupPositionAndShow(new PositionCallback() {
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {
                Element anchor = Document.get().getElementById("usrMngmtFlyover");
                if (anchor != null) {
                    int left = anchor.getAbsoluteLeft() + anchor.getOffsetWidth() - offsetWidth + 15;
                    popupPanel.setPopupPosition(left, anchor.getAbsoluteTop() + 20);
                }
                getPresenter().onVisibilityChanged(true);
            }
        });
    }
}
