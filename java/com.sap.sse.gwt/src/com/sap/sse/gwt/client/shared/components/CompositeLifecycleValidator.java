package com.sap.sse.gwt.client.shared.components;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.CompositeLifecycleTabbedSettingsDialogComponent.ComponentLifecycleWithSettingsAndDialogComponent;

public class CompositeLifecycleValidator implements Validator<CompositeLifecycleSettings> {
    
    private final Map<ComponentLifecycleAndSettings<?,?>, Validator<?>> validatorsMappedByComponentLifecycle;

    public CompositeLifecycleValidator(Iterable<ComponentLifecycleWithSettingsAndDialogComponent<?,?>> componentLifecycleAndDialogComponents) {
        validatorsMappedByComponentLifecycle = new HashMap<>();
        for (ComponentLifecycleWithSettingsAndDialogComponent<?,?> component : componentLifecycleAndDialogComponents) {
            validatorsMappedByComponentLifecycle.put(component.getComponentLifecycleAndSettings(), component.getDialogComponent().getValidator());
        }
    }

    @Override
    public String getErrorMessage(CompositeLifecycleSettings valueToValidate) {
        StringBuilder result = new StringBuilder();
        for (ComponentLifecycleAndSettings<?,?> componentAndSettings : valueToValidate.getSettingsPerComponentLifecycle()) {
            final String errorMessage = getErrorMessage(componentAndSettings);
            if (errorMessage != null && !errorMessage.isEmpty()) {
                result.append(errorMessage);
            }
        }
        return result.toString();
    }

    private <C extends ComponentLifecycle<?,S,?,?>, S extends Settings> String getErrorMessage(ComponentLifecycleAndSettings<C,S> componentLifecycleAndSettings) {
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
