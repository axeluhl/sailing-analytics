package com.sap.sailing.gwt.home.desktop.places.user.profile.detailstab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.desktop.partials.useraccountDetails.UserAccountDetails;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sailing.gwt.home.shared.partials.userdetails.UserDetailsView;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.AuthorizedContentDecoratorDesktop;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class UserProfileDetailsViewImpl extends Composite implements UserProfileDetailsView {

    interface MyBinder extends UiBinder<Widget, UserProfileDetailsViewImpl> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @UiField(provided = true)
    AuthorizedContentDecoratorDesktop decoratorUi;
    
    @UiField
    UserAccountDetails accountDetailsUi;

    private UserProfileView.Presenter currentPresenter;


    public UserProfileDetailsViewImpl() {
    }
    
    public UserDetailsView getUserDetailsView() {
        return accountDetailsUi;
    }
    
    @Override
    public void setPresenter(Presenter presenter) {
        decoratorUi = new AuthorizedContentDecoratorDesktop(currentPresenter);
        initWidget(ourUiBinder.createAndBindUi(this));
    }
    
    @Override
    public NeedsAuthenticationContext getDecorator() {
        return decoratorUi;
    }
}