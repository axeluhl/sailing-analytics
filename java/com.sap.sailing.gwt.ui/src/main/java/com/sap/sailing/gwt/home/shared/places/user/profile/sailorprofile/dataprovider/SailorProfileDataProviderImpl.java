package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.BadgeDTO;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.ParticipatedEventDTO;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.ParticipatedRegattaDTO;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.SailorProfileEntry;

public class SailorProfileDataProviderImpl implements SailorProfileDataProvider {

    private Map<UUID, SailorProfileEntry> entries;

    private Collection<ParticipatedEventDTO> events;

    public SailorProfileDataProviderImpl() {

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

        UUID uid1 = UUID.fromString("f92aa40e-5870-4f0c-9435-90a9069b0e65");
        UUID uid2 = UUID.fromString("71dd204f-376b-4129-a8ef-ad190e49ed02");
        SailorProfileEntry e1 = new SailorProfileEntry(uid1, "My Favorite Guy", competitors, badges, boatclasses);
        SailorProfileEntry e2 = new SailorProfileEntry(uid2, "This Other Guy", competitors2, badges2, boatclasses2);

        entries = new HashMap<>();
        entries.put(uid1, e1);
        entries.put(uid2, e2);

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

        events = new ArrayList<>();
        events.add(pe1);
        events.add(pe2);
    }

    @Override
    public Collection<SailorProfileEntry> loadSailorProfiles() {
        return entries.values();
    }

    @Override
    public void findSailorProfileById(UUID uuid, AsyncCallback<SailorProfileEntry> asyncCallback) {
        asyncCallback.onSuccess(entries.get(uuid));
    }

    @Override
    public void getEvents(UUID key, AsyncCallback<Collection<ParticipatedEventDTO>> asyncCallback) {
        asyncCallback.onSuccess(events);
    }

}
