package com.sap.sse.gwt.client.shared.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycle;

public class ComponentUtils {

    /**
     * Determines the lifecycle from the provided current lifecycle tree which corresponds to the provided path.
     * 
     * @param path
     *            The path of the component which lifecycle should be determined
     * @param current
     *            The root lifecycle from that the desired lifecycle is reachable by the provided path
     * @return The lifecycle which was determined for the provided path starting from the provided root lifecycle
     * @throws IllegalStateException
     *             When the path cannot be resolved from the provided root lifecycle
     */
    @SuppressWarnings("unchecked")
    public static <CS extends Settings> ComponentLifecycle<CS> determineLifecycle(Iterable<String> path,
            ComponentLifecycle<? extends Settings> current) {
        final Iterator<String> pathIterator = path.iterator();
        while (current instanceof PerspectiveLifecycle<?> && pathIterator.hasNext()) {
            String last = pathIterator.next();
            current = ((PerspectiveLifecycle<?>) current).getLifecycleForId(last);
        }
        if (pathIterator.hasNext() || current == null) {
            throw new IllegalStateException("Settings path is not finished, but no perspective at current level");
        }
        return (ComponentLifecycle<CS>) current;
    }

    @SuppressWarnings("unchecked")
    public static <CS extends Settings> CS determineComponentSettingsFromPerspectiveSettings(Iterable<String> path,
            Settings current) {
        final Iterator<String> pathIterator = path.iterator();
        while (current instanceof PerspectiveCompositeSettings<?> && pathIterator.hasNext()) {
            String last = pathIterator.next();
            current = ((PerspectiveCompositeSettings<?>) current).findSettingsByComponentId(last);
        }
        if (pathIterator.hasNext() || current == null) {
            throw new IllegalStateException("Settings path is not finished, but no settings at current level");
        }
        return (CS) current;
    }

    @SuppressWarnings("unchecked")
    public static <CS extends Settings> CS patchSettingsTree(List<String> path, Settings newSettings, CS settingsTree) {

        List<PerspectiveCompositeSettings<?>> perspectiveSettingsPath = new ArrayList<>();

        if (path.isEmpty() && !(settingsTree instanceof PerspectiveCompositeSettings<?>)) {
            // top level settings belong to a component, not perspective
            return (CS) newSettings;
        }

        if (!(settingsTree instanceof PerspectiveCompositeSettings<?>)) {
            throw new IllegalStateException(
                    "Settings tree does not belong to a perspective, but due to a not empty path it has to");
        }

        Settings current = settingsTree;
        PerspectiveCompositeSettings<?> perspectiveSettings = (PerspectiveCompositeSettings<?>) settingsTree;

        String childComponentId = null;
        while (current instanceof PerspectiveCompositeSettings<?> && !path.isEmpty()) {
            childComponentId = path.remove(path.size() - 1);
            perspectiveSettings = (PerspectiveCompositeSettings<?>) current;
            perspectiveSettingsPath.add(perspectiveSettings);
            current = perspectiveSettings.findSettingsByComponentId(childComponentId);
        }
        if (!path.isEmpty() || current == null) {
            throw new IllegalStateException("Settings path is not finished, but no settings at current level");
        }

        if (current instanceof PerspectiveCompositeSettings<?>) {
            Settings perspectiveOwnSettings = ((PerspectiveCompositeSettings<?>) current).getPerspectiveOwnSettings();
            newSettings = new PerspectiveCompositeSettings<Settings>(perspectiveOwnSettings,
                    perspectiveSettings.getSettingsPerComponentId());
        }

        while (!perspectiveSettingsPath.isEmpty()) {
            PerspectiveCompositeSettings<?> parent = perspectiveSettingsPath.remove(perspectiveSettingsPath.size() - 1);
            Map<String, Settings> originalSettingsPerComponent = parent.getSettingsPerComponentId();
            Map<String, Settings> newSettingsPerComponent = new HashMap<>();
            for (Entry<String, Settings> entry : originalSettingsPerComponent.entrySet()) {
                String componentId = entry.getKey();
                if (childComponentId.equals(componentId)) {
                    newSettingsPerComponent.put(childComponentId, newSettings);
                } else {
                    newSettingsPerComponent.put(componentId, entry.getValue());
                }
            }

            newSettings = new PerspectiveCompositeSettings<Settings>(parent.getPerspectiveOwnSettings(),
                    newSettingsPerComponent);
        }

        return (CS) newSettings;
    }

}
