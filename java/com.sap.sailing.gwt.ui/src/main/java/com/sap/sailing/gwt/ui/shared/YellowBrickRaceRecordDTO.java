package com.sap.sailing.gwt.ui.shared;

import java.util.Collections;

public class YellowBrickRaceRecordDTO extends AbstractRaceRecordDTO {
    public String regattaName;
    public String yellowBrickRaceUrl;

    @Deprecated // GWT only
    YellowBrickRaceRecordDTO() {}
    
    public YellowBrickRaceRecordDTO(String regattaName, String yellowBrickRaceUrl, boolean hasRememberedRegatta) {
        super(yellowBrickRaceUrl, hasRememberedRegatta);
        this.regattaName = regattaName;
    }

    @Override
    public Iterable<String> getBoatClassNames() {
        return Collections.emptySet();
    }
}