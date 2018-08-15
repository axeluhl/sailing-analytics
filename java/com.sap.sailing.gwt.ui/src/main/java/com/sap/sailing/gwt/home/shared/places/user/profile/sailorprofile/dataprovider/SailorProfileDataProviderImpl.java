package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.CreateSailorProfileAction;
import com.sap.sailing.gwt.home.communication.user.profile.GetSailorProfilesAction;
import com.sap.sailing.gwt.home.communication.user.profile.UpdateSailorProfileCompetitorsAction;
import com.sap.sailing.gwt.home.communication.user.profile.UpdateSailorProfileTitleAction;
import com.sap.sailing.gwt.home.communication.user.profile.domain.BadgeDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfilesDTO;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.ParticipatedEventDTO;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.ParticipatedRegattaDTO;

public class SailorProfileDataProviderImpl implements SailorProfileDataProvider {

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

    private final ClientFactoryWithDispatch clientFactory;

    public SailorProfileDataProviderImpl(ClientFactoryWithDispatch clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void loadSailorProfiles(AsyncCallback<SailorProfilesDTO> callback) {
        clientFactory.getDispatch().execute(new GetSailorProfilesAction(), callback);
    }

    @Override
    public void findSailorProfileById(UUID uuid, AsyncCallback<SailorProfileDTO> callback) {
        clientFactory.getDispatch().execute(new GetSailorProfilesAction(uuid), new AsyncCallback<SailorProfilesDTO>() {

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(SailorProfilesDTO result) {
                callback.onSuccess(result.getEntries().get(0));
            }
        });
    }

    @Override
    public void getEvents(UUID key, AsyncCallback<Iterable<ParticipatedEventDTO>> asyncCallback) {
        asyncCallback.onSuccess(createEvents());
    }

    @Override
    public void createNewSailorProfile(UUID key, String name, AsyncCallback<SailorProfileDTO> callback) {
        clientFactory.getDispatch().execute(new CreateSailorProfileAction(key, name), callback);
    }

    @Override
    public void updateTitle(UUID key, String updatedTitle, AsyncCallback<SailorProfileDTO> callback) {
        clientFactory.getDispatch().execute(new UpdateSailorProfileTitleAction(key, updatedTitle),
                callback);
    }

    @Override
    public void updateCompetitors(UUID key, Collection<SimpleCompetitorWithIdDTO> competitors,
            AsyncCallback<SailorProfileDTO> callback) {
        clientFactory.getDispatch().execute(new UpdateSailorProfileCompetitorsAction(key, competitors),
                callback);
    }
}
