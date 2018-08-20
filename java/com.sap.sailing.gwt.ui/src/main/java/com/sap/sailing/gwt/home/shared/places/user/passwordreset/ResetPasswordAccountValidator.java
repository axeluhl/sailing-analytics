package com.sap.sailing.gwt.home.shared.places.user.passwordreset;

import com.sap.sse.security.ui.client.component.NewAccountValidator;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class ResetPasswordAccountValidator extends NewAccountValidator {

    public ResetPasswordAccountValidator(StringMessages stringMessages) {
        super(stringMessages);
    }

    @Override
    protected String validateUsername(String username) {
        // the userName is invalid, if any ErrorMessage is returned
        boolean invalidUserName = super.validateUsername(username) != null;
        // however since the username is extracted from the reseturl, this means the reseturl is corrupt
        return invalidUserName ? stringMessages.cannotResetInvalidURL() : null;
    }

}
