package com.sap.sailing.server.impl.preferences.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsList;

public class SailorProfilePreferences extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = -3773945485974367205L;

    // TODO: setting preference title
    public static final String PREF_NAME = "user.profile.sailorProfiles";

    private transient SettingsList<SailorProfilePreference> sailorProfiles;

    public SailorProfilePreferences(CompetitorAndBoatStore store) {
        sailorProfiles = new SettingsList<>("sailorProfiles", this, () -> new SailorProfilePreference(store));
    }

    @Override
    protected void addChildSettings() {
        // We do not create the Setting instances here, because access to the RacingEventService would not be given.
        // Doing this, Java/GWT Serialization isn't working anymore. Because the preferences are only serialized as JSON
        // in the backend an transferred as DTO to the frontend, this isn't a problem. Due to usage of BoatClass and
        // Competitor domain objects, it wouldn't be GWT compatible anyway.
        // The usage of Java Serialization isn't planned by now, either.
    }

    public Iterable<SailorProfilePreference> getSailorProfiles() {
        return sailorProfiles.getValues();
    }

    public void setSailorProfiles(Iterable<SailorProfilePreference> sailorProfiles) {
        this.sailorProfiles.setValues(sailorProfiles);
    }

    public void updateOrInsert(final SailorProfilePreference sailorProfilePreference) {

        boolean noElementChanged = true;
        // copy to list
        List<SailorProfilePreference> list = new ArrayList<>();
        for (SailorProfilePreference p : sailorProfiles.getValues()) {
            noElementChanged = p.getUuid().equals(sailorProfilePreference.getUuid());
            list.add(p);
        }

        if (!noElementChanged) {
            // replace if necessary
            Iterator<SailorProfilePreference> it = list.iterator();
            SailorProfilePreference pref;
            while ((pref = it.next()) != null) {
                if (pref.getUuid().equals(sailorProfilePreference.getUuid())) {
                    it.remove();
                    list.add(sailorProfilePreference);
                }

            }
            sailorProfiles.setValues(list);
        }
    }
}
