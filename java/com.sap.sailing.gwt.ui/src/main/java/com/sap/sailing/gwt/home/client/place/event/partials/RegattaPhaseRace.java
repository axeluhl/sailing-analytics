package com.sap.sailing.gwt.home.client.place.event.partials;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO.RaceColumnLiveState;
import com.sap.sse.gwt.client.player.Timer;

public class RegattaPhaseRace extends UIObject {
    private static RegattaPhaseRaceUiBinder uiBinder = GWT.create(RegattaPhaseRaceUiBinder.class);

    interface RegattaPhaseRaceUiBinder extends UiBinder<DivElement, RegattaPhaseRace> {
    }

    @UiField DivElement raceColumnStatus;

    private enum DataStatusEnum { 
        FINISHED ("finished"), LIVE("live"), SCHEDULED("scheduled");
        
        private final String attributeValue; 
        private static final String ATTRIBUTE_NAME = "data-status";
        
        DataStatusEnum(String attributeValue) {
            this.attributeValue = attributeValue;
        }
    }
       
    public RegattaPhaseRace(RaceColumnDTO raceColumn, Timer timerForClientServerOffset) {
        RegattaResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
    
        DataStatusEnum dataStatus = DataStatusEnum.SCHEDULED;
        RaceColumnLiveState liveState = raceColumn.getLiveState(timerForClientServerOffset.getLiveTimePointInMillis());
        
        switch(liveState) {
            case NOT_TRACKED:
                dataStatus = DataStatusEnum.SCHEDULED;
                break;
            case TRACKED:
                dataStatus = DataStatusEnum.FINISHED;
                break;
            case TRACKED_AND_LIVE:
                dataStatus = DataStatusEnum.LIVE;
                break;
        }
        raceColumnStatus.setAttribute(DataStatusEnum.ATTRIBUTE_NAME, dataStatus.attributeValue);
    }
}
