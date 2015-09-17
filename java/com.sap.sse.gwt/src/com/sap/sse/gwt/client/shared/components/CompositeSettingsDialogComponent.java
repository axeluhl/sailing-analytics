package com.sap.sse.gwt.client.shared.components;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.CompositeSettings.ComponentAndSettingsPair;

public class CompositeSettingsDialogComponent implements SettingsDialogComponent<CompositeSettings> {
    
    private final Collection<Component<?>> components;
    private final Map<Component<?>, SettingsDialogComponent<?>> dialogComponents;

    public CompositeSettingsDialogComponent(Iterable<Component<?>> components) {
        this.components = new HashSet<>();
        for (Component<?> component : components) {
            if (component.hasSettings()) {
                this.components.add(component);
            }
        }
        dialogComponents = new HashMap<>();
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        TabPanel result = new TabPanel();
        for (Component<?> component : components) {
            SettingsDialogComponent<?> settingsDialogComponent = component.getSettingsDialogComponent();
            dialogComponents.put(component, settingsDialogComponent);
            Widget w = settingsDialogComponent.getAdditionalWidget((DataEntryDialog<?>) dialog);
            result.add(w, component.getLocalizedShortName());
        }
        result.selectTab(0);
        return result;
    }

    @Override
    public CompositeSettings getResult() {
        Collection<ComponentAndSettingsPair<?>> settings = new HashSet<>();
        for (Component<?> component : components) {
            settings.add(getComponentAndSettings(component));
        }
        return new CompositeSettings(settings);
    }

    private <SettingsType extends Settings> ComponentAndSettingsPair<SettingsType> getComponentAndSettings(Component<SettingsType> component) {
        return new ComponentAndSettingsPair<SettingsType>(component, getDialogComponent(component).getResult());
    }
    
    @SuppressWarnings("unchecked")
    private <SettingsType extends Settings> SettingsDialogComponent<SettingsType> getDialogComponent(Component<SettingsType> component) {
        return (SettingsDialogComponent<SettingsType>) dialogComponents.get(component);
    }

    @Override
    public Validator<CompositeSettings> getValidator() {
        return new CompositeValidator(dialogComponents.values());
    }

    @Override
    public FocusWidget getFocusWidget() {
        for (Component<?> component : components) {
            FocusWidget fw = getDialogComponent(component).getFocusWidget();
            if (fw != null) {
                return fw;
            }
        }
        return null;
    }

}
