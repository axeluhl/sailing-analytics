package com.sap.sailing.gwt.home.mobile.partials.eventsteps;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
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
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.LinkUtil;

public class EventStepsPhase extends Composite {

    private static final StringMessages I18N = StringMessages.INSTANCE;
    private static EventStepsPhaseUiBinder uiBinder = GWT.create(EventStepsPhaseUiBinder.class);

    interface EventStepsPhaseUiBinder extends UiBinder<Widget, EventStepsPhase> {
    }
    
    @UiField AnchorElement anchorUi;
    @UiField DivElement nameUi;
    @UiField ImageElement checkUi;
    @UiField DivElement progressUi;
    @UiField DivElement fleetsContainerUi;

    EventStepsPhase(RegattaProgressSeriesDTO seriesProgress, PlaceNavigation<?> placeNavigation, boolean showName) {
        initWidget(uiBinder.createAndBindUi(this));
        LinkUtil.configureForAction(anchorUi, () -> placeNavigation.goToPlace());
        if (showName) {
            nameUi.setInnerText(seriesProgress.getName());
        } else {
            nameUi.removeFromParent();
        }
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
