package com.sap.sailing.gwt.home.shared.usermanagement.confirm;

import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.usermanagement.AbstractAuthenticationPlace;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class ConfirmationInfoPlace extends AbstractAuthenticationPlace implements HasMobileVersion {
    private final String name;
    private final Action action;

    public enum Action {
        ERROR, ACCOUNT_CREATED, RESET_REQUESTED;  
    };

    public ConfirmationInfoPlace(Action action, String name) {
        this.action = action;
        this.name = name;
    }

    public Action getAction() {
        return action;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ConfirmationInfoPlace [name=" + name + ", action=" + action + "]";
    }
    
    @Override
    public String getLocationTitle() {
        return StringMessages.INSTANCE.accountConfirmation();
    }

}
