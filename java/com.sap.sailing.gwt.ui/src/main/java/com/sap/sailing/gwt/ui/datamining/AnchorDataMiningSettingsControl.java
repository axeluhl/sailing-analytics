package com.sap.sailing.gwt.ui.datamining;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentResources;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;
import com.sap.sse.gwt.client.shared.components.CompositeSettings.ComponentAndSettingsPair;
import com.sap.sse.gwt.client.shared.components.CompositeTabbedSettingsDialogComponent;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class AnchorDataMiningSettingsControl implements DataMiningSettingsControl {
    public static final ComponentResources resources = GWT.create(ComponentResources.class);
    
    private final StringMessages stringMessages;
    private final Set<Component<?>> components;
    private final Anchor anchor;
    
    public AnchorDataMiningSettingsControl(final StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        components = new HashSet<>();
        
        anchor = new Anchor(AbstractImagePrototype.create(resources.darkSettingsIcon()).getSafeHtml());
        anchor.addStyleName("settingsAnchor");
        anchor.setTitle(stringMessages.dataMiningSettings());
        anchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new SettingsDialog<>(AnchorDataMiningSettingsControl.this, stringMessages).show();
            }
        });
    }

    @Override
    public void addSettingsComponent(Component<?> component) {
        components.add(component);
    }

    @Override
    public void removeSettingsComponent(Component<?> component) {
        components.remove(component);
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.dataMiningSettings();
    }

    @Override
    public Widget getEntryWidget() {
        return anchor;
    }

    @Override
    public boolean isVisible() {
        return anchor.isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        anchor.setVisible(visibility);
    }

    @Override
    public boolean hasSettings() {
        for (Component<?> component : components) {
            if (component.hasSettings()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public SettingsDialogComponent<CompositeSettings> getSettingsDialogComponent() {
        return new CompositeTabbedSettingsDialogComponent(components);
    }

    @Override
    public void updateSettings(CompositeSettings newSettings) {
        for (CompositeSettings.ComponentAndSettingsPair<?> componentAndSettings : newSettings.getSettingsPerComponent()) {
            updateSettings(componentAndSettings);
        }
    }

    private <SettingsType extends Settings> void updateSettings(ComponentAndSettingsPair<SettingsType> componentAndSettings) {
        componentAndSettings.getA().updateSettings(componentAndSettings.getB());
    }

    @Override
    public String getDependentCssClassName() {
        return "AnchorDataMiningSettingsControl";
    }

}
