package com.sap.sse.gwt.client.shared.perspective;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentIdAndSettings;
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
        private final ComponentIdAndSettings<S> componentIdAndSettings;
        private final SettingsDialogComponent<S> settingsDialog;
        private final String componentName;

        public ComponentIdWithSettingsAndDialogComponent(String componentName, ComponentIdAndSettings<S> componentIdAndSettings, SettingsDialogComponent<S> settingsDialog) {
            this.componentName = componentName;
            this.componentIdAndSettings = componentIdAndSettings;
            this.settingsDialog = settingsDialog;
        }
        
        public ComponentIdAndSettings<S> getComponentIdAndSettings() {
            return componentIdAndSettings;
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
        private final PerspectiveIdAndSettings<PS> perspectiveIdAndSettings;
        private final SettingsDialogComponent<PS> settingsDialog;
        private final String perspectiveName;

        public PerspectiveIdWithSettingsAndDialogComponent(String perspectiveName, PerspectiveIdAndSettings<PS> perspectiveIdAndSettings, 
                SettingsDialogComponent<PS> settingsDialog) {
            this.perspectiveName = perspectiveName;
            this.perspectiveIdAndSettings = perspectiveIdAndSettings;
            this.settingsDialog = settingsDialog;
        }
        
        public PerspectiveIdAndSettings<PS> getPerspectiveIdAndSettings() {
            return perspectiveIdAndSettings;
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
        for (Component<?> component: perspective.getComponents()) {
            componentIdsAndDialogComponents.add(createComponentAndDialogComponent(component));
        }
        perspectiveIdsAndSettingsDialog = createPerspectiveAndDialogComponent(perspective);
    }

    public PerspectiveCompositeTabbedSettingsDialogComponent(PerspectiveLifecycleWithAllSettings<?, PS> perspectiveLifecycleWithAllSettings) {
        this.componentIdsAndDialogComponents = new ArrayList<>();
        CompositeSettings componentSettings = perspectiveLifecycleWithAllSettings.getComponentSettings();
        for(ComponentLifecycle<?,?> componentLifecycle: perspectiveLifecycleWithAllSettings.getPerspectiveLifecycle().getComponentLifecycles()) {
            if (componentLifecycle.hasSettings()) {
                ComponentIdAndSettings<?> settingsOfComponent = componentSettings.findComponentAndSettingsByLifecycle(componentLifecycle);
                this.componentIdsAndDialogComponents.add(createComponentIdAndDialogComponent(componentLifecycle, settingsOfComponent));
            }
        }
        perspectiveIdsAndSettingsDialog = createPerspectiveIdAndDialogComponent(perspectiveLifecycleWithAllSettings.getPerspectiveLifecycle(),
                perspectiveLifecycleWithAllSettings.getAllSettings().getPerspectiveAndSettings()); 
    }
    
    private <S extends Settings> ComponentIdWithSettingsAndDialogComponent<?> createComponentIdAndDialogComponent(ComponentLifecycle<?,?> componentLifecycle, ComponentIdAndSettings<S> componentLifecycleAndSettings) {
        S settings = componentLifecycleAndSettings.getSettings();
        @SuppressWarnings("unchecked")
        ComponentLifecycle<S,?> typedComponentLifecycle =  (ComponentLifecycle<S, ?>) componentLifecycle;
        return new ComponentIdWithSettingsAndDialogComponent<S>(componentLifecycle.getLocalizedShortName(), componentLifecycleAndSettings,
                typedComponentLifecycle.getSettingsDialogComponent(settings));
    }

    /**
     * Creates the dialog for the settings of perspective itself 
     * @param perspectiveLifecycleAndSettings
     */
    private PerspectiveIdWithSettingsAndDialogComponent<PS> createPerspectiveIdAndDialogComponent(PerspectiveLifecycle<PS,?,?> perspectiveLifecycle, PerspectiveIdAndSettings<PS> perspectiveIdAndSettings) {
        PS settings = perspectiveIdAndSettings.getSettings();
        return new PerspectiveIdWithSettingsAndDialogComponent<PS>(perspectiveLifecycle.getLocalizedShortName(), perspectiveIdAndSettings, perspectiveLifecycle.getPerspectiveOwnSettingsDialogComponent(settings));
    }
    
    private PerspectiveIdWithSettingsAndDialogComponent<PS> createPerspectiveAndDialogComponent(Perspective<PS> perspective) {
        PerspectiveIdWithSettingsAndDialogComponent<PS> result = null;
        if(perspective.hasPerspectiveOwnSettings()) {
            result = new PerspectiveIdWithSettingsAndDialogComponent<PS>(perspective.getLocalizedShortName(),
                    new PerspectiveIdAndSettings<PS>(perspective.getId(), perspective.getSettings().getPerspectiveSettings()),
                    perspective.getPerspectiveOwnSettingsDialogComponent());
        }
        return result;
    }

    private <SettingsType extends Settings> ComponentIdWithSettingsAndDialogComponent<SettingsType> createComponentAndDialogComponent(Component<SettingsType> component) {
        return new ComponentIdWithSettingsAndDialogComponent<SettingsType>(component.getLocalizedShortName(),
                new ComponentIdAndSettings<SettingsType>(component.getId(), component.getSettings()),
                component.getSettingsDialogComponent());
    }
    
    private <C extends Component<S>, S extends Settings> ComponentIdAndSettings<S> getComponentAndSettings(ComponentIdWithSettingsAndDialogComponent<S> componentAndDialog) {
        return new ComponentIdAndSettings<S>(componentAndDialog.componentIdAndSettings.getComponentId(), componentAndDialog.getSettingsDialog().getResult());
    }

    private PerspectiveIdAndSettings<PS> getPerspectiveAndSettings(PerspectiveIdWithSettingsAndDialogComponent<PS> perspectiveAndDialog) {
        return new PerspectiveIdAndSettings<PS>(perspectiveAndDialog.perspectiveIdAndSettings.getPerspectiveId(), perspectiveAndDialog.getSettingsDialog().getResult());
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
        PerspectiveIdAndSettings<PS> perspectiveLifeycycleAndSettings = perspectiveIdsAndSettingsDialog != null ? getPerspectiveAndSettings(perspectiveIdsAndSettingsDialog) : null;
        List<ComponentIdAndSettings<?>> settings = new ArrayList<>();
        for (ComponentIdWithSettingsAndDialogComponent<?> component : componentIdsAndDialogComponents) {
            settings.add(getComponentAndSettings(component));
        }
        return new PerspectiveCompositeSettings<PS>(perspectiveLifeycycleAndSettings, settings);
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
