package com.sap.sailing.gwt.managementconsole.places.regatta.overview;

import java.util.List;
import java.util.UUID;

import com.google.gwt.user.client.ui.RequiresResize;
import com.sap.sailing.gwt.managementconsole.mvp.View;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public interface RegattaOverviewView extends View<RegattaOverviewView.Presenter>, RequiresResize {

    void renderEventName(String eventName);

    void renderRegattas(List<RegattaDTO> regattas);

    interface Presenter extends com.sap.sailing.gwt.managementconsole.mvp.Presenter {
        void reloadRegattaList(UUID eventId);

        void navigateToEvents();

        void navigateToRegatta(final RegattaDTO regatta);
    }

}
