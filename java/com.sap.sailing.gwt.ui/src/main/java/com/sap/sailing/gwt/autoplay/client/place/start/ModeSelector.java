package com.sap.sailing.gwt.autoplay.client.place.start;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class ModeSelector extends Composite {
    private static ModeSelectorUiBinder uiBinder = GWT.create(ModeSelectorUiBinder.class);

    interface ModeSelectorUiBinder extends UiBinder<Widget, ModeSelector> {
    }

    @UiField(provided=true) ListBox eventSelectionBox;
    @UiField Button startAutoPlayButton;
    
    public ModeSelector() {
        super();
        
        eventSelectionBox = new ListBox(false);
        
        ModeSelectorResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setEvents(List<EventDTO> events) {
        for(EventDTO event: events) {
            eventSelectionBox.addItem(event.getName());
        }
    }
    
    @UiHandler("eventSelectionBox")
    void onLocaleSelectionChange(ChangeEvent event) {
    }
    
    @UiHandler("startAutoPlayButton")
    void startAutoPlayClicked(ClickEvent event) {
        Window.alert("start auto play clicked");
    }
    
}
