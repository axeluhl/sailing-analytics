package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import java.util.Collection;

import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;

public interface SailorProfileOverview extends SailorProfileView {

    void setProfileList(Collection<SailorProfileDTO> entries);

}
