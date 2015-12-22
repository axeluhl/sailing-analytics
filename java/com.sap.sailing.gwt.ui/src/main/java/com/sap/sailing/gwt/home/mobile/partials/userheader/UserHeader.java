package com.sap.sailing.gwt.home.mobile.partials.userheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.usermanagement.app.UserManagementContext;
import com.sap.sse.security.ui.shared.UserDTO;

public class UserHeader extends Composite {

    interface MyUiBinder extends UiBinder<Widget, UserHeader> {
    }
    
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    
    @UiField HeadingElement titleUi;
    @UiField DivElement imageUi;
    @UiField DivElement subtitleUi;
    
    public UserHeader() {
        UserHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        imageUi.getStyle().setBackgroundImage("url(images/home/userdefault.svg)");
        titleUi.setInnerText("testuser");
        subtitleUi.setInnerText("jennifer@auchnichtwennduderletztemenschauferdenwaerts.looser");
    }

    public void setUserManagementContext(UserManagementContext userManagementContext) {
        UserDTO currentUser = userManagementContext.getCurrentUser();
        // TODO correct message
        imageUi.setTitle("TODO picture of: " + currentUser.getName());
        // TODO use image from user when field is available
        imageUi.getStyle().setBackgroundImage("url(images/home/userdefault.svg)");
        
        titleUi.setInnerText(currentUser.getName());
        subtitleUi.setInnerText(currentUser.getEmail());
    }

}
