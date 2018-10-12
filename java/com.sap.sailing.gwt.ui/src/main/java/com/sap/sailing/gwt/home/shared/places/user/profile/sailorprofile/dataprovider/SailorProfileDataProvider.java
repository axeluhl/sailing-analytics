package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider;

import java.util.Collection;
import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEventsDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfilesDTO;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.EditSailorProfileView;

public interface SailorProfileDataProvider {

    void setCompetitorSelectionPresenter(SailorProfilesCompetitorSelectionPresenter competitorSelectionPresenter);

    void setView(EditSailorProfileView sailorView);

    void loadSailorProfile(UUID uuid);

    void updateTitle(UUID uuid, String newTitle);

    void getEvents(UUID uuid, AsyncCallback<SailorProfileEventsDTO> asyncCallback);

    void loadSailorProfiles(AsyncCallback<SailorProfilesDTO> callback);

    void createNewEntry(UUID uuid, String newTitle);

    void removeSailorProfile(UUID uuid, AsyncCallback<SailorProfileDTO> callback);

    void getStatisticFor(UUID uuid, SailorProfileNumericStatisticType type,
            AsyncCallback<SailorProfileStatisticDTO> callback);

    void updateCompetitors(UUID uuid, Collection<SimpleCompetitorWithIdDTO> competitors,
            SailorProfilesCompetitorSelectionPresenter competitorSelectionProvider);
}
