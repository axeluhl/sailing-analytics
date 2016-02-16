package com.sap.sailing.gwt.home.mobile.partials.userheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;
import com.sap.sse.security.ui.shared.UserDTO;

public class UserHeader extends Composite implements NeedsAuthenticationContext {

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
    
    @Override
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        UserDTO currentUser = authenticationContext.getCurrentUser();
        // TODO correct message
        imageUi.setTitle("TODO picture of: " + currentUser.getName());
        // TODO use image from user when field is available
        imageUi.getStyle().setBackgroundImage("url(images/home/userdefault.svg)");
        
        titleUi.setInnerText(authenticationContext.getUserTitle());
        subtitleUi.setInnerText(authenticationContext.getUserSubtitle());
    }

}
