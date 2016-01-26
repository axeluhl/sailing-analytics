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
import com.sap.sse.security.ui.authentication.UserManagementResources;
import com.sap.sse.security.ui.authentication.UserManagementSharedResources;
import com.sap.sse.security.ui.authentication.UserManagementResources.LocalCss;

public class UserManagementViewDesktop extends Composite implements UserManagementView {
    
    private static final LocalCss LOCAL_CSS = UserManagementResources.INSTANCE.css(); 
    
    interface UserManagementViewUiBinder extends UiBinder<Widget, UserManagementViewDesktop> {
    }
    
    private static UserManagementViewUiBinder uiBinder = GWT.create(UserManagementViewUiBinder.class);
    
    private final PopupPanel popupPanel = new PopupPanel(true, false);
    
    @UiField DivElement headingUi;
    @UiField SimplePanel contentContainerUi;
    
    @UiField(provided = true)
    UserManagementSharedResources res = SharedResources.INSTANCE;

    public UserManagementViewDesktop() {
        LOCAL_CSS.ensureInjected();
        popupPanel.addStyleName(LOCAL_CSS.flyover());
//        popupPanel.addAutoHidePartner(autoHidePartner);
        super.initWidget(uiBinder.createAndBindUi(this));
        popupPanel.setWidget(this);
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
