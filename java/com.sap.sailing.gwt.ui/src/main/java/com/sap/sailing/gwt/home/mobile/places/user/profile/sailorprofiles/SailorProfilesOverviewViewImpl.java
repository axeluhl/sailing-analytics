package com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles;

import java.util.List;

import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailingProfileOverviewPresenter;
import com.sap.sailing.gwt.home.mobile.places.user.profile.AbstractUserProfileView;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

public class SailorProfilesOverviewViewImpl extends AbstractUserProfileView implements SailorProfilesOverviewView {

    private final SailorProfilesOverview sailorProfilesOverview;

    public SailorProfilesOverviewViewImpl(SailorProfilesOverviewView.Presenter presenter) {
        super(presenter);
        this.sailorProfilesOverview = new SailorProfilesOverview();
        this.setViewContent(sailorProfilesOverview);
    }

    @Override
    public void setPresenter(SailingProfileOverviewPresenter presenter) {
        // TODO Auto-generated method stub

    }

    @Override
    public NeedsAuthenticationContext getAuthenticationContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setProfileList(List<SailorProfileDTO> entries) {
        sailorProfilesOverview.setProfileList(entries);
    }

}
