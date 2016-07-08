package com.sap.sailing.domain.common.preferences;

import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.EnumSetting;
import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.generic.base.AbstractValueSetting;
import com.sap.sse.common.settings.value.StringValue;
import com.sap.sse.common.settings.value.Value;

/**
 * We could use {@link EnumSetting} but this woul lead to problems if a enum constant in {@link BoatClassMasterdata} is
 * renamed. This Setting instead used the display name that is also used in the backend to store which boat class is
 * meant. {@link BoatClassMasterdata} can resolve the enum values from the display named which also works with some
 * alternative names so that we don'T rely on the exact display name but that the display name will at least be
 * available as alternative name.
 */
public class BoatClassSetting extends AbstractValueSetting<BoatClassMasterdata> {

    protected BoatClassSetting(String name, AbstractGenericSerializableSettings settings) {
        super(name, settings, null, Converter.INSTANCE);
    }

    public static class Converter implements ValueConverter<BoatClassMasterdata> {
        
        public static final Converter INSTANCE = new Converter();

        @Override
        public Object toJSONValue(BoatClassMasterdata value) {
            return value == null ? null : value.getDisplayName();
        }

        @Override
        public BoatClassMasterdata fromJSONValue(Object jsonValue) {
            return fromStringValue((String) jsonValue);
        }

        @Override
        public String toStringValue(BoatClassMasterdata value) {
            return value == null ? null : value.getDisplayName();
        }

        @Override
        public BoatClassMasterdata fromStringValue(String stringValue) {
            return stringValue == null ? null : BoatClassMasterdata.resolveBoatClass(stringValue);
        }

        @Override
        public BoatClassMasterdata fromValue(Value value) {
            return fromStringValue(((StringValue) value).getValue());
        }

        @Override
        public Value toValue(BoatClassMasterdata value) {
            return new StringValue(toStringValue(value));
        }
    }
}
