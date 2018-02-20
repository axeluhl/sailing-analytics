package com.sap.sailing.gwt.autoplay.client.configs.impl;

import java.util.Collection;
import java.util.UUID;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayContextImpl;
import com.sap.sailing.gwt.autoplay.client.app.AutoplayPerspectiveLifecycle;
import com.sap.sailing.gwt.autoplay.client.app.AutoplayPerspectiveOwnSettings;
import com.sap.sailing.gwt.autoplay.client.configs.AutoPlayConfiguration;
import com.sap.sailing.gwt.autoplay.client.configs.AutoPlayContextDefinition;
import com.sap.sailing.gwt.autoplay.client.nodes.RootNodeClassic;
import com.sap.sailing.gwt.autoplay.client.utils.AutoplayHelper;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.shared.components.LinkWithSettingsGenerator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogForLinkSharing;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

public class AutoPlayClassicConfiguration extends AutoPlayConfiguration {
    private AutoPlayClientFactory cf;

    private Collection<DetailType> availableDetailTypes;

    @Override
    public void startRootNode(AutoPlayClientFactory cf, AutoPlayContextDefinition context,
            PerspectiveCompositeSettings<?> settings) {
        this.cf = cf;
        final UUID eventUUID = context.getEventId();
        AsyncCallback<EventDTO> getEventByIdAsyncCallback = new AsyncCallback<EventDTO>() {
            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(final EventDTO event) {
                cf.getSailingService().getAvailableDetailTypesForLeaderboard(context.getLeaderboardName(), new AsyncCallback<Collection<DetailType>>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        caught.printStackTrace();
                    }

                    @Override
                    public void onSuccess(Collection<DetailType> result) {
                        StrippedLeaderboardDTO leaderBoardDTO = AutoplayHelper.getSelectedLeaderboard(event,
                                context.getLeaderboardName());
                        AutoplayPerspectiveLifecycle autoplayLifecycle = new AutoplayPerspectiveLifecycle(leaderBoardDTO, cf.getUserService(), result);
                        cf.setAutoPlayContext(new AutoPlayContextImpl(autoplayLifecycle,
                                (PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings>) settings,
                                AutoPlayClassicConfiguration.this, context));
                        // start sixty inch slide loop nodes...
                        RootNodeClassic root = new RootNodeClassic(cf);
                        root.start(cf.getEventBus());
                    }
                });
            }

            @Override
            public void onFailure(Throwable caught) {
                caught.printStackTrace();
            }
        };
        cf.getSailingService().getEventById(eventUUID, true, getEventByIdAsyncCallback);
    }

    @Override
    public void openSettingsDialog(EventDTO selectedEvent, StrippedLeaderboardDTO leaderboard,
            OnSettingsCallback settingsCallback, PerspectiveCompositeSettings<?> settings,AutoPlayContextDefinition apcd) {
        DialogCallback<PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings>> callback = new DialogCallback<PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings>>() {
            @Override
            public void ok(PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> editedObject) {
                settingsCallback.newSettings(editedObject);
            }

            @Override
            public void cancel() {
            }
        };
        AutoplayPerspectiveLifecycle autoplayLifecycle = new AutoplayPerspectiveLifecycle(leaderboard, cf.getUserService(), availableDetailTypes);
        PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> autoplayPerspectiveSettings = autoplayLifecycle
                .createDefaultSettings();
        LinkWithSettingsGenerator<PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings>> settingsGenerator = new LinkWithSettingsGenerator<>(Window.Location.getPath(), autoplayLifecycle::createDefaultSettings, apcd);
        new SettingsDialogForLinkSharing<PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings>>(settingsGenerator,
                autoplayLifecycle, autoplayPerspectiveSettings, StringMessages.INSTANCE, true, callback).show();
    }

    @Override
    public void loadSettingsDefault(EventDTO selectedEvent, StrippedLeaderboardDTO leaderboard,
            OnSettingsCallback settingsCallback) {
        AutoplayPerspectiveLifecycle autoplayLifecycle = new AutoplayPerspectiveLifecycle(leaderboard, cf.getUserService(), availableDetailTypes);
        settingsCallback.newSettings(autoplayLifecycle.createDefaultSettings());
    }

}
