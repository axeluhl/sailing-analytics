package com.sap.sse.gwt.client.shared.perspective;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeTabbedSettingsDialogComponent.ComponentIdWithSettingsAndDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeTabbedSettingsDialogComponent.PerspectiveIdWithSettingsAndDialogComponent;

/**
 * @author Frank
 *
 * @param <PS>
 *      the type of the perspective own settings
 **/
public class PerspectiveCompositeValidator<PS extends Settings> implements Validator<PerspectiveCompositeSettings<PS>> {
    private final Map<String, Validator<?>> validatorsMappedByComponent;
    private final Validator<PS> perspectiveValidator;

    public PerspectiveCompositeValidator(PerspectiveIdWithSettingsAndDialogComponent<PS> perspectiveAndDialogComponent, Iterable<ComponentIdWithSettingsAndDialogComponent<?>> componentsAndDialogComponents) {
        if (perspectiveAndDialogComponent.getSettingsDialogComponent() != null) {
            this.perspectiveValidator = perspectiveAndDialogComponent.getSettingsDialogComponent().getValidator();
        } else {
            perspectiveValidator = null;
        }
        validatorsMappedByComponent = new HashMap<>();
        for (ComponentIdWithSettingsAndDialogComponent<?> componentsAndSettingsDialog : componentsAndDialogComponents) {
            validatorsMappedByComponent.put(componentsAndSettingsDialog.getComponentId(), componentsAndSettingsDialog.getSettingsDialog().getValidator());
        }
    }

    @Override
    public String getErrorMessage(PerspectiveCompositeSettings<PS> valueToValidate) {
        StringBuilder result = new StringBuilder();
        for (Entry<String, Settings> componentAndSettings : valueToValidate.getSettingsPerComponentId().entrySet()) {
            final String errorMessage = getComponentErrorMessage(componentAndSettings);
            if (errorMessage != null && !errorMessage.isEmpty()) {
                result.append(errorMessage);
            }
        }
        String perspectiveErrorMessage = getPerspectiveErrorMessage(valueToValidate.getPerspectiveOwnSettings());
        if (perspectiveErrorMessage != null && !perspectiveErrorMessage.isEmpty()) {
            result.append(perspectiveErrorMessage);
        }
        return result.toString();
    }

    private String getPerspectiveErrorMessage(PS perspectiveSettings) {
        String errorMessage = null;
        if (perspectiveValidator != null) {
            errorMessage = perspectiveValidator.getErrorMessage(perspectiveSettings);
        }
        return errorMessage;
    }
    
    private <SettingsType extends Settings> String getComponentErrorMessage(Entry<String, SettingsType> componentIdAndSettings) {
        String errorMessage = null;
        @SuppressWarnings("unchecked")
        Validator<SettingsType> validator = (Validator<SettingsType>) validatorsMappedByComponent.get(componentIdAndSettings.getKey());
        if (validator != null) {
            errorMessage = validator.getErrorMessage(componentIdAndSettings.getValue());
            if (errorMessage != null && !errorMessage.isEmpty() && !getClass().equals(validator.getClass())) {
                errorMessage += "; ";
            }
        }
        return errorMessage;
    }

}
