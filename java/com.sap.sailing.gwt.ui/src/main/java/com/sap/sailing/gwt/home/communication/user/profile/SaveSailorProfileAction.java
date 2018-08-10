package com.sap.sailing.gwt.home.communication.user.profile;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.SailorProfile;
import com.sap.sailing.domain.base.impl.SailorProfileImpl;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEntry;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreference;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreferences;
import com.sap.sse.gwt.dispatch.shared.commands.VoidResult;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * {@link SailingAction} implementation to save favorite boat classes for the currently logged in user as selected on
 * the preferences page.
 */
public class SaveSailorProfileAction implements SailingAction<VoidResult> {
    
    private SailorProfileEntry sailorProfileEntry;
    
    protected SaveSailorProfileAction() {}

    public SaveSailorProfileAction(SailorProfileEntry sailorProfileEntry) {
        this.sailorProfileEntry = sailorProfileEntry;
    }

    @Override
    @GwtIncompatible
    public VoidResult execute(SailingDispatchContext ctx) throws DispatchException {
        DomainFactory domainFactory = ctx.getRacingEventService().getBaseDomainFactory();

        List<SailorProfilePreference> sailorProfilePreferences = new ArrayList<>();
        SailorProfilePreferences prefs = ctx.getPreferenceForCurrentUser(SailorProfilePreferences.PREF_NAME);
        if (prefs == null) {
            prefs = new SailorProfilePreferences(domainFactory);
        }
        
        prefs.updateOrInsert(new SailorProfilePreference(domainFactory, convert(sailorProfileEntry)));
       
        prefs.setSailorProfiles(sailorProfilePreferences);
        ctx.setPreferenceForCurrentUser(SailorProfilePreferences.PREF_NAME, prefs);
        return new VoidResult();
    }

    @GwtIncompatible
    private SailorProfile convert(SailorProfileEntry entr) {
        return new SailorProfileImpl(entr.getName(), entr.getKey());
    }

}
