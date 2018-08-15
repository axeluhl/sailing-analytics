package com.sap.sailing.gwt.home.communication.user.profile;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreference;

/**
 * {@link SailingAction} implementation to load sailor profiles for the currently logged in user to bee shown on the
 * sailor profiles overview page, preparing the appropriate data structure.
 */
public class UpdateSailorProfileTitleAction extends UpdateSailorProfileAction {

    private String title;

    public UpdateSailorProfileTitleAction(UUID uuid, String title) {
        super(uuid);
        this.title = title;
    }

    public UpdateSailorProfileTitleAction() {
    }

    @GwtIncompatible
    @Override
    protected SailorProfilePreference updatePreference(CompetitorAndBoatStore store, SailorProfilePreference p) {
        return new SailorProfilePreference(store, p, this.title);
    }

}
