package com.sap.sailing.gwt.home.desktop.partials.multiregattalist;

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
import com.sap.sailing.gwt.home.communication.race.FleetMetadataDTO;
import com.sap.sailing.gwt.home.communication.regatta.RegattaProgressFleetDTO;
import com.sap.sailing.gwt.home.communication.regatta.RegattaProgressSeriesDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;

public class MultiRegattaListStepsBody extends UIObject implements RequiresResize {

    private static final StringMessages I18N = StringMessages.INSTANCE;
    private static MultiRegattaListStepsBodyUiBinder uiBinder = GWT.create(MultiRegattaListStepsBodyUiBinder.class);

    interface MultiRegattaListStepsBodyUiBinder extends UiBinder<Element, MultiRegattaListStepsBody> {
    }
    
    @UiField DivElement longNameDummyUi;
    @UiField DivElement mediumNameDummyUi;
    
    @UiField DivElement nameUi;
    @UiField DivElement checkUi;
    @UiField DivElement progressUi;
    @UiField DivElement fleetsContainerUi;
    @UiField DivElement textContainerUi;

    private final String seriesName, seriesNameMedium, seriesNameShort;
    private int seriesNameLength, seriesNameMediumLength;

    MultiRegattaListStepsBody(RegattaProgressSeriesDTO seriesProgress, boolean showName) {
        setElement(uiBinder.createAndBindUi(this));
        textContainerUi.getStyle().setVisibility(Visibility.HIDDEN);
        longNameDummyUi.setInnerText(seriesName = caculateSeriesName(seriesProgress, showName));
        mediumNameDummyUi.setInnerText(seriesNameMedium = caculateSeriesNameMedium());
        seriesNameShort = caculateSeriesNameShort();
        if (seriesProgress.isCompleted()) {
            progressUi.setInnerText(I18N.racesCount(seriesProgress.getTotalRaceCount()));
        } else {
            checkUi.getStyle().setDisplay(Display.NONE);
            progressUi.setInnerText(I18N.currentOfTotalRaces(
                    seriesProgress.getProgressRaceCount(), seriesProgress.getTotalRaceCount()));
        }
        addFleetProgresses(seriesProgress.getFleetState(), seriesProgress.getMaxRacesPerFleet());
        setFleetsTooltip(seriesProgress.getFleetNames());
    }
    
    void init() {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                int textContainerLength = textContainerUi.getOffsetWidth();
                seriesNameLength = longNameDummyUi.getOffsetWidth() + textContainerLength;
                seriesNameMediumLength= mediumNameDummyUi.getOffsetWidth() + textContainerLength;
                longNameDummyUi.removeFromParent();
                mediumNameDummyUi.removeFromParent();
                textContainerUi.getStyle().clearVisibility();
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
    
    private String caculateSeriesName(RegattaProgressSeriesDTO seriesProgress, boolean showName) {
        if (!showName || seriesProgress.getName() == null || seriesProgress.getName().isEmpty()) {
            nameUi.getStyle().setDisplay(Display.NONE);
            return I18N.races();
        } else {
            return seriesProgress.getName();
        }
    }
    
    private String caculateSeriesNameMedium() {
        String[] tokens = seriesName.split(" ");
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].isEmpty()) { // this may happen, e.g., in case of a FlexibleLeaderboard that does not have a series
                initials.append('R');
            } else {
                initials.append(tokens[i].charAt(0));
            }
        }
        return initials.toString();
    }
    
    private String caculateSeriesNameShort() {
        return seriesName.substring(0, 1);
    }
    
    private void renderNames() {
        if (fleetsContainerUi.getOffsetWidth() >= seriesNameLength) {
            nameUi.setInnerText(seriesName);
        } else if (fleetsContainerUi.getOffsetWidth() >= seriesNameMediumLength) {
            nameUi.setInnerText(seriesNameMedium);
        } else {
            nameUi.setInnerText(seriesNameShort);
        }
    }
    
    @Override
    public void onResize() {
        renderNames();
    }
}
