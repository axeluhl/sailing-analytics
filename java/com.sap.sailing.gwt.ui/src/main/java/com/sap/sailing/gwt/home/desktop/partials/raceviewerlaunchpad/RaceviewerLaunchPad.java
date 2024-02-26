package com.sap.sailing.gwt.home.desktop.partials.raceviewerlaunchpad;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.security.SecuredDomainType.TrackedRaceActions;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardModes;
import com.sap.sse.gwt.client.dialog.ConfirmationDialog;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.premium.PaywallResolver;

class RaceviewerLaunchPad<T extends SimpleRaceMetadataDTO> extends Composite {

    private static RaceviewerLaunchPadUiBinder uiBinder = GWT.create(RaceviewerLaunchPadUiBinder.class);

    interface RaceviewerLaunchPadUiBinder extends UiBinder<Widget, RaceviewerLaunchPad<?>> {
    }

    @UiField RaceviewerLaunchPadResources local_res;
    @UiField DivElement itemContainerUi;
    private final BiFunction<? super T, String, String> raceboardUrlFactory;
    private final Function<? super T, String> mapAndWindChartUrlFactory;

    private final PopupPanel parent;
    private final PaywallResolver paywallResolver;
    private Set<RaceviewerLaunchPadItem> launchpadItems = new HashSet<>();

    RaceviewerLaunchPad(final T data, final BiFunction<? super T, String, String> raceboardUrlFactory,
            final Function<? super T, String> mapAndWindChartUrlFactory, final PopupPanel parent, PaywallResolver paywallResolver) {
        this.raceboardUrlFactory = raceboardUrlFactory;
        this.mapAndWindChartUrlFactory = mapAndWindChartUrlFactory;
        this.parent = parent;
        this.paywallResolver = paywallResolver;
        initWidget(uiBinder.createAndBindUi(this));
        local_res.css().ensureInjected();
        paywallResolver.registerUserStatusEventHandler((dto, notifyOtherInstances) -> {
            removeItems();
            initItems(data);
        });
        initItems(data);
        initStyles(data);
        sinkEvents(Event.ONCLICK);
    }
    
    private void removeItems() {
        for (RaceviewerLaunchPadItem element : launchpadItems) {
            element.getElement().removeFromParent();
        }
        launchpadItems.clear();
    }

    private ConfirmationDialog createSubscribeDialog(Action action, SecuredDTO contextDTO) {
        final StringMessages i18n = StringMessages.INSTANCE;
        return ConfirmationDialog.create(i18n.subscriptionSuggestionTitle(),
                i18n.pleaseSubscribeToUse(), i18n.takeMeToSubscriptions(), i18n.cancel(),
                () -> paywallResolver.getUnlockingSubscriptionPlans(action, contextDTO, (unlockingPlans) -> 
                Window.open(EntryPointLinkFactory.createSubscriptionPageLink(unlockingPlans), "_blank", "")));
    }

    private void initItems(final T data) {
        if (!data.isFinished()) {
            addItem(data, RaceviewerLaunchPadMenuItem.WATCH_LIVE);
        } else {
            addItem(data, RaceviewerLaunchPadMenuItem.REPLAY);
            addItem(data, RaceviewerLaunchPadMenuItem.RACE_ANALYSIS);
        }
        if (data.isRunning() || data.isFinished()) {
            addItem(data, RaceviewerLaunchPadMenuItem.START_ANALYSIS);
            addItem(data, RaceviewerLaunchPadMenuItem.WINNING_LANES);
        }
        if (!data.isFinished()) {
            addItem(data, RaceviewerLaunchPadMenuItem.WIND_AND_COURSE);
        }
    }

    private void initStyles(final T data) {
        if (!data.isRunning() && !data.isFinished()) {
            addStyleName(local_res.css().raceviewerlaunchpadplanned());
        } else if (data.isRunning()) {
            addStyleName(local_res.css().raceviewerlaunchpadlive());
        }
    }

    @Override
    public void onBrowserEvent(final Event event) {
        if (event.getTypeInt() == Event.ONCLICK) {
            parent.hide();
        }
        super.onBrowserEvent(event);
    }

    private void addItem(final T data, final RaceviewerLaunchPadMenuItem item) {
        if(paywallResolver.hasPermission(item.action, data)){
            final String url = RaceviewerLaunchPadMenuItem.WIND_AND_COURSE == item ? 
                    mapAndWindChartUrlFactory.apply(data)
                    : raceboardUrlFactory.apply(data, item.raceBoardMode);
            final RaceviewerLaunchPadItem element = new RaceviewerLaunchPadItem(item.label, item.icon, url);
            this.launchpadItems.add(element);
            itemContainerUi.appendChild(element.getElement());
        }else {
            createSubscribeDialog(item.action, data);
        }
        
    }

    private enum RaceviewerLaunchPadMenuItem {
        REPLAY(StringMessages.INSTANCE.replay(), "launch-play", RaceBoardModes.PLAYER.name(), null),
        WATCH_LIVE(StringMessages.INSTANCE.watchLive(), "launch-play", RaceBoardModes.PLAYER.name(), null),
        RACE_ANALYSIS(StringMessages.INSTANCE.raceAnalysis(), "launch-loupe", 
                RaceBoardModes.FULL_ANALYSIS.name(), null),
        START_ANALYSIS(StringMessages.INSTANCE.startAnalysis(), "launch-start", RaceBoardModes.START_ANALYSIS.name(), 
                TrackedRaceActions.VIEWANALYSISCHARTS),
        WINNING_LANES(StringMessages.INSTANCE.winningLanes(), "launch-winning-lanes", 
                RaceBoardModes.WINNING_LANES.name(), null),
        WIND_AND_COURSE(StringMessages.INSTANCE.windAndCourse(), "launch-wind-course", null, null);

        private String label, icon, raceBoardMode;
        private final Action action;

        private RaceviewerLaunchPadMenuItem(final String label, final String iconKey, final String raceBoardMode, 
                final Action action) {
            this.label = label;
            this.action = action;
            this.icon = "<svg><use xlink:href=\"#" + iconKey + "\"></use></svg>";
            this.raceBoardMode = raceBoardMode;
        }
    }

}
