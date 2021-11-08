package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.view;

import java.util.Collection;

import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;

/** {@link SailorProfileView} corresponding to sailor profile overview for mobile and desktop */
public interface SailorProfileOverview extends SailorProfileView {

    void setProfileList(Collection<SailorProfileDTO> entries);

}
