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
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class PerspectiveCompositeTabbedSettingsDialogComponent<P extends Perspective<PST>, PST extends Settings>
    implements SettingsDialogComponent<PerspectiveCompositeSettings<PST>> {
    
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

    public static class PerspectiveAndDialogComponent<PST extends Settings> implements Serializable {
        private static final long serialVersionUID = -139706230424184935L;

        private final Perspective<PST> perspective;
        private final SettingsDialogComponent<PerspectiveCompositeSettings<PST>> settingsDialog;
        
        public PerspectiveAndDialogComponent(Perspective<PST> perspective, SettingsDialogComponent<PerspectiveCompositeSettings<PST>> settingsDialog) {
            this.perspective = perspective;
            this.settingsDialog = settingsDialog;
        }

        public Perspective<PST> getPerspective() {
            return perspective;
        }

        public SettingsDialogComponent<PerspectiveCompositeSettings<PST>> getSettingsDialog() {
            return settingsDialog;
        }
    }

    private final Collection<ComponentAndDialogComponent<?>> componentsAndSettingsDialogs;
    private final PerspectiveAndDialogComponent<PST> perspectiveAndSettingsDialog;
    
    public PerspectiveCompositeTabbedSettingsDialogComponent(Perspective<PST> perspective) {
        this.componentsAndSettingsDialogs = new ArrayList<>();
        for (Component<?> component : perspective.getComponents()) {
            if (component.hasSettings()) {
                this.componentsAndSettingsDialogs.add(createComponentAndDialogComponent(component));
            }
        }
        perspectiveAndSettingsDialog = createPerspectiveAndDialogComponent(perspective);
    }

    private PerspectiveAndDialogComponent<PST> createPerspectiveAndDialogComponent(Perspective<PST> perspective) {
        if(perspective.hasSettings()) {
            return new PerspectiveAndDialogComponent<PST>(perspective, perspective.getSettingsDialogComponent());
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
    public PerspectiveCompositeSettings<PST> getResult() {
        return perspectiveAndSettingsDialog.getSettingsDialog().getResult();
    }

    @Override
    public PerspectiveCompositeValidator<P, PST> getValidator() {
        return new PerspectiveCompositeValidator<P, PST>(perspectiveAndSettingsDialog, componentsAndSettingsDialogs);
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
