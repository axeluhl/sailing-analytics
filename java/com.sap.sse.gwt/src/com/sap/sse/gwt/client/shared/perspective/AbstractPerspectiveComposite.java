package com.sap.sse.gwt.client.shared.perspective;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.AbstractCompositeComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

/**
 * An abstract base class for a {@link Perspective} with a widget.
 * 
 * @author Frank Mittag
 *
 */
public abstract class AbstractPerspectiveComposite<PL extends PerspectiveLifecycle<PS>, PS extends Settings>
     extends AbstractCompositeComponent<PerspectiveCompositeSettings<PS>> implements Perspective<PS> {

    private final PL perspectiveLifecycle;
    private PS perspectiveOwnSettings;

    private final Map<String, Component<? extends Settings>> childComponents = new HashMap<>();
    
    /**
     * Adds the provided component as a child to the maintained component tree of this perspective.
     * 
     * @param childComponent The component to be added as a child to this perspective
     */
    protected void addChildComponent(Component<? extends Settings> childComponent) {
        Component<? extends Settings> old = childComponents.put(childComponent.getId(), childComponent);
        if (old != null) {
            throw new IllegalStateException("Child with same id is already added " + childComponent.getId());
        }
    }
    
    @Override
    public Collection<Component<? extends Settings>> getComponents() {
        return childComponents.values();
    }
    
    protected SettingsDialogComponent<PS> getPerspectiveSettingsDialogComponent(PS perspectiveSettings) {
        return perspectiveLifecycle.getPerspectiveOwnSettingsDialogComponent(perspectiveSettings);
    }

    public AbstractPerspectiveComposite(Component<?> parent,
            ComponentContext<PerspectiveCompositeSettings<PS>> componentContext,
            PL lifecycle, PerspectiveCompositeSettings<PS> settings) {
        super(parent, componentContext);
        this.perspectiveLifecycle = lifecycle;
        this.perspectiveOwnSettings = settings.getPerspectiveOwnSettings();
    }

    @Override
    public PerspectiveCompositeSettings<PS> getSettings() {
        Map<String, Settings> settingsPerComponent = new HashMap<>();
        for (Component<?> c : getComponents()) {
            if (c.hasSettings()) {
                settingsPerComponent.put(c.getId(), c.getSettings());
            }
        }
        return new PerspectiveCompositeSettings<>(perspectiveOwnSettings, settingsPerComponent);
    }

    @Override
    public void updateSettings(PerspectiveCompositeSettings<PS> newSettings) {
        for (Entry<String, Settings> componentAndSettings : newSettings.getSettingsPerComponentId().entrySet()) {
            updateSettings(componentAndSettings);
        }
        this.perspectiveOwnSettings = newSettings.getPerspectiveOwnSettings();
    }

    private <S extends Settings> void updateSettings(Entry<String, S> componentIdAndSettings) {
        @SuppressWarnings("unchecked")
        Component<S> component = (Component<S>) findComponentById(componentIdAndSettings.getKey());
        if (component != null) {
            component.updateSettings(componentIdAndSettings.getValue());
        }
    }

    private Component<?> findComponentById(String componentId) {
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

    @Override
    public String getLocalizedShortName() {
        return perspectiveLifecycle.getLocalizedShortName();
    }

    public boolean hasSettings() {
        return perspectiveLifecycle.hasSettings();
    }

    @Override
    public SettingsDialogComponent<PerspectiveCompositeSettings<PS>> getSettingsDialogComponent(PerspectiveCompositeSettings<PS> settings) {
        return new PerspectiveCompositeTabbedSettingsDialogComponent<>(this);
    }

    protected PS getPerspectiveSettings() {
        return perspectiveOwnSettings;
    }
    
    protected PL getPerspectiveLifecycle() {
        return perspectiveLifecycle;
    }
    
}
