package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.StringMessages;
import com.sap.sse.gwt.client.shared.perspective.ComponentContext;

public class SettingsDialogForLinkSharing<SettingsType extends Settings> extends SettingsDialog<SettingsType> {
    
    private SettingsDialogForLinkSharing(ComponentContext<?, SettingsType> componentContext, ComponentLifecycle<SettingsType, ?> componentLifecycle,
            StringMessages stringMessages, boolean animationEnabled) {
        this(componentContext, componentLifecycle, componentLifecycle.createDefaultSettings(), stringMessages, animationEnabled, null);
    }
    
    private SettingsDialogForLinkSharing(final ComponentContext<?, SettingsType> componentContext, ComponentLifecycle<SettingsType, ?> componentLifecycle, SettingsType settings,
            StringMessages stringMessages, boolean animationEnabled, DialogCallback<SettingsType> callback) {
        super(componentLifecycle, settings, stringMessages, animationEnabled, callback);
        Button shareButton = new Button(/*stringMessages.save()*/ "TODO share");
        shareButton.getElement().getStyle().setMargin(3, Unit.PX);
        shareButton.ensureDebugId("ShareButton");
        getLeftButtonPannel().add(shareButton);
        shareButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                Window.alert(componentContext.createUrlForSharingFromCurrentLocation(getResult(), /* TODO */ null).buildString());
            }
        });
    }
}
