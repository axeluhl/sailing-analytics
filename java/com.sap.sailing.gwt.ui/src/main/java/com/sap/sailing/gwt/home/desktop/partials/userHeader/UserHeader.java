package com.sap.sailing.gwt.home.desktop.partials.userHeader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.app.UserManagementContext;

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

    public void setUserManagementContext(UserManagementContext userManagementContext) {
        UserHeaderResources.INSTANCE.css().ensureInjected();
        imageUi.getStyle().setBackgroundImage("url(images/home/userdefault.svg)");
        nameUi.setInnerText(userManagementContext.getCurrentUser().getName());
        
        // TODO there is no distinction between usernam und the user's name, so we show the email instead as username
        usernameUi.setInnerText(userManagementContext.getCurrentUser().getEmail());
    }
}
