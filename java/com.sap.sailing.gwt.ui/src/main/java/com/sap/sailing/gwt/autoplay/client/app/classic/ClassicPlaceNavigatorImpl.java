package com.sap.sailing.gwt.autoplay.client.app.classic;

import java.util.UUID;

import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.nodes.ClassicStartupNode;
import com.sap.sailing.gwt.autoplay.client.utils.AutoplayHelper;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.settings.SettingsToStringSerializer;

public class ClassicPlaceNavigatorImpl implements ClassicPlaceNavigator {

    private PlaceController placeController;

    public ClassicPlaceNavigatorImpl(PlaceController placeController) {
        super();
        this.placeController = placeController;
    }

    @Override
    public void goToPlayer(String serializedSettings, AutoPlayClientFactoryClassic cf) {

        ClassicSetting context = new ClassicSetting();

        SettingsToStringSerializer stringSerializer = new SettingsToStringSerializer();
        stringSerializer.fromString(serializedSettings, context);
        final UUID eventUUID = context.getEventId();

        AsyncCallback<EventDTO> getEventByIdAsyncCallback = new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(final EventDTO event) {
                StrippedLeaderboardDTO leaderBoardDTO = AutoplayHelper.getSelectedLeaderboard(event,
                        context.getLeaderboardName());

                AutoplayPerspectiveLifecycle autoplayLifecycle = new AutoplayPerspectiveLifecycle(leaderBoardDTO);
                PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> autoplaySettings = stringSerializer
                        .fromString(serializedSettings, autoplayLifecycle.createDefaultSettings());

                cf.setSlideContext(
                        new ClassicContextImpl(cf.getEventBus(), autoplayLifecycle, autoplaySettings, context));
                // start sixty inch slide loop nodes...
                ClassicStartupNode root = new ClassicStartupNode(cf);
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
