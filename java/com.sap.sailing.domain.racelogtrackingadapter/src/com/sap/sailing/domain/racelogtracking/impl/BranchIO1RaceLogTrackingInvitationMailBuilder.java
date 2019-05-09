package com.sap.sailing.domain.racelogtracking.impl;

import java.util.Locale;

import com.sap.sailing.domain.common.BranchIOConstants;

public class BranchIO1RaceLogTrackingInvitationMailBuilder extends BranchIORaceLogTrackingInvitationMailBuilder {

    BranchIO1RaceLogTrackingInvitationMailBuilder(Locale locale) {
        super(locale);
    }

    @Override
    protected String getSailInsightBranchIO() {
        return BranchIOConstants.SAILINSIGHT_APP_BRANCHIO;
    }

    @Override
    protected String getBouyPingerBranchIO() {
        return BranchIOConstants.BUOYPINGER_APP_BRANCHIO;
    }

}
