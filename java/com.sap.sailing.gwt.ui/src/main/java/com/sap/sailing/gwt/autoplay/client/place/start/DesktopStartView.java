package com.sap.sailing.gwt.autoplay.client.place.start;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.autoplay.client.app.PlaceNavigator;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class DesktopStartView extends Composite implements StartView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, DesktopStartView> {
    }

    @UiField ModeSelector modeSelector;
    
    public DesktopStartView(PlaceNavigator navigator) {
        
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setEvents(List<EventDTO> events) {
        modeSelector.setEvents(events);
    }
}
