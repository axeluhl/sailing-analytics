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
public class CreateSailorProfileAction extends UpdateSailorProfileAction {

    private String name;

    public CreateSailorProfileAction(UUID uuid, String name) {
        super(uuid);
        this.name = name;
    }

    public CreateSailorProfileAction() {
    }

    @GwtIncompatible
    @Override
    protected SailorProfilePreference updatePreference(CompetitorAndBoatStore store, SailorProfilePreference p) {
        return new SailorProfilePreference(store, uuid, name);
    }

}
