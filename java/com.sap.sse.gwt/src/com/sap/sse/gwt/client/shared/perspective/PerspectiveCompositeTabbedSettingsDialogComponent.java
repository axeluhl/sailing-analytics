package com.sap.sse.gwt.client.shared.perspective;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentAndSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class PerspectiveCompositeTabbedSettingsDialogComponent<P extends Perspective<PS>, PS extends Settings>
    implements SettingsDialogComponent<PerspectiveCompositeSettings<PS>> {
    
    public static class ComponentAndDialogComponent<S extends Settings> implements Serializable {
        private static final long serialVersionUID = -4342002423677523158L;

        private final Component<S> component;
        private final SettingsDialogComponent<S> settingsDialog;
        
        public ComponentAndDialogComponent(Component<S> component, SettingsDialogComponent<S> settingsDialog) {
            this.component = component;
            this.settingsDialog = settingsDialog;
        }

        public Component<S> getComponent() {
            return component;
        }

        public SettingsDialogComponent<S> getSettingsDialog() {
            return settingsDialog;
        }
    }

    public static class PerspectiveAndDialogComponent<PS extends Settings> implements Serializable {
        private static final long serialVersionUID = -139706230424184935L;

        private final Perspective<PS> perspective;
        private final SettingsDialogComponent<PerspectiveCompositeSettings<PS>> settingsDialog;
        
        public PerspectiveAndDialogComponent(Perspective<PS> perspective, SettingsDialogComponent<PerspectiveCompositeSettings<PS>> settingsDialog) {
            this.perspective = perspective;
            this.settingsDialog = settingsDialog;
        }

        public Perspective<PS> getPerspective() {
            return perspective;
        }

        public SettingsDialogComponent<PerspectiveCompositeSettings<PS>> getSettingsDialog() {
            return settingsDialog;
        }
    }

    private final Collection<ComponentAndDialogComponent<?>> componentsAndSettingsDialogs;
    private final PerspectiveAndDialogComponent<PS> perspectiveAndSettingsDialog;
    
    public PerspectiveCompositeTabbedSettingsDialogComponent(PerspectiveCompositeSettings<PS> perspectiveCompositeSettings) {
        this.componentsAndSettingsDialogs = new ArrayList<>();
        for (ComponentAndSettings<?> componentAndSettings : perspectiveCompositeSettings.getSettingsPerComponent()) {
            componentsAndSettingsDialogs.add(createComponentAndDialogComponent(componentAndSettings.getComponent()));
        }
        perspectiveAndSettingsDialog = createPerspectiveAndDialogComponent(perspectiveCompositeSettings.getPerspectiveAndSettings().getPerspective());
    }

    private PerspectiveAndDialogComponent<PS> createPerspectiveAndDialogComponent(Perspective<PS> perspective) {
        if(perspective.hasSettings()) {
            return new PerspectiveAndDialogComponent<PS>(perspective, perspective.getSettingsDialogComponent());
        }
        return null;
    }

    private <SettingsType extends Settings> ComponentAndDialogComponent<SettingsType> createComponentAndDialogComponent(Component<SettingsType> component) {
        return new ComponentAndDialogComponent<SettingsType>(component, component.getSettingsDialogComponent());
    }

    @Override
    public Widget getAdditionalWidget(final DataEntryDialog<?> dialog) {
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
        return perspectiveAndSettingsDialog.getSettingsDialog().getResult();
    }

    @Override
    public PerspectiveCompositeValidator<P, PS> getValidator() {
        return new PerspectiveCompositeValidator<P, PS>(perspectiveAndSettingsDialog, componentsAndSettingsDialogs);
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
