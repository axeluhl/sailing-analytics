package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base;

import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.SixtyInchSetting;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public interface SlideContext {
    SixtyInchSetting getSettings();

    void updateEvent(EventDTO event);

    EventDTO getEvent();
}
