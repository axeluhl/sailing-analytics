package com.sap.sailing.gwt.managementconsole.places.event;

import com.sap.sailing.gwt.common.communication.event.EventMetadataDTO;
import com.sap.sailing.gwt.managementconsole.partials.contextmenu.HasContextMenuView;

public interface EventPresenter extends HasContextMenuView.Presenter<EventMetadataDTO> {

    void navigateToEvent(EventMetadataDTO event);

}
