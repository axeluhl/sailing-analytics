package com.sap.sailing.gwt.home.mobile.partials.eventsteps;

import static com.sap.sailing.domain.common.LeaderboardNameConstants.DEFAULT_SERIES_NAME;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.race.FleetMetadataDTO;
import com.sap.sailing.gwt.home.communication.regatta.RegattaProgressFleetDTO;
import com.sap.sailing.gwt.home.communication.regatta.RegattaProgressSeriesDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class EventStepsPhase extends Composite {

    private static final StringMessages I18N = StringMessages.INSTANCE;
    private static EventStepsPhaseUiBinder uiBinder = GWT.create(EventStepsPhaseUiBinder.class);

    interface EventStepsPhaseUiBinder extends UiBinder<Widget, EventStepsPhase> {
    }
    
    @UiField DivElement nameUi;
    @UiField ImageElement checkUi;
    @UiField DivElement progressUi;
    @UiField DivElement fleetsContainerUi;

    public EventStepsPhase(RegattaProgressSeriesDTO seriesProgress) {
        initWidget(uiBinder.createAndBindUi(this));
        nameUi.setInnerText(DEFAULT_SERIES_NAME.equals(seriesProgress.getName()) ? I18N.races() : seriesProgress.getName());
        if (seriesProgress.isCompleted()) {
            progressUi.setInnerText(I18N.racesCount(seriesProgress.getTotalRaceCount()));
        } else {
            checkUi.getStyle().setDisplay(Display.NONE);
            int current = seriesProgress.getProgressRaceCount(), total = seriesProgress.getTotalRaceCount();
            progressUi.setInnerText(I18N.currentOfTotalRaces(current, total));
        }
        if (seriesProgress.getProgressRaceCount() == 0) {
            addStyleName(EventStepsResources.INSTANCE.css().eventsteps_phases_phaseinactive());
        }
        addFleetProgresses(seriesProgress.getFleetState(), seriesProgress.getMaxRacesPerFleet());
    }
    
    private void addFleetProgresses(Map<FleetMetadataDTO, RegattaProgressFleetDTO> fleetStates, int totalRaceCount) {
        double height = fleetStates.isEmpty() ? 100.0 : 100.0 / fleetStates.size();
        for (Entry<FleetMetadataDTO, RegattaProgressFleetDTO> fleetState : fleetStates.entrySet()) {
            double finishedWidth = (fleetState.getValue().getFinishedRaceCount() * 100.0) / totalRaceCount;
            double liveWidth = (fleetState.getValue().getFinishedAndLiveRaceCount() * 100.0) / totalRaceCount;
            String fleetColor = fleetState.getKey().getFleetColor();
            EventStepsPhaseFleet fleet = new EventStepsPhaseFleet(finishedWidth, liveWidth, height, fleetColor);
            fleetsContainerUi.appendChild(fleet.getElement());
        }
    }

}
