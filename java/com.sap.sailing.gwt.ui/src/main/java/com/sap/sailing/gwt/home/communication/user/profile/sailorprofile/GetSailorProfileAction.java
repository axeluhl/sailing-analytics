package com.sap.sailing.gwt.home.communication.user.profile.sailorprofile;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreference;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreferences;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * {@link SailingAction} implementation to load a sailor profile with a specific uuid for the currently logged in user
 * to bee shown on the sailor profile details page, preparing the appropriate data structure.
 */
public class GetSailorProfileAction implements SailingAction<SailorProfileDTO>, SailorProfileConverter {

    private UUID uuid;

    public GetSailorProfileAction(UUID uuid) {
        this.uuid = uuid;
    }

    protected GetSailorProfileAction() {
    }

    @Override
    @GwtIncompatible
    public SailorProfileDTO execute(SailingDispatchContext ctx) throws DispatchException {

        CompetitorAndBoatStore store = ctx.getRacingEventService().getCompetitorAndBoatStore();

        SailorProfilePreferences prefs = ctx.getPreferenceForCurrentUser(SailorProfilePreferences.PREF_NAME);
        SailorProfilePreference pref = findSailorProfile(store, prefs);
        if (pref != null) {

            return convertSailorProfilePreferenceToDto(pref, store);
        } else {
            throw new NullPointerException("Unknown sailor profile with uuid " + uuid);
        }
    }

    @GwtIncompatible
    private SailorProfilePreference findSailorProfile(CompetitorAndBoatStore store, SailorProfilePreferences prefs) {
        if (prefs == null) {
            throw new NullPointerException("no sailor profile present");
        } else {
            for (SailorProfilePreference p : prefs.getSailorProfiles()) {
                if (p.getUuid().equals(uuid)) {
                    return p;
                }
            }
            return null;
        }
    }

}
