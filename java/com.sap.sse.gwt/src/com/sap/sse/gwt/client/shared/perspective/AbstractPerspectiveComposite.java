package com.sap.sse.gwt.client.shared.perspective;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.AbstractCompositeComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * An abstract base class for a {@link Perspective} with a widget.
 * 
 * @author Frank Mittag
 *
 */
public abstract class AbstractPerspectiveComposite<PL extends PerspectiveLifecycle<PS, ?, ?>, PS extends Settings>
     extends AbstractCompositeComponent<PerspectiveCompositeSettings<PS>> implements Perspective<PS> {

    private final PL perspectiveLifecycle;
    private PS perspectiveOwnSettings;

    protected final List<Component<?>> components;
    
    protected SettingsDialogComponent<PS> getPerspectiveSettingsDialogComponent(PS perspectiveSettings) {
        return perspectiveLifecycle.getPerspectiveOwnSettingsDialogComponent(perspectiveSettings);
    }

    public AbstractPerspectiveComposite(PL perspectiveLifecycle, PS perspectiveSettings) {
        this.components = new ArrayList<>();
        this.perspectiveLifecycle = perspectiveLifecycle;
        this.perspectiveOwnSettings = perspectiveSettings;
    }

    @Override
    public PerspectiveCompositeSettings<PS> getSettings() {
        Map<Serializable, Settings> settingsPerComponent = new HashMap<>();
        for (Component<?> c : getComponents()) {
            if (c.hasSettings()) {
                settingsPerComponent.put(c.getId(), c.getSettings());
            }
        }
        return new PerspectiveCompositeSettings<>(perspectiveOwnSettings, settingsPerComponent);
    }

    @Override
    public void updateSettings(PerspectiveCompositeSettings<PS> newSettings) {
        for (Entry<Serializable, Settings> componentAndSettings : newSettings.getSettingsPerComponentId().entrySet()) {
            updateSettings(componentAndSettings);
        }
        this.perspectiveOwnSettings = newSettings.getPerspectiveOwnSettings();
    }

    private <S extends Settings> void updateSettings(Entry<Serializable, S> componentIdAndSettings) {
        @SuppressWarnings("unchecked")
        Component<S> component = (Component<S>) findComponentById(componentIdAndSettings.getKey());
        if (component != null) {
            component.updateSettings(componentIdAndSettings.getValue());
        }
    }

    private Component<?> findComponentById(Serializable componentId) {
        for (Component<?> component : getComponents()) {
            if (component.getId().equals(componentId)) {
                return component;
            }
        }
        return null;
    }

    @Override
    public Widget getEntryWidget() {
        return this.asWidget();
    }

    public List<Component<?>> getComponents() {
        return components;
    }
    
    @Override
    public String getLocalizedShortName() {
        return perspectiveLifecycle.getLocalizedShortName();
    }

    public boolean hasSettings() {
        return perspectiveLifecycle.hasSettings();
    }

    @Override
    public SettingsDialogComponent<PerspectiveCompositeSettings<PS>> getSettingsDialogComponent() {
        return new PerspectiveCompositeTabbedSettingsDialogComponent<>(this);
    }

    protected PS getPerspectiveSettings() {
        return perspectiveOwnSettings;
    }
    
    protected PL getPerspectiveLifecycle() {
        return perspectiveLifecycle;
    }

}
