package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.wrapper.SailorProfileWrapperView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SailorProfileEntry;

public interface SailorProfileView extends IsWidget {

    void setPresenter(SailorProfileWrapperView.Presenter presenter);

    void setProfileList(List<SailorProfileEntry> entries);
}
