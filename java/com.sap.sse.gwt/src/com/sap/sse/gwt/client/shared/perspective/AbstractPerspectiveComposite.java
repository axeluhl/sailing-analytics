package com.sap.sse.gwt.client.shared.perspective;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentAndSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * An abstract base class for perspectives with a widget.
 * 
 * @author Frank Mittag
 *
 */
public abstract class AbstractPerspectiveComposite<PL extends PerspectiveLifecycle<PS, ?, ?>, PS extends Settings>
     extends Composite implements Perspective<PS> {

    private final PL perspectiveLifecycle;
    private PS perspectiveSettings;

    protected final List<Component<?>> components;
    
    protected SettingsDialogComponent<PS> getPerspectiveSettingsDialogComponent(PS perspectiveSettings) {
        return perspectiveLifecycle.getPerspectiveSettingsDialogComponent(perspectiveSettings);
    }

    public AbstractPerspectiveComposite(PL perspectiveLifecycle, PS perspectiveSettings) {
        this.components = new ArrayList<>();
        this.perspectiveLifecycle = perspectiveLifecycle;
        this.perspectiveSettings = perspectiveSettings;
    }

    @Override
    public PerspectiveCompositeSettings<PS> getSettings() {
        List<ComponentAndSettings<?>> settingsPerComponent = new ArrayList<>();
        for (Component<?> c: getComponents()) {
            if (c.hasSettings()) {
                settingsPerComponent.add(createComponentAndSettings(c));
            }
        }
        PerspectiveAndSettings<PS> perspectiveAndSettings = new PerspectiveAndSettings<>(this, perspectiveSettings);         
        return new PerspectiveCompositeSettings<>(perspectiveAndSettings, settingsPerComponent);
    }

    private <S extends Settings> ComponentAndSettings<S> createComponentAndSettings(Component<S> c) {
        return new ComponentAndSettings<S>(c, c.getSettings());
    }

    @Override
    public void updateSettings(PerspectiveCompositeSettings<PS> newSettings) {
        for (ComponentAndSettings<?> componentAndSettings : newSettings.getSettingsPerComponent()) {
            updateSettings(componentAndSettings);
        }
        this.perspectiveSettings = newSettings.getPerspectiveSettings();
    }

    private <S extends Settings> void updateSettings(ComponentAndSettings<S> componentAndSettings) {
        Component<S> component = componentAndSettings.getComponent();
        component.updateSettings(componentAndSettings.getSettings());
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
        return new PerspectiveCompositeTabbedSettingsDialogComponent<>(getSettings());
    }

    protected PS getPerspectiveSettings() {
        return perspectiveSettings;
    }
    
    protected PL getPerspectiveLifecycle() {
        return perspectiveLifecycle;
    }

}
