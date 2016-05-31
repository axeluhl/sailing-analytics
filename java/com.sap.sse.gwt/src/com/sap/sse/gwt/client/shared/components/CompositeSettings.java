package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.client.shared.perspective.Perspective;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

/**
 * Groups settings for multiple {@link Component}s. This can be of particular interest when working with
 * {@link Perspective}s and the perspective's {@link PerspectiveCompositeSettings composite settings}.
 */
public class CompositeSettings extends AbstractSettings {
    private final Iterable<ComponentIdAndSettings<?>> settingsPerComponentId;

    public CompositeSettings(Iterable<ComponentIdAndSettings<?>> settingsPerComponentId) {
        this.settingsPerComponentId = settingsPerComponentId;
    }

    public Iterable<ComponentIdAndSettings<?>> getSettingsPerComponentId() {
        return settingsPerComponentId;
    }
}
