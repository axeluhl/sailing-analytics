package com.sap.sailing.gwt.home.communication.user.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.BadgeDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEntries;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEntry;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * {@link SailingAction} implementation to load favorites for the currently logged in user to bee shown on the
 * preferences page, preparing the appropriate data structure.
 */
public class GetSailorProfilesAction implements SailingAction<SailorProfileEntries> {

    private HashMap<UUID, SailorProfileEntry> entries;

    public GetSailorProfilesAction() {
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
    }

    @Override
    @GwtIncompatible
    public SailorProfileEntries execute(SailingDispatchContext ctx) throws DispatchException {
        return new SailorProfileEntries(entries.values());
    }

}
