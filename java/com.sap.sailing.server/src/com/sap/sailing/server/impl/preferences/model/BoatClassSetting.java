package com.sap.sailing.server.impl.preferences.model;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.generic.base.AbstractValueSetting;
import com.sap.sse.common.settings.value.StringValue;
import com.sap.sse.common.settings.value.Value;

/**
 * Specific setting to hold a {@link BoatClass} instance. The competitor is being serialized using the boat classes'
 * name and deserialized using the given {@link DomainFactory}.
 */
public class BoatClassSetting extends AbstractValueSetting<BoatClass> {

    protected BoatClassSetting(String name, AbstractGenericSerializableSettings settings, DomainFactory domainFactory) {
        super(name, settings, null, new Converter(domainFactory));
    }

    public static class Converter implements ValueConverter<BoatClass> {

        private final DomainFactory domainFactory;

        public Converter(DomainFactory domainFactory) {
            this.domainFactory = domainFactory;
        }

        @Override
        public Object toJSONValue(BoatClass value) {
            return toStringValue(value);
        }

        @Override
        public BoatClass fromJSONValue(Object jsonValue) {
            return fromStringValue((String) jsonValue);
        }

        @Override
        public String toStringValue(BoatClass value) {
            return value == null ? null : value.getDisplayName();
        }

        @Override
        public BoatClass fromStringValue(String stringValue) {
            return stringValue == null ? null : domainFactory.getOrCreateBoatClass(stringValue);
        }

        @Override
        public BoatClass fromValue(Value value) {
            return fromStringValue(((StringValue) value).getValue());
        }

        @Override
        public Value toValue(BoatClass value) {
            return new StringValue(toStringValue(value));
        }
    }
}
