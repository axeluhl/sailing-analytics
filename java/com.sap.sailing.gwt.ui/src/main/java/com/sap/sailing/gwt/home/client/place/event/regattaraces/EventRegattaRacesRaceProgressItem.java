package com.sap.sailing.gwt.home.client.place.event.regattaraces;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.gwt.home.client.place.event.regatta.RegattaResources;

public class EventRegattaRacesRaceProgressItem extends UIObject {
    private static EventRegattaRacesRaceProgressItemUiBinder uiBinder = GWT.create(EventRegattaRacesRaceProgressItemUiBinder.class);

    interface EventRegattaRacesRaceProgressItemUiBinder extends UiBinder<DivElement, EventRegattaRacesRaceProgressItem> {
    }

    private enum DataStatusEnum { 
        FINISHED ("finished"), LIVE("running");
        
        private final String attributeValue; 
        private static final String ATTRIBUTE_NAME = "data-status";
        
        DataStatusEnum(String attributeValue) {
            this.attributeValue = attributeValue;
        }
    }
    
    @UiField DivElement legProgressItem;
    
    public EventRegattaRacesRaceProgressItem(boolean isLive) {
        RegattaResources.INSTANCE.css().ensureInjected();

        setElement(uiBinder.createAndBindUi(this));
        
        if(isLive) {
            DataStatusEnum dataStatus = DataStatusEnum.LIVE;
            legProgressItem.setAttribute(DataStatusEnum.ATTRIBUTE_NAME, dataStatus.attributeValue);
        }
    }
}
