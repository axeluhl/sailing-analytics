package com.sap.sse.gwt.client.shared.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;

public class CompositeLifecycleTabbedSettingsDialogComponent implements SettingsDialogComponent<CompositeLifecycleSettings> {
    
    public class ComponentLifecycleAndDialogComponent<SettingsType extends Settings> extends Util.Pair<ComponentLifecycle<?, SettingsType, ?, ?>, SettingsDialogComponent<SettingsType>> {
        private static final long serialVersionUID = -4342002423677523158L;

        public ComponentLifecycleAndDialogComponent(ComponentLifecycle<?, SettingsType, ?, ?> a, SettingsDialogComponent<SettingsType> b) {
            super(a, b);
        }
    }
    
    private final Collection<ComponentLifecycleAndDialogComponent<?>> componentLifecycleAndDialogComponents;

    public CompositeLifecycleTabbedSettingsDialogComponent(Iterable<ComponentLifecycle<?,?,?,?>> componentLifecycles) {
        this.componentLifecycleAndDialogComponents = new ArrayList<>();
        for (ComponentLifecycle<?,?,?,?> componentLifecycle : componentLifecycles) {
            if (componentLifecycle.hasSettings()) {
                this.componentLifecycleAndDialogComponents.add(createComponentLifecycleAndDialogComponent(componentLifecycle));
            }
        }
    }

    private <SettingsType extends Settings> ComponentLifecycleAndDialogComponent<SettingsType> createComponentLifecycleAndDialogComponent(ComponentLifecycle<?, SettingsType, ?, ?> componentLifecycle) {
        SettingsType settings = componentLifecycle.createDefaultSettings();
        return new ComponentLifecycleAndDialogComponent<SettingsType>(componentLifecycle, componentLifecycle.getSettingsDialogComponent(settings));
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        TabPanel result = new TabPanel();
        for (ComponentLifecycleAndDialogComponent<?> component : componentLifecycleAndDialogComponents) {
            Widget w = component.getB().getAdditionalWidget((DataEntryDialog<?>) dialog);
            result.add(w, component.getA().getLocalizedShortName());
        }
        result.selectTab(0);
        return result;
    }

    @Override
    public CompositeLifecycleSettings getResult() {
        Collection<ComponentLifecycleAndSettings<?>> settings = new HashSet<>();
        for (ComponentLifecycleAndDialogComponent<?> component : componentLifecycleAndDialogComponents) {
            settings.add(getComponentAndSettings(component));
        }
        return new CompositeLifecycleSettings(settings);
    }

    private <SettingsType extends Settings> ComponentLifecycleAndSettings<SettingsType> getComponentAndSettings(ComponentLifecycleAndDialogComponent<SettingsType> component) {
        return new ComponentLifecycleAndSettings<SettingsType>(component.getA(), component.getB().getResult());
    }

    @Override
    public Validator<CompositeLifecycleSettings> getValidator() {
        return new CompositeLifecycleValidator(componentLifecycleAndDialogComponents);
    }

    @Override
    public FocusWidget getFocusWidget() {
        for (ComponentLifecycleAndDialogComponent<?> component : componentLifecycleAndDialogComponents) {
            FocusWidget fw = component.getB().getFocusWidget();
            if (fw != null) {
                return fw;
            }
        }
        return null;
    }
}
