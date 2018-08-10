package com.sap.sailing.server.impl.preferences.model;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.SailorProfile;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.generic.base.AbstractValueSetting;
import com.sap.sse.common.settings.value.StringValue;
import com.sap.sse.common.settings.value.Value;

/**
 * Specific setting to hold a {@link BoatClass} instance. The competitor is being serialized using the boat classes'
 * name and deserialized using the given {@link DomainFactory}.
 */
public class SailorProfileSetting extends AbstractValueSetting<SailorProfile> {

    protected SailorProfileSetting(String name, AbstractGenericSerializableSettings settings, DomainFactory domainFactory) {
        super(name, settings, null, new Converter());
    }

    public static class Converter implements ValueConverter<SailorProfile> {


        public Converter() {
        }

        @Override
        public Object toJSONValue(SailorProfile value) {
            return toStringValue(value);
        }

        @Override
        public SailorProfile fromJSONValue(Object jsonValue) {
            return fromStringValue((String) jsonValue);
        }

        @Override
        public String toStringValue(SailorProfile value) {
            return value == null ? null : value.getName();
        }

        @Override
        public SailorProfile fromStringValue(String stringValue) {
            // TODO implement to return correct sailor profile
            return stringValue == null ? null : null;
        }

        @Override
        public SailorProfile fromValue(Value value) {
            return fromStringValue(((StringValue) value).getValue());
        }

        @Override
        public Value toValue(SailorProfile value) {
            return new StringValue(toStringValue(value));
        }
    }
}
