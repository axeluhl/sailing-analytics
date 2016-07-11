package com.sap.sailing.gwt.home.communication.user.profile;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.server.impl.preferences.model.BoatClassNotificationPreference;
import com.sap.sailing.server.impl.preferences.model.BoatClassNotificationPreferences;
import com.sap.sse.gwt.dispatch.shared.commands.VoidResult;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * {@link SailingAction} implementation to save favorite boat classes for the currently logged in user as selected on
 * the preferences page.
 */
public class SaveFavoriteBoatClassesAction implements SailingAction<VoidResult> {
    
    private FavoriteBoatClassesDTO favorites;
    
    protected SaveFavoriteBoatClassesAction() {}

    public SaveFavoriteBoatClassesAction(FavoriteBoatClassesDTO favorites) {
        this.favorites = favorites;
    }

    @Override
    @GwtIncompatible
    public VoidResult execute(SailingDispatchContext ctx) throws DispatchException {
        BoatClassNotificationPreferences prefs = new BoatClassNotificationPreferences(ctx.getRacingEventService());
        List<BoatClassNotificationPreference> boatClassPreferences = new ArrayList<>();
        DomainFactory domainFactory = ctx.getRacingEventService().getBaseDomainFactory();
        for (BoatClassDTO boatClassDTO : favorites.getSelectedBoatClasses()) {
            BoatClass boatClass = domainFactory.getOrCreateBoatClass(boatClassDTO.getName());
            boatClassPreferences.add(new BoatClassNotificationPreference(domainFactory, boatClass, 
                    favorites.isNotifyAboutUpcomingRaces(), favorites.isNotifyAboutResults()));
        }
        prefs.setBoatClasses(boatClassPreferences);
        ctx.setPreferenceForCurrentUser(BoatClassNotificationPreferences.PREF_NAME, prefs);
        return new VoidResult();
    }

}
