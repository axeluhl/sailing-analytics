package com.sap.sse.gwt.client.shared.perspective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

/**
 * An abstract base class for perspective lifecycle's. Subclasses need to add the {@link ComponentLifecycle} instances
 * for their child components to the {@link #componentLifecycles} structure in their constructor.
 * 
 * @param <PS>
 *            the perspective settings type
 * @author Frank Mittag
 *
 */
public abstract class AbstractPerspectiveLifecycle<PS extends Settings> implements PerspectiveLifecycle<PS> {
    final List<ComponentLifecycle<?>> componentLifecycles;
    
    public AbstractPerspectiveLifecycle() {
        componentLifecycles = new ArrayList<>();
    }
    
    protected final void addLifeCycle(ComponentLifecycle<?> cycle) {
        for (ComponentLifecycle<?> old : componentLifecycles) {
            if (old.getComponentId().equals(cycle.getComponentId())) {
                throw new IllegalStateException("LifeCycle with duplicate ID " + cycle.getComponentId());
            }
        }
        componentLifecycles.add(cycle);
    }

    public PerspectiveCompositeTabbedSettingsDialogComponent<PS> getSettingsDialogComponent(PerspectiveCompositeSettings<PS> settings) {
        return new PerspectiveCompositeTabbedSettingsDialogComponent<PS>(this, settings);
    }
    
    @Override
    public PerspectiveCompositeSettings<PS> createDefaultSettings() {
        PS perspectiveOwnSettings = createPerspectiveOwnDefaultSettings();
        return new PerspectiveCompositeSettings<>(perspectiveOwnSettings, createDefaultComponentIdsAndSettings());
    }
    
    public Map<String, Settings> createDefaultComponentIdsAndSettings() {
        Map<String, Settings> componentIdsAndSettings = new HashMap<>();
        for (ComponentLifecycle<?> componentLifecycle : componentLifecycles) {
            componentIdsAndSettings.put(componentLifecycle.getComponentId(), componentLifecycle.createDefaultSettings());
        }
        return componentIdsAndSettings;
    }

    public Iterable<ComponentLifecycle<?>> getComponentLifecycles() {
        return componentLifecycles;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <SS extends Settings> ComponentLifecycle<SS> getLifecycleForId(String id) {
        for (ComponentLifecycle<?> componentLifecycle : componentLifecycles) {
            if (id.equals(componentLifecycle.getComponentId())) {
                return (ComponentLifecycle<SS>) componentLifecycle;
            }
        }
        throw new IllegalStateException("No componentlivecycle for id " + id + " found");
    }

    @Override
    public final PerspectiveCompositeSettings<PS> extractDocumentSettings(PerspectiveCompositeSettings<PS> settings) {
        HashMap<String, Settings> settingsPerComponent = new HashMap<>();
        for (Entry<String, Settings> childSet : settings.getSettingsPerComponentId().entrySet()) {
            String childId = childSet.getKey();
            Settings childNewSettings = childSet.getValue();
            ComponentLifecycle<Settings> childLiveCycle = getLifecycleForId(childId);
            Settings extracted = childLiveCycle.extractDocumentSettings(childNewSettings);
            settingsPerComponent.put(childId, extracted);
        }

        PS ownGlobalSettings = extractOwnDocumentSettings(
                hasSettings() ? settings.getPerspectiveOwnSettings() : createPerspectiveOwnDefaultSettings());
        return new PerspectiveCompositeSettings<PS>(ownGlobalSettings, settingsPerComponent);
    }

    @Override
    public final PerspectiveCompositeSettings<PS> extractUserSettings(PerspectiveCompositeSettings<PS> settings) {
        HashMap<String, Settings> settingsPerComponent = new HashMap<>();
        for (Entry<String, Settings> childSet : settings.getSettingsPerComponentId().entrySet()) {
            String childId = childSet.getKey();
            Settings childNewSettings = childSet.getValue();
            ComponentLifecycle<Settings> childLiveCycle = getLifecycleForId(childId);
            Settings extracted = childLiveCycle.extractUserSettings(childNewSettings);
            settingsPerComponent.put(childId, extracted);
        }

        PS ownGlobalSettings = extractOwnUserSettings(
                hasSettings() ? settings.getPerspectiveOwnSettings() : createPerspectiveOwnDefaultSettings());
        return new PerspectiveCompositeSettings<PS>(ownGlobalSettings, settingsPerComponent);
    }

    protected abstract PS extractOwnUserSettings(PS settings);

    protected abstract PS extractOwnDocumentSettings(PS settings);
}
