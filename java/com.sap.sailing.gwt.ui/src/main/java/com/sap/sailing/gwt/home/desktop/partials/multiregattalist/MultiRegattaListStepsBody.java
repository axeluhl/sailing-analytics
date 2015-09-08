package com.sap.sailing.gwt.home.desktop.partials.multiregattalist;

import static com.sap.sailing.domain.common.LeaderboardNameConstants.DEFAULT_SERIES_NAME;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaProgressFleetDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaProgressSeriesDTO;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;
import com.sap.sse.common.Util;

public class MultiRegattaListStepsBody extends UIObject implements RequiresResize {

    private static final StringMessages I18N = StringMessages.INSTANCE;
    private static MultiRegattaListStepsBodyUiBinder uiBinder = GWT.create(MultiRegattaListStepsBodyUiBinder.class);

    interface MultiRegattaListStepsBodyUiBinder extends UiBinder<Element, MultiRegattaListStepsBody> {
    }
    
    @UiField DivElement nameUi;
    @UiField DivElement checkUi;
    @UiField DivElement progressUi;
    @UiField DivElement fleetsContainerUi;

    private final String seriesName;

    public MultiRegattaListStepsBody(RegattaProgressSeriesDTO seriesProgress) {
        setElement(uiBinder.createAndBindUi(this));
        nameUi.getStyle().setVisibility(Visibility.HIDDEN);
        seriesName = DEFAULT_SERIES_NAME.equals(seriesProgress.getName()) ? I18N.races() : seriesProgress.getName();
        nameUi.setInnerText(seriesName);
        if (seriesProgress.isCompleted()) {
            progressUi.setInnerText(String.valueOf(seriesProgress.getTotalRaceCount()));
        } else {
            checkUi.getStyle().setDisplay(Display.NONE);
            progressUi.setInnerText(I18N.currentOfTotal(
                    seriesProgress.getProgressRaceCount(), seriesProgress.getTotalRaceCount()));
        }
        addFleetProgresses(seriesProgress.getFleetState(), seriesProgress.getTotalRaceCount());
        setFleetsTooltip(seriesProgress.getFleetNames());
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                renderNames();
            }
        });
    }
    
    private void addFleetProgresses(Map<FleetMetadataDTO, RegattaProgressFleetDTO> fleetStates, int totalRaceCount) {
        double height = fleetStates.isEmpty() ? 100.0 : 100.0 / fleetStates.size();
        for (Entry<FleetMetadataDTO, RegattaProgressFleetDTO> fleetState : fleetStates.entrySet()) {
            double finishedWidth = (fleetState.getValue().getFinishedRaceCount() * 100.0) / totalRaceCount;
            double liveWidth = (fleetState.getValue().getFinishedAndLiveRaceCount() * 100.0) / totalRaceCount;
            String fleetColor = fleetState.getKey().getFleetColor();
            MultiRegattaListStepsBodyFleet fleet = new MultiRegattaListStepsBodyFleet(finishedWidth, liveWidth, height, fleetColor);
            fleetsContainerUi.appendChild(fleet.getElement());
        }
    }
    
    private void setFleetsTooltip(String[] fleetNames) {
        if (fleetNames.length > 1) {
            String fleetCount = String.valueOf(fleetNames.length);
            String fleetNameList = "(" + Util.join(", ", fleetNames) + ")";
            fleetsContainerUi.setTitle(Util.join(" ", fleetCount, I18N.fleets(), fleetNameList));
        }
    }
    
    private void renderNames() {
        if (getElement().getOffsetWidth() < nameUi.getOffsetWidth() + checkUi.getOffsetWidth()
                + progressUi.getOffsetWidth()) {
            String[] tokens = seriesName.split(" ");
            StringBuilder initials = new StringBuilder();
            for (int i = 0; i < tokens.length; i++) {
                initials.append(tokens[i].charAt(0));
            }
            nameUi.setInnerText(initials.toString());
        }
        nameUi.setTitle(seriesName);
        nameUi.getStyle().setVisibility(Visibility.VISIBLE);
    }

    @Override
    public void onResize() {
        renderNames();
    }
}
