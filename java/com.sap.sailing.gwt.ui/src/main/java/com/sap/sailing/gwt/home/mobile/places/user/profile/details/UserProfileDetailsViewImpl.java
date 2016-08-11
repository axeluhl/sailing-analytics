package com.sap.sailing.gwt.home.mobile.places.user.profile.details;

import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.mobile.places.user.profile.AbstractUserProfileView;
import com.sap.sse.security.ui.userprofile.mobile.userdetails.UserDetails;
import com.sap.sse.security.ui.userprofile.shared.userdetails.UserDetailsView;

public class UserProfileDetailsViewImpl extends AbstractUserProfileView implements UserProfileDetailsView {

    private final UserDetails userDetailsUi = new UserDetails(SharedResources.INSTANCE);
    
    public UserProfileDetailsViewImpl(UserProfileDetailsView.Presenter presenter) {
        super(presenter);
        setViewContent(userDetailsUi);
    }
    
    @Override
    public UserDetailsView getUserDetailsView() {
        return userDetailsUi;
    }
    
}
