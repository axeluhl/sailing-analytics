package com.sap.sailing.gwt.home.shared.usermanagement.decorator;

import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.app.UserManagementContext;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class AuthorizedContentDecorator extends Composite {
    
    private final SimplePanel contentHolder = new SimplePanel();
    private Widget content;
    private final NotLoggedInView notLoggedInView;

    public AuthorizedContentDecorator(NotLoggedInPresenter presenter, NotLoggedInView notLoggedInView) {
        this.notLoggedInView = notLoggedInView;
        
        notLoggedInView.setPresenter(presenter);
        notLoggedInView.setMessage(StringMessages.INSTANCE.youAreNotSignedIn());
        notLoggedInView.setSignInText(StringMessages.INSTANCE.signIn());
        
        initWidget(contentHolder);
    }
    
    @UiChild(limit = 1)
    public void addContent(Widget content) {
        this.content = content;
    }
    
    public void setUserManagementContext(UserManagementContext userManagementContext) {
        contentHolder.setWidget(userManagementContext.isLoggedIn() ? content : notLoggedInView);
    }
}
