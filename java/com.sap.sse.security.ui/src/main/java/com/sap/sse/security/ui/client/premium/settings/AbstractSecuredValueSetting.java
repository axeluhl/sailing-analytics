package com.sap.sse.security.ui.client.premium.settings;

import com.google.gwt.core.client.GWT;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.generic.base.AbstractValueSetting;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.premium.PaywallResolver;
import com.sap.sse.security.ui.client.premium.PaywallResolverProxy;
import com.sap.sse.security.ui.client.premium.SecuredDTOProxy;

public abstract class AbstractSecuredValueSetting<T> extends AbstractValueSetting<T> {

    private final PaywallResolverProxy paywallResolverProxy;
    private final Action action;
    private final SecuredDTOProxy dtoContextProxy;

    protected AbstractSecuredValueSetting(String name, AbstractGenericSerializableSettings settings, T defaultValue,
            ValueConverter<T> valueConverter, PaywallResolverProxy paywallResolverProxy, Action action,
            SecuredDTOProxy dtoContextProxy) {
        super(name, settings, defaultValue, valueConverter);
        this.paywallResolverProxy = paywallResolverProxy;
        this.action = action;
        this.dtoContextProxy = dtoContextProxy;
    }

    @Override
    public T getValue() {
        if (paywallResolverProxy.getPaywallResolver() == null) {
            GWT.log("getValue paywall resolver not available.");
        }
        if (dtoContextProxy.getSecuredDTO() == null) {
            GWT.log("getValue securedDTO context not available!");
        }
        if (dtoContextProxy != null && dtoContextProxy.isPresent() && paywallResolverProxy != null
                && paywallResolverProxy.getPaywallResolver().hasPermission(action, dtoContextProxy.getSecuredDTO())) {
            return super.getValue();
        } else {
            return super.getDefaultValue();
        }
    }

    @Override
    public void setValue(T value) {
        if (paywallResolverProxy.getPaywallResolver() == null) {
            GWT.log("setValue Paywall resolver not available.");
        }
        if (dtoContextProxy == null) {
            GWT.log("setValue DTOContext not set!");
        }
        if (dtoContextProxy != null && dtoContextProxy.isPresent() && paywallResolverProxy.getPaywallResolver() != null
                && paywallResolverProxy.getPaywallResolver().hasPermission(action, dtoContextProxy.getSecuredDTO())) {
            super.setValue(value);
        }
    }

    public PaywallResolver getPaywallResolver() {
        return paywallResolverProxy.getPaywallResolver();
    }

    public void setPaywallResolver(PaywallResolver paywallResolver) {
        paywallResolverProxy.setPaywallResolver(paywallResolver);
    }

    public Action getAction() {
        return action;
    }

    public SecuredDTOProxy getDtoContext() {
        return dtoContextProxy;
    }
}
