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
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.SailorProfileEntry;

public class SailorProfileDataProviderImpl implements SailorProfileDataProvider {

    private Map<UUID, SailorProfileEntry> entries;

    public SailorProfileDataProviderImpl() {
        List<BadgeDTO> badges = new ArrayList<>();

        UUID uid1 = UUID.randomUUID();
        UUID uid2 = UUID.randomUUID();
        BadgeDTO b1 = new BadgeDTO(uid1.toString(), "Best Sailor Ever");
        BadgeDTO b2 = new BadgeDTO(uid2.toString(), "100 Miles Sailed");
        badges.add(b1);
        badges.add(b2);

        List<SimpleCompetitorWithIdDTO> competitors = new ArrayList<>();
        SimpleCompetitorWithIdDTO c1 = new SimpleCompetitorWithIdDTO("0000", "EZ Competition", "", "", "");
        SimpleCompetitorWithIdDTO c2 = new SimpleCompetitorWithIdDTO("0001", "Hard Competition", "", "", "");
        competitors.add(c1);
        competitors.add(c2);

        List<BoatClassDTO> boatclasses = new ArrayList<>();
        BoatClassDTO bc1 = new BoatClassDTO("Class-A", null, null);
        BoatClassDTO bc2 = new BoatClassDTO("Class-A", null, null);
        boatclasses.add(bc1);
        boatclasses.add(bc2);

        SailorProfileEntry e1 = new SailorProfileEntry(uid1.toString(), "My Favourite Guy", competitors,
                badges, boatclasses);
        SailorProfileEntry e2 = new SailorProfileEntry(uid2.toString(), "This Other Guy", competitors,
                badges, boatclasses);

        entries = new HashMap<>();
        entries.put(uid1, e1);
        entries.put(uid2, e2);
    }

    @Override
    public Collection<SailorProfileEntry> loadSailorProfiles() {
        return entries.values();
    }

    @Override
    public void findSailorProfileById(UUID uuid, AsyncCallback<SailorProfileEntry> asyncCallback) {
        SailorProfileEntry entr = entries.get(uuid);
        asyncCallback.onSuccess(entr);
    }

}
