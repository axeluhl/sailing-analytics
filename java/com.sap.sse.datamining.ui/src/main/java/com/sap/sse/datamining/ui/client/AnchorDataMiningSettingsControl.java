package com.sap.sse.datamining.ui.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentResources;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;
import com.sap.sse.gwt.client.shared.components.CompositeTabbedSettingsDialogComponent;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class AnchorDataMiningSettingsControl extends AbstractDataMiningComponent<CompositeSettings>
        implements DataMiningSettingsControl {
    public static final ComponentResources resources = GWT.create(ComponentResources.class);

    private final Collection<Component<?>> components;
    private final Anchor anchor;

    public AnchorDataMiningSettingsControl(Component<?> parent, ComponentContext<?> context) {
        super(parent, context);
        components = new LinkedHashSet<>();

        anchor = new Anchor(AbstractImagePrototype.create(resources.darkSettingsIcon()).getSafeHtml());
        anchor.addStyleName("settingsAnchor");
        anchor.setTitle(getDataMiningStringMessages().dataMiningSettings());
        anchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new SettingsDialog<>(AnchorDataMiningSettingsControl.this, getDataMiningStringMessages()).show();
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
        return getDataMiningStringMessages().dataMiningSettings();
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
    // FIXME why does this not use a perspective?
    public SettingsDialogComponent<CompositeSettings> getSettingsDialogComponent(CompositeSettings settings) {
        return new CompositeTabbedSettingsDialogComponent(components);
    }

    @Override
    public void updateSettings(CompositeSettings newSettings) {
        for (Entry<String, Settings> componentAndSettings : newSettings.getSettingsPerComponentId().entrySet()) {
            updateSettings(componentAndSettings);
        }
    }

    @Override
    public CompositeSettings getSettings() {
        Map<String, Settings> settings = new HashMap<>();
        for (Component<?> component : components) {
            settings.put(component.getId(), component.hasSettings() ? component.getSettings() : null);
        }
        return new CompositeSettings(settings);
    }

    private <S extends Settings> void updateSettings(Entry<String, S> componentIdAndSettings) {
        // we assume that the component to which the ID resolves matches with the settings type provided
        @SuppressWarnings("unchecked")
        Component<S> component = (Component<S>) findComponentById(componentIdAndSettings.getKey());
        if (component != null) {
            final S settings = componentIdAndSettings.getValue();
            component.updateSettings(settings);
        }
    }

    private Component<?> findComponentById(String componentId) {
        for (Component<?> component : components) {
            if (component.getId().equals(componentId)) {
                return component;
            }
        }
        return null;
    }

    @Override
    public String getDependentCssClassName() {
        return "AnchorDataMiningSettingsControl";
    }

    @Override
    public String getId() {
        return "AnchorDataMiningSettingsControl";
    }

}
