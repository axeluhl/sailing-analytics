package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Date;

import com.sap.sailing.gwt.ui.shared.race.FlagStateDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceProgressDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleWindDTO;
import com.sap.sse.common.Util;

public class LiveRaceDTO extends RaceMetadataDTO implements Comparable<LiveRaceDTO> {
    
    private FlagStateDTO flagState;
    
    private SimpleWindDTO wind;
    
    private RaceProgressDTO progress;

    @SuppressWarnings("unused")
    private LiveRaceDTO() {
    }
    
    public LiveRaceDTO(String regattaName, String raceName) {
        super(regattaName, raceName);
    }

    public SimpleWindDTO getWind() {
        return wind;
    }

    public void setWind(SimpleWindDTO wind) {
        this.wind = wind;
    }

    public FlagStateDTO getFlagState() {
        return flagState;
    }

    public void setFlagState(FlagStateDTO flagState) {
        this.flagState = flagState;
    }

    public RaceProgressDTO getProgress() {
        return progress;
    }

    public void setProgress(RaceProgressDTO progress) {
        this.progress = progress;
    }

    @Override
    public int compareTo(LiveRaceDTO o) {
        Date thisStart = getStart();
        Date otherStart = o.getStart();
        if(Util.equalsWithNull(thisStart, otherStart)) {
            return 0;
        }
        if(thisStart == null) {
            return 1;
        }
        if(otherStart == null) {
            return -1;
        }
        return -thisStart.compareTo(otherStart);
    }
}
