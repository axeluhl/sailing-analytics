package com.sap.sailing.gwt.autoplay.client.configs.impl;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.security.SecuredDomainType;
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
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.shared.components.LinkWithSettingsGenerator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogForLinkSharing;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.premium.PaywallResolver;
import com.sap.sse.security.ui.client.subscription.SubscriptionServiceFactory;

public class AutoPlayClassicConfiguration extends AutoPlayConfiguration {
    private static final Logger logger = Logger.getLogger(AutoPlayClassicConfiguration.class.getName());

    @Override
    public void startRootNode(AutoPlayClientFactory cf, AutoPlayContextDefinition context,
            PerspectiveCompositeSettings<?> settings, EventDTO initialEventData) {
        cf.getSailingService().getAvailableDetailTypesForLeaderboard(context.getLeaderboardName(),
                null, new AsyncCallback<Iterable<DetailType>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        logger.log(Level.WARNING, "Could not load detailtypes for leaderboard", caught);
                    }
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onSuccess(Iterable<DetailType> result) {
                        StrippedLeaderboardDTO leaderBoardDTO = AutoplayHelper.getSelectedLeaderboard(initialEventData,
                                context.getLeaderboardName());
                        PaywallResolver raceboardPaywallResolver = new PaywallResolver(cf.getUserService(), cf.getSubscriptionServiceFactory(), null);
                        if (leaderBoardDTO != null) {
                            PaywallResolver leaderboardPaywallResolver = new PaywallResolver(cf.getUserService(), cf.getSubscriptionServiceFactory(), leaderBoardDTO);
                            AutoplayPerspectiveLifecycle autoplayLifecycle = new AutoplayPerspectiveLifecycle(
                                    leaderBoardDTO, cf.getUserService(), leaderboardPaywallResolver, raceboardPaywallResolver, result);
                            cf.setAutoPlayContext(new AutoPlayContextImpl(autoplayLifecycle,
                                    (PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings>) settings,
                                    AutoPlayClassicConfiguration.this, context, initialEventData));
                            // start sixty inch slide loop nodes...
                            new RootNodeClassic(cf).start(cf.getEventBus());
                        } else {
                            new PaywallResolver(cf.getUserService(), cf.getSubscriptionServiceFactory(),  context.getLeaderboardName(), SecuredDomainType.LEADERBOARD, 
                                    new AsyncCallback<PaywallResolver>() {
                                        @Override
                                        public void onSuccess(PaywallResolver resolver) {
                                            AutoplayPerspectiveLifecycle autoplayLifecycle = new AutoplayPerspectiveLifecycle(
                                                    leaderBoardDTO, cf.getUserService(), resolver, raceboardPaywallResolver, result);
                                            cf.setAutoPlayContext(new AutoPlayContextImpl(autoplayLifecycle,
                                                    (PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings>) settings,
                                                    AutoPlayClassicConfiguration.this, context, initialEventData));
                                            // start sixty inch slide loop nodes...
                                            new RootNodeClassic(cf).start(cf.getEventBus());
                                        }
                                        
                                        @Override
                                        public void onFailure(Throwable caught) {
                                            Notification.notify(StringMessages.INSTANCE.error(), NotificationType.ERROR);
                                            GWT.log("Error while init PaywallResolver", caught);
                                        }
                                    });
                        }
                        
                    }
                });
    }

    @Override
    public void openSettingsDialog(EventDTO selectedEvent, StrippedLeaderboardDTO leaderboard,
            OnSettingsCallback settingsCallback, PerspectiveCompositeSettings<?> settings,
            AutoPlayContextDefinition apcd, UserService userService, SubscriptionServiceFactory subscriptionServiceFactory,
            PaywallResolver leaderboarPaywallResolver) {
        DialogCallback<PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings>> callback = new DialogCallback<PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings>>() {
            @Override
            public void ok(PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> editedObject) {
                settingsCallback.newSettings(editedObject,
                        getUrlWithSettings(apcd, editedObject, leaderboard, userService, subscriptionServiceFactory));
            }

            @Override
            public void cancel() {
            }
        };
        PaywallResolver racePaywallResolver = new PaywallResolver(userService, subscriptionServiceFactory, null);
        AutoplayPerspectiveLifecycle autoplayLifecycle = new AutoplayPerspectiveLifecycle(leaderboard, userService, leaderboarPaywallResolver, racePaywallResolver,
                Arrays.asList(DetailType.values()));
        @SuppressWarnings("unchecked")
        PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> autoplayPerspectiveSettings = settings != null
                ? (PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings>) settings
                : autoplayLifecycle.createDefaultSettings();
        LinkWithSettingsGenerator<PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings>> settingsGenerator = new LinkWithSettingsGenerator<>(
                Window.Location.getPath(), autoplayLifecycle::createDefaultSettings, apcd);
        new SettingsDialogForLinkSharing<PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings>>(
                settingsGenerator, autoplayLifecycle, autoplayPerspectiveSettings, StringMessages.INSTANCE, true,
                callback).show();
    }

    @Override
    public void loadSettingsDefault(EventDTO selectedEvent, AutoPlayContextDefinition apcd,
            StrippedLeaderboardDTO leaderboard, UserService userService, SubscriptionServiceFactory subscriptionServiceFactory, OnSettingsCallback settingsCallback) {
        AutoplayPerspectiveLifecycle autoplayLifecycle = new AutoplayPerspectiveLifecycle(leaderboard, userService, subscriptionServiceFactory,
                Arrays.asList(DetailType.values()));
        PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> defaultSettings = autoplayLifecycle
                .createDefaultSettings();
        settingsCallback.newSettings(defaultSettings,
                getUrlWithSettings(apcd, defaultSettings, leaderboard, userService, subscriptionServiceFactory));
    }

    private String getUrlWithSettings(AutoPlayContextDefinition apcd,
            PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> settings, StrippedLeaderboardDTO leaderboard,
            UserService userService, SubscriptionServiceFactory subscriptionServiceFactory) {
        AutoplayPerspectiveLifecycle autoplayLifecycle = new AutoplayPerspectiveLifecycle(leaderboard, userService, subscriptionServiceFactory,
                Arrays.asList(DetailType.values()));
        LinkWithSettingsGenerator<PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings>> settingsGenerator = new LinkWithSettingsGenerator<>(
                Window.Location.getPath(), autoplayLifecycle::createDefaultSettings, apcd);

        return settingsGenerator.createUrl(settings);
    }
}
