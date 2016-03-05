package com.sap.sse.gwt.client.shared.perspective;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycleAndSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeLifecycleTabbedSettingsDialogComponent.ComponentLifecycleWithSettingsAndDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeLifecycleTabbedSettingsDialogComponent.PerspectiveLifecycleWithSettingsAndDialogComponent;

public class PerspectiveCompositeLifecycleValidator<P extends PerspectiveLifecycle<?,PS,?>, PS extends Settings> implements Validator<PerspectiveCompositeLifecycleSettings<P,PS>> {
    
    private final Map<ComponentLifecycleAndSettings<?,?>, Validator<?>> validatorsMappedByComponentLifecycle;
    private final Validator<?> perspectiveValidator;

    public PerspectiveCompositeLifecycleValidator(PerspectiveLifecycleWithSettingsAndDialogComponent<P,PS> perspectiveLifecycleAndDialogComponent,
            Iterable<ComponentLifecycleWithSettingsAndDialogComponent<?,?>> componentLifecycleAndDialogComponents) {
        perspectiveValidator = perspectiveLifecycleAndDialogComponent.getDialogComponent().getValidator();
        validatorsMappedByComponentLifecycle = new HashMap<>();
        for (ComponentLifecycleWithSettingsAndDialogComponent<?,?> component : componentLifecycleAndDialogComponents) {
            validatorsMappedByComponentLifecycle.put(component.getComponentLifecycleAndSettings(), component.getDialogComponent().getValidator());
        }
    }

    @Override
    public String getErrorMessage(PerspectiveCompositeLifecycleSettings<P,PS> valueToValidate) {
        StringBuilder result = new StringBuilder();
        for (ComponentLifecycleAndSettings<?,?> componentAndSettings : valueToValidate.getComponentLifecyclesAndSettings().getSettingsPerComponentLifecycle()) {
            final String errorMessage = getComponentErrorMessage(componentAndSettings);
            if (errorMessage != null && !errorMessage.isEmpty()) {
                result.append(errorMessage);
            }
        }
        String perspectiveErrorMessage = getPerspectiveErrorMessage(valueToValidate.getPerspectiveLifecycleAndSettings());
        if (perspectiveErrorMessage != null && !perspectiveErrorMessage.isEmpty()) {
            result.append(perspectiveErrorMessage);
        }
        return result.toString();
    }

    private <SettingsType extends Settings> String getPerspectiveErrorMessage(PerspectiveLifecycleAndSettings<?,SettingsType> perspectiveAndSettings) {
        String errorMessage = null;
        @SuppressWarnings("unchecked")
        Validator<SettingsType> validator = (Validator<SettingsType>) perspectiveValidator;
        if (validator != null) {
            errorMessage = validator.getErrorMessage(perspectiveAndSettings.getSettings());
        }
        return errorMessage;
    }

    private <C extends ComponentLifecycle<?,S,?>, S extends Settings> String getComponentErrorMessage(ComponentLifecycleAndSettings<C,S> componentLifecycleAndSettings) {
        String errorMessage = null;
        @SuppressWarnings("unchecked")
        Validator<S> validator = (Validator<S>) validatorsMappedByComponentLifecycle.get(componentLifecycleAndSettings.getComponentLifecycle());
        if (validator != null) {
            errorMessage = validator.getErrorMessage(componentLifecycleAndSettings.getSettings());
            if (errorMessage != null && !errorMessage.isEmpty() && !getClass().equals(validator.getClass())) {
                errorMessage += "; ";
            }
        }
        return errorMessage;
    }
}
