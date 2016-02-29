package com.sap.sailing.gwt.home.mobile.places.user.profile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.mobile.partials.userdetails.UserDetails;
import com.sap.sailing.gwt.home.shared.partials.userdetails.UserDetailsView;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.AuthorizedContentDecoratorMobile;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.userprofile.mobile.userheader.UserHeader;

public class UserProfileDetailsViewImpl extends Composite implements UserProfileDetailsView {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, UserProfileDetailsViewImpl> {
    }

    @UiField(provided = true) final UserHeader userHeaderUi;
    @UiField(provided = true) final AuthorizedContentDecoratorMobile decoratorUi;
    @UiField(provided = true) final UserDetails userDetailsUi;
    
    public UserProfileDetailsViewImpl(Presenter presenter) {
        userHeaderUi = new UserHeader(SharedResources.INSTANCE);
        decoratorUi = new AuthorizedContentDecoratorMobile(presenter);
        userDetailsUi = new UserDetails(SharedResources.INSTANCE);
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
