package com.sap.sse.gwt.client.shared.components;

import java.util.ArrayList;

import com.sap.sse.common.Util;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.CompositeSettings.ComponentAndSettingsPair;

public class CompositeValidator implements Validator<CompositeSettings> {
    private static class ComponentAndValidator<SettingsType extends Settings> extends Util.Pair<SettingsDialogComponent<SettingsType>, Validator<SettingsType>> {
        private static final long serialVersionUID = -4190322565836849861L;

        public ComponentAndValidator(SettingsDialogComponent<SettingsType> a, Validator<SettingsType> b) {
            super(a, b);
        }
    }
    
    private final Iterable<ComponentAndValidator<?>> validators;

    public CompositeValidator(Iterable<SettingsDialogComponent<?>> dialogComponents) {
        ArrayList<ComponentAndValidator<?>> v = new ArrayList<ComponentAndValidator<?>>();
        for (SettingsDialogComponent<?> dialogComponent : dialogComponents) {
            v.add(getComponentAndValidator(dialogComponent));
        }
        validators = v;
    }

    private <SettingsType extends Settings> ComponentAndValidator<SettingsType> getComponentAndValidator(SettingsDialogComponent<SettingsType> dialogComponents) {
        return new ComponentAndValidator<SettingsType>(dialogComponents, dialogComponents.getValidator());
    }

    @Override
    public String getErrorMessage(CompositeSettings valueToValidate) {
        StringBuilder result = new StringBuilder();
        for (ComponentAndSettingsPair<?> componentAndSettings : valueToValidate.getSettingsPerComponent()) {
            final String errorMessage = getErrorMessage(componentAndSettings);
            if (errorMessage != null) {
                result.append(errorMessage);
                result.append("; ");
            }
        }
        return result.toString();
    }

    private <SettingsType extends Settings> String getErrorMessage(ComponentAndSettingsPair<SettingsType> componentAndSettings) {
        for (ComponentAndValidator<?> componentAndValidator : validators) {
            if (componentAndValidator.getA() == componentAndSettings.getA()) {
                @SuppressWarnings("unchecked")
                final Validator<SettingsType> validator = (Validator<SettingsType>) componentAndValidator.getB();
                return validator.getErrorMessage(componentAndSettings.getB());
            }
        }
        return null;
    }

}
