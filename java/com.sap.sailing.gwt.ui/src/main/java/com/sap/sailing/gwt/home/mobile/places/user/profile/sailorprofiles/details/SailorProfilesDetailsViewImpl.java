package com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles.details;

import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailingProfileOverviewPresenter;
import com.sap.sailing.gwt.home.mobile.places.user.profile.AbstractUserProfileView;
import com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles.SailorProfilesOverviewView;
import com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles.SailorProfilesViewWithAuthenticationContext;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.EditSailorProfileView;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

public class SailorProfilesDetailsViewImpl extends AbstractUserProfileView
        implements SailorProfilesViewWithAuthenticationContext, EditSailorProfileView {

    private final SailorProfilesDetailsImpl sailorProfilesDetailsView;

    public SailorProfilesDetailsViewImpl(SailorProfilesOverviewView.Presenter presenter) {
        super(presenter);
        this.sailorProfilesDetailsView = new SailorProfilesDetailsImpl();
        this.setViewContent(sailorProfilesDetailsView);
    }

    @Override
    public void setPresenter(SailingProfileOverviewPresenter presenter) {
        sailorProfilesDetailsView.setPresenter(presenter);

    }

    @Override
    public NeedsAuthenticationContext getAuthenticationContext() {
        return sailorProfilesDetailsView.getAuthenticationContext();
    }

    @Override
    public void setEntry(SailorProfileDTO entry) {
        sailorProfilesDetailsView.setEntry(entry);
    }

}
