package com.sap.sse.gwt.client.shared.perspective;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentAndSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeTabbedSettingsDialogComponent.ComponentAndDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeTabbedSettingsDialogComponent.PerspectiveAndDialogComponent;

public class PerspectiveCompositeValidator<P extends Perspective<PST>, PST extends Settings>
    implements Validator<PerspectiveCompositeSettings<PST>> {
    
    private final Map<Component<?>, Validator<?>> validatorsMappedByComponent;
    private final Validator<PerspectiveCompositeSettings<PST>> perspectiveValidator;
    
    public PerspectiveCompositeValidator(PerspectiveAndDialogComponent<PST> perspectiveAndDialogComponent, Iterable<ComponentAndDialogComponent<?>> componentsAndDialogComponents) {
        this.perspectiveValidator = perspectiveAndDialogComponent.getSettingsDialog().getValidator();
        validatorsMappedByComponent = new HashMap<>();
        for (ComponentAndDialogComponent<?> componentsAndSettingsDialog : componentsAndDialogComponents) {
            validatorsMappedByComponent.put(componentsAndSettingsDialog.getComponent(), componentsAndSettingsDialog.getSettingsDialog().getValidator());
        }
    }

    @Override
    public String getErrorMessage(PerspectiveCompositeSettings<PST> valueToValidate) {
        StringBuilder result = new StringBuilder();
        for (ComponentAndSettings<?> componentAndSettings : valueToValidate.getSettingsPerComponent()) {
            final String errorMessage = getComponentErrorMessage(componentAndSettings);
            if (errorMessage != null && !errorMessage.isEmpty()) {
                result.append(errorMessage);
            }
        }
        String perspectiveErrorMessage = getPerspectiveErrorMessage(valueToValidate);
        if (perspectiveErrorMessage != null && !perspectiveErrorMessage.isEmpty()) {
            result.append(perspectiveErrorMessage);
        }
        return result.toString();
    }

    private String getPerspectiveErrorMessage(PerspectiveCompositeSettings<PST> perspectiveSettings) {
        String errorMessage = null;
        if (perspectiveValidator != null) {
            errorMessage = perspectiveValidator.getErrorMessage(perspectiveSettings);
        }
        return errorMessage;
    }
    
    private <SettingsType extends Settings> String getComponentErrorMessage(ComponentAndSettings<SettingsType> componentAndSettings) {
        String errorMessage = null;
        @SuppressWarnings("unchecked")
        Validator<SettingsType> validator = (Validator<SettingsType>) validatorsMappedByComponent.get(componentAndSettings.getComponent());
        if (validator != null) {
            errorMessage = validator.getErrorMessage(componentAndSettings.getSettings());
            if (errorMessage != null && !errorMessage.isEmpty() && !getClass().equals(validator.getClass())) {
                errorMessage += "; ";
            }
        }
        return errorMessage;
    }

}
