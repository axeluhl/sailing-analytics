package com.sap.sse.security.ui.authentication.decorator;

import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;
import com.sap.sse.security.ui.client.i18n.StringMessages;

/**
 * Decorator widget that either shows its child widget or depending on the authentication state a
 * {@link NotLoggedInView}.
 *
 */
public class AuthorizedContentDecorator extends Composite implements RequiresResize, NeedsAuthenticationContext {
    private final SimplePanel contentHolder = new SimplePanel();
    private Widget content;
    private WidgetFactory contentWidgetFactory;
    private final NotLoggedInView notLoggedInView;
    private WildcardPermission permissionToCheck;

    public AuthorizedContentDecorator(NotLoggedInPresenter presenter, NotLoggedInView notLoggedInView) {
        this.notLoggedInView = notLoggedInView;
        notLoggedInView.setPresenter(presenter);
        notLoggedInView.setSignInText(StringMessages.INSTANCE.signIn());
        initWidget(contentHolder);
    }

    /**
     * @param content the child widget to show if the user is authenticated and has the required permission
     */
    @UiChild(limit = 1)
    public void addContent(Widget content) {
        this.content = content;
    }

    /**
     * An alternative to setting the child widget to show for permitted users is to set a factory that creates the
     * widget when needed the first time. This is useful if creating the UI required the user to be authenticated to
     * make remote calls to succeed that would cause an error for non permitted users.
     * 
     * @param contentWidgetFactory
     *            a factory that creates the child widget on demand when the user is authenticated and permitted the
     *            first time
     */
    public void setContentWidgetFactory(WidgetFactory contentWidgetFactory) {
        this.contentWidgetFactory = contentWidgetFactory;
    }

    private Widget getContentWidget() {
        if (contentWidgetFactory != null) {
            content = contentWidgetFactory.get();
            contentWidgetFactory = null;
        }
        return content;
    }

    @Override
    protected void onLoad() {
        super.onLoad();

        if (getParent() instanceof ProvidesResize || getParent() instanceof HeaderPanel) {
            contentHolder.setSize("100%", "100%");
        }
    }

    @Override
    public void onResize() {
        Widget currentContentWidget = contentHolder.getWidget();
        if (currentContentWidget instanceof RequiresResize) {
            ((RequiresResize) currentContentWidget).onResize();
        }
    }

    /**
     * You need to set the {@link AuthenticationContext} whenever the authentication changes to make the decorator react
     * by showing the correct content.
     */
    @Override
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        boolean isAuthenticated = authenticationContext.isLoggedIn();
        boolean isPermitted = isPermitted(authenticationContext);
        boolean maySeeRealContent = isAuthenticated && isPermitted;
        IsWidget isWidget = maySeeRealContent ? getContentWidget() : notLoggedInView;
        if (!maySeeRealContent) {
            String message = !isAuthenticated ? StringMessages.INSTANCE.youAreNotSignedIn() : StringMessages.INSTANCE
                    .youDontHaveRequiredPermission();
            notLoggedInView.setMessage(message);
        }
        Widget widget = isWidget.asWidget();
        if (widget instanceof RequiresResize) {
            widget.setSize("100%", "100%");
        }
        contentHolder.setWidget(widget);
        if (widget instanceof RequiresResize) {
            ((RequiresResize) widget).onResize();
        }
    }

    private boolean isPermitted(AuthenticationContext userManagementContext) {
        return permissionToCheck == null
                || userManagementContext.hasPermission(permissionToCheck);
    }

    /**
     * Setting a permission causes that the user not only needs to be logged in but also needs to have the given permission.
     * 
     * @param permissionToCheck the permission to check
     */
    public void setPermissionToCheck(WildcardPermission permissionToCheck) {
        this.permissionToCheck = permissionToCheck;
    }
}
