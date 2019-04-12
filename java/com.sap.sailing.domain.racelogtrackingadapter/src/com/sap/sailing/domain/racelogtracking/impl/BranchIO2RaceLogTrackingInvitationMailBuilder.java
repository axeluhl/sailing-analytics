package com.sap.sailing.domain.racelogtracking.impl;

import java.util.Locale;

import com.sap.sailing.domain.common.BranchIOConstants;

public class BranchIO2RaceLogTrackingInvitationMailBuilder extends BranchIORaceLogTrackingInvitationMailBuilder {

    BranchIO2RaceLogTrackingInvitationMailBuilder(Locale locale) {
        super(locale);
    }

    @Override
    protected String getSailInsightBranchIO() {
        return BranchIOConstants.SAILINSIGHT_2_APP_BRANCHIO;
    }

    @Override
    protected String getBouyPingerBranchIO() {
        // FIXME the app should in the future at some point also support bouy pinging, replace this then
        return BranchIOConstants.BUOYPINGER_APP_BRANCHIO;
    }

}
