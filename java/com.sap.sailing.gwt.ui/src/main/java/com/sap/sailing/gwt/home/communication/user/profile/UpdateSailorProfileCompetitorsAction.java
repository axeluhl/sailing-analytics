package com.sap.sailing.gwt.home.communication.user.profile;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreference;
import com.sap.sse.common.Util;

/**
 * {@link SailingAction} implementation to load sailor profiles for the currently logged in user to bee shown on the
 * sailor profiles overview page, preparing the appropriate data structure.
 */
public class UpdateSailorProfileCompetitorsAction extends UpdateSailorProfileAction {

    private ArrayList<SimpleCompetitorWithIdDTO> competitors = new ArrayList<>();

    public UpdateSailorProfileCompetitorsAction(UUID uuid, Iterable<SimpleCompetitorWithIdDTO> competitors) {
        super(uuid);
        Util.addAll(competitors, this.competitors);
    }

    public UpdateSailorProfileCompetitorsAction() {
    }

    /** convert SimpleCompetitorIdWithDTOs to Competitors */
    @GwtIncompatible
    private Iterable<Competitor> convertCompetitors(final Iterable<SimpleCompetitorWithIdDTO> comps,
            CompetitorAndBoatStore store) {
        return StreamSupport.stream(comps.spliterator(), false)
                .map(c -> store.getExistingCompetitorByIdAsString(c.getIdAsString())).collect(Collectors.toList());
    }

    @GwtIncompatible
    @Override
    protected SailorProfilePreference updatePreference(CompetitorAndBoatStore store, SailorProfilePreference p) {
        return new SailorProfilePreference(store, p, convertCompetitors(this.competitors, store));
    }

}
