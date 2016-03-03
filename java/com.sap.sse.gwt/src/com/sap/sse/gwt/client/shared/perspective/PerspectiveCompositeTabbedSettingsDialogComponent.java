package com.sap.sse.gwt.client.shared.perspective;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentAndSettingsPair;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class PerspectiveCompositeTabbedSettingsDialogComponent implements SettingsDialogComponent<PerspectiveCompositeSettings> {
    
    public class ComponentAndDialogComponent<SettingsType extends Settings> implements Serializable {
        private static final long serialVersionUID = -4342002423677523158L;

        private final Component<SettingsType> component;
        private final SettingsDialogComponent<SettingsType> settingsDialog;
        
        public ComponentAndDialogComponent(Component<SettingsType> component, SettingsDialogComponent<SettingsType> settingsDialog) {
            this.component = component;
            this.settingsDialog = settingsDialog;
        }

        public Component<SettingsType> getComponent() {
            return component;
        }

        public SettingsDialogComponent<SettingsType> getSettingsDialog() {
            return settingsDialog;
        }
    }

    public class PerspectiveAndDialogComponent<SettingsType extends Settings> implements Serializable {
        private static final long serialVersionUID = -139706230424184935L;

        private final Perspective<SettingsType> perspective;
        private final SettingsDialogComponent<SettingsType> settingsDialog;
        
        public PerspectiveAndDialogComponent(Perspective<SettingsType> perspective, SettingsDialogComponent<SettingsType> settingsDialog) {
            this.perspective = perspective;
            this.settingsDialog = settingsDialog;
        }

        public Perspective<SettingsType> getPerspective() {
            return perspective;
        }

        public SettingsDialogComponent<SettingsType> getSettingsDialog() {
            return settingsDialog;
        }
    }

    private final Collection<ComponentAndDialogComponent<?>> componentsAndSettingsDialogs;
    private final PerspectiveAndDialogComponent<?> perspectiveAndSettingsDialog;
    
    public PerspectiveCompositeTabbedSettingsDialogComponent(Perspective<?> perspective) {
        this.componentsAndSettingsDialogs = new ArrayList<>();
        for (Component<?> component : perspective.getComponents()) {
            if (component.hasSettings()) {
                this.componentsAndSettingsDialogs.add(createComponentAndDialogComponent(component));
            }
        }
        perspectiveAndSettingsDialog = createPerspectiveAndDialogComponent(perspective);
    }

    private <SettingsType extends Settings> PerspectiveAndDialogComponent<SettingsType> createPerspectiveAndDialogComponent(Perspective<SettingsType> perspective) {
        if(perspective.hasSettings()) {
            return new PerspectiveAndDialogComponent<SettingsType>(perspective, perspective.getSettingsDialogComponent());
        }
        return null;
    }

    private <SettingsType extends Settings> ComponentAndDialogComponent<SettingsType> createComponentAndDialogComponent(Component<SettingsType> component) {
        return new ComponentAndDialogComponent<SettingsType>(component, component.getSettingsDialogComponent());
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        TabPanel result = new TabPanel();
        if(perspectiveAndSettingsDialog != null) {
            Widget w = perspectiveAndSettingsDialog.getSettingsDialog().getAdditionalWidget((DataEntryDialog<?>) dialog);
            result.add(w, perspectiveAndSettingsDialog.getPerspective().getLocalizedShortName());
        }
        for (ComponentAndDialogComponent<?> componentAndSettingsDialog : componentsAndSettingsDialogs) {
            Widget w = componentAndSettingsDialog.getSettingsDialog().getAdditionalWidget((DataEntryDialog<?>) dialog);
            result.add(w, componentAndSettingsDialog.getComponent().getLocalizedShortName());
        }
        result.selectTab(0);
        return result;
    }

    @Override
    public PerspectiveCompositeSettings getResult() {
        PerspectiveAndSettingsPair<?> perspectiveAndSettings = perspectiveAndSettingsDialog != null ? getPerspectiveAndSettings(perspectiveAndSettingsDialog) : null;
        Collection<ComponentAndSettingsPair<?>> componentsAndSettings = new HashSet<>();
        for (ComponentAndDialogComponent<?> componentAndSettingsDialog : componentsAndSettingsDialogs) {
            componentsAndSettings.add(getComponentAndSettings(componentAndSettingsDialog));
        }
        return new PerspectiveCompositeSettings(perspectiveAndSettings, componentsAndSettings);
    }

    private <SettingsType extends Settings> ComponentAndSettingsPair<SettingsType> getComponentAndSettings(ComponentAndDialogComponent<SettingsType> componentAndSettingsDialog) {
        return new ComponentAndSettingsPair<SettingsType>(componentAndSettingsDialog.getComponent(), componentAndSettingsDialog.getSettingsDialog().getResult());
    }

    private <SettingsType extends Settings> PerspectiveAndSettingsPair<SettingsType> getPerspectiveAndSettings(PerspectiveAndDialogComponent<SettingsType> perspectiveAndSettingsDialog) {
        return new PerspectiveAndSettingsPair<SettingsType>(perspectiveAndSettingsDialog.getPerspective(), perspectiveAndSettingsDialog.getSettingsDialog().getResult());
    }

    @Override
    public Validator<PerspectiveCompositeSettings> getValidator() {
        return new PerspectiveCompositeValidator(perspectiveAndSettingsDialog, componentsAndSettingsDialogs);
    }

    @Override
    public FocusWidget getFocusWidget() {
        for (ComponentAndDialogComponent<?> componentAndSettingsDialog : componentsAndSettingsDialogs) {
            FocusWidget fw = componentAndSettingsDialog.getSettingsDialog().getFocusWidget();
            if (fw != null) {
                return fw;
            }
        }
        return null;
    }

}
