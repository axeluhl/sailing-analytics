package com.sap.sse.gwt.client.shared.components;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.CompositeLifecycleTabbedSettingsDialogComponent.ComponentLifecycleAndDialogComponent;

public class CompositeLifecycleValidator implements Validator<CompositeLifecycleSettings> {
    
    private final Map<ComponentLifecycle<?,?,?,?>, Validator<?>> validatorsMappedByComponentLifecycle;

    public CompositeLifecycleValidator(Iterable<ComponentLifecycleAndDialogComponent<?>> componentLifecycleAndDialogComponents) {
        validatorsMappedByComponentLifecycle = new HashMap<>();
        for (ComponentLifecycleAndDialogComponent<?> component : componentLifecycleAndDialogComponents) {
            validatorsMappedByComponentLifecycle.put(component.getA(), component.getB().getValidator());
        }
    }

    @Override
    public String getErrorMessage(CompositeLifecycleSettings valueToValidate) {
        StringBuilder result = new StringBuilder();
        for (ComponentLifecycleAndSettings<?> componentAndSettings : valueToValidate.getSettingsPerComponentLifecycle()) {
            final String errorMessage = getErrorMessage(componentAndSettings);
            if (errorMessage != null && !errorMessage.isEmpty()) {
                result.append(errorMessage);
            }
        }
        return result.toString();
    }

    private <SettingsType extends Settings> String getErrorMessage(ComponentLifecycleAndSettings<SettingsType> componentLifecycleAndSettings) {
        String errorMessage = null;
        @SuppressWarnings("unchecked")
        Validator<SettingsType> validator = (Validator<SettingsType>) validatorsMappedByComponentLifecycle.get(componentLifecycleAndSettings.getComponentLifecycle());
        if (validator != null) {
            errorMessage = validator.getErrorMessage(componentLifecycleAndSettings.getSettings());
            if (errorMessage != null && !errorMessage.isEmpty() && !getClass().equals(validator.getClass())) {
                errorMessage += "; ";
            }
        }
        return errorMessage;
    }

}
