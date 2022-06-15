package com.sap.sailing.gwt.home.desktop.places.user.profile.subscriptionstab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.AuthorizedContentDecoratorDesktop;
import com.sap.sse.gwt.resources.CommonControlsCSS;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

/**
 * Implementation view of {@link UserProfileSubscriptionsView}
 *
 * @author Tu Tran
 */
public class UserProfileSubscriptionsViewImpl extends Composite implements UserProfileSubscriptionsView {
    interface MyBinder extends UiBinder<Widget, UserProfileSubscriptionsViewImpl> {
    }

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    @UiField(provided = true)
    AuthorizedContentDecoratorDesktop decoratorUi;
    @UiField(provided = true)
    UserSubscriptions userSubscriptionUi;

    @Override
    public void setPresenter(final Presenter presenter) {
        decoratorUi = new AuthorizedContentDecoratorDesktop(presenter);
        userSubscriptionUi = new UserSubscriptions(presenter.getUserSubscriptionPresenter());
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        CommonControlsCSS.ensureInjected();
    }

    @Override
    public NeedsAuthenticationContext getDecorator() {
        return decoratorUi;
    }

}
