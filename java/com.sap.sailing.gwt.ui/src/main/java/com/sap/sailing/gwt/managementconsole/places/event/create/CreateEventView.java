package com.sap.sailing.gwt.managementconsole.places.event.create;

import com.google.gwt.user.client.ui.RequiresResize;
import com.sap.sailing.gwt.managementconsole.mvp.View;

public interface CreateEventView extends View<CreateEventView.Presenter>, RequiresResize {

    interface Presenter extends com.sap.sailing.gwt.managementconsole.mvp.Presenter {
        void createEvent();
        void cancelCreateEvent();
    }

}
