package com.sap.sailing.gwt.ui.adminconsole;

public class RaceMapSettings {

    private boolean showDouglasPeuckerPoints = false;

    private boolean showManeuverHeadUp = false;

    private boolean showManeuverBearAway = false;

    private boolean showManeuverTack = false;

    private boolean showManeuverJibe = false;

    private boolean showManeuverPenaltyCircle = false;

    private boolean showManeuverMarkPassing = false;

    private boolean showManeuverOther = false;

    private boolean showOnlySelectedCompetitors = false;

    private long tailLengthInMilliSeconds = 30000l;

    public long getTailLengthInMilliSeconds() {
        return tailLengthInMilliSeconds;
    }

    public void setTailLengthInMilliSeconds(long tailLengthInMilliSeconds) {
        this.tailLengthInMilliSeconds = tailLengthInMilliSeconds;
    }

    public boolean isShowDouglasPeuckerPoints() {
        return showDouglasPeuckerPoints;
    }

    public void setShowDouglasPeuckerPoints(boolean showDouglasPeuckerPoints) {
        this.showDouglasPeuckerPoints = showDouglasPeuckerPoints;
    }

    public boolean isShowManeuverHeadUp() {
        return showManeuverHeadUp;
    }

    public void setShowManeuverHeadUp(boolean showManeuverHeadUp) {
        this.showManeuverHeadUp = showManeuverHeadUp;
    }

    public boolean isShowManeuverBearAway() {
        return showManeuverBearAway;
    }

    public void setShowManeuverBearAway(boolean showManeuverBearAway) {
        this.showManeuverBearAway = showManeuverBearAway;
    }

    public boolean isShowManeuverTack() {
        return showManeuverTack;
    }

    public void setShowManeuverTack(boolean showManeuverTack) {
        this.showManeuverTack = showManeuverTack;
    }

    public boolean isShowManeuverJibe() {
        return showManeuverJibe;
    }

    public void setShowManeuverJibe(boolean showManeuverJibe) {
        this.showManeuverJibe = showManeuverJibe;
    }

    public boolean isShowManeuverPenaltyCircle() {
        return showManeuverPenaltyCircle;
    }

    public void setShowManeuverPenaltyCircle(boolean showManeuverPenaltyCircle) {
        this.showManeuverPenaltyCircle = showManeuverPenaltyCircle;
    }

    public boolean isShowManeuverMarkPassing() {
        return showManeuverMarkPassing;
    }

    public void setShowManeuverMarkPassing(boolean showManeuverMarkPassing) {
        this.showManeuverMarkPassing = showManeuverMarkPassing;
    }

    public boolean isShowManeuverOther() {
        return showManeuverOther;
    }

    public void setShowManeuverOther(boolean showManeuverOther) {
        this.showManeuverOther = showManeuverOther;
    }

    public boolean isShowOnlySelectedCompetitors() {
        return showOnlySelectedCompetitors;
    }

    public void setShowOnlySelectedCompetitors(boolean showOnlySelectedCompetitors) {
        this.showOnlySelectedCompetitors = showOnlySelectedCompetitors;
    }

}
