package com.sap.sailing.gwt.autoplay.client.place.start;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class ModeSelector extends Composite {
    private static ModeSelectorUiBinder uiBinder = GWT.create(ModeSelectorUiBinder.class);

    interface ModeSelectorUiBinder extends UiBinder<Widget, ModeSelector> {
    }

    public ModeSelector() {
        ModeSelectorResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setEvents(List<EventDTO> events) {
    }
}
