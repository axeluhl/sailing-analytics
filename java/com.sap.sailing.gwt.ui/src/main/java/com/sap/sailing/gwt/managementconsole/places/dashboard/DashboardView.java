package com.sap.sailing.gwt.managementconsole.places.dashboard;

import com.sap.sailing.gwt.managementconsole.mvp.View;

public interface DashboardView extends View<DashboardView.Presenter> {

    interface Presenter extends com.sap.sailing.gwt.managementconsole.mvp.Presenter {

        void navigateToEventSeries();

        void navigateToCreateEventSeries();

        void navigateToEvents();

        void navigateToCreateEvent();

    }

}
