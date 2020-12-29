package com.sap.sailing.gwt.managementconsole.places.event.media;

import com.sap.sailing.gwt.managementconsole.mvp.View;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public interface EventMediaView extends View<EventMediaView.Presenter> {

    void setEvent(EventDTO event);

    interface Presenter extends com.sap.sailing.gwt.managementconsole.mvp.Presenter {
    }

}
