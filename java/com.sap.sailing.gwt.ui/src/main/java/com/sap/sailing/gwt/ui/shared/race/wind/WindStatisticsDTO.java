package com.sap.sailing.gwt.ui.shared.race.wind;

public class WindStatisticsDTO extends AbstractWindDTO {

    private Double trueLowerboundWindInKnots;
    private Double trueUpperboundWindInKnots;

    protected WindStatisticsDTO() {
        super();
    }

    public WindStatisticsDTO(Double trueWindFromDeg, Double trueLowerboundWindInKnots, Double trueUpperboundWindInKnots) {
        super(trueWindFromDeg);
        this.trueLowerboundWindInKnots = trueLowerboundWindInKnots;
        this.trueUpperboundWindInKnots = trueUpperboundWindInKnots;
    }

    public Double getTrueLowerboundWindInKnots() {
        return trueLowerboundWindInKnots;
    }

    public Double getTrueUpperboundWindInKnots() {
        return trueUpperboundWindInKnots;
    }


}
