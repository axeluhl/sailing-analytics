package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SailorProfileEntry;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SharedSailorProfileView;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;
import com.sap.sse.security.ui.authentication.decorator.NotLoggedInPresenter;

public interface SailorProfileView extends IsWidget {

    void setPresenter(Presenter presenter);
    
    void setProfileList(List<SailorProfileEntry> entries);

    public interface Presenter extends NotLoggedInPresenter, NeedsAuthenticationContext {
        SharedSailorProfileView.Presenter getSharedSailorProfilePresenter();
    }

    NeedsAuthenticationContext getDecorator();
}
