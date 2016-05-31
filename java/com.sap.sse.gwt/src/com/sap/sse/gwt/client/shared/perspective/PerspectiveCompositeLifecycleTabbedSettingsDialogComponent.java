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

/**
 * @author Frank Mittag
 *
 * @param <PL>
 *      The {@link PerspectiveLifeycle} type
 * @param <PS>
 *      the perspective settings type
 */
public class PerspectiveCompositeLifecycleTabbedSettingsDialogComponent<PL extends PerspectiveLifecycle<PS, ?, ?>, PS extends Settings>
    implements SettingsDialogComponent<PerspectiveCompositeLifecycleSettings<PL,PS>> {
    
    public static class ComponentLifecycleWithSettingsAndDialogComponent<ComponentLifecycleType extends ComponentLifecycle<S,?>, S extends Settings> {
        private ComponentLifecycleAndSettings<ComponentLifecycleType, S> componentLifecycleAndSettings;
        private SettingsDialogComponent<S> dialogComponent;

        public ComponentLifecycleWithSettingsAndDialogComponent(ComponentLifecycleAndSettings<ComponentLifecycleType, S> componentLifecycleAndSettings, SettingsDialogComponent<S> dialogComponent) {
            this.componentLifecycleAndSettings = componentLifecycleAndSettings;
            this.dialogComponent = dialogComponent;
        }
        
        public ComponentLifecycleAndSettings<ComponentLifecycleType,S> getComponentLifecycleAndSettings() {
            return componentLifecycleAndSettings;
        }

        public SettingsDialogComponent<S> getDialogComponent() {
            return dialogComponent;
        }
    }

    /**
     * @author Frank Mittag
     *
     * @param <PL>
     *          the {@link PerspectiveLifecycle} type
     * @param <PS>
     *          the {@link Perspective} settings type
     */
    public static class PerspectiveLifecycleWithSettingsAndDialogComponent<PL extends PerspectiveLifecycle<PS, ?,?>, PS extends Settings> {
        private PerspectiveLifecycleAndSettings<PL, PS> perspectiveLifecycleAndSettings;
        private SettingsDialogComponent<PS> dialogComponent;

        public PerspectiveLifecycleWithSettingsAndDialogComponent(PerspectiveLifecycleAndSettings<PL, PS> perspectiveLifecycleAndSettings, 
                SettingsDialogComponent<PS> dialogComponent) {
            this.perspectiveLifecycleAndSettings = perspectiveLifecycleAndSettings;
            this.dialogComponent = dialogComponent;
        }
        
        public PerspectiveLifecycleAndSettings<PL,PS> getPerspectiveLifecycleAndSettings() {
            return perspectiveLifecycleAndSettings;
        }

        public SettingsDialogComponent<PS> getDialogComponent() {
            return dialogComponent;
        }
    }

    private final Collection<ComponentLifecycleWithSettingsAndDialogComponent<?,?>> componentLifecycleAndDialogComponents;
    private final PerspectiveLifecycleWithSettingsAndDialogComponent<PL,PS> perspectiveLifecycleAndSettingsDialog;

    public PerspectiveCompositeLifecycleTabbedSettingsDialogComponent(PerspectiveCompositeLifecycleSettings<PL,PS> componentLifecyclesSettings) {
        this.componentLifecycleAndDialogComponents = new ArrayList<>();
        for (ComponentLifecycleAndSettings<?,?> componentLifecycleAndSettings : componentLifecyclesSettings.getComponentLifecyclesAndSettings().getSettingsPerComponentLifecycle()) {
            if (componentLifecycleAndSettings.getComponentLifecycle().hasSettings()) {
                this.componentLifecycleAndDialogComponents.add(createComponentLifecycleAndDialogComponent(componentLifecycleAndSettings));
            }
        }
        perspectiveLifecycleAndSettingsDialog = createPerspectiveLifecycleAndDialogComponent(componentLifecyclesSettings.getPerspectiveLifecycleAndSettings()); 
    }

    private <C extends ComponentLifecycle<S,?>, S extends Settings> ComponentLifecycleWithSettingsAndDialogComponent<C,S> createComponentLifecycleAndDialogComponent(ComponentLifecycleAndSettings<C,S> componentLifecycleAndSettings) {
        S settings = componentLifecycleAndSettings.getSettings();
        return new ComponentLifecycleWithSettingsAndDialogComponent<C,S>(componentLifecycleAndSettings, componentLifecycleAndSettings.getComponentLifecycle().getSettingsDialogComponent(settings));
    }

    /**
     * Creates the dialog for the settings of perspective itself 
     * @param perspectiveLifecycleAndSettings
     */
    private PerspectiveLifecycleWithSettingsAndDialogComponent<PL,PS> createPerspectiveLifecycleAndDialogComponent(PerspectiveLifecycleAndSettings<PL,PS> perspectiveLifecycleAndSettings) {
        PS settings = perspectiveLifecycleAndSettings.getSettings();
        PL perspectiveLifecycle = perspectiveLifecycleAndSettings.getPerspectiveLifecycle();
        return new PerspectiveLifecycleWithSettingsAndDialogComponent<PL,PS>(perspectiveLifecycleAndSettings, perspectiveLifecycle.getPerspectiveOwnSettingsDialogComponent(settings));
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
    public PerspectiveCompositeLifecycleSettings<PL,PS> getResult() {
        PerspectiveLifecycleAndSettings<PL,PS> perspectiveLifeycycleAndSettings = perspectiveLifecycleAndSettingsDialog != null ? getPerspectiveAndSettings(perspectiveLifecycleAndSettingsDialog) : null;
        Collection<ComponentLifecycleAndSettings<?,?>> settings = new HashSet<>();
        for (ComponentLifecycleWithSettingsAndDialogComponent<?,?> component : componentLifecycleAndDialogComponents) {
            settings.add(getComponentAndSettings(component));
        }
        return new PerspectiveCompositeLifecycleSettings<PL,PS>(perspectiveLifeycycleAndSettings, new CompositeLifecycleSettings(settings));
    }

    private <C extends ComponentLifecycle<S,?>, S extends Settings> ComponentLifecycleAndSettings<C,S> getComponentAndSettings(ComponentLifecycleWithSettingsAndDialogComponent<C,S> component) {
        return new ComponentLifecycleAndSettings<C,S>(component.getComponentLifecycleAndSettings().getComponentLifecycle(), component.getDialogComponent().getResult());
    }

    private PerspectiveLifecycleAndSettings<PL,PS> getPerspectiveAndSettings(PerspectiveLifecycleWithSettingsAndDialogComponent<PL,PS> perspective) {
        return new PerspectiveLifecycleAndSettings<PL,PS>(perspective.getPerspectiveLifecycleAndSettings().getPerspectiveLifecycle(), perspective.getDialogComponent().getResult());
    }

    @Override
    public Validator<PerspectiveCompositeLifecycleSettings<PL,PS>> getValidator() {
        return new PerspectiveCompositeLifecycleValidator<PL,PS>(perspectiveLifecycleAndSettingsDialog, componentLifecycleAndDialogComponents);
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
