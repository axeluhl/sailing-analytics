package com.sap.sailing.gwt.autoplay.client.app;

import java.util.logging.Logger;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.configs.AutoPlayConfiguration;
import com.sap.sailing.gwt.autoplay.client.configs.AutoPlayContextDefinition;
import com.sap.sailing.gwt.autoplay.client.nodes.base.AutoPlaySequenceNode;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

public class AutoPlayContextImpl implements AutoPlayContext {
    private static final Logger LOG = Logger.getLogger(AutoPlaySequenceNode.class.getName());
    private AutoPlayConfiguration configuration;
    private AutoPlayContextDefinition contextDefinition;
    private EventDTO event;
    private RegattaAndRaceIdentifier liveRace;
    private RegattaAndRaceIdentifier lastRace;
    private RegattaAndRaceIdentifier preLiveRace;
    private AutoplayPerspectiveLifecycle autoplayLifecycle;
    private PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> autoplaySettings;

    public AutoPlayContextImpl(AutoplayPerspectiveLifecycle autoplayLifecycle,
            PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> autoplaySettings,
            AutoPlayConfiguration configuration, AutoPlayContextDefinition contextDefinition,
            EventDTO initialEventData) {
        this(configuration, contextDefinition, initialEventData);
        if (autoplayLifecycle == null) {
            throw new IllegalStateException("No autoplayLifecycle in creation");
        }
        if (autoplaySettings == null) {
            throw new IllegalStateException("No autoplaySettings in creation");
        }
        this.autoplayLifecycle = autoplayLifecycle;
        this.autoplaySettings = autoplaySettings;
    }

    public AutoPlayContextImpl(AutoPlayConfiguration configuration, AutoPlayContextDefinition contextDefinition,
            EventDTO initialEventData) {
        if (configuration == null) {
            throw new IllegalStateException("No configuration in creation");
        }
        if (contextDefinition == null) {
            throw new IllegalStateException("No settings in creation");
        }
        this.configuration = configuration;
        this.contextDefinition = contextDefinition;
        this.event = initialEventData;
    }

    @Override
    public AutoPlayContextDefinition getContextDefinition() {
        return contextDefinition;
    }


    @Override
    public void updateLiveRace(RegattaAndRaceIdentifier currentPreLifeRace,
            RegattaAndRaceIdentifier currentLiveRace) {
        if (this.liveRace != null) {
            if (!this.liveRace.equals(currentLiveRace)) {
                LOG.info("Autoplay: Lastrace was " + lastRace + " liferace is now " + liveRace);
                this.lastRace = this.liveRace;
            }
        }
        this.preLiveRace = currentPreLifeRace;
        this.liveRace = currentLiveRace;
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
    public RegattaAndRaceIdentifier getLiveRace() {
        return liveRace;
    }

    @Override
    public RegattaAndRaceIdentifier getPreLiveRace() {
        return preLiveRace;
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

    @Override
    public RegattaAndRaceIdentifier getLifeOrPreLiveRace() {
        if(getLiveRace() != null){
            return getLiveRace();
        }
        return getPreLiveRace();
    }
}
