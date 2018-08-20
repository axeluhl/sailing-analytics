package com.sap.sailing.gwt.home.desktop.partials.regattacompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.sap.sailing.gwt.home.shared.resources.SharedHomeResources;

public interface RegattaCompetitionResources extends SharedHomeResources {
    public static final RegattaCompetitionResources INSTANCE = GWT.create(RegattaCompetitionResources.class);

    @Source("RegattaCompetition.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String phase();
        String phase_head();
        String phase_head_title();
        String phase_head_info();
        String phase_head_info_item();
        String regattalistitem();
        String fleet();
        String default_fleet();
        String fleet_corner();
        String fleet_details();
        String fleet_details_name();
        String fleet_details_competitors();
        String fleet_races();
        String fleet_races_race();
        String fleet_races_race_name();
        String fleet_races_race_info();
        String fleet_races_raceuntracked();
        String fleet_races_racelive();
        String fleet_races_raceplanned();
        String fleet_races_race_date();
        String fleet_races_race_state();
    }
}
