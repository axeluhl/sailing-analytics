package com.sap.sailing.gwt.ui.shared;

import java.util.Collections;

/**
 * The "race URL" is the part of the YellowBrick API URL that provides an ID representing the race. For example, the
 * "race URL" for the Rolex Middle Sea Race 2019 is "rmsr2019".
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class YellowBrickRaceRecordDTO extends AbstractRaceRecordDTO {
    public String raceUrl;

    @Deprecated // GWT only
    YellowBrickRaceRecordDTO() {
    }

    public YellowBrickRaceRecordDTO(String name, String raceUrl, boolean hasRememberedRegatta) {
        super(name, hasRememberedRegatta);
        this.raceUrl = raceUrl;
    }

    @Override
    public Iterable<String> getBoatClassNames() {
        return Collections.emptySet();
    }
}