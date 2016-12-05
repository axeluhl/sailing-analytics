package com.sap.sse.gwt.client.shared.perspective;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.AbstractCompositeComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

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
    private final PerspectiveLifecycleWithAllSettings<PL, PS> perspectiveLifecycleWithAllSettings;

    private final Map<String, Component<? extends Settings>> childComponents = new HashMap<>();
    
    protected void addChildComponent(Component<? extends Settings> childComponent) {
        childComponents.put(childComponent.getId(), childComponent);
        childComponent.getComponentTreeNodeInfo().setParentPerspective(this);
        childComponent.getComponentTreeNodeInfo().setComponentContext(getComponentTreeNodeInfo().getComponentContext());
    }
    
    @Override
    public Collection<Component<? extends Settings>> getComponents() {
        return childComponents.values();
    }
    
    protected SettingsDialogComponent<PS> getPerspectiveSettingsDialogComponent(PS perspectiveSettings) {
        return perspectiveLifecycle.getPerspectiveOwnSettingsDialogComponent(perspectiveSettings);
    }

    public AbstractPerspectiveComposite(AbstractComponentContext<PL, PS> componentContext, PerspectiveLifecycleWithAllSettings<PL, PS> perspectiveLifecycleWithAllSettings) {
        this.perspectiveLifecycle = perspectiveLifecycleWithAllSettings.getPerspectiveLifecycle();
        this.perspectiveOwnSettings = perspectiveLifecycleWithAllSettings.getPerspectiveSettings();
        this.perspectiveLifecycleWithAllSettings = perspectiveLifecycleWithAllSettings;
        getComponentTreeNodeInfo().setComponentContext(componentContext);
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
    public SettingsDialogComponent<PerspectiveCompositeSettings<PS>> getSettingsDialogComponent() {
        return new PerspectiveCompositeTabbedSettingsDialogComponent<>(this);
    }

    protected PS getPerspectiveSettings() {
        return perspectiveOwnSettings;
    }
    
    protected PL getPerspectiveLifecycle() {
        return perspectiveLifecycle;
    }
    
    public <C extends ComponentLifecycle<S,?>, S extends Settings> S findComponentSettingsByLifecycle(C componentLifecycle) {
        return perspectiveLifecycleWithAllSettings.findComponentSettingsByLifecycle(componentLifecycle);
    }
    
    @Override
    public PerspectiveLifecycleWithAllSettings<PL, PS> getPerspectiveLifecycleWithAllSettings() {
        return perspectiveLifecycleWithAllSettings;
    }

}
