package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details;

import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEntry;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailorProfileView;

public interface SailorProfileDetailsView extends SailorProfileView {

    void setEntry(SailorProfileEntry entry);

}
