package com.sap.sse.gwt.client.shared.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;

public class CompositeTabbedSettingsDialogComponent implements SettingsDialogComponent<CompositeSettings> {
    
    public class ComponentAndDialogComponent<SettingsType extends Settings> extends Util.Pair<Component<SettingsType>, SettingsDialogComponent<SettingsType>> {
        private static final long serialVersionUID = -4342002423677523158L;

        public ComponentAndDialogComponent(Component<SettingsType> a, SettingsDialogComponent<SettingsType> b) {
            super(a, b);
        }
    }
    
    private final Collection<ComponentAndDialogComponent<?>> components;

    public CompositeTabbedSettingsDialogComponent(Iterable<Component<?>> components) {
        this.components = new ArrayList<>();
        for (Component<?> component : components) {
            if (component.hasSettings()) {
                this.components.add(createComponentAndDialogComponent(component));
            }
        }
    }

    private <SettingsType extends Settings> ComponentAndDialogComponent<SettingsType> createComponentAndDialogComponent(Component<SettingsType> component) {
        return new ComponentAndDialogComponent<SettingsType>(component, component.getSettingsDialogComponent(component.getSettings()));
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        TabPanel result = new TabPanel();
        for (ComponentAndDialogComponent<?> component : components) {
            Widget w = component.getB().getAdditionalWidget((DataEntryDialog<?>) dialog);
            result.add(w, component.getA().getLocalizedShortName());
        }
        result.selectTab(0);
        return result;
    }

    @Override
    public CompositeSettings getResult() {
        Map<String, Settings> settings = new HashMap<>();
        for (ComponentAndDialogComponent<?> component : components) {
            settings.put(component.getA().getId(), component.getB().getResult());
        }
        return new CompositeSettings(settings);
    }

    @Override
    public Validator<CompositeSettings> getValidator() {
        return new CompositeValidator(components);
    }

    @Override
    public FocusWidget getFocusWidget() {
        for (ComponentAndDialogComponent<?> component : components) {
            FocusWidget fw = component.getB().getFocusWidget();
            if (fw != null) {
                return fw;
            }
        }
        return null;
    }

}
