package com.sap.sse.gwt.client.shared.perspective;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycleAndSettings;
import com.sap.sse.gwt.client.shared.components.CompositeLifecycleSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class PerspectiveCompositeLifecycleTabbedSettingsDialogComponent<P extends PerspectiveLifecycle<?,PS,?>, PS extends Settings> 
    implements SettingsDialogComponent<PerspectiveCompositeLifecycleSettings<P,PS>> {
    
    public static class ComponentLifecycleWithSettingsAndDialogComponent<ComponentLifecycleType extends ComponentLifecycle<?,SettingsType,?>, SettingsType extends Settings> {
        private ComponentLifecycleAndSettings<ComponentLifecycleType, SettingsType> componentLifecycleAndSettings;
        private SettingsDialogComponent<SettingsType> dialogComponent;

        public ComponentLifecycleWithSettingsAndDialogComponent(ComponentLifecycleAndSettings<ComponentLifecycleType, SettingsType> componentLifecycleAndSettings, SettingsDialogComponent<SettingsType> dialogComponent) {
            this.componentLifecycleAndSettings = componentLifecycleAndSettings;
            this.dialogComponent = dialogComponent;
        }
        
        public ComponentLifecycleAndSettings<ComponentLifecycleType,SettingsType> getComponentLifecycleAndSettings() {
            return componentLifecycleAndSettings;
        }

        public SettingsDialogComponent<SettingsType> getDialogComponent() {
            return dialogComponent;
        }
    }

    public static class PerspectiveLifecycleWithSettingsAndDialogComponent<PerspectiveLifecycleType extends PerspectiveLifecycle<?,SettingsType,?>, SettingsType extends Settings> {
        private PerspectiveLifecycleAndSettings<PerspectiveLifecycleType, SettingsType> perspectiveLifecycleAndSettings;
        private SettingsDialogComponent<SettingsType> dialogComponent;

        public PerspectiveLifecycleWithSettingsAndDialogComponent(PerspectiveLifecycleAndSettings<PerspectiveLifecycleType, SettingsType> perspectiveLifecycleAndSettings, SettingsDialogComponent<SettingsType> dialogComponent) {
            this.perspectiveLifecycleAndSettings = perspectiveLifecycleAndSettings;
            this.dialogComponent = dialogComponent;
        }
        
        public PerspectiveLifecycleAndSettings<PerspectiveLifecycleType,SettingsType> getPerspectiveLifecycleAndSettings() {
            return perspectiveLifecycleAndSettings;
        }

        public SettingsDialogComponent<SettingsType> getDialogComponent() {
            return dialogComponent;
        }
    }

    private final Collection<ComponentLifecycleWithSettingsAndDialogComponent<?,?>> componentLifecycleAndDialogComponents;
    private final PerspectiveLifecycleWithSettingsAndDialogComponent<P,PS> perspectiveLifecycleAndSettingsDialog;

    public PerspectiveCompositeLifecycleTabbedSettingsDialogComponent(PerspectiveCompositeLifecycleSettings<P,PS> componentLifecyclesSettings) {
        this.componentLifecycleAndDialogComponents = new ArrayList<>();
        for (ComponentLifecycleAndSettings<?,?> componentLifecycleAndSettings : componentLifecyclesSettings.getComponentLifecyclesAndSettings().getSettingsPerComponentLifecycle()) {
            if (componentLifecycleAndSettings.getComponentLifecycle().hasSettings()) {
                this.componentLifecycleAndDialogComponents.add(createComponentLifecycleAndDialogComponent(componentLifecycleAndSettings));
            }
        }
        perspectiveLifecycleAndSettingsDialog = createPerspectiveLifecycleAndDialogComponent(componentLifecyclesSettings.getPerspectiveLifecycleAndSettings()); 
    }

    private <C extends ComponentLifecycle<?,S,?>, S extends Settings> ComponentLifecycleWithSettingsAndDialogComponent<C,S> createComponentLifecycleAndDialogComponent(ComponentLifecycleAndSettings<C,S> componentLifecycleAndSettings) {
        S settings = componentLifecycleAndSettings.getSettings();
        return new ComponentLifecycleWithSettingsAndDialogComponent<C,S>(componentLifecycleAndSettings, componentLifecycleAndSettings.getComponentLifecycle().getSettingsDialogComponent(settings));
    }

    private PerspectiveLifecycleWithSettingsAndDialogComponent<P,PS> createPerspectiveLifecycleAndDialogComponent(PerspectiveLifecycleAndSettings<P,PS> perspectiveLifecycleAndSettings) {
        PS settings = perspectiveLifecycleAndSettings.getSettings();
        return new PerspectiveLifecycleWithSettingsAndDialogComponent<P,PS>(perspectiveLifecycleAndSettings, perspectiveLifecycleAndSettings.getPerspectiveLifecycle().getSettingsDialogComponent(settings));
    }

    @Override
    public Widget getAdditionalWidget(final DataEntryDialog<?> dialog) {
        TabPanel result = new TabPanel();
        if(perspectiveLifecycleAndSettingsDialog != null) {
            Widget w = perspectiveLifecycleAndSettingsDialog.getDialogComponent().getAdditionalWidget((DataEntryDialog<?>) dialog);
            result.add(w, perspectiveLifecycleAndSettingsDialog.getPerspectiveLifecycleAndSettings().getPerspectiveLifecycle().getLocalizedShortName());
        }
        for (ComponentLifecycleWithSettingsAndDialogComponent<?,?> component : componentLifecycleAndDialogComponents) {
            Widget w = component.getDialogComponent().getAdditionalWidget((DataEntryDialog<?>) dialog);
            result.add(w, component.getComponentLifecycleAndSettings().getComponentLifecycle().getLocalizedShortName());
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
    public PerspectiveCompositeLifecycleSettings<P,PS> getResult() {
        PerspectiveLifecycleAndSettings<P,PS> perspectiveLifeycycleAndSettings = perspectiveLifecycleAndSettingsDialog != null ? getPerspectiveAndSettings(perspectiveLifecycleAndSettingsDialog) : null;
        Collection<ComponentLifecycleAndSettings<?,?>> settings = new HashSet<>();
        for (ComponentLifecycleWithSettingsAndDialogComponent<?,?> component : componentLifecycleAndDialogComponents) {
            settings.add(getComponentAndSettings(component));
        }
        return new PerspectiveCompositeLifecycleSettings<P,PS>(perspectiveLifeycycleAndSettings, new CompositeLifecycleSettings(settings));
    }

    private <C extends ComponentLifecycle<?,S,?>, S extends Settings> ComponentLifecycleAndSettings<C,S> getComponentAndSettings(ComponentLifecycleWithSettingsAndDialogComponent<C,S> component) {
        return new ComponentLifecycleAndSettings<C,S>(component.getComponentLifecycleAndSettings().getComponentLifecycle(), component.getDialogComponent().getResult());
    }

    private PerspectiveLifecycleAndSettings<P,PS> getPerspectiveAndSettings(PerspectiveLifecycleWithSettingsAndDialogComponent<P,PS> perspective) {
        return new PerspectiveLifecycleAndSettings<P,PS>(perspective.getPerspectiveLifecycleAndSettings().getPerspectiveLifecycle(), perspective.getDialogComponent().getResult());
    }

    @Override
    public Validator<PerspectiveCompositeLifecycleSettings<P,PS>> getValidator() {
        return new PerspectiveCompositeLifecycleValidator<P,PS>(perspectiveLifecycleAndSettingsDialog, componentLifecycleAndDialogComponents);
    }

    @Override
    public FocusWidget getFocusWidget() {
        for (ComponentLifecycleWithSettingsAndDialogComponent<?,?> component : componentLifecycleAndDialogComponents) {
            FocusWidget fw = component.getDialogComponent().getFocusWidget();
            if (fw != null) {
                return fw;
            }
        }
        return null;
    }
}
