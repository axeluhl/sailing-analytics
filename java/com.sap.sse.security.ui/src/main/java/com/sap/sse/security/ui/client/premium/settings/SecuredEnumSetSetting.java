package com.sap.sse.security.ui.client.premium.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettingsWithContext;
import com.sap.sse.common.settings.generic.StringToEnumConverter;
import com.sap.sse.common.settings.generic.base.AbstractValueSetSetting;
import com.sap.sse.common.settings.generic.converter.EnumConverter;
import com.sap.sse.common.settings.value.Value;
import com.sap.sse.common.settings.value.ValueCollectionValue;
import com.sap.sse.security.shared.HasPermissions.SecuredEnum;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.premium.PaywallResolver;

public class SecuredEnumSetSetting<T extends Enum<T> & SecuredEnum> extends AbstractValueSetSetting<T> {
    
    private final PaywallResolver paywallResolver;
    private final SecuredDTO securedDto;

    public SecuredEnumSetSetting(String name, AbstractGenericSerializableSettingsWithContext<?> settings,
            Iterable<T> defaultValues, StringToEnumConverter<T> stringToEnumConverter,
            PaywallResolver paywallResolver, SecuredDTO securedDto) {
        super(name, settings, defaultValues, new EnumConverter<>(stringToEnumConverter));
        this.paywallResolver = paywallResolver;
        this.securedDto = securedDto;
    }

    @Override
    public Iterable<T> getValues() {
        if (paywallResolver == null) {
            GWT.log("getValue paywall resolver not available.");
        }
        if (securedDto == null) {
            GWT.log("getValue securedDTO context not available!");
        }
        ValueCollectionValue<Set<Value>> value = getValue();
        // TODO bug5774 clean up later
        GWT.log("getValues() last, result: " + value.getValues(getValueConverter()));
        Iterable<T> result = value.getValues(getValueConverter());
        Collection<T> permittedCollection = new ArrayList<T>();
        for (T singleValue: result) {
            if (securedDto == null 
                    || paywallResolver != null 
                    && paywallResolver.hasPermission(singleValue.getPremiumAction(), securedDto)) {
                permittedCollection.add(singleValue);
            }
        }
        return permittedCollection;
    }
}
