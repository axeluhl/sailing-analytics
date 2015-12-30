package com.sap.sse.gwt.client.shared.components;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.CompositeSettings.ComponentLifecycleAndSettingsPair;
import com.sap.sse.gwt.client.shared.components.CompositeTabbedSettingsDialogComponent.ComponentLifecycleAndDialogComponent;

public class CompositeValidator implements Validator<CompositeSettings> {
    
    private final Map<ComponentLifecycle<?,?,?>, Validator<?>> validatorsMappedByComponent;

    public CompositeValidator(Iterable<ComponentLifecycleAndDialogComponent<?>> componentsAndDialogComponents) {
        validatorsMappedByComponent = new HashMap<>();
        for (ComponentLifecycleAndDialogComponent<?> component : componentsAndDialogComponents) {
            validatorsMappedByComponent.put(component.getA(), component.getB().getValidator());
        }
    }

    @Override
    public String getErrorMessage(CompositeSettings valueToValidate) {
        StringBuilder result = new StringBuilder();
        for (ComponentLifecycleAndSettingsPair<?> componentAndSettings : valueToValidate.getSettingsPerComponentLifecycle()) {
            final String errorMessage = getErrorMessage(componentAndSettings);
            if (errorMessage != null && !errorMessage.isEmpty()) {
                result.append(errorMessage);
            }
        }
        return result.toString();
    }

    private <SettingsType extends Settings> String getErrorMessage(ComponentLifecycleAndSettingsPair<SettingsType> componentAndSettings) {
        String errorMessage = null;
        @SuppressWarnings("unchecked")
        Validator<SettingsType> validator = (Validator<SettingsType>) validatorsMappedByComponent.get(componentAndSettings.getComponentLifecycle());
        if (validator != null) {
            errorMessage = validator.getErrorMessage(componentAndSettings.getSettings());
            if (errorMessage != null && !errorMessage.isEmpty() && !getClass().equals(validator.getClass())) {
                errorMessage += "; ";
            }
        }
        return errorMessage;
    }

}
