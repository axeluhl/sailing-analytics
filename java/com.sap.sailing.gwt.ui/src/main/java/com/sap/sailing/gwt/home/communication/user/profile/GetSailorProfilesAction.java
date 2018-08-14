package com.sap.sailing.gwt.home.communication.user.profile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.BadgeDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEntries;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEntry;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreference;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreferences;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * {@link SailingAction} implementation to load favorites for the currently logged in user to bee shown on the
 * preferences page, preparing the appropriate data structure.
 */
public class GetSailorProfilesAction implements SailingAction<SailorProfileEntries> {

    private UUID uuid;

    public GetSailorProfilesAction(UUID uuid) {
        this();
        this.uuid = uuid;
    }

    public GetSailorProfilesAction() {
    }

    @Override
    @GwtIncompatible
    public SailorProfileEntries execute(SailingDispatchContext ctx) throws DispatchException {
        SailorProfilePreferences preferences = ctx.getPreferenceForCurrentUser(SailorProfilePreferences.PREF_NAME);
        SailorProfileEntries result;
        List<SailorProfileEntry> list = new ArrayList<>();
        StreamSupport.stream(preferences.getSailorProfiles().spliterator(), false)
                .filter(e -> uuid == null || (uuid != null && uuid.equals(e.getUuid())))
                .forEach(s -> list.add(convertToDto(s)));
        result = new SailorProfileEntries(list);
        return result;
    }

    @GwtIncompatible
    private SailorProfileEntry convertToDto(SailorProfilePreference pref) {
        return new SailorProfileEntry(pref.getUuid(), pref.getName(), convertCompetitors(pref.getCompetitors()),
                new ArrayList<BadgeDTO>(), new ArrayList<BoatClassDTO>());
    }

    @GwtIncompatible
    private List<SimpleCompetitorWithIdDTO> convertCompetitors(Iterable<Competitor> comps) {
        return StreamSupport.stream(comps.spliterator(), false).filter(c -> c != null)
                .map(c -> new SimpleCompetitorWithIdDTO(c)).collect(Collectors.toList());
    }

}
