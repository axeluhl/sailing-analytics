package com.sap.sailing.gwt.autoplay.client.shared.leaderboard;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.autoplay.client.shared.oldleaderboard.OldLeaderboard;
import com.sap.sailing.gwt.common.client.CSS3Util;
import com.sap.sailing.gwt.common.client.FullscreenUtil;
import com.sap.sailing.gwt.settings.client.leaderboard.MultiRaceLeaderboardPanelLifecycle;
import com.sap.sailing.gwt.settings.client.leaderboard.MultiRaceLeaderboardSettings;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.FlagImageResolverImpl;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.ClassicLeaderboardStyle;
import com.sap.sailing.gwt.ui.leaderboard.MultiRaceLeaderboardPanel;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveComposite;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.settings.ComponentContextWithSettingsStorage;

/**
 * A perspective managing a header with a single leaderboard filling the rest of the screen.
 * 
 * @author Frank Mittag
 *
 */
public class MultiRaceLeaderboardWithZoomingPerspective extends AbstractPerspectiveComposite<LeaderboardWithZoomingPerspectiveLifecycle,
    LeaderboardWithZoomingPerspectiveSettings> implements LeaderboardUpdateProvider {
    private final DockLayoutPanel dockPanel;
    private final static int SAP_HEADER_HEIGHT = 75;
    private final Widget currentContentWidget;
    private final MultiRaceLeaderboardPanel leaderboardPanel;
    private final StringMessages stringMessages;
    
    public MultiRaceLeaderboardWithZoomingPerspective(Component<?> parent,
            ComponentContextWithSettingsStorage<PerspectiveCompositeSettings<LeaderboardWithZoomingPerspectiveSettings>> componentContext,
            LeaderboardWithZoomingPerspectiveLifecycle lifecycle,
            PerspectiveCompositeSettings<LeaderboardWithZoomingPerspectiveSettings> settings,
            SailingServiceAsync sailingService, UserService userService, AsyncActionsExecutor asyncActionsExecutor,
            CompetitorSelectionProvider competitorSelectionProvider, Timer timer,
            String leaderboardName, final ErrorReporter errorReporter, final StringMessages stringMessages,
            boolean startInFullScreenMode, Iterable<DetailType> availableDetailTypes) {
        super(parent, componentContext, lifecycle, settings);
        this.stringMessages = stringMessages;
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                if (MultiRaceLeaderboardWithZoomingPerspective.this.getPerspectiveSettings().isLeaderboardAutoZoom()) {
                    autoZoomContentWidget(SAP_HEADER_HEIGHT, currentContentWidget);
                }
            }
        });
        
        leaderboardPanel = createLeaderboardPanel(lifecycle, settings, sailingService, asyncActionsExecutor,
                competitorSelectionProvider, timer, leaderboardName, errorReporter, stringMessages, availableDetailTypes);
        leaderboardPanel.getContentWidget().getElement().getStyle().setFontWeight(FontWeight.BOLD);
        addChildComponent(leaderboardPanel);
        dockPanel = new DockLayoutPanel(Unit.PX);
        OldLeaderboard oldLeaderboard = new OldLeaderboard(leaderboardPanel, stringMessages);
        leaderboardPanel.addLeaderboardUpdateListener(oldLeaderboard);
        currentContentWidget = oldLeaderboard.getContentWidget();
        if (getPerspectiveSettings().isLeaderboardAutoZoom()) {
            autoZoomContentWidget(SAP_HEADER_HEIGHT, currentContentWidget);
        } else {
            Double zoom = getPerspectiveSettings().getLeaderboardZoomFactor();
            zoomContentWidget(SAP_HEADER_HEIGHT, currentContentWidget, zoom);
        }
        dockPanel.add(oldLeaderboard);
        initWidget(dockPanel);
    }

    @Override
    public Widget getEntryWidget() {
        return this;
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public void setVisible(boolean visibility) {
    }

    @Override
    public String getDependentCssClassName() {
        return "";
    }
    
    private void zoomContentWidget(final int headerHeight, final Widget contentWidget, final double scaleFactor) {
        if(contentWidget != null) {
            Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {
                public boolean execute () {
                    boolean invokeAgain = true;
                    if(currentContentWidget.getOffsetHeight() > 50) {
                        scaleContentWidget(headerHeight, contentWidget, scaleFactor);
                        FullscreenUtil.requestFullscreen();

                        invokeAgain = false;
                    }
                    return invokeAgain;
                }
              }, 1000);
        }
    }

    private void autoZoomContentWidget(final int headerHeight, final Widget contentWidget) {
        if(contentWidget != null) {
            Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {
                public boolean execute () {
                    boolean invokeAgain = true;
                    if(currentContentWidget.getOffsetHeight() > 50) {
                        scaleContentWidget(headerHeight, contentWidget);
                        invokeAgain = false;
                    }
                    return invokeAgain;
                }
              }, 1000);
        }
    }

    private void scaleContentWidget(int headerHeight, Widget contentWidget, double scaleFactor) {
        int clientWidth = Window.getClientWidth();
        int contentWidth = contentWidget.getOffsetWidth();

        double diffX = clientWidth - contentWidth * scaleFactor;
        
        scaleContentWidget(headerHeight, contentWidget, scaleFactor, diffX);
    }
    
    private void scaleContentWidget(int headerHeight, Widget contentWidget) {
        int clientWidth = Window.getClientWidth();
        int clientHeight = Window.getClientHeight() - headerHeight;

        int contentWidth = contentWidget.getOffsetWidth();
        int contentHeight = contentWidget.getOffsetHeight();
        
        double scaleFactorX = clientWidth / (double) contentWidth;
        double scaleFactorY = clientHeight / (double) contentHeight;
        
        Double scaleFactor = scaleFactorX > scaleFactorY ? scaleFactorY : scaleFactorX;
        if(scaleFactor < 1.0) {
            scaleFactor = 1.0;
        }
        double diffX = clientWidth - contentWidth * scaleFactor;

        scaleContentWidget(headerHeight, contentWidget, scaleFactor, diffX);
    }

    private void scaleContentWidget(int headerHeight, Widget contentWidget, double scaleFactor, double diffX) {
        if(scaleFactor > 0.0) {
            CSS3Util.setProperty(contentWidget.getElement().getStyle(), "transform", "translateX(" + diffX / 2.0 + "px) scale(" + scaleFactor + ")");
            CSS3Util.setProperty(contentWidget.getElement().getStyle(), "transformOrigin", "0 0");
        }
    }

    @Override
    public void addLeaderboardUpdateListener(LeaderboardUpdateListener listener) {
        leaderboardPanel.addLeaderboardUpdateListener(listener);
    }

    @Override
    public void removeLeaderboardUpdateListener(LeaderboardUpdateListener listener) {
        leaderboardPanel.removeLeaderboardUpdateListener(listener);
    }
    
    private MultiRaceLeaderboardPanel createLeaderboardPanel(LeaderboardWithZoomingPerspectiveLifecycle lifecycle,
            PerspectiveCompositeSettings<LeaderboardWithZoomingPerspectiveSettings> settings,
            SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            CompetitorSelectionProvider competitorSelectionProvider, Timer timer, 
            String leaderboardName, final ErrorReporter errorReporter, final StringMessages stringMessages, Iterable<DetailType> availableDetailTypes) {
        MultiRaceLeaderboardPanelLifecycle leaderboardPanelLifecycle = getPerspectiveLifecycle().getLeaderboardPanelLifecycle();
        MultiRaceLeaderboardSettings leaderboardSettings = settings
                .findSettingsByComponentId(leaderboardPanelLifecycle.getComponentId());

        MultiRaceLeaderboardPanel leaderboardPanel = new MultiRaceLeaderboardPanel(this, getComponentContext(), sailingService,
                asyncActionsExecutor,
                leaderboardSettings, /*isEmbedded*/true,
                competitorSelectionProvider, timer, /* leaderboardGroupName */"",
                leaderboardName, errorReporter, stringMessages,
                /* showRaceDetails */false, /* competitorSearchTextBox */ null, /* showRegattaRank */
                /* showSelectionCheckbox */false, /* raceTimesInfoProvider */null, false, /* autoExpandLastRaceColumn */
                /* adjustTimerDelay */ true, /* autoApplyTopNFilter */ false, /* showCompetitorFilterStatus */ false, /* enableSyncScroller */ false,
                new ClassicLeaderboardStyle(), FlagImageResolverImpl.get(), availableDetailTypes);

        return leaderboardPanel;
    }

    @Override
    public SettingsDialogComponent<LeaderboardWithZoomingPerspectiveSettings> getPerspectiveOwnSettingsDialogComponent() {
        return new LeaderboardPerspectiveSettingsDialogComponent(getPerspectiveSettings(), stringMessages);
    }

    @Override
    public boolean hasPerspectiveOwnSettings() {
        return true;
    }

    @Override
    public String getId() {
        return LeaderboardWithZoomingPerspectiveLifecycle.ID;
    }

}
