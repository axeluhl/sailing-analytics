package com.sap.sailing.server.impl.preferences.model;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.generic.base.AbstractValueSetting;
import com.sap.sse.common.settings.value.StringValue;
import com.sap.sse.common.settings.value.Value;

/**
 * Specific setting to hold a {@link Competitor} instance. The competitor is being serialized as ID and deserialized
 * doing as lookup in the given {@link CompetitorStore}.
 */
public class CompetitorSetting extends AbstractValueSetting<Competitor> {

    protected CompetitorSetting(String name, AbstractGenericSerializableSettings settings,
            CompetitorStore competitorStore) {
        super(name, settings, null, new Converter(competitorStore));
    }

    public static class Converter implements ValueConverter<Competitor> {

        private final CompetitorStore competitorStore;

        public Converter(CompetitorStore competitorStore) {
            this.competitorStore = competitorStore;
        }

        @Override
        public Object toJSONValue(Competitor value) {
            return toStringValue(value);
        }

        @Override
        public Competitor fromJSONValue(Object jsonValue) {
            return fromStringValue((String) jsonValue);
        }

        @Override
        public String toStringValue(Competitor value) {
            return value == null ? null : value.getId().toString();
        }

        @Override
        public Competitor fromStringValue(String stringValue) {
            return stringValue == null ? null : competitorStore.getExistingCompetitorByIdAsString(stringValue);
        }

        @Override
        public Competitor fromValue(Value value) {
            return fromStringValue(((StringValue) value).getValue());
        }

        @Override
        public Value toValue(Competitor value) {
            return new StringValue(toStringValue(value));
        }
    }
}
