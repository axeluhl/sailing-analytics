package com.sap.sse.security.ui.client.premium.settings;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.generic.base.AbstractValueSetting;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.premium.PaywallResolver;
import com.sap.sse.security.ui.client.premium.SecuredDTOProxy;

public abstract class AbstractSecuredValueSetting<T> extends AbstractValueSetting<T> {

    private final PaywallResolver paywallResolver;
    private final Action action;
    private final SecuredDTOProxy dtoContext;

    protected AbstractSecuredValueSetting(String name, AbstractGenericSerializableSettings settings, T defaultValue,
            ValueConverter<T> valueConverter, PaywallResolver paywallResolver, Action action,
            SecuredDTOProxy dtoContext) {
        super(name, settings, defaultValue, valueConverter);
        this.paywallResolver = paywallResolver;
        this.action = action;
        this.dtoContext = dtoContext;
    }

    @Override
    public T getValue() {
        if (dtoContext.isPresent() && paywallResolver.hasPermission(action, dtoContext.getSecuredDTO())) {
            return super.getValue();
        } else {
            return super.getDefaultValue();
        }
    }

    @Override
    public void setValue(T value) {
        if (dtoContext.isPresent() && paywallResolver.hasPermission(action, dtoContext.getSecuredDTO())) {
            super.setValue(value);
        }
    }

    public PaywallResolver getPaywallResolver() {
        return paywallResolver;
    }

    public Action getAction() {
        return action;
    }

    public SecuredDTOProxy getDtoContext() {
        return dtoContext;
    }
}
