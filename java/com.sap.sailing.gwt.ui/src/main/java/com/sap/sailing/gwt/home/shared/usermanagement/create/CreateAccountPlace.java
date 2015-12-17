package com.sap.sailing.gwt.home.shared.usermanagement.create;

import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.usermanagement.AbstractUserManagementPlace;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class CreateAccountPlace extends AbstractUserManagementPlace implements HasMobileVersion {

    @Override
    public String getLocationTitle() {
        return StringMessages.INSTANCE.signUp();
    }
    
}
