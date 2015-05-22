package com.sap.sailing.gwt.ui.shared.race;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.racelog.Flags;

public class FlagStateDTO implements IsSerializable {
    private Flags lastUpperFlag;
    private Flags lastLowerFlag;
    private boolean lastFlagsAreDisplayed;
    private boolean lastFlagsDisplayedStateChanged;

    @SuppressWarnings("unused")
    private FlagStateDTO() {
    }

    public FlagStateDTO(Flags lastUpperFlag, Flags lastLowerFlag, boolean lastFlagsAreDisplayed,
            boolean lastFlagsDisplayedStateChanged) {
        super();
        this.lastUpperFlag = lastUpperFlag;
        this.lastLowerFlag = lastLowerFlag;
        this.lastFlagsAreDisplayed = lastFlagsAreDisplayed;
        this.lastFlagsDisplayedStateChanged = lastFlagsDisplayedStateChanged;
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
