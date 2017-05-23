package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.configs.AutoPlayConfiguration;
import com.sap.sailing.gwt.autoplay.client.configs.AutoPlayContextDefinition;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

public class AutoPlayContextImpl implements AutoPlayContext {
    private AutoPlayConfiguration configuration;
    private AutoPlayContextDefinition contextDefinition;
    private EventDTO event;
    private RegattaAndRaceIdentifier lifeRace;
    private RegattaAndRaceIdentifier lastRace;
    private AutoplayPerspectiveLifecycle autoplayLifecycle;
    private PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> autoplaySettings;

    public AutoPlayContextImpl(AutoplayPerspectiveLifecycle autoplayLifecycle,
            PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> autoplaySettings,
            AutoPlayConfiguration configuration, AutoPlayContextDefinition contextDefinition) {
        this(configuration, contextDefinition);
        if (autoplayLifecycle == null) {
            throw new IllegalStateException("No autoplayLifecycle in creation");
        }
        if (autoplaySettings == null) {
            throw new IllegalStateException("No autoplaySettings in creation");
        }
        this.autoplayLifecycle = autoplayLifecycle;
        this.autoplaySettings = autoplaySettings;
    }

    public AutoPlayContextImpl(AutoPlayConfiguration configuration, AutoPlayContextDefinition contextDefinition) {
        if (configuration == null) {
            throw new IllegalStateException("No configuration in creation");
        }
        if (contextDefinition == null) {
            throw new IllegalStateException("No settings in creation");
        }
        this.configuration = configuration;
        this.contextDefinition = contextDefinition;
    }

    @Override
    public AutoPlayContextDefinition getContextDefinition() {
        return contextDefinition;
    }

    @Override
    public RegattaAndRaceIdentifier getLifeRace() {
        return lifeRace;
    }

    @Override
    public void setCurrentLifeRace(RegattaAndRaceIdentifier lifeRace) {
        if (this.lifeRace != null) {
            if (!this.lifeRace.equals(lifeRace)) {
                this.lastRace = this.lifeRace;
                GWT.log("lastrace was " + lastRace + " liferace is now " + lifeRace);
            }
        }
        this.lifeRace = lifeRace;
    }

    @Override
    public AutoPlayConfiguration getAutoPlayConfiguration() {
        return configuration;
    }

    @Override
    public EventDTO getEvent() {
        return event;
    }

    @Override
    public void updateEvent(EventDTO event) {
        this.event = event;
    }

    @Override
    public RegattaAndRaceIdentifier getLastRace() {
        return lastRace;
    }

    public PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> getAutoplaySettings() {
        return autoplaySettings;
    }

    public AutoplayPerspectiveLifecycle getAutoplayLifecycle() {
        return autoplayLifecycle;
    }
}
