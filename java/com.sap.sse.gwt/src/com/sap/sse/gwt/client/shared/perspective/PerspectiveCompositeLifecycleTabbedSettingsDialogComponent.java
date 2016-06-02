package com.sap.sse.gwt.client.shared.perspective;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.ComponentIdAndSettings;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeTabbedSettingsDialogComponent.ComponentIdWithSettingsAndDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeTabbedSettingsDialogComponent.PerspectiveIdWithSettingsAndDialogComponent;

/**
 * @author Frank Mittag
 *
 * @param <PL>
 *      The {@link PerspectiveLifeycle} type
 * @param <PS>
 *      the perspective settings type
 */
public class PerspectiveCompositeLifecycleTabbedSettingsDialogComponent<PS extends Settings>
    implements SettingsDialogComponent<PerspectiveCompositeSettings<PS>> {
    
    private final List<ComponentIdWithSettingsAndDialogComponent<?>> componentIdsAndDialogComponents;
    private final PerspectiveIdWithSettingsAndDialogComponent<PS> perspectiveIdsAndSettingsDialog;

    public PerspectiveCompositeLifecycleTabbedSettingsDialogComponent(PerspectiveLifecycleWithAllSettings<?, PS> perspectiveLifecycleWithAllSettings) {
        this.componentIdsAndDialogComponents = new ArrayList<>();
        CompositeSettings componentSettings = perspectiveLifecycleWithAllSettings.getComponentSettings();
        for(ComponentLifecycle<?,?> componentLifecycle: perspectiveLifecycleWithAllSettings.getPerspectiveLifecycle().getComponentLifecycles()) {
            if (componentLifecycle.hasSettings()) {
                ComponentIdAndSettings<?> settingsOfComponent = componentSettings.findComponentAndSettingsByLifecycle(componentLifecycle);
                this.componentIdsAndDialogComponents.add(createComponentIdAndDialogComponent(componentLifecycle, settingsOfComponent));
            }
        }
        perspectiveIdsAndSettingsDialog = createPerspectiveIdAndDialogComponent(perspectiveLifecycleWithAllSettings.getPerspectiveLifecycle(),
                perspectiveLifecycleWithAllSettings.getAllSettings().getPerspectiveAndSettings()); 
    }
    
    private <S extends Settings> ComponentIdWithSettingsAndDialogComponent<?> createComponentIdAndDialogComponent(ComponentLifecycle<?,?> componentLifecycle, ComponentIdAndSettings<S> componentLifecycleAndSettings) {
        S settings = componentLifecycleAndSettings.getSettings();
        @SuppressWarnings("unchecked")
        ComponentLifecycle<S,?> typedComponentLifecycle =  (ComponentLifecycle<S, ?>) componentLifecycle;
        return new ComponentIdWithSettingsAndDialogComponent<S>(componentLifecycle.getLocalizedShortName(), componentLifecycleAndSettings,
                typedComponentLifecycle.getSettingsDialogComponent(settings));
    }

    /**
     * Creates the dialog for the settings of perspective itself 
     * @param perspectiveLifecycleAndSettings
     */
    private PerspectiveIdWithSettingsAndDialogComponent<PS> createPerspectiveIdAndDialogComponent(PerspectiveLifecycle<PS,?,?> perspectiveLifecycle, PerspectiveIdAndSettings<PS> perspectiveIdAndSettings) {
        PS settings = perspectiveIdAndSettings.getSettings();
        return new PerspectiveIdWithSettingsAndDialogComponent<PS>(perspectiveLifecycle.getLocalizedShortName(), perspectiveIdAndSettings, perspectiveLifecycle.getPerspectiveOwnSettingsDialogComponent(settings));
    }

    @Override
    public Widget getAdditionalWidget(final DataEntryDialog<?> dialog) {
        TabPanel result = new TabPanel();
        if(perspectiveIdsAndSettingsDialog != null) {
            Widget w = perspectiveIdsAndSettingsDialog.getSettingsDialog().getAdditionalWidget((DataEntryDialog<?>) dialog);
            result.add(w, perspectiveIdsAndSettingsDialog.getPerspectiveName());
        }
        for (ComponentIdWithSettingsAndDialogComponent<?> componentIdAndSettingsAndDialog : componentIdsAndDialogComponents) {
            Widget w = componentIdAndSettingsAndDialog.getSettingsDialog().getAdditionalWidget((DataEntryDialog<?>) dialog);
            result.add(w, componentIdAndSettingsAndDialog.getComponentName());
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
        PerspectiveIdAndSettings<PS> perspectiveLifeycycleAndSettings = perspectiveIdsAndSettingsDialog != null ? getPerspectiveAndSettings(perspectiveIdsAndSettingsDialog) : null;
        List<ComponentIdAndSettings<?>> settings = new ArrayList<>();
        for (ComponentIdWithSettingsAndDialogComponent<?> component : componentIdsAndDialogComponents) {
            settings.add(getComponentAndSettings(component));
        }
        return new PerspectiveCompositeSettings<PS>(perspectiveLifeycycleAndSettings, settings);
    }

    private <C extends ComponentLifecycle<S,?>, S extends Settings> ComponentIdAndSettings<S> getComponentAndSettings(ComponentIdWithSettingsAndDialogComponent<S> component) {
        return new ComponentIdAndSettings<S>(component.getComponentIdAndSettings().getComponentId(), component.getSettingsDialog().getResult());
    }

    private PerspectiveIdAndSettings<PS> getPerspectiveAndSettings(PerspectiveIdWithSettingsAndDialogComponent<PS> perspective) {
        return new PerspectiveIdAndSettings<PS>(perspective.getPerspectiveIdAndSettings().getPerspectiveId(), perspective.getSettingsDialog().getResult());
    }

    @Override
    public Validator<PerspectiveCompositeSettings<PS>> getValidator() {
        return new PerspectiveCompositeValidator<PS>(perspectiveIdsAndSettingsDialog, componentIdsAndDialogComponents);
    }

    @Override
    public FocusWidget getFocusWidget() {
        for (ComponentIdWithSettingsAndDialogComponent<?> component : componentIdsAndDialogComponents) {
            FocusWidget fw = component.getSettingsDialog().getFocusWidget();
            if (fw != null) {
                return fw;
            }
        }
        return null;
    }
}
