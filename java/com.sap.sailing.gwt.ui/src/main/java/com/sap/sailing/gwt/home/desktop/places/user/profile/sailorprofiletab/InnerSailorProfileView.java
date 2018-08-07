package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailorProfileView.Presenter;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SailorProfileEntry;

public interface InnerSailorProfileView extends IsWidget {

    void setPresenter(Presenter presenter);

    void setProfileList(List<SailorProfileEntry> entries);

}
