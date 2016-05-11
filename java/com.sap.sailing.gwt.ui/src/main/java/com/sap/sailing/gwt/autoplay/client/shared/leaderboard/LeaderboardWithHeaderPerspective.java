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
import com.sap.sailing.gwt.autoplay.client.shared.header.SAPHeaderComponent;
import com.sap.sailing.gwt.autoplay.client.shared.header.SAPHeaderComponentLifecycle;
import com.sap.sailing.gwt.autoplay.client.shared.header.SAPHeaderComponentSettings;
import com.sap.sailing.gwt.autoplay.client.shared.oldleaderboard.OldLeaderboard;
import com.sap.sailing.gwt.common.client.CSS3Util;
import com.sap.sailing.gwt.common.client.FullscreenUtil;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveComposite;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleWithAllSettings;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

/**
 * A perspective managing a header with a single leaderboard filling the rest of the screen.
 * 
 * @author Frank Mittag
 *
 */
public class LeaderboardWithHeaderPerspective extends AbstractPerspectiveComposite<LeaderboardWithHeaderPerspectiveLifecycle,
    LeaderboardWithHeaderPerspectiveSettings> implements LeaderboardUpdateProvider {
    private final DockLayoutPanel dockPanel;
    private final static int SAP_HEADER_HEIGHT = 70;
    private final Widget currentContentWidget;
    private final LeaderboardPanel leaderboardPanel;
    private final PerspectiveLifecycleWithAllSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> componentLifecyclesAndSettings;
    
    public LeaderboardWithHeaderPerspective(PerspectiveLifecycleWithAllSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> perspectiveLifecycleWithAllSettings,
            SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            CompetitorSelectionProvider competitorSelectionProvider, Timer timer,
            String leaderboardName, final ErrorReporter errorReporter, final StringMessages stringMessages,
            UserAgentDetails userAgent, boolean startInFullScreenMode) {
        super(perspectiveLifecycleWithAllSettings.getPerspectiveLifecycle(), perspectiveLifecycleWithAllSettings.getPerspectiveSettings());

        this.componentLifecyclesAndSettings = perspectiveLifecycleWithAllSettings;
        
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                if(LeaderboardWithHeaderPerspective.this.getPerspectiveSettings().isLeaderboardAutoZoom()) {
                    autoZoomContentWidget(SAP_HEADER_HEIGHT, currentContentWidget);
                }
            }
        });

        SAPHeaderComponentLifecycle sapHeaderLifecycle = getPerspectiveLifecycle().getSapHeaderLifecycle();
        SAPHeaderComponent sapHeader = createSAPHeader(sapHeaderLifecycle, 
                perspectiveLifecycleWithAllSettings.getComponentSettings().getSettingsOfComponentLifecycle(sapHeaderLifecycle),
                stringMessages, startInFullScreenMode);
        leaderboardPanel = createLeaderboardPanel(sailingService, asyncActionsExecutor,
                competitorSelectionProvider, timer, leaderboardName, errorReporter, stringMessages, userAgent);
        
        leaderboardPanel.getContentWidget().getElement().getStyle().setFontWeight(FontWeight.BOLD);

        components.add(sapHeader);
        components.add(leaderboardPanel);
        
        dockPanel = new DockLayoutPanel(Unit.PX);
        dockPanel.addNorth(sapHeader, SAP_HEADER_HEIGHT);
        
        OldLeaderboard oldLeaderboard = new OldLeaderboard(leaderboardPanel);
        leaderboardPanel.addLeaderboardUpdateListener(oldLeaderboard);
        
        currentContentWidget = oldLeaderboard.getContentWidget();
        
        if(getPerspectiveSettings().isLeaderboardAutoZoom()) {
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
    
    private SAPHeaderComponent createSAPHeader(SAPHeaderComponentLifecycle componentLifecycle, SAPHeaderComponentSettings settings, 
            final StringMessages stringMessages, boolean withFullscreenButton) {
        return new SAPHeaderComponent(componentLifecycle, settings, stringMessages, withFullscreenButton);
    }

    private LeaderboardPanel createLeaderboardPanel(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            CompetitorSelectionProvider competitorSelectionProvider, Timer timer, 
            String leaderboardName, final ErrorReporter errorReporter, final StringMessages stringMessages,
            final UserAgentDetails userAgent) {
        LeaderboardPanelLifecycle leaderboardPanelLifecycle = getPerspectiveLifecycle().getLeaderboardPanelLifecycle();
        LeaderboardSettings leaderboardSettings = componentLifecyclesAndSettings.getComponentSettings().getSettingsOfComponentLifecycle(leaderboardPanelLifecycle);

        LeaderboardPanel leaderboardPanel = new LeaderboardPanel(sailingService, asyncActionsExecutor,
                leaderboardSettings, /*isEmbedded*/true, /* preSelectedRace */null,
                competitorSelectionProvider, timer, /* leaderboardGroupName */"",
                leaderboardName, errorReporter, stringMessages,
                userAgent, /*showRaceDetails */false, /* competitorSearchTextBox */ null, /* showRegattaRank */
                /* showSelectionCheckbox */false, /* raceTimesInfoProvider */null, false, /* autoExpandLastRaceColumn */
                /* adjustTimerDelay */true, /*autoApplyTopNFilter*/ false, false);

        return leaderboardPanel;
    }

}
