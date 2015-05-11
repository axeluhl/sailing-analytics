package com.sap.sailing.gwt.ui.shared.race;

import com.google.gwt.core.shared.GwtIncompatible;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;

public class FlagStateDTO implements IsSerializable {
    private Flags lastUpperFlag;
    private Flags lastLowerFlag;
    private boolean lastFlagsAreDisplayed;
    private boolean lastFlagsDisplayedStateChanged;

    @SuppressWarnings("unused")
    private FlagStateDTO() {
    }

    @GwtIncompatible
    public FlagStateDTO(FlagPole mostInterestingFlagPole, FlagPoleState previousFlagState) {
        lastLowerFlag = mostInterestingFlagPole.getLowerFlag();
        lastUpperFlag = mostInterestingFlagPole.getUpperFlag();
        lastFlagsAreDisplayed = mostInterestingFlagPole.isDisplayed();
        lastFlagsDisplayedStateChanged = previousFlagState.hasPoleChanged(mostInterestingFlagPole);
    }

    public Flags getLastUpperFlag() {
        return lastUpperFlag;
    }

    public Flags getLastLowerFlag() {
        return lastLowerFlag;
    }

    public boolean isLastFlagsAreDisplayed() {
        return lastFlagsAreDisplayed;
    }

    public boolean isLastFlagsDisplayedStateChanged() {
        return lastFlagsDisplayedStateChanged;
    }
}
