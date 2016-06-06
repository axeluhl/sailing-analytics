package com.sap.sse.gwt.client.shared.perspective;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * @author Frank Mittag
 *
 * @param <PS>
 *      the type of the perspective own settings
 **/
public class PerspectiveCompositeTabbedSettingsDialogComponent<PS extends Settings>
    implements SettingsDialogComponent<PerspectiveCompositeSettings<PS>> {
    
    public static class ComponentIdWithSettingsAndDialogComponent<S extends Settings> {
        private final Serializable componentId;
        private final S settings;
        private final SettingsDialogComponent<S> settingsDialog;
        private final String componentName;

        public ComponentIdWithSettingsAndDialogComponent(String componentName, Serializable componentId, S settings, SettingsDialogComponent<S> settingsDialog) {
            this.componentName = componentName;
            this.componentId = componentId;
            this.settings = settings;
            this.settingsDialog = settingsDialog;
        }
        
        public Serializable getComponentId() {
            return componentId;
        }

        public S getSettings() {
            return settings;
        }


        public SettingsDialogComponent<S> getSettingsDialog() {
            return settingsDialog;
        }

        public String getComponentName() {
            return componentName;
        }
    }

    /**
     * @param <PS>
     *          the {@link Perspective} own settings type
     */
    public static class PerspectiveIdWithSettingsAndDialogComponent<PS extends Settings> {
        private final PS perspectiveOwnSettings;
        private final SettingsDialogComponent<PS> settingsDialog;
        private final String perspectiveName;

        public PerspectiveIdWithSettingsAndDialogComponent(String perspectiveName, PS perspectiveOwnSettings, 
                SettingsDialogComponent<PS> settingsDialog) {
            this.perspectiveName = perspectiveName;
            this.perspectiveOwnSettings = perspectiveOwnSettings;
            this.settingsDialog = settingsDialog;
        }
        
        public PS getPerspectiveOwnSettings() {
            return perspectiveOwnSettings;
        }

        public SettingsDialogComponent<PS> getSettingsDialog() {
            return settingsDialog;
        }

        public String getPerspectiveName() {
            return perspectiveName;
        }
    }

    private final List<ComponentIdWithSettingsAndDialogComponent<?>> componentIdsAndDialogComponents;
    private final PerspectiveIdWithSettingsAndDialogComponent<PS> perspectiveIdsAndSettingsDialog;

    public PerspectiveCompositeTabbedSettingsDialogComponent(Perspective<PS> perspective) {
        this.componentIdsAndDialogComponents = new ArrayList<>();
        for (Component<?> component : perspective.getComponents()) {
            componentIdsAndDialogComponents.add(createComponentAndDialogComponent(component));
        }
        perspectiveIdsAndSettingsDialog = createPerspectiveAndDialogComponent(perspective);
    }

    /**
     * From the {@link PerspectiveLifecycle} {@link PerspectiveLifecycleWithAllSettings#getPerspectiveLifecycle()
     * referenced} by {@code perspectiveLifecycleWithAllSettings}, this constructor grabs all the
     * {@link ComponentLifecycle} objects for those nested components that have settings,
     * {@link ComponentLifecycle#getSettingsDialogComponent(Settings) fetches their settings dialog component} and maps
     * it in {@link #componentIdsAndDialogComponents}. Then, the
     * {@link #createPerspectiveIdAndDialogComponent(PerspectiveLifecycle, Settings)} method assembles them, together
     * with the perspective's own settings dialog component.
     */
    public PerspectiveCompositeTabbedSettingsDialogComponent(PerspectiveLifecycleWithAllSettings<?, PS> perspectiveLifecycleWithAllSettings) {
        this.componentIdsAndDialogComponents = new ArrayList<>();
        CompositeSettings componentSettings = perspectiveLifecycleWithAllSettings.getComponentSettings();
        for (ComponentLifecycle<?,?> componentLifecycle : perspectiveLifecycleWithAllSettings.getPerspectiveLifecycle().getComponentLifecycles()) {
            if (componentLifecycle.hasSettings()) {
                createComponentIdWithSettingsAndDialogComponent(componentSettings, componentLifecycle);
            }
        }
        perspectiveIdsAndSettingsDialog = createPerspectiveIdAndDialogComponent(perspectiveLifecycleWithAllSettings.getPerspectiveLifecycle(),
                perspectiveLifecycleWithAllSettings.getAllSettings().getPerspectiveOwnSettings()); 
    }

    private <S extends Settings, C extends Component<S>> void createComponentIdWithSettingsAndDialogComponent(CompositeSettings componentSettings,
            ComponentLifecycle<S, ?> componentLifecycle) {
        @SuppressWarnings("unchecked")
        S settingsOfComponent = (S) componentSettings.findSettingsByComponentId(componentLifecycle.getComponentId());
        this.componentIdsAndDialogComponents.add(new ComponentIdWithSettingsAndDialogComponent<S>(componentLifecycle.getLocalizedShortName(), componentLifecycle.getComponentId(), settingsOfComponent,
                componentLifecycle.getSettingsDialogComponent(settingsOfComponent)));
    }
    
    /**
     * Creates the dialog for the settings of perspective itself 
     * @param perspectiveLifecycleAndSettings
     */
    private PerspectiveIdWithSettingsAndDialogComponent<PS> createPerspectiveIdAndDialogComponent(PerspectiveLifecycle<PS,?,?> perspectiveLifecycle, PS perspectiveOwnSettings) {
        return new PerspectiveIdWithSettingsAndDialogComponent<PS>(perspectiveLifecycle.getLocalizedShortName(),
                perspectiveOwnSettings, perspectiveLifecycle.getPerspectiveOwnSettingsDialogComponent(perspectiveOwnSettings));
    }
    
    private PerspectiveIdWithSettingsAndDialogComponent<PS> createPerspectiveAndDialogComponent(Perspective<PS> perspective) {
        PerspectiveIdWithSettingsAndDialogComponent<PS> result = null;
        if(perspective.hasPerspectiveOwnSettings()) {
            result = new PerspectiveIdWithSettingsAndDialogComponent<PS>(perspective.getLocalizedShortName(),
                    perspective.getSettings().getPerspectiveOwnSettings(),
                    perspective.getPerspectiveOwnSettingsDialogComponent());
        }
        return result;
    }

    private <SettingsType extends Settings> ComponentIdWithSettingsAndDialogComponent<SettingsType> createComponentAndDialogComponent(Component<SettingsType> component) {
        return new ComponentIdWithSettingsAndDialogComponent<SettingsType>(component.getLocalizedShortName(), component.getId(), component.getSettings(),
                component.getSettingsDialogComponent());
    }
    
    private PS getPerspectiveOwnSettings(PerspectiveIdWithSettingsAndDialogComponent<PS> perspectiveAndDialog) {
        return perspectiveAndDialog.getSettingsDialog().getResult();
    }

    @Override
    public Widget getAdditionalWidget(final DataEntryDialog<?> dialog) {
        TabPanel result = new TabPanel();
        if(perspectiveIdsAndSettingsDialog != null) {
            Widget w = perspectiveIdsAndSettingsDialog.getSettingsDialog().getAdditionalWidget((DataEntryDialog<?>) dialog);
            result.add(w, perspectiveIdsAndSettingsDialog.getPerspectiveName());
        }
        for (ComponentIdWithSettingsAndDialogComponent<?> componentAndSettingsDialog : componentIdsAndDialogComponents) {
            Widget w = componentAndSettingsDialog.getSettingsDialog().getAdditionalWidget((DataEntryDialog<?>) dialog);
            result.add(w, componentAndSettingsDialog.getComponentName());
        }
        result.selectTab(0);
        result.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                dialog.center();
            }
          });
        return result;
    }

    @Override
    public PerspectiveCompositeSettings<PS> getResult() {
        PS perspectiveOwnSettings = perspectiveIdsAndSettingsDialog != null ? getPerspectiveOwnSettings(perspectiveIdsAndSettingsDialog) : null;
        Map<Serializable, Settings> settings = new HashMap<>();
        for (ComponentIdWithSettingsAndDialogComponent<?> componentAndDialog : componentIdsAndDialogComponents) {
            settings.put(componentAndDialog.getComponentId(), componentAndDialog.getSettingsDialog().getResult());
        }
        return new PerspectiveCompositeSettings<PS>(perspectiveOwnSettings, settings);
    }

    @Override
    public PerspectiveCompositeValidator<PS> getValidator() {
        return new PerspectiveCompositeValidator<PS>(perspectiveIdsAndSettingsDialog, componentIdsAndDialogComponents);
    }

    @Override
    public FocusWidget getFocusWidget() {
        for (ComponentIdWithSettingsAndDialogComponent<?> componentAndSettingsDialog : componentIdsAndDialogComponents) {
            FocusWidget fw = componentAndSettingsDialog.getSettingsDialog().getFocusWidget();
            if (fw != null) {
                return fw;
            }
        }
        return null;
    }

}
