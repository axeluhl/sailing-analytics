package com.sap.sailing.gwt.home.shared.usermanagement.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sse.gwt.common.CommonSharedResources;
import com.sap.sse.security.ui.authentication.UserManagementResources;
import com.sap.sse.security.ui.authentication.UserManagementResources.LocalCss;
import com.sap.sse.security.ui.authentication.view.FlyoutAuthenticationView;

public class AuthenticationViewDesktop extends Composite implements FlyoutAuthenticationView {
    
    private static final LocalCss LOCAL_CSS = UserManagementResources.INSTANCE.css(); 
    
    interface AuthenticationViewUiBinder extends UiBinder<Widget, AuthenticationViewDesktop> {
    }
    
    private static AuthenticationViewUiBinder uiBinder = GWT.create(AuthenticationViewUiBinder.class);
    
    private final PopupPanel popupPanel = new PopupPanel(true, false);
    
    @UiField DivElement headingUi;
    @UiField SimplePanel contentContainerUi;
    
    @UiField(provided = true)
    CommonSharedResources res = SharedResources.INSTANCE;

    public AuthenticationViewDesktop() {
        LOCAL_CSS.ensureInjected();
        popupPanel.addStyleName(LOCAL_CSS.flyover());
        super.initWidget(uiBinder.createAndBindUi(this));
        popupPanel.setWidget(this);
    }
    
    @Override
    public void setAutoHidePartner(IsWidget autoHidePartner) {
        popupPanel.addAutoHidePartner(autoHidePartner.asWidget().getElement());
    }
    
    @Override
    public void setHeading(String heading) {
        headingUi.setInnerText(heading);
        UIObject.setVisible(headingUi, heading != null && !heading.isEmpty());
    }
    
    @Override
    public void setWidget(IsWidget w) {
        contentContainerUi.setWidget(w);
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
            }
        });
    }
    
    public void hide() {
        popupPanel.hide();
    }
    
    public boolean isShowing() {
        return popupPanel.isShowing();
    }

}
