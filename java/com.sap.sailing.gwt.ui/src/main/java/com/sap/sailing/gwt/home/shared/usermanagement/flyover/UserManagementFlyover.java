package com.sap.sailing.gwt.home.shared.usermanagement.flyover;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementResources;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementResources.LocalCss;

public class UserManagementFlyover extends Composite implements AcceptsOneWidget {
    
    private static final LocalCss LOCAL_CSS = UserManagementResources.INSTANCE.css(); 
    
    interface UserManagementFlyoverUiBinder extends UiBinder<Widget, UserManagementFlyover> {
    }
    
    private static UserManagementFlyoverUiBinder uiBinder = GWT.create(UserManagementFlyoverUiBinder.class);
    
    private final PopupPanel popupPanel = new PopupPanel(true, false);
    
    @UiField SimplePanel contentContainerUi;

    public UserManagementFlyover() {
        LOCAL_CSS.ensureInjected();
        popupPanel.addStyleName(LOCAL_CSS.flyover());
//        popupPanel.addAutoHidePartner(autoHidePartner);
        super.initWidget(uiBinder.createAndBindUi(this));
        popupPanel.setWidget(this);
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
                    popupPanel.setPopupPosition(anchor.getAbsoluteLeft() + anchor.getOffsetWidth() - offsetWidth + 20,
                            anchor.getAbsoluteTop() + 20);
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
