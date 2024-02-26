package com.sap.sailing.gwt.managementconsole.places.eventseries.create;

import com.google.gwt.user.client.ui.RequiresResize;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.gwt.managementconsole.mvp.View;

public interface CreateEventSeriesView extends View<CreateEventSeriesView.Presenter>, RequiresResize {

    interface Presenter extends com.sap.sailing.gwt.managementconsole.mvp.Presenter {
        void createEventSeries(String name, String description, String shortName, 
                boolean isPublic, String baseUrlAsString, ScoringSchemeType scoringSchemeType, 
                int[]discardThresholds);
        void cancelCreateEventSeries();
    }

}
