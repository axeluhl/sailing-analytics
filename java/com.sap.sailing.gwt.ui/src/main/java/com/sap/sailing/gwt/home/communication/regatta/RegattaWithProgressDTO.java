package com.sap.sailing.gwt.home.communication.regatta;

import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.home.communication.eventview.RegattaReferenceDTO;

public class RegattaWithProgressDTO extends RegattaMetadataDTO {
    private RegattaProgressDTO progress;
    
    public RegattaWithProgressDTO() {
    }

    public RegattaWithProgressDTO(RegattaProgressDTO progress) {
        super();
        this.progress = progress;
    }
    
    public RegattaProgressDTO getProgress() {
        return progress;
    }
    
    public void setProgress(RegattaProgressDTO progress) {
        this.progress = progress;
    }
    
    @Override
    public int compareTo(RegattaReferenceDTO o) {
        if (o instanceof RegattaWithProgressDTO) {
            RegattaWithProgressDTO other = (RegattaWithProgressDTO) o;
            RegattaState state = getState();
            RegattaState otherState = other.getState();
            if(state != otherState) {
                if(state == RegattaState.RUNNING) {
                    return -1;
                }
                if(otherState == RegattaState.RUNNING) {
                    return 1;
                }
                if(state == RegattaState.PROGRESS) {
                    return -1;
                }
                if(otherState == RegattaState.PROGRESS) {
                    return 1;
                }
                if(state == RegattaState.FINISHED) {
                    return -1;
                }
                if(otherState == RegattaState.FINISHED) {
                    return 1;
                }
            }
        }
        return super.compareTo(o);
    }
}
