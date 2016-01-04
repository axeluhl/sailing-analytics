package com.sap.sse.gwt.client.shared.components;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.CompositeSettings.ComponentAndSettingsPair;
import com.sap.sse.gwt.client.shared.components.CompositeTabbedSettingsDialogComponent.ComponentAndDialogComponent;

public class CompositeValidator implements Validator<CompositeSettings> {
    
    private final Map<Component<?>, Validator<?>> validatorsMappedByComponent;

    public CompositeValidator(Iterable<ComponentAndDialogComponent<?>> componentsAndDialogComponents) {
        validatorsMappedByComponent = new HashMap<>();
        for (ComponentAndDialogComponent<?> component : componentsAndDialogComponents) {
            validatorsMappedByComponent.put(component.getA(), component.getB().getValidator());
        }
    }

    @Override
    public String getErrorMessage(CompositeSettings valueToValidate) {
        StringBuilder result = new StringBuilder();
        for (ComponentAndSettingsPair<?> componentAndSettings : valueToValidate.getSettingsPerComponent()) {
            final String errorMessage = getErrorMessage(componentAndSettings);
            if (errorMessage != null && !errorMessage.isEmpty()) {
                result.append(errorMessage);
            }
        }
        return result.toString();
    }

    private <SettingsType extends Settings> String getErrorMessage(ComponentAndSettingsPair<SettingsType> componentAndSettings) {
        String errorMessage = null;
        @SuppressWarnings("unchecked")
        Validator<SettingsType> validator = (Validator<SettingsType>) validatorsMappedByComponent.get(componentAndSettings.getA());
        if (validator != null) {
            errorMessage = validator.getErrorMessage(componentAndSettings.getB());
            if (errorMessage != null && !errorMessage.isEmpty() && !getClass().equals(validator.getClass())) {
                errorMessage += "; ";
            }
        }
        return errorMessage;
    }

}
