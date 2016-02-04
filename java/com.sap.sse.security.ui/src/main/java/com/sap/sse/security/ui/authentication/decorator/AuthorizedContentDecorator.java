package com.sap.sse.security.ui.authentication.decorator;

import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.security.shared.Permission;
import com.sap.sse.security.shared.PermissionsForRoleProvider;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class AuthorizedContentDecorator extends Composite {
    
    private final SimplePanel contentHolder = new SimplePanel();
    private Widget content;
    private WidgetFactory contentWidgetFactory;
    private final NotLoggedInView notLoggedInView;
    private String permissionToCheck;
    private PermissionsForRoleProvider permissionsForRoleProvider;

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
    
    public void setContentWidgetFactory(WidgetFactory contentWidgetFactory) {
        this.contentWidgetFactory = contentWidgetFactory;
    }
    
    private Widget getContentWidget() {
        if(contentWidgetFactory != null) {
            content = contentWidgetFactory.get();
            contentWidgetFactory = null;
        }
        return content;
    }
    
    public void setUserManagementContext(AuthenticationContext userManagementContext) {
        contentHolder.setWidget(isPermitted(userManagementContext) ? getContentWidget() : notLoggedInView);
    }

    private boolean isPermitted(AuthenticationContext userManagementContext) {
        if(!userManagementContext.isLoggedIn()) {
            return false;
        }
        return permissionToCheck == null || userManagementContext.getCurrentUser().hasPermission(permissionToCheck, permissionsForRoleProvider);
    }
    
    public void setPermissionsForRoleProvider(PermissionsForRoleProvider permissionsForRoleProvider) {
        this.permissionsForRoleProvider = permissionsForRoleProvider;
    }
    
    public void setPermissionToCheck(String permissionToCheck) {
        this.permissionToCheck = permissionToCheck;
    }
    
    public void setPermissionToCheck(Permission permissionToCheck) {
        setPermissionToCheck(permissionToCheck.getStringPermission());
    }
    
    public void setPermissionToCheck(Permission permissionToCheck, PermissionsForRoleProvider permissionsForRoleProvider) {
        setPermissionToCheck(permissionToCheck);
        setPermissionsForRoleProvider(permissionsForRoleProvider);
    }
}
