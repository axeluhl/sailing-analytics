package com.sap.sailing.gwt.managementconsole.places.regatta.overview;

import java.util.List;
import java.util.UUID;

import com.sap.sailing.gwt.managementconsole.mvp.View;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public interface RegattaOverviewView extends View<RegattaOverviewView.Presenter> {

    interface Presenter extends com.sap.sailing.gwt.managementconsole.mvp.Presenter {
        void reloadRegattaList(UUID eventId);

        void navigateToRegatta(final RegattaDTO regatta);
    }

    void renderRegattas(List<RegattaDTO> regattas);
}
