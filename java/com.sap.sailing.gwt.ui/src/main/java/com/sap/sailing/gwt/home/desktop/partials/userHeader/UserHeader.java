package com.sap.sailing.gwt.home.desktop.partials.userHeader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.usermanagement.app.AuthenticationContext;

public class UserHeader extends Composite {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, UserHeader> {
    }
    
    @UiField DivElement imageUi;
    @UiField DivElement nameUi;
    @UiField DivElement usernameUi;
    
    public UserHeader() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setUserManagementContext(AuthenticationContext userManagementContext) {
        UserHeaderResources.INSTANCE.css().ensureInjected();
        imageUi.getStyle().setBackgroundImage("url(images/home/userdefault.svg)");
        
        nameUi.setInnerText(userManagementContext.getUserTitle());
        usernameUi.setInnerText(userManagementContext.getUserSubtitle());
    }
}
