package com.sap.sailing.gwt.settings.client.base;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardUrlSettings;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.StringSetting;

public class RegattaAndRaceIdentifierSettings extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = -6633826106133939710L;

    private transient StringSetting raceName;
    private transient StringSetting regattaName;

    public RegattaAndRaceIdentifierSettings() {
    }

    public RegattaAndRaceIdentifierSettings(RegattaAndRaceIdentifier regattaAndRaceIdentifier) {
        raceName.setValue(regattaAndRaceIdentifier == null ? null : regattaAndRaceIdentifier.getRaceName());
        regattaName.setValue(regattaAndRaceIdentifier == null ? null : regattaAndRaceIdentifier.getRegattaName());
    }

    @Override
    protected void addChildSettings() {
        raceName = new StringSetting(LeaderboardUrlSettings.PARAM_RACE_NAME, this);
        regattaName = new StringSetting(LeaderboardUrlSettings.PARAM_REGATTA_NAME, this);
    }

    public String getRaceName() {
        return raceName.getValue();
    }

    public String getRegattaName() {
        return regattaName.getValue();
    }

    public RegattaAndRaceIdentifier getRegattaAndRaceIdentifier() {
        final String raceName = this.raceName.getValue();
        final String regattaName = this.regattaName.getValue();
        RegattaAndRaceIdentifier result;
        if (raceName != null && !raceName.isEmpty() && regattaName != null && !regattaName.isEmpty()) {
            result = new RegattaNameAndRaceName(regattaName, raceName);
        } else {
            result = null;
        }
        return result;
    }
}
