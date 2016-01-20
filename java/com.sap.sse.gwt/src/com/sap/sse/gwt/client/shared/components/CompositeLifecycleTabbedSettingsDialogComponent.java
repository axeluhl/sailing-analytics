package com.sap.sse.gwt.client.shared.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;

public class CompositeLifecycleTabbedSettingsDialogComponent implements SettingsDialogComponent<CompositeLifecycleSettings> {
    
    public class ComponentLifecycleWithSettingsAndDialogComponent<SettingsType extends Settings> {
        private ComponentLifecycleAndSettings<SettingsType> componentLifecycleAndSettings;
        private SettingsDialogComponent<SettingsType> dialogComponent;

        public ComponentLifecycleWithSettingsAndDialogComponent(ComponentLifecycleAndSettings<SettingsType> componentLifecycleAndSettings, SettingsDialogComponent<SettingsType> dialogComponent) {
            this.componentLifecycleAndSettings = componentLifecycleAndSettings;
            this.dialogComponent = dialogComponent;
        }
        
        public ComponentLifecycleAndSettings<SettingsType> getComponentLifecycleAndSettings() {
            return componentLifecycleAndSettings;
        }

        public SettingsDialogComponent<SettingsType> getDialogComponent() {
            return dialogComponent;
        }
    }
    
    private final Collection<ComponentLifecycleWithSettingsAndDialogComponent<?>> componentLifecycleAndDialogComponents;

    public CompositeLifecycleTabbedSettingsDialogComponent(CompositeLifecycleSettings componentLifecyclesSettings) {
        this.componentLifecycleAndDialogComponents = new ArrayList<>();
        for (ComponentLifecycleAndSettings<?> componentLifecycleAndSettings : componentLifecyclesSettings.getSettingsPerComponentLifecycle()) {
            if (componentLifecycleAndSettings.getComponentLifecycle().hasSettings()) {
                this.componentLifecycleAndDialogComponents.add(createComponentLifecycleAndDialogComponent(componentLifecycleAndSettings));
            }
        }
    }

    private <SettingsType extends Settings> ComponentLifecycleWithSettingsAndDialogComponent<SettingsType> createComponentLifecycleAndDialogComponent(ComponentLifecycleAndSettings<SettingsType> componentLifecycleAndSettings) {
        SettingsType settings = componentLifecycleAndSettings.getSettings();
        return new ComponentLifecycleWithSettingsAndDialogComponent<SettingsType>(componentLifecycleAndSettings, componentLifecycleAndSettings.getComponentLifecycle().getSettingsDialogComponent(settings));
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        TabPanel result = new TabPanel();
        for (ComponentLifecycleWithSettingsAndDialogComponent<?> component : componentLifecycleAndDialogComponents) {
            Widget w = component.getDialogComponent().getAdditionalWidget((DataEntryDialog<?>) dialog);
            result.add(w, component.getComponentLifecycleAndSettings().getComponentLifecycle().getLocalizedShortName());
        }
        result.selectTab(0);
        return result;
    }

    @Override
    public CompositeLifecycleSettings getResult() {
        Collection<ComponentLifecycleAndSettings<?>> settings = new HashSet<>();
        for (ComponentLifecycleWithSettingsAndDialogComponent<?> component : componentLifecycleAndDialogComponents) {
            settings.add(getComponentAndSettings(component));
        }
        return new CompositeLifecycleSettings(settings);
    }

    private <SettingsType extends Settings> ComponentLifecycleAndSettings<SettingsType> getComponentAndSettings(ComponentLifecycleWithSettingsAndDialogComponent<SettingsType> component) {
        return new ComponentLifecycleAndSettings<SettingsType>(component.getComponentLifecycleAndSettings().getComponentLifecycle(), component.getDialogComponent().getResult());
    }

    @Override
    public Validator<CompositeLifecycleSettings> getValidator() {
        return new CompositeLifecycleValidator(componentLifecycleAndDialogComponents);
    }

    @Override
    public FocusWidget getFocusWidget() {
        for (ComponentLifecycleWithSettingsAndDialogComponent<?> component : componentLifecycleAndDialogComponents) {
            FocusWidget fw = component.getDialogComponent().getFocusWidget();
            if (fw != null) {
                return fw;
            }
        }
        return null;
    }
}
