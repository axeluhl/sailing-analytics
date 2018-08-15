package com.sap.sailing.gwt.home.communication.user.profile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.BadgeDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreference;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreferences;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * {@link SailingAction} implementation to update attributes of a sailor profile identified by its UUID for the
 * currently logged in user. Returns an updated SailorProfileDTO to bee shown on the sailor profile details page,
 * preparing the appropriate data structure.
 */
public abstract class UpdateSailorProfileAction implements SailingAction<SailorProfileDTO> {

    protected UUID uuid;

    public UpdateSailorProfileAction(UUID uuid) {
        this();
        this.uuid = uuid;
    }

    public UpdateSailorProfileAction() {
    }

    @Override
    @GwtIncompatible
    public SailorProfileDTO execute(SailingDispatchContext ctx) throws DispatchException {
        CompetitorAndBoatStore store = ctx.getRacingEventService().getCompetitorAndBoatStore();

        SailorProfilePreferences prefs = ctx.getPreferenceForCurrentUser(SailorProfilePreferences.PREF_NAME);
        Pair<SailorProfilePreferences, SailorProfilePreference> pair = findAndUpdateCorrectPreference(store, prefs);
        prefs = pair.getA();
        ctx.setPreferenceForCurrentUser(SailorProfilePreferences.PREF_NAME, prefs);
        return pair.getB() != null ? convertToDto(pair.getB(), store) : null;
    }

    @GwtIncompatible
    private Pair<SailorProfilePreferences, SailorProfilePreference> findAndUpdateCorrectPreference(
            CompetitorAndBoatStore store, SailorProfilePreferences prefs) {
        List<SailorProfilePreference> sailorProfilePreferences = new ArrayList<>();
        SailorProfilePreference sp = null;
        if (prefs == null) {
            throw new NullPointerException("no sailor profile present");
        } else {
            // 1) copy existing SailorProfilePreference objects from SailorProfilePreferences into list except the
            // changed
            // SailorProfilePreference
            // 2) add the new version of the SailorProfilePreference to the list
            // 3) add the list to a new SailorProfilePreferences
            for (SailorProfilePreference p : prefs.getSailorProfiles()) {
                if (!p.getUuid().equals(uuid)) {
                    sailorProfilePreferences.add(p);
                } else {
                    sp = updatePreference(store, p);
                    if (sp != null) {
                        sailorProfilePreferences.add(sp);
                    }
                }
            }

            // if might be a new SailorProfile (unknown UUID), so create a new preference for it
            if (sp == null) {
                sp = updatePreference(store, null);
                if (sp != null) {
                    sailorProfilePreferences.add(sp);
                }
            }

            prefs = new SailorProfilePreferences(store);
            prefs.setSailorProfiles(sailorProfilePreferences);
        }
        return new Pair<SailorProfilePreferences, SailorProfilePreference>(prefs, sp);
    }

    @GwtIncompatible
    protected abstract SailorProfilePreference updatePreference(CompetitorAndBoatStore store,
            SailorProfilePreference p);

    /** converts SailorProfilePreference to SailorProfileDTO */
    @GwtIncompatible
    private SailorProfileDTO convertToDto(final SailorProfilePreference pref, final CompetitorAndBoatStore store) {
        return new SailorProfileDTO(pref.getUuid(), pref.getName(), convertCompetitors(pref.getCompetitors()),
                new ArrayList<BadgeDTO>(), getCorrespondingBoats(pref.getCompetitors(), store));
    }

    /** convert Competitors to SimpleCompetitorIdWithDTOs */
    @GwtIncompatible
    private List<SimpleCompetitorWithIdDTO> convertCompetitors(final Iterable<Competitor> comps) {
        return StreamSupport.stream(comps.spliterator(), false).filter(c -> c != null)
                .map(c -> new SimpleCompetitorWithIdDTO(c)).collect(Collectors.toList());
    }

    /** get the corresponding boat for each competitor in the list of competitors */
    @GwtIncompatible
    private Set<BoatClassDTO> getCorrespondingBoats(final Iterable<Competitor> comps,
            final CompetitorAndBoatStore store) {
        final Set<BoatClassDTO> boatclasses = new HashSet<>();
        for (Competitor c : comps) {
            if (c != null) {
                CompetitorWithBoat cwd = store.getExistingCompetitorWithBoatById(c.getId());
                if (cwd != null && cwd.getBoat() != null && cwd.getBoat().getBoatClass() != null) {
                    BoatClassDTO dto = convertBoatClass(cwd.getBoat().getBoatClass());
                    boatclasses.add(dto);
                }
            }
        }
        return boatclasses;
    }

    @GwtIncompatible
    private BoatClassDTO convertBoatClass(BoatClass bc) {
        return new BoatClassDTO(bc.getName(), bc.getDisplayName(), bc.getHullLength(), bc.getHullBeam());
    }
}
