package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.StringMessages;

public class SettingsDialogForLinkSharing<SettingsType extends Settings> extends AbstractSettingsDialog<SettingsType> {

    public SettingsDialogForLinkSharing(LinkWithSettingsGenerator<SettingsType> linkWithSettingsGenerator,
            ComponentLifecycle<SettingsType> componentLifecycle, StringMessages stringMessages) {
        this(linkWithSettingsGenerator, componentLifecycle, componentLifecycle.createDefaultSettings(), stringMessages,
                true, null);
    }

    public SettingsDialogForLinkSharing(LinkWithSettingsGenerator<SettingsType> linkWithSettingsGenerator,
            ComponentLifecycle<SettingsType> componentLifecycle, SettingsType settings,
            StringMessages stringMessages, boolean animationEnabled, DialogCallback<SettingsType> callback) {
        super(componentLifecycle.getLocalizedShortName(), componentLifecycle.getSettingsDialogComponent(settings),
                stringMessages, animationEnabled, linkWithSettingsGenerator, callback);
    }
}
