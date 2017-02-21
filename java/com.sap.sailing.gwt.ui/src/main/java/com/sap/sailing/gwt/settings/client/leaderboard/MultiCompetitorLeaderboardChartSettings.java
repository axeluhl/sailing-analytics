package com.sap.sailing.gwt.settings.client.leaderboard;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.EnumSetting;
import com.sap.sse.common.settings.generic.StringToEnumConverter;

public class MultiCompetitorLeaderboardChartSettings extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = 2456555424331709738L;

    private EnumSetting<DetailType> detailType;

    public MultiCompetitorLeaderboardChartSettings(DetailType detailType) {
        this.detailType.setValue(detailType);
    }

    @Override
    protected void addChildSettings() {
        detailType = new EnumSetting<>("detailType", this, new StringToEnumConverter<DetailType>() {
            @Override
            public DetailType fromString(String stringValue) {
                return DetailType.valueOf(stringValue);
            }
        });
    }

    public DetailType getDetailType() {
        return detailType.getValue();
    }
}
