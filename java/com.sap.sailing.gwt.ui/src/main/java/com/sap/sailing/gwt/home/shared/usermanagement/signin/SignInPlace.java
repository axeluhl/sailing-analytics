package com.sap.sailing.gwt.home.shared.usermanagement.signin;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.usermanagement.AbstractUserManagementPlace;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class SignInPlace extends AbstractUserManagementPlace implements HasMobileVersion {
    
    public SignInPlace(Place nextTarget) {
        super(nextTarget);
    }
    
    @Override
    public String getLocationTitle() {
        return StringMessages.INSTANCE.signIn();
    }
    
}
