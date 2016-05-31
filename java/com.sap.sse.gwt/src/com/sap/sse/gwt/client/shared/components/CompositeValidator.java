package com.sap.sse.gwt.client.shared.components;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.CompositeTabbedSettingsDialogComponent.ComponentAndDialogComponent;

public class CompositeValidator implements Validator<CompositeSettings> {
    
    private final Map<Serializable, Validator<?>> validatorsMappedByComponent;

    public CompositeValidator(Iterable<ComponentAndDialogComponent<?>> componentsAndDialogComponents) {
        validatorsMappedByComponent = new HashMap<>();
        for (ComponentAndDialogComponent<?> component : componentsAndDialogComponents) {
            validatorsMappedByComponent.put(component.getA().getId(), component.getB().getValidator());
        }
    }

    @Override
    public String getErrorMessage(CompositeSettings valueToValidate) {
        final StringBuilder result = new StringBuilder();
        for (ComponentIdAndSettings<?> componentAndSettings : valueToValidate.getSettingsPerComponent()) {
            final String errorMessage = getErrorMessage(componentAndSettings);
            if (errorMessage != null && !errorMessage.isEmpty()) {
                result.append(errorMessage);
            }
        }
        return result.toString();
    }

    private <SettingsType extends Settings> String getErrorMessage(ComponentIdAndSettings<SettingsType> componentAndSettings) {
        String errorMessage = null;
        @SuppressWarnings("unchecked")
        final Validator<SettingsType> validator = (Validator<SettingsType>) validatorsMappedByComponent.get(componentAndSettings.getComponentId());
        if (validator != null) {
            errorMessage = validator.getErrorMessage(componentAndSettings.getSettings());
            if (errorMessage != null && !errorMessage.isEmpty() && !getClass().equals(validator.getClass())) {
                errorMessage += "; ";
            }
        }
        return errorMessage;
    }

}
