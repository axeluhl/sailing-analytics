package com.sap.sailing.gwt.home.mobile.places.user.profile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.userdetails.UserDetails;
import com.sap.sailing.gwt.home.mobile.partials.userheader.UserHeader;
import com.sap.sailing.gwt.home.shared.partials.userdetails.UserDetailsView;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.AuthorizedContentDecoratorMobile;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

public class UserProfileDetailsViewImpl extends Composite implements UserProfileDetailsView {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, UserProfileDetailsViewImpl> {
    }

    @UiField UserHeader userHeaderUi;
    @UiField(provided = true) AuthorizedContentDecoratorMobile decoratorUi;
    @UiField(provided = true) UserDetails userDetailsUi;
    
    public UserProfileDetailsViewImpl(Presenter presenter) {
        decoratorUi = new AuthorizedContentDecoratorMobile(presenter);
        userDetailsUi = new UserDetails();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public UserDetailsView getUserDetailsView() {
        return userDetailsUi;
    }
    
    @Override
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        userHeaderUi.setAuthenticationContext(authenticationContext);
        decoratorUi.setAuthenticationContext(authenticationContext);
    }
}
