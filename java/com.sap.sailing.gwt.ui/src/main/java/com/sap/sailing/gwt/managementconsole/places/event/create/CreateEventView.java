package com.sap.sailing.gwt.managementconsole.places.event.create;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.ui.RequiresResize;
import com.sap.sailing.gwt.managementconsole.mvp.View;

public interface CreateEventView extends View<CreateEventView.Presenter>, RequiresResize {

    interface Presenter extends com.sap.sailing.gwt.managementconsole.mvp.Presenter {
        void createEvent(String name, String venue, Date date, List<String> courseAreaNames);
        void cancelCreateEvent();
    }

}
