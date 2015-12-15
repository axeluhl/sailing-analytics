package com.sap.sailing.gwt.home.mobile.places.user.profile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.userdetails.UserDetails;
import com.sap.sailing.gwt.home.mobile.partials.userheader.UserHeader;
import com.sap.sailing.gwt.home.shared.app.UserManagementContext;

public class UserProfileDetailsViewImpl extends Composite implements UserProfilDetailsView {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, UserProfileDetailsViewImpl> {
    }

    @UiField UserHeader userHeaderUi;
    @UiField(provided = true) UserDetails userDetailsUi;
    
    public UserProfileDetailsViewImpl(Presenter presenter) {
        userDetailsUi = new UserDetails(presenter);
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public void setUserManagementContext(UserManagementContext userManagementContext) {
        userHeaderUi.setUserManagementContext(userManagementContext);
        userDetailsUi.setUserManagementContext(userManagementContext);
    }
}
