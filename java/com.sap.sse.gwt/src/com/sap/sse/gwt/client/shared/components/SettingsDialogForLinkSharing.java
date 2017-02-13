package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Anchor;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.StringMessages;

public class SettingsDialogForLinkSharing<SettingsType extends Settings> extends AbstractSettingsDialog<SettingsType> {

    private LinkWithSettingsGenerator<SettingsType> linkWithSettingsGenerator;
    private Anchor shareAnchor;

    public SettingsDialogForLinkSharing(LinkWithSettingsGenerator<SettingsType> linkWithSettingsGenerator,
            ComponentLifecycle<SettingsType, ?> componentLifecycle, StringMessages stringMessages) {
        this(linkWithSettingsGenerator, componentLifecycle, componentLifecycle.createDefaultSettings(), stringMessages,
                true, null);
    }

    private SettingsDialogForLinkSharing(LinkWithSettingsGenerator<SettingsType> linkWithSettingsGenerator,
            ComponentLifecycle<SettingsType, ?> componentLifecycle, SettingsType settings,
            StringMessages stringMessages, boolean animationEnabled, DialogCallback<SettingsType> callback) {
        super(componentLifecycle.getLocalizedShortName(), componentLifecycle.getSettingsDialogComponent(settings),
                stringMessages, animationEnabled, callback);
        this.linkWithSettingsGenerator = linkWithSettingsGenerator;
        shareAnchor = new Anchor(stringMessages.sharedSettingsLink());
        shareAnchor.getElement().getStyle().setMargin(3, Unit.PX);
        shareAnchor.ensureDebugId("ShareAnchor");
        shareAnchor.setTarget("_blank");
        getLeftButtonPannel().add(shareAnchor);
        // // initial link
        // onChange(settings);
    }

    @Override
    protected void onChange(SettingsType result) {
        String link = linkWithSettingsGenerator.createUrl(getResult());
        shareAnchor.setHref(link);
    }
}
