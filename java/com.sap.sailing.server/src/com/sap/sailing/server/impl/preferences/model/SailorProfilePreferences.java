package com.sap.sailing.server.impl.preferences.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsList;

public class SailorProfilePreferences extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = -3773945485974367205L;

    // TODO: setting preference title
    public static final String PREF_NAME = "sailorProfiles";

    private transient SettingsList<SailorProfilePreference> sailorProfiles;

    public SailorProfilePreferences(DomainFactory domainFactory) {
        sailorProfiles = new SettingsList<>("sailorProfiles", this, () -> new SailorProfilePreference(domainFactory));
    }

    @Override
    protected void addChildSettings() {
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
            noElementChanged = p.getSailorProfile().equals(sailorProfilePreference.getSailorProfile());
            list.add(p);
        }

        if (!noElementChanged) {
            // replace if necessary
            Iterator<SailorProfilePreference> it = list.iterator();
            SailorProfilePreference pref;
            while ((pref = it.next()) != null) {
                if (pref.getSailorProfile().equals(sailorProfilePreference.getSailorProfile())) {
                    it.remove();
                    list.add(sailorProfilePreference);
                }

            }
            sailorProfiles.setValues(list);
        }
    }
}
