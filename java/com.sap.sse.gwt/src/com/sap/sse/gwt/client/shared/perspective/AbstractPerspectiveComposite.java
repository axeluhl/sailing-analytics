package com.sap.sse.gwt.client.shared.perspective;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.Window;
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

    private final Map<Serializable, Component<? extends Settings>> childComponents = new HashMap<>();
    
    protected void addChildComponent(Component<? extends Settings> childComponent) {
        childComponents.put(childComponent.getId(), childComponent);
        childComponent.getComponentTreeNodeInfo().setParentPerspective(this);
    }
    
    @Override
    public void childComponentDefaultSettingsChanged(Component<? extends Settings> childComponent, Settings childSettings) {
        Map<Serializable, Settings> originalSettingsPerComponent = perspectiveLifecycleWithAllSettings.getComponentSettings().getSettingsPerComponentId();
        Map<Serializable, Settings> newSettingsPerComponent = new HashMap<>();
        for (Entry<Serializable, Settings> entry : originalSettingsPerComponent.entrySet()) {
            Serializable componentId = entry.getKey();
            if(childComponent.getId().equals(componentId)) {
                newSettingsPerComponent.put(childComponent.getId(), childSettings);
            } else {
                newSettingsPerComponent.put(componentId, entry.getValue());
            }
        }
        
        PerspectiveCompositeSettings<PS> allSettings = new PerspectiveCompositeSettings<>(perspectiveOwnSettings, newSettingsPerComponent);
        perspectiveLifecycleWithAllSettings.setAllSettings(allSettings);
        
        Perspective<? extends Settings> parentPerspective = getComponentTreeNodeInfo().getParentPerspective();
        if(parentPerspective != null) {
            parentPerspective.childComponentDefaultSettingsChanged(childComponent, childSettings);
        } else {
            storeNewDefaultSettings();
        }
    }
    
    private void storeNewDefaultSettings() {
        // TODO store on backend and on localstorage
        Window.alert("Default Settings got saved by perspective");
    }

    @Override
    public Collection<Component<? extends Settings>> getComponents() {
        return childComponents.values();
    }
    
    protected SettingsDialogComponent<PS> getPerspectiveSettingsDialogComponent(PS perspectiveSettings) {
        return perspectiveLifecycle.getPerspectiveOwnSettingsDialogComponent(perspectiveSettings);
    }

    public AbstractPerspectiveComposite(PerspectiveLifecycleWithAllSettings<PL, PS> perspectiveLifecycleWithAllSettings) {
        this.perspectiveLifecycle = perspectiveLifecycleWithAllSettings.getPerspectiveLifecycle();
        this.perspectiveOwnSettings = perspectiveLifecycleWithAllSettings.getPerspectiveSettings();
        this.perspectiveLifecycleWithAllSettings = perspectiveLifecycleWithAllSettings;
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

}
