package com.sap.sailing.gwt.ui.shared.dispatch.regatta;

import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;

public class RegattaWithProgressDTO extends RegattaMetadataDTO {
    private RegattaProgressDTO progress;
    
    @SuppressWarnings("unused")
    private RegattaWithProgressDTO() {
    }

    public RegattaWithProgressDTO(RegattaProgressDTO progress) {
        super();
        this.progress = progress;
    }
    
    public RegattaProgressDTO getProgress() {
        return progress;
    }
}
