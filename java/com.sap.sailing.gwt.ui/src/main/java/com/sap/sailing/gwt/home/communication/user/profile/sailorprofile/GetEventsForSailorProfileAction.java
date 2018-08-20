package com.sap.sailing.gwt.home.communication.user.profile.sailorprofile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.BadgeDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.ParticipatedEventDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.ParticipatedRegattaDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEventsDTO;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreference;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreferences;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * {@link SailingAction} implementation to load all events, the competitors in a sailor profile with a specific uuid for
 * the currently logged in user have participated in to be shown on the sailor profile details page in the events
 * container.
 */
public class GetEventsForSailorProfileAction implements SailingAction<SailorProfileEventsDTO>, SailorProfileConverter {

    private UUID uuid;

    public GetEventsForSailorProfileAction(UUID uuid) {
        this.uuid = uuid;
    }

    public GetEventsForSailorProfileAction() {
    }

    @Override
    @GwtIncompatible
    public SailorProfileEventsDTO execute(SailingDispatchContext ctx) throws DispatchException {

        CompetitorAndBoatStore store = ctx.getRacingEventService().getCompetitorAndBoatStore();

        SailorProfilePreferences prefs = ctx.getPreferenceForCurrentUser(SailorProfilePreferences.PREF_NAME);
        SailorProfilePreference pref = findSailorProfile(store, prefs);
        if (pref != null) {
            return new SailorProfileEventsDTO(createEvents());
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

    @GwtIncompatible
    private Collection<ParticipatedEventDTO> createEvents() {

        List<BadgeDTO> badges = new ArrayList<>();
        BadgeDTO b1 = new BadgeDTO(UUID.randomUUID(), "Best Sailor Ever");
        BadgeDTO b2 = new BadgeDTO(UUID.randomUUID(), "100 Miles Sailed");
        badges.add(b1);
        badges.add(b2);

        List<BadgeDTO> badges2 = new ArrayList<>();
        badges2.add(b2);

        List<SimpleCompetitorWithIdDTO> competitors = new ArrayList<>();
        SimpleCompetitorWithIdDTO c1 = new SimpleCompetitorWithIdDTO("0000", "EZ Competition", "EZC", "DE", "");
        SimpleCompetitorWithIdDTO c2 = new SimpleCompetitorWithIdDTO("0001", "Hard Competition", "HC", "SE", "");
        competitors.add(c1);
        competitors.add(c2);

        List<SimpleCompetitorWithIdDTO> competitors2 = new ArrayList<>();
        competitors2.add(c1);

        List<BoatClassDTO> boatclasses = new ArrayList<>();
        BoatClassDTO bc1 = new BoatClassDTO("J/70", null, null);
        BoatClassDTO bc2 = new BoatClassDTO("12 Meter", null, null);
        BoatClassDTO bc3 = new BoatClassDTO("5O5", null, null);
        boatclasses.add(bc1);
        boatclasses.add(bc2);
        boatclasses.add(bc3);

        List<BoatClassDTO> boatclasses2 = new ArrayList<>();
        boatclasses2.add(bc2);
        boatclasses2.add(bc3);

        // init events
        ParticipatedRegattaDTO r1 = new ParticipatedRegattaDTO("Hello-Regatta", 1, c1, "Supa Klub", "1337", "1338", 27);
        ParticipatedRegattaDTO r2 = new ParticipatedRegattaDTO("Ciao-Regatta", 4, c2, "Subba Clubba", "139", "136", 25);
        ParticipatedRegattaDTO r3 = new ParticipatedRegattaDTO("Lurch-Regatta", 25, c1, "Supa Klub", "1335", "134", 85);

        Collection<ParticipatedRegattaDTO> p1 = new ArrayList<>();
        p1.add(r1);
        p1.add(r2);

        ParticipatedEventDTO pe1 = new ParticipatedEventDTO("Cooles Event", "01", p1);

        Collection<ParticipatedRegattaDTO> p2 = new ArrayList<>();
        p2.add(r1);
        p2.add(r3);
        ParticipatedEventDTO pe2 = new ParticipatedEventDTO("Super Cooles Event", "02", p2);

        List<ParticipatedEventDTO> events = new ArrayList<>();
        events.add(pe1);
        events.add(pe2);
        return events;
    }

}
