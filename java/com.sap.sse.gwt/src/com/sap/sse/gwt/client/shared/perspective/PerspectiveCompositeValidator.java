package com.sap.sse.gwt.client.shared.perspective;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.ComponentIdAndSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeTabbedSettingsDialogComponent.ComponentAndDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeTabbedSettingsDialogComponent.PerspectiveAndDialogComponent;

/**
 * @author Frank
 *
 * @param <P>
 *      the type of the perspective
 * @param <PS>
 *      the type of the perspective settings
 **/
public class PerspectiveCompositeValidator<P extends Perspective<PS>, PS extends Settings>
    implements Validator<PerspectiveCompositeSettings<PS>> {
    
    private final Map<Serializable, Validator<?>> validatorsMappedByComponent;
    private final Validator<PS> perspectiveValidator;
    
    public PerspectiveCompositeValidator(PerspectiveAndDialogComponent<PS> perspectiveAndDialogComponent, Iterable<ComponentAndDialogComponent<?>> componentsAndDialogComponents) {
        this.perspectiveValidator = perspectiveAndDialogComponent.getSettingsDialog().getValidator();
        validatorsMappedByComponent = new HashMap<>();
        for (ComponentAndDialogComponent<?> componentsAndSettingsDialog : componentsAndDialogComponents) {
            validatorsMappedByComponent.put(componentsAndSettingsDialog.getComponent().getId(), componentsAndSettingsDialog.getSettingsDialog().getValidator());
        }
    }

    @Override
    public String getErrorMessage(PerspectiveCompositeSettings<PS> valueToValidate) {
        StringBuilder result = new StringBuilder();
        for (ComponentIdAndSettings<?> componentAndSettings : valueToValidate.getSettingsPerComponent()) {
            final String errorMessage = getComponentErrorMessage(componentAndSettings);
            if (errorMessage != null && !errorMessage.isEmpty()) {
                result.append(errorMessage);
            }
        }
        String perspectiveErrorMessage = getPerspectiveErrorMessage(valueToValidate.getPerspectiveSettings());
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
    
    private <SettingsType extends Settings> String getComponentErrorMessage(ComponentIdAndSettings<SettingsType> componentAndSettings) {
        String errorMessage = null;
        @SuppressWarnings("unchecked")
        Validator<SettingsType> validator = (Validator<SettingsType>) validatorsMappedByComponent.get(componentAndSettings.getComponentId());
        if (validator != null) {
            errorMessage = validator.getErrorMessage(componentAndSettings.getSettings());
            if (errorMessage != null && !errorMessage.isEmpty() && !getClass().equals(validator.getClass())) {
                errorMessage += "; ";
            }
        }
        return errorMessage;
    }

}
