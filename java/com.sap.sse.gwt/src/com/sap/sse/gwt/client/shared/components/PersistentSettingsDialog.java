package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.StringMessages;

public class PersistentSettingsDialog<SettingsType extends Settings> extends SettingsDialog<SettingsType> {
    
    public PersistentSettingsDialog(final Component<SettingsType> component, StringMessages stringMessages) {
        this(component, stringMessages, /* animationEnabled */ true);
    }

    public PersistentSettingsDialog(final Component<SettingsType> component, StringMessages stringMessages, boolean animationEnabled) {
        super(component, stringMessages, animationEnabled);
        initAdditionalButtons(component, stringMessages);
    }

    public PersistentSettingsDialog(final Component<SettingsType> component, StringMessages stringMessages, DialogCallback<SettingsType> callback) {
        super(component, stringMessages, callback);
        initAdditionalButtons(component, stringMessages);
    }

    private void initAdditionalButtons(final Component<SettingsType> component, StringMessages stringMessages) {
        Button makeDefaultButton = new Button(stringMessages.makeDefault());
        makeDefaultButton.getElement().getStyle().setMargin(3, Unit.PX);
        makeDefaultButton.ensureDebugId("MakeDefaultButton");
        getLeftButtonPannel().add(makeDefaultButton);
        makeDefaultButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                component.getComponentTreeNodeInfo().getComponentContext().makeSettingsDefault(component, getResult());
                //TODO i18n + use nice styled dialog
                Window.alert("Current settings have been set to default");
            }
        });
        
        Button restoreDefaultButton = new Button(stringMessages.restoreDefault());
        restoreDefaultButton.getElement().getStyle().setMargin(3, Unit.PX);
        restoreDefaultButton.ensureDebugId("RestoreDefaultButton");
        getLeftButtonPannel().add(restoreDefaultButton);
        restoreDefaultButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                //TODO apply settings - add update method?
//                SettingsType defaultSettings = component.getComponentTreeNodeInfo().getComponentContext().getDefaultSettingsForComponent(component);
            }
        });
    }

}
