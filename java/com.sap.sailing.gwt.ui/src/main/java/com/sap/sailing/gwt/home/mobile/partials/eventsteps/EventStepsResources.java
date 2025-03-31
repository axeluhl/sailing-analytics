package com.sap.sailing.gwt.home.mobile.partials.eventsteps;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface EventStepsResources extends ClientBundle {
    public static final EventStepsResources INSTANCE = GWT.create(EventStepsResources.class);

    @Source("EventSteps.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String eventsteps();
        String grid();
        String eventsteps_phases();
        String eventsteps_phases_phase();
        String eventsteps_phases_phaseinactive();
        String eventsteps_phases_phase_link();
        String eventsteps_phases_phase_name();
        String eventsteps_phases_phase_progressbar();
        String eventsteps_phases_phase_progressbar_fleet();
        String eventsteps_phases_phase_progressbar_fleet_progress();
        String eventsteps_phases_phase_progress();
        String eventsteps_phases_phase_progress_icon();
        String eventsteps_phases_phase_progress_text();
    }
}
