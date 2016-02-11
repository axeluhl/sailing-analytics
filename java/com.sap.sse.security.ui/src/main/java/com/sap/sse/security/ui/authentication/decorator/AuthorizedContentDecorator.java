package com.sap.sse.security.ui.authentication.decorator;

import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.security.shared.Permission;
import com.sap.sse.security.shared.PermissionsForRoleProvider;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class AuthorizedContentDecorator extends Composite implements RequiresResize {
    
    private final SimplePanel contentHolder = new SimplePanel();
    private Widget content;
    private WidgetFactory contentWidgetFactory;
    private final NotLoggedInView notLoggedInView;
    private String permissionToCheck;
    private PermissionsForRoleProvider permissionsForRoleProvider;

    public AuthorizedContentDecorator(NotLoggedInPresenter presenter, NotLoggedInView notLoggedInView) {
        this.notLoggedInView = notLoggedInView;
        
        notLoggedInView.setPresenter(presenter);
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
    
    @Override
    protected void onLoad() {
        super.onLoad();
        
        if(getParent() instanceof ProvidesResize || getParent() instanceof HeaderPanel) {
            contentHolder.setSize("100%", "100%");
        }
    }
    
    @Override
    public void onResize() {
        Widget currentContentWidget = contentHolder.getWidget();
        if(currentContentWidget instanceof RequiresResize) {
            ((RequiresResize) currentContentWidget).onResize();
        }
    }
    
    public void setUserManagementContext(AuthenticationContext userManagementContext) {
        boolean isAuthenticated = userManagementContext.isLoggedIn();
        boolean isPermitted = isPermitted(userManagementContext);
        boolean maySeeRealContent = isAuthenticated == isPermitted;
        
        IsWidget isWidget = maySeeRealContent ? getContentWidget() : notLoggedInView;
        
        if(!maySeeRealContent) {
            String message = !isAuthenticated ? StringMessages.INSTANCE.youAreNotSignedIn() : StringMessages.INSTANCE
                    .youDontHaveRequiredPermission();
            notLoggedInView.setMessage(message);
        }

        Widget widget = isWidget.asWidget();
        if(widget instanceof RequiresResize) {
            widget.setSize("100%", "100%");
        }
        contentHolder.setWidget(widget);
        if(widget instanceof RequiresResize) {
            ((RequiresResize) widget).onResize();
        }
    }

    private boolean isPermitted(AuthenticationContext userManagementContext) {
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
