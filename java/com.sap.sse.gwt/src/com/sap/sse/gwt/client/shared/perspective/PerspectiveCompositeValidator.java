package com.sap.sse.gwt.client.shared.perspective;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentAndSettingsPair;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeTabbedSettingsDialogComponent.ComponentAndDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeTabbedSettingsDialogComponent.PerspectiveAndDialogComponent;

public class PerspectiveCompositeValidator implements Validator<PerspectiveCompositeSettings> {
    
    private final Map<Component<?>, Validator<?>> validatorsMappedByComponent;
    private final PerspectiveAndDialogComponent<?> perspectiveAndDialogComponent;
    
    public PerspectiveCompositeValidator(PerspectiveAndDialogComponent<?> perspectiveAndDialogComponent, Iterable<ComponentAndDialogComponent<?>> componentsAndDialogComponents) {
        this.perspectiveAndDialogComponent = perspectiveAndDialogComponent;
        validatorsMappedByComponent = new HashMap<>();
        for (ComponentAndDialogComponent<?> componentsAndSettingsDialog : componentsAndDialogComponents) {
            validatorsMappedByComponent.put(componentsAndSettingsDialog.getComponent(), componentsAndSettingsDialog.getSettingsDialog().getValidator());
        }
    }

    @Override
    public String getErrorMessage(PerspectiveCompositeSettings valueToValidate) {
        StringBuilder result = new StringBuilder();
        for (ComponentAndSettingsPair<?> componentAndSettings : valueToValidate.getSettingsPerComponent()) {
            final String errorMessage = getComponentErrorMessage(componentAndSettings);
            if (errorMessage != null && !errorMessage.isEmpty()) {
                result.append(errorMessage);
            }
        }
        PerspectiveAndSettingsPair<?> perspectiveSettings = valueToValidate.getPerspectiveSettings();
        @SuppressWarnings("unchecked")
        Validator<Settings> validator = (Validator<Settings>) perspectiveAndDialogComponent.getSettingsDialog().getValidator();
        String perspectiveErrorMessage = validator.getErrorMessage(perspectiveSettings.getSettings());
        if (perspectiveErrorMessage != null && !perspectiveErrorMessage.isEmpty()) {
            result.append(perspectiveErrorMessage);
        }
        
        return result.toString();
    }

    private <SettingsType extends Settings> String getComponentErrorMessage(ComponentAndSettingsPair<SettingsType> componentAndSettings) {
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
