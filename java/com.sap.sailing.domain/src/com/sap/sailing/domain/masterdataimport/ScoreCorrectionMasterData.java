package com.sap.sailing.domain.masterdataimport;

import java.util.Map;

public class ScoreCorrectionMasterData {

    private String comment;
    private long timepointMillis;
    private Map<String, Iterable<SingleScoreCorrectionMasterData>> correctionForRaceColumns;

    public ScoreCorrectionMasterData(String comment, long timepointMillis,
            Map<String, Iterable<SingleScoreCorrectionMasterData>> correctionForRaceColumns) {
                this.comment = comment;
                this.timepointMillis = timepointMillis;
                this.correctionForRaceColumns = correctionForRaceColumns;
    }

    public String getComment() {
        return comment;
    }

    public long getTimepointMillis() {
        return timepointMillis;
    }

    public Map<String, Iterable<SingleScoreCorrectionMasterData>> getCorrectionForRaceColumns() {
        return correctionForRaceColumns;
    }
}
