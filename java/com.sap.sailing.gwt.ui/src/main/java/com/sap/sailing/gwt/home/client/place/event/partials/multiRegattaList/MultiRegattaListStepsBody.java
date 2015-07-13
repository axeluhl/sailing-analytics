package com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaProgressSeriesDTO;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;

public class MultiRegattaListStepsBody extends UIObject {

    private static MultiRegattaListStepsBodyUiBinder uiBinder = GWT.create(MultiRegattaListStepsBodyUiBinder.class);

    interface MultiRegattaListStepsBodyUiBinder extends UiBinder<Element, MultiRegattaListStepsBody> {
    }
    
    @UiField DivElement nameUi;
    @UiField DivElement checkUi;
    @UiField DivElement progressUi;
    @UiField DivElement fleetsContainerUi;

    public MultiRegattaListStepsBody(RegattaProgressSeriesDTO seriesProgress) {
        setElement(uiBinder.createAndBindUi(this));
        nameUi.setInnerText(seriesProgress.getName());
        if (seriesProgress.isCompleted()) {
            progressUi.setInnerText(String.valueOf(seriesProgress.getTotalRaceCount()));
        } else {
            checkUi.removeFromParent();
            progressUi.setInnerText(StringMessages.INSTANCE.currentOfTotal(
                    seriesProgress.getProgressRaceCount(), seriesProgress.getTotalRaceCount()));
        }
        addFleetProgresses(seriesProgress.getFleetState(), seriesProgress.getTotalRaceCount());
    }
    
    private void addFleetProgresses(Map<FleetMetadataDTO, Integer> fleetStates, int totalRaceCount) {
        double height = fleetStates.isEmpty() ? 100.0 : 100.0 / fleetStates.size();
        for (Entry<FleetMetadataDTO, Integer> fleetState : fleetStates.entrySet()) {
            double fleetWidth = (fleetState.getValue() * 100.0) / totalRaceCount;
            String fleetColor = fleetState.getKey().getFleetColor();
            MultiRegattaListStepsBodyFleet fleet = new MultiRegattaListStepsBodyFleet(fleetWidth, height, fleetColor);
            fleetsContainerUi.appendChild(fleet.getElement());
        }
    }
    
}
