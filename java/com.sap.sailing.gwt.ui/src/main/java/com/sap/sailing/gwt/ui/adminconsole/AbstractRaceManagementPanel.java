package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public abstract class AbstractRaceManagementPanel extends AbstractEventManagementPanel implements RaceSelectionChangeListener {
    protected RegattaAndRaceIdentifier singleSelectedRace;
    
    protected RaceDTO selectedRaceDTO;
    
    protected final CaptionPanel selectedCaptionRacePanel;
    
    protected List<RegattaDTO> savedEvents;
    
    protected final VerticalPanel selectedRaceContentPanel;
    
    public AbstractRaceManagementPanel(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            RegattaRefresher regattaRefresher, StringMessages stringMessages) {
        super(sailingService, regattaRefresher, errorReporter, new RaceSelectionModel(), stringMessages);

        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");
        
        mainPanel.add(trackedRacesListComposite);

        trackedRacesListComposite.addRaceSelectionChangeListener(this);
        
        singleSelectedRace = null;
        
        selectedCaptionRacePanel = new CaptionPanel(stringMessages.race());
        selectedCaptionRacePanel.setWidth("100%");
        mainPanel.add(selectedCaptionRacePanel);

        selectedRaceContentPanel = new VerticalPanel();
        selectedRaceContentPanel.setWidth("100%");
        selectedCaptionRacePanel.setContentWidget(selectedRaceContentPanel);
        selectedCaptionRacePanel.setVisible(false);
    }

    abstract void refreshSelectedRaceData();

    @Override
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
        if (selectedRaces.size() == 1) {
            singleSelectedRace = selectedRaces.get(0);
            selectedCaptionRacePanel.setCaptionText(singleSelectedRace.getRaceName());
            selectedCaptionRacePanel.setVisible(true);
            for (RegattaDTO regatta : getAvailableRegattas()) {
                for (RaceDTO race : regatta.races) {
                    if (race != null && race.getRaceIdentifier().equals(singleSelectedRace)) {
                        this.selectedRaceDTO = race;
                        refreshSelectedRaceData();
                        break;
                    }
                }
            }
        } else {
            selectedCaptionRacePanel.setCaptionText("");
            singleSelectedRace = null;
            selectedCaptionRacePanel.setVisible(false);
        }
    }
}
