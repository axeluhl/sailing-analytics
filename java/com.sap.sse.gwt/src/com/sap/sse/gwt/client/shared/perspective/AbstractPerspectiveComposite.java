package com.sap.sse.gwt.client.shared.perspective;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.AbstractCompositeComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentIdAndSettings;
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
    private PS perspectiveSettings;

    protected final List<Component<?>> components;
    
    protected SettingsDialogComponent<PS> getPerspectiveSettingsDialogComponent(PS perspectiveSettings) {
        return perspectiveLifecycle.getPerspectiveOwnSettingsDialogComponent(perspectiveSettings);
    }

    public AbstractPerspectiveComposite(PL perspectiveLifecycle, PS perspectiveSettings) {
        this.components = new ArrayList<>();
        this.perspectiveLifecycle = perspectiveLifecycle;
        this.perspectiveSettings = perspectiveSettings;
    }

    @Override
    public PerspectiveCompositeSettings<PS> getSettings() {
        List<ComponentIdAndSettings<?>> settingsPerComponent = new ArrayList<>();
        for (Component<?> c: getComponents()) {
            if (c.hasSettings()) {
                settingsPerComponent.add(createComponentAndSettings(c));
            }
        }
        PerspectiveIdAndSettings<PS> perspectiveAndSettings = new PerspectiveIdAndSettings<>(perspectiveSettings);         
        return new PerspectiveCompositeSettings<>(perspectiveAndSettings, settingsPerComponent);
    }

    private <S extends Settings> ComponentIdAndSettings<S> createComponentAndSettings(Component<S> c) {
        return new ComponentIdAndSettings<S>(c.getId(), c.getSettings());
    }

    @Override
    public void updateSettings(PerspectiveCompositeSettings<PS> newSettings) {
        for (ComponentIdAndSettings<?> componentAndSettings : newSettings.getSettingsPerComponentId()) {
            updateSettings(componentAndSettings);
        }
        this.perspectiveSettings = newSettings.getPerspectiveSettings();
    }

    private <S extends Settings> void updateSettings(ComponentIdAndSettings<S> componentAndSettings) {
        @SuppressWarnings("unchecked")
        Component<S> component = (Component<S>) findComponentById(componentAndSettings.getComponentId());
        if (component != null) {
            component.updateSettings(componentAndSettings.getSettings());
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
        return perspectiveSettings;
    }
    
    protected PL getPerspectiveLifecycle() {
        return perspectiveLifecycle;
    }

}
