package com.sap.sailing.server.impl.preferences.model;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.base.AbstractValueSetting;

/**
 * Specific setting to hold a {@link Competitor} instance. The competitor is being serialized as ID and deserialized
 * doing as lookup in the given {@link CompetitorAndBoatStore}.
 */
public class CompetitorSetting extends AbstractValueSetting<Competitor> {

    protected CompetitorSetting(String name, AbstractGenericSerializableSettings settings,
            CompetitorAndBoatStore competitorStore) {
        super(name, settings, null, new CompetitorConverter(competitorStore));
    }
}
