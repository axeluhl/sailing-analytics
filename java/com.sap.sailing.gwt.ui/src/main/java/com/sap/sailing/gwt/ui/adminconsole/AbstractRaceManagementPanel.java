package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.security.ui.client.UserService;

public abstract class AbstractRaceManagementPanel extends AbstractEventManagementPanel {
    protected RegattaAndRaceIdentifier singleSelectedRace;
    
    protected RaceDTO selectedRaceDTO;
    
    protected final CaptionPanel selectedCaptionRacePanel;
    
    protected List<RegattaDTO> savedEvents;
    
    protected final VerticalPanel selectedRaceContentPanel;
    
    public AbstractRaceManagementPanel(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            RegattaRefresher regattaRefresher, boolean actionButtonsEnabled, StringMessages stringMessages) {
        this(sailingService, null, errorReporter, regattaRefresher, actionButtonsEnabled, stringMessages);
    }
    
    public AbstractRaceManagementPanel(final SailingServiceAsync sailingService,
            UserService userService, ErrorReporter errorReporter,
            RegattaRefresher regattaRefresher, boolean actionButtonsEnabled, StringMessages stringMessages) {
        super(sailingService, userService, regattaRefresher, errorReporter, actionButtonsEnabled, stringMessages);
        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");
        mainPanel.add(trackedRacesListComposite);
        trackedRacesListComposite.getSelectionModel().addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Set<RaceDTO> selectedRaces = trackedRacesListComposite.getSelectionModel().getSelectedSet();
                if (selectedRaces.size() == 1) {
                    singleSelectedRace = selectedRaces.iterator().next().getRaceIdentifier();
                    selectedCaptionRacePanel.setCaptionText(singleSelectedRace.getRaceName());
                    selectedCaptionRacePanel.setVisible(true);
                    for (RegattaDTO regatta : getAvailableRegattas()) {
                        for (RaceDTO race : regatta.races) {
                            if (race != null && race.getRaceIdentifier().equals(singleSelectedRace)) {
                                AbstractRaceManagementPanel.this.selectedRaceDTO = race;
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
        });
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
}
