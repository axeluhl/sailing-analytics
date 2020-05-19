package com.sap.sailing.gwt.home.desktop.places.user.profile.subscriptiontab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.AuthorizedContentDecoratorDesktop;
import com.sap.sse.gwt.resources.CommonControlsCSS;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

public class UserProfileSubscriptionViewImpl extends Composite implements UserProfileSubscriptionView {
    interface MyBinder extends UiBinder<Widget, UserProfileSubscriptionViewImpl> {
    }

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    @UiField(provided = true)
    AuthorizedContentDecoratorDesktop decoratorUi;
    @UiField(provided = true)
    UserSubscription userSubscriptionUi;

    @Override
    public void setPresenter(Presenter presenter) {
        decoratorUi = new AuthorizedContentDecoratorDesktop(presenter);
        userSubscriptionUi = new UserSubscription(presenter.getUserSubscriptionPresenter());
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        CommonControlsCSS.ensureInjected();
    }

    @Override
    public NeedsAuthenticationContext getDecorator() {
        // TODO Auto-generated method stub
        return decoratorUi;
    }

}
