package com.sap.sse.security.ui.client.premium.settings;

import java.util.List;
import java.util.function.Consumer;

import com.google.gwt.core.client.GWT;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.generic.base.AbstractValueSetting;
import com.sap.sse.security.paywall.SecuredDTOProxy;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.premium.PaywallResolver;

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
    
    /*
     * If the user has permissions to access the setting, the actual value is returned. 
     * If the user does not have permission, or the dtoContext is not set (may be a default setting or an old setting from storage), the default value is returned.
     */
    @Override
    public final T getValue() {
        if(dtoContext != null 
                && dtoContext.isPresent()
                && paywallResolver.hasPermission(action, dtoContext.getSecuredDTO())) {
            return super.getValue();
        }else {
            return super.getDefaultValue();
        }
    }
    
    /*
     * If the user has permissions to access the setting, the actual value is overridden with the given parameter. 
     * If the user does not have permission, or the dtoContext is not set (may be a default setting or an old setting from storage), nothing happens.
     */
    @Override
    public void setValue(T value) {
        if(dtoContext != null && dtoContext.isPresent() && paywallResolver.hasPermission(action, dtoContext.getSecuredDTO())) {
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
    
    /*
     * Checks whether the paywallresolver and therefore the Setting is able to check the permission of the current user on the respective dto context. 
     * Default or Initial Seetings may not have a paywallresolver or dto context.
     */
    public boolean isPermissionAware() {
        return this.paywallResolver != null && this.dtoContext != null;
    }
    /*
     * Will call the given callback with a list of all #SubscriptionPlan that would grant the permissions to perform the associated action.
     */
    public void getUnlockingSubscriptionPlans(Consumer<List<String>> callback) {
        this.paywallResolver.getUnlockingSubscriptionPlans(action, dtoContext.getSecuredDTO(), callback);
    }

    public boolean hasPermission() {
        if(isPermissionAware()) {
            return paywallResolver.hasPermission(this.action, this.dtoContext.getSecuredDTO());
        }else {
            GWT.log("No Permission Awareness set for: " + settingName);
            return false;
        }
    }

    /*
     * Registers a #UserStatusEventHandler in the #PaywallResolver to be triggered when the user's permission change.
     */
    public void registerUserStatusEventHandler(UserStatusEventHandler handler) {
        if(isPermissionAware()) {
            paywallResolver.registerUserStatusEventHandler(handler);
        }else {
            GWT.log("No Permission Awareness set for: " + settingName);
        }
    }
}
