package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.BadgeDTO;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.SailorProfileEntry;

public class SailorProfileDataProviderImpl implements SailorProfileDataProvider {

    @Override
    public List<SailorProfileEntry> loadSailorProfiles() {

        List<BadgeDTO> badges = new ArrayList<>();
        BadgeDTO b1 = new BadgeDTO("0000", "Best Sailor Ever");
        BadgeDTO b2 = new BadgeDTO("0001", "100 Miles Sailed");
        badges.add(b1);
        badges.add(b2);

        List<CompetitorDTO> competitors = new ArrayList<>();
        CompetitorDTOImpl c1 = new CompetitorDTOImpl();
        c1.setName("Hard Competition");
        CompetitorDTOImpl c2 = new CompetitorDTOImpl();
        c2.setName("Easy Competition");
        competitors.add(c1);
        competitors.add(c2);

        List<BoatClassDTO> boatclasses = new ArrayList<>();
        BoatClassDTO bc1 = new BoatClassDTO("Class-A", null, null);
        BoatClassDTO bc2 = new BoatClassDTO("Class-A", null, null);
        boatclasses.add(bc1);
        boatclasses.add(bc2);

        SailorProfileEntry e1 = new SailorProfileEntry("0000", "My Favourite Guy", competitors, badges, boatclasses);
        SailorProfileEntry e2 = new SailorProfileEntry("0001", "This Other Guy", competitors, badges, boatclasses);

        List<SailorProfileEntry> entries = new ArrayList<>();
        entries.add(e1);
        entries.add(e2);
        return entries;
    }

}
