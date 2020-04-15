package com.sap.sailing.gwt.home.shared.partials.dialog.whatsnew;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.LongSetting;

/**
 * Stores the number of characters in the changelog when the last login occured. This is needed to determine whether to
 * show a 'What's New' dialog to the user after login.
 */
public class WhatsNewSettings extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = 1545042411098078525L;

    public static final String PREF_NAME = "sailing.whatsnew";;

    private transient LongSetting numberOfCharsOnLastLogin;

    public WhatsNewSettings() {
    }

    public WhatsNewSettings(Long numberOfCharsOnLastLogin) {
        this.numberOfCharsOnLastLogin.setValue(numberOfCharsOnLastLogin);
    }

    @Override
    protected void addChildSettings() {
        numberOfCharsOnLastLogin = new LongSetting("numberOfCharsOnLastLogin", this, 0l);
    }

    public Long getNumberOfCharsOnLastLogin() {
        return numberOfCharsOnLastLogin.getValue();
    }
}
