package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.wrapper.SailorProfileOverviewWrapperView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.SailorProfileEntry;

public interface SailorProfileOverviewView extends IsWidget {

    void setPresenter(SailorProfileOverviewWrapperView.Presenter presenter);

    void setProfileList(List<SailorProfileEntry> entries);
}
