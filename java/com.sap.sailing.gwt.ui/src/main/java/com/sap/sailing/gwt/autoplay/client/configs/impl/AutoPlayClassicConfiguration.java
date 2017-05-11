package com.sap.sailing.gwt.autoplay.client.configs.impl;

import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayContextImpl;
import com.sap.sailing.gwt.autoplay.client.app.AutoplayPerspectiveLifecycle;
import com.sap.sailing.gwt.autoplay.client.app.AutoplayPerspectiveOwnSettings;
import com.sap.sailing.gwt.autoplay.client.configs.AutoPlayConfiguration;
import com.sap.sailing.gwt.autoplay.client.configs.AutoPlayContextDefinition;
import com.sap.sailing.gwt.autoplay.client.nodes.RootNodeClassic;
import com.sap.sailing.gwt.autoplay.client.utils.AutoplayHelper;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

public class AutoPlayClassicConfiguration extends AutoPlayConfiguration {

    @Override
    public void startRootNode(AutoPlayClientFactory cf, AutoPlayContextDefinition context,
            PerspectiveCompositeSettings<?> settings) {

        final UUID eventUUID = context.getEventId();
        AsyncCallback<EventDTO> getEventByIdAsyncCallback = new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(final EventDTO event) {
                StrippedLeaderboardDTO leaderBoardDTO = AutoplayHelper.getSelectedLeaderboard(event,
                        context.getLeaderboardName());
                AutoplayPerspectiveLifecycle autoplayLifecycle = new AutoplayPerspectiveLifecycle(leaderBoardDTO);
                cf.setAutoPlayContext(new AutoPlayContextImpl(autoplayLifecycle,
                        (PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings>) settings,
                        AutoPlayClassicConfiguration.this, context));
                // start sixty inch slide loop nodes...
                RootNodeClassic root = new RootNodeClassic(cf);
                root.start(cf.getEventBus());
            }

            @Override
            public void onFailure(Throwable caught) {
                caught.printStackTrace();
            }
        };
        cf.getSailingService().getEventById(eventUUID, true, getEventByIdAsyncCallback);
    }


}
