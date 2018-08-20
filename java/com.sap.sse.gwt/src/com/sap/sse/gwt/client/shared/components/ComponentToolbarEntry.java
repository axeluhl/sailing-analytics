package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.client.StringMessages;

/**
 * One entry in a component toolbar. Represents one {@link Component} that can be selected and whose
 * settings editing dialog can be reached from here.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <SettingsType>
 */
public class ComponentToolbarEntry<SettingsType extends AbstractSettings> extends HorizontalPanel {
    private static ComponentResources resources = GWT.create(ComponentResources.class);

    private final Component<SettingsType> component;
    
    private final StringMessages stringMessages;

    public ComponentToolbarEntry(final Component<SettingsType> component, final StringMessages stringMessages) {
        super();
        this.setSpacing(10);
        this.component = component;
        this.stringMessages = stringMessages;
    }

    public void addSettingsButton() {
        if (component.hasSettings()) {
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
