package com.sap.sailing.gwt.home.communication.user.profile;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.server.impl.preferences.model.CompetitorNotificationPreference;
import com.sap.sailing.server.impl.preferences.model.CompetitorNotificationPreferences;
import com.sap.sse.gwt.dispatch.shared.commands.HasWriteAction;
import com.sap.sse.gwt.dispatch.shared.commands.VoidResult;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * {@link SailingAction} implementation to save favorite competitors for the currently logged in user as selected on
 * the preferences page.
 */
public class SaveFavoriteCompetitorsAction implements SailingAction<VoidResult>, HasWriteAction {
    
    private FavoriteCompetitorsDTO favorites;
    
    protected SaveFavoriteCompetitorsAction() {}

    public SaveFavoriteCompetitorsAction(FavoriteCompetitorsDTO favorites) {
        this.favorites = favorites;
    }

    @Override
    @GwtIncompatible
    public VoidResult execute(SailingDispatchContext ctx) throws DispatchException {
        CompetitorNotificationPreferences prefs = new CompetitorNotificationPreferences(ctx.getRacingEventService());
        List<CompetitorNotificationPreference> competitorPreferences = new ArrayList<>();
        CompetitorAndBoatStore competitorStore = ctx.getRacingEventService().getCompetitorAndBoatStore();
        for (SimpleCompetitorWithIdDTO competitorDTO : favorites.getSelectedCompetitors()) {
            String competitorIdAsString = competitorDTO.getIdAsString();
            if (competitorStore.getExistingCompetitorByIdAsString(competitorIdAsString) != null) {
                competitorPreferences
                        .add(new CompetitorNotificationPreference(competitorIdAsString, favorites.isNotifyAboutResults()));
            } else {
                throw new DispatchException(
                        "Competitor with ID: " + competitorIdAsString + "is not an existing Competitor on this server.");
            }
        }
        prefs.setCompetitors(competitorPreferences);
        ctx.setPreferenceForCurrentUser(CompetitorNotificationPreferences.PREF_NAME, prefs);
        return new VoidResult();
    }

}
