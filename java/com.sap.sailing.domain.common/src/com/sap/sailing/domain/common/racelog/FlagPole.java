package com.sap.sailing.domain.common.racelog;

public class FlagPole {
    
    private final Flags upperFlag;
    private final Flags lowerFlag;
    private final boolean isDisplayed;
    
    public FlagPole(Flags flag, boolean isDisplayed) {
        this(flag, Flags.NONE, isDisplayed);
    }
    
    public FlagPole(Flags upperFlag, Flags lowerFlag, boolean isDisplayed) {
        this.upperFlag = upperFlag;
        this.lowerFlag = lowerFlag;
        this.isDisplayed = isDisplayed;
    }

    public Flags getUpperFlag() {
        return upperFlag;
    }

    public Flags getLowerFlag() {
        return lowerFlag;
    }
    
    public boolean isDisplayed() {
        return isDisplayed;
    }

    public boolean describesSame(FlagPole otherPole) {
        return this.upperFlag == otherPole.upperFlag &&
                this.lowerFlag == otherPole.lowerFlag &&
                this.isDisplayed == otherPole.isDisplayed;
    }

}
