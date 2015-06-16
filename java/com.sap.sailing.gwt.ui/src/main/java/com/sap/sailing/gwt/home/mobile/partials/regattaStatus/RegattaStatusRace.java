package com.sap.sailing.gwt.home.mobile.partials.regattaStatus;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.regattaoverview.client.FlagsMeaningExplanator;
import com.sap.sailing.gwt.regattaoverview.client.SailingFlagsBuilder;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;
import com.sap.sailing.gwt.ui.shared.race.FlagStateDTO;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO.RaceViewState;
import com.sap.sailing.gwt.ui.shared.race.RaceProgressDTO;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RegattaStatusRace extends Composite {
    
    private static final StringMessages I18N = StringMessages.INSTANCE;
    
    private static RegattaStatusRaceUiBinder uiBinder = GWT.create(RegattaStatusRaceUiBinder.class);

    interface RegattaStatusRaceUiBinder extends UiBinder<Widget, RegattaStatusRace> {
    }
    
    @UiField DivElement fleetCornerUi;
    @UiField DivElement raceTitleUi;
    @UiField DivElement raceFlagContainerUi;
    @UiField DivElement raceStateInfoUi;
    @UiField DivElement raceLegsInfoUi;
    @UiField DivElement raceLegsInfoLabelUi;
    @UiField DivElement raceLegsInfoProgressUi;

    public RegattaStatusRace(LiveRaceDTO race) {
        initWidget(uiBinder.createAndBindUi(this));
        initRaceFleetCorner(race.getFleet());
        raceTitleUi.setInnerText(race.getRaceName());
        initRaceFlagInfo(race.getFlagState());
        initRaceStateOrLegsInfo(race.getViewState(), race.getStart(), race.getProgress());
    }

    private void initRaceFleetCorner(FleetMetadataDTO fleet) {
        if (fleet != null) {
            fleetCornerUi.getStyle().setProperty("borderColor", fleet.getFleetColor());
        }
    }
    
    private void initRaceFlagInfo(FlagStateDTO value) {
        raceFlagContainerUi.setInnerSafeHtml(SailingFlagsBuilder.render(value, 0.40, FlagsMeaningExplanator.getFlagsMeaning(I18N,
                value.getLastUpperFlag(), value.getLastLowerFlag(), value.isLastFlagsAreDisplayed())));
    }

    private void initRaceStateOrLegsInfo(RaceViewState state, Date start, RaceProgressDTO progress) {
        if (state == RaceViewState.RUNNING && progress != null) {
            raceStateInfoUi.removeFromParent();
            raceLegsInfoLabelUi.setInnerText(I18N.currentOfTotalLegs(progress.getCurrentLeg(), progress.getTotalLegs()));
            raceLegsInfoProgressUi.getStyle().setWidth(progress.getPercentageProgress(), Unit.PCT);
        } else {
            raceLegsInfoUi.removeFromParent();
            raceStateInfoUi.setInnerText(state.getLabel());
            if (state == RaceViewState.SCHEDULED) {
                double min = MillisecondsTimePoint.now().until(new MillisecondsTimePoint(start)).asMinutes();
                raceStateInfoUi.setInnerText(I18N.startingInMinutes((int) min));
            }
        }
    }

}
