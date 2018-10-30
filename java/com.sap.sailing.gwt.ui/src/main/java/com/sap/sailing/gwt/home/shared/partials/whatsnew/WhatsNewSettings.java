package com.sap.sailing.gwt.home.shared.partials.whatsnew;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.LongSetting;

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
