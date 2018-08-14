package com.sap.sailing.gwt.home.communication.user.profile;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.SailorProfile;
import com.sap.sailing.domain.base.impl.SailorProfileImpl;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEntry;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreference;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreferences;
import com.sap.sse.gwt.dispatch.shared.commands.VoidResult;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * {@link SailingAction} implementation to save favorite a sailor profiles for the currently logged in user
 */
public class SaveSailorProfileAction implements SailingAction<VoidResult> {

    private SailorProfileEntry sailorProfileEntry;

    protected SaveSailorProfileAction() {
    }

    public SaveSailorProfileAction(SailorProfileEntry sailorProfileEntry) {
        this.sailorProfileEntry = sailorProfileEntry;
    }

    @Override
    @GwtIncompatible
    public VoidResult execute(SailingDispatchContext ctx) throws DispatchException {
        CompetitorAndBoatStore store = ctx.getRacingEventService().getCompetitorAndBoatStore();

        List<SailorProfilePreference> sailorProfilePreferences = new ArrayList<>();
        SailorProfilePreferences prefs = ctx.getPreferenceForCurrentUser(SailorProfilePreferences.PREF_NAME);
        // if user has already stored SailorProfilePreferences, override the changed SailorProfilePreferences, or else
        // create a new SailorProfilePreferences object with an added SailorProfilePreference object representing the
        // SailorProfileEntry to store
        if (prefs == null) {
            // create new SailorProfilePreferences
            prefs = new SailorProfilePreferences(store);
            sailorProfilePreferences.add(new SailorProfilePreference(store, convert(sailorProfileEntry, store)));
            prefs.setSailorProfiles(sailorProfilePreferences);
        } else {
            // 1) copy existing SailorProfilePreference objects from SailorProfilePreferences into list except the
            // changed
            // SailorProfilePreference
            // 2) add the new version of the SailorProfilePreference to the list
            // 3) add the list to a new SailorProfilePreferences
            for (SailorProfilePreference p : prefs.getSailorProfiles()) {
                if (!p.getUuid().equals(sailorProfileEntry.getKey())) {
                    sailorProfilePreferences.add(p);
                }
            }
            sailorProfilePreferences.add(new SailorProfilePreference(store, convert(sailorProfileEntry, store)));
            prefs = new SailorProfilePreferences(store);
            prefs.setSailorProfiles(sailorProfilePreferences);
        }
        ctx.setPreferenceForCurrentUser(SailorProfilePreferences.PREF_NAME, prefs);
        return new VoidResult();
    }

    /** convert SailorProfileEntry object to persistable SailorProfile */
    @GwtIncompatible
    private SailorProfile convert(SailorProfileEntry entr, CompetitorAndBoatStore competitorStore) {
        List<Competitor> competitors = new ArrayList<>();
        for (SimpleCompetitorWithIdDTO c : entr.getCompetitors()) {
            Competitor competitor = competitorStore.getExistingCompetitorByIdAsString(c.getIdAsString());
            competitors.add(competitor);
        }
        return new SailorProfileImpl(entr.getName(), entr.getKey(), competitors);
    }

}
