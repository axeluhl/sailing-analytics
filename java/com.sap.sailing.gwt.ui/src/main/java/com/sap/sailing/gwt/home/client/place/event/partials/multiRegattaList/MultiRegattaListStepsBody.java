package com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList;

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
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                renderNames();
            }
        });
        if (seriesProgress.isCompleted()) {
            progressUi.setInnerText(String.valueOf(seriesProgress.getTotalRaceCount()));
        } else {
            checkUi.getStyle().setDisplay(Display.NONE);
            progressUi.setInnerText(I18N.currentOfTotal(
                    seriesProgress.getProgressRaceCount(), seriesProgress.getTotalRaceCount()));
        }
        addFleetProgresses(seriesProgress.getFleetState(), seriesProgress.getTotalRaceCount());
    }

    
    private void addFleetProgresses(Map<FleetMetadataDTO, RegattaProgressFleetDTO> fleetStates, int totalRaceCount) {
        double height = fleetStates.isEmpty() ? 100.0 : 100.0 / fleetStates.size();
        for (Entry<FleetMetadataDTO, RegattaProgressFleetDTO> fleetState : fleetStates.entrySet()) {
            double fleetWidth = (fleetState.getValue().getFinishedRaceCount() * 100.0) / totalRaceCount;
            String fleetColor = fleetState.getKey().getFleetColor();
            MultiRegattaListStepsBodyFleet fleet = new MultiRegattaListStepsBodyFleet(fleetWidth, height, fleetColor);
            fleetsContainerUi.appendChild(fleet.getElement());
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
        nameUi.setTitle(seriesName + " " + nameUi.getOffsetWidth() + " / " + getElement().getOffsetWidth());
        nameUi.getStyle().setVisibility(Visibility.VISIBLE);
    }

    @Override
    public void onResize() {
        renderNames();
    }
}
