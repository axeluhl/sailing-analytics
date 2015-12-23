package com.sap.sse.gwt.client.shared.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.CompositeSettings.ComponentLifecycleAndSettingsPair;

public class CompositeTabbedSettingsDialogComponent implements SettingsDialogComponent<CompositeSettings> {
    
    public class ComponentLifecycleAndDialogComponent<SettingsType extends Settings> extends Util.Pair<ComponentLifecycle<?, SettingsType, ?>, SettingsDialogComponent<SettingsType>> {
        private static final long serialVersionUID = -4342002423677523158L;

        public ComponentLifecycleAndDialogComponent(ComponentLifecycle<?, SettingsType, ?> a, SettingsDialogComponent<SettingsType> b) {
            super(a, b);
        }
    }
    
    private final Collection<ComponentLifecycleAndDialogComponent<?>> components;

    // TODO: Remove after lifecycle fix of datamining
    public CompositeTabbedSettingsDialogComponent(Iterable<Component<?>> components, String todo) {
        this.components = new ArrayList<>();
    }

    public CompositeTabbedSettingsDialogComponent(Iterable<ComponentLifecycle<?,?,?>> components) {
        this.components = new ArrayList<>();
        for (ComponentLifecycle<?,?,?> componentLifecycle : components) {
            if (componentLifecycle.hasSettings()) {
                this.components.add(createComponentLifecycleAndDialogComponent(componentLifecycle));
            }
        }
    }

    private <SettingsType extends Settings> ComponentLifecycleAndDialogComponent<SettingsType> createComponentLifecycleAndDialogComponent(ComponentLifecycle<?, SettingsType, ?> componentLifecycle) {
        SettingsType settings = componentLifecycle.getComponent() != null ? componentLifecycle.getComponent().getSettings() : componentLifecycle.createDefaultSettings();
        return new ComponentLifecycleAndDialogComponent<SettingsType>(componentLifecycle, componentLifecycle.getSettingsDialogComponent(settings));
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        TabPanel result = new TabPanel();
        for (ComponentLifecycleAndDialogComponent<?> component : components) {
            Widget w = component.getB().getAdditionalWidget((DataEntryDialog<?>) dialog);
            result.add(w, component.getA().getLocalizedShortName());
        }
        result.selectTab(0);
        return result;
    }

    @Override
    public CompositeSettings getResult() {
        Collection<ComponentLifecycleAndSettingsPair<?>> settings = new HashSet<>();
        for (ComponentLifecycleAndDialogComponent<?> component : components) {
            settings.add(getComponentAndSettings(component));
        }
        return new CompositeSettings(settings);
    }

    private <SettingsType extends Settings> ComponentLifecycleAndSettingsPair<SettingsType> getComponentAndSettings(ComponentLifecycleAndDialogComponent<SettingsType> component) {
        return new ComponentLifecycleAndSettingsPair<SettingsType>(component.getA(), component.getB().getResult());
    }

    @Override
    public Validator<CompositeSettings> getValidator() {
        return new CompositeValidator(components);
    }

    @Override
    public FocusWidget getFocusWidget() {
        for (ComponentLifecycleAndDialogComponent<?> component : components) {
            FocusWidget fw = component.getB().getFocusWidget();
            if (fw != null) {
                return fw;
            }
        }
        return null;
    }

}
