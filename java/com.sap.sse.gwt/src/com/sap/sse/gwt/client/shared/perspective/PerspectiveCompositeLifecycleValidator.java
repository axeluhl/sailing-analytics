package com.sap.sse.gwt.client.shared.perspective;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycleAndSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeLifecycleTabbedSettingsDialogComponent.ComponentLifecycleWithSettingsAndDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeLifecycleTabbedSettingsDialogComponent.PerspectiveLifecycleWithSettingsAndDialogComponent;

/**
 * A validator for the composite settings of a perspective based on the perspective lifecycle. 
 * @author Frank
 *
 * @param <PL>
 *      the {@link PerspectiveLifecycle} type
 * @param <PS>
 *      the {@link Perspective} settings type
 */
public class PerspectiveCompositeLifecycleValidator<PL extends PerspectiveLifecycle<PS, ?, ?>, PS extends Settings>
    implements Validator<PerspectiveCompositeLifecycleSettings<PL,PS>> {
    
    private final Map<ComponentLifecycleAndSettings<?,?>, Validator<?>> validatorsMappedByComponentLifecycle;
    private final Validator<PS> perspectiveValidator;

    public PerspectiveCompositeLifecycleValidator(PerspectiveLifecycleWithSettingsAndDialogComponent<PL,PS> perspectiveLifecycleAndDialogComponent,
            Iterable<ComponentLifecycleWithSettingsAndDialogComponent<?,?>> componentLifecycleAndDialogComponents) {
        perspectiveValidator = perspectiveLifecycleAndDialogComponent.getDialogComponent().getValidator();
        validatorsMappedByComponentLifecycle = new HashMap<>();
        for (ComponentLifecycleWithSettingsAndDialogComponent<?,?> component : componentLifecycleAndDialogComponents) {
            validatorsMappedByComponentLifecycle.put(component.getComponentLifecycleAndSettings(), component.getDialogComponent().getValidator());
        }
    }

    @Override
    public String getErrorMessage(PerspectiveCompositeLifecycleSettings<PL,PS> valueToValidate) {
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

    private String getPerspectiveErrorMessage(PerspectiveLifecycleAndSettings<PL, PS> perspectiveLifecycleAndSettings) {
        String errorMessage = null;
        if (perspectiveValidator != null) {
            errorMessage = perspectiveValidator.getErrorMessage(perspectiveLifecycleAndSettings.getSettings());
        }
        return errorMessage;
    }

    private <C extends ComponentLifecycle<S,?>, S extends Settings> String getComponentErrorMessage(ComponentLifecycleAndSettings<C,S> componentLifecycleAndSettings) {
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
