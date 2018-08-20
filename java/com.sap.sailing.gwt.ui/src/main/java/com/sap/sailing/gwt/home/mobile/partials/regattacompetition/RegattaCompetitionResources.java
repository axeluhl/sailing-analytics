package com.sap.sailing.gwt.home.mobile.partials.regattacompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.sap.sailing.gwt.home.shared.resources.SharedHomeResources;

public interface RegattaCompetitionResources extends SharedHomeResources {
    public static final RegattaCompetitionResources INSTANCE = GWT.create(RegattaCompetitionResources.class);

    @Source("RegattaCompetition.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String accordion();
        String accordion_trigger();
        String accordion_content();
        String regattacompetition();
        String grid();
        String regattacompetition_phase();
        String accordioncollapsed();
        String sectionheader_item_togglearrow();
        String regattacompetition_phase_fleet();
        String regattacompetition_phase_fleetfullwidth();
        String regattacompetition_phase_fleet_race_title();
        String regattacompetition_phase_fleetcompact();
        String regattacompetition_phase_fleet_race_subtitle();
        String regattacompetition_phase_fleet_race_arrow();
        String regattacompetition_phase_fleet_title();
        String regattacompetition_phase_fleet_race();
        String regattacompetition_phase_fleet_racetracked();
        String regattacompetition_phase_fleet_raceuntracked();
        String regattacompetition_phase_fleet_racelive();
        String regattacompetition_phase_fleet_raceplanned();
        String regattacompetition_phase_fleet_race_state();
        String regattacompetition_phase_fleet_race_date();
        String regattacompetition_phase_fleet_race_title_big();
    }
}
