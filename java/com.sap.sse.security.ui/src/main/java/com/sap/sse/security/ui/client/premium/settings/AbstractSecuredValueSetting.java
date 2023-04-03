package com.sap.sse.security.ui.client.premium.settings;

import java.util.List;
import java.util.function.Consumer;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.generic.base.AbstractValueSetting;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.premium.PaywallResolver;
import com.sap.sse.security.ui.client.premium.SecuredDTOProxy;

public abstract class AbstractSecuredValueSetting<T>  extends AbstractValueSetting<T>{

    private PaywallResolver paywallResolver;
    private Action action;
    private SecuredDTOProxy dtoContext;

    protected AbstractSecuredValueSetting(String name, AbstractGenericSerializableSettings settings, T defaultValue,
            ValueConverter<T> valueConverter, PaywallResolver paywallResolver, Action action, SecuredDTOProxy dtoContext) {
        super(name, settings, defaultValue, valueConverter);
        this.paywallResolver = paywallResolver;
        this.action = action;
        this.dtoContext = dtoContext;
    }
    
    @Override
    public final T getValue() {
        if(dtoContext.isPresent() && paywallResolver.hasPermission(action, dtoContext.getSecuredDTO())) {
            return super.getValue();
        }else {
            return super.getDefaultValue();
        }
    }
    
    @Override
    public void setValue(T value) {
        if(dtoContext.isPresent() && paywallResolver.hasPermission(action, dtoContext.getSecuredDTO())) {
            super.setValue(value);
        }
    }
    
    @Override
    public final void resetToDefault() {
        super.setValue(getDefaultValue());
    }
    
    @Override
    public final boolean isDefaultValue() {
        final T value = super.getValue();
        if (value == getDefaultValue()) {
            return true;
        }
        if (value != null) {
            return value.equals(getDefaultValue());
        }
        // value == null && defaultValue != null => value isn't default
        return false;
    }
    
    public void getUnlockingSubscriptionPlans(Consumer<List<String>> callback) {
        this.paywallResolver.getUnlockingSubscriptionPlans(action, dtoContext.getSecuredDTO(), callback);
    }

    public boolean hasPermission() {
        return paywallResolver.hasPermission(this.action, this.dtoContext.getSecuredDTO());
    }

    public void registerUserStatusEventHandler(UserStatusEventHandler handler) {
       paywallResolver.registerUserStatusEventHandler(handler);
    }
}
