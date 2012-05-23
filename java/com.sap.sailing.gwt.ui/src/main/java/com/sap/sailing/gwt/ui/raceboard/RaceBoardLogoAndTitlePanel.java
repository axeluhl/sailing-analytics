package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceDTO;

public class RaceBoardLogoAndTitlePanel extends LogoAndTitlePanel {
    private final DateTimeFormat dateFormatter = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM); 

    public RaceBoardLogoAndTitlePanel(RaceDTO raceDTO, StringMessages stringConstants) {
        super(raceDTO.name, stringConstants);
        
        String headlineText = raceDTO.name;
        if(raceDTO.startOfRace != null) {
            headlineText += " / " + dateFormatter.format(raceDTO.startOfRace); 
        }
        
        titleLabel.setText(headlineText);
    }
}
