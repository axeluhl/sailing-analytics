package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.SailorProfileEntry;

public interface SailorProfileOverview extends IsWidget {

    void setPresenter(SailingProfileOverviewPresenter presenter);

    void setProfileList(List<SailorProfileEntry> entries);


}
