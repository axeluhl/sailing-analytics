package com.sap.sailing.gwt.home.communication.user.profile;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreference;

/**
 * {@link UpdateSailorProfileAction} implementation to remove a sailor profile identified by the UUID for the currently
 * logged in user.
 */
public class RemoveSailorProfileAction extends UpdateSailorProfileAction {

    public RemoveSailorProfileAction(UUID uuid) {
        super(uuid);
    }

    protected RemoveSailorProfileAction() {

    }

    @GwtIncompatible
    @Override
    protected SailorProfilePreference updatePreference(CompetitorAndBoatStore store, SailorProfilePreference p) {
        return null;
    }
}
