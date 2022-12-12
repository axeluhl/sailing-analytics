package com.sap.sse.security.paywall.settings;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.generic.base.AbstractValueSetting;
import com.sap.sse.security.paywall.PaywallResolver;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;

public abstract class AbstractSecuredValueSetting<T>  extends AbstractValueSetting<T>{

    private PaywallResolver paywallResolver;
    private Action action;
    private SecuredDTO dtoContext;

    protected AbstractSecuredValueSetting(String name, AbstractGenericSerializableSettings settings, T defaultValue,
            ValueConverter<T> valueConverter, PaywallResolver paywallResolver, Action action, SecuredDTO dtoContext) {
        super(name, settings, defaultValue, valueConverter);
        this.paywallResolver = paywallResolver;
        this.action = action;
        this.dtoContext = dtoContext;
    }
    
    @Override
    public T getValue() {
        if(paywallResolver.hasPermission(action, dtoContext)) {
            return super.getValue();
        }else {
            return super.getDefaultValue();
        }
    }
    
    @Override
    public void setValue(T value) {
        if(paywallResolver.hasPermission(action, dtoContext)) {
            super.setValue(value);
        }
    }
}
