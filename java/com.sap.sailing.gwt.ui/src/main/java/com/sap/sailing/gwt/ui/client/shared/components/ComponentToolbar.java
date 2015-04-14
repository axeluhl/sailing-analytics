package com.sap.sailing.gwt.ui.client.shared.components;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sap.sailing.domain.common.settings.Settings;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class ComponentToolbar<SettingsType extends Settings> extends HorizontalPanel {
    
    private static ComponentResources resources = GWT.create(ComponentResources.class);

    private final Component<SettingsType> component;
    final StringMessages stringMessages;
    
    public ComponentToolbar(final Component<SettingsType> component, final StringMessages stringMessages) {
        super();
        
        this.setSpacing(10);
        this.component = component;
        this.stringMessages = stringMessages;
    }

    public void addSettingsButton() {
        if(component.hasSettings()) {
            ImageResource settingsImage = resources.darkSettingsIcon();
            Anchor showConfigAnchor = new Anchor(AbstractImagePrototype.create(settingsImage).getSafeHtml());
            showConfigAnchor.setTitle(stringMessages.configuration());
            showConfigAnchor.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    new SettingsDialog<SettingsType>(component, stringMessages).show();
                }
            });
    
            this.add(showConfigAnchor);
        }
   }

}


