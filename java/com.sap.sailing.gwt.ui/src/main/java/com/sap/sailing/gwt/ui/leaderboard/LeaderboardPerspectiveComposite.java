package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveComposite;
import com.sap.sse.gwt.client.shared.perspective.ComponentContext;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycle;

public class LeaderboardPerspectiveComposite extends AbstractPerspectiveComposite {


    public LeaderboardPerspectiveComposite(Component parent, ComponentContext componentContext,
            PerspectiveLifecycle lifecycle, PerspectiveCompositeSettings settings) {
        super(parent, componentContext, lifecycle, settings);
    }

    @Override
    public SettingsDialogComponent getPerspectiveOwnSettingsDialogComponent() {
        return null;
    }

    @Override
    public boolean hasPerspectiveOwnSettings() {
        return false;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getDependentCssClassName() {
        return null;
    }

}
