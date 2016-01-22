package com.sap.sailing.gwt.autoplay.client.place.player;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.autoplay.client.shared.header.SAPHeader;
import com.sap.sailing.gwt.autoplay.client.shared.header.SAPHeaderLifecycle.SAPHeaderConstructionParameters;
import com.sap.sailing.gwt.autoplay.client.shared.oldleaderboard.OldLeaderboard;
import com.sap.sailing.gwt.common.client.CSS3Util;
import com.sap.sailing.gwt.common.client.FullscreenUtil;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanelLifecycle.ConstructionParameters;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPerspectiveSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPerspectiveSettingsDialogComponent;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveComposite;

/**
 * A perspective managing a header with a single leaderboard filling the rest of the screen.
 * @author Frank
 *
 */
public class LeaderboardWithHeaderPerspective extends AbstractPerspectiveComposite<LeaderboardPerspectiveSettings> {
    private LeaderboardPerspectiveSettings settings;
    private final DockLayoutPanel dockPanel;
    private final static int SAP_HEADER_HEIGHT = 70;
    private final Widget currentContentWidget;
    
    public LeaderboardWithHeaderPerspective(LeaderboardPerspectiveSettings perspectiveSettings, 
            SAPHeaderConstructionParameters sapHeaderConstructionParameters,
            ConstructionParameters leaderboardParameters,
            StringMessages stringMessages) {
        super();
        this.settings = perspectiveSettings;

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                if(LeaderboardWithHeaderPerspective.this.settings.isLeaderboardAutoZoom()) {
                    autoZoomContentWidget(SAP_HEADER_HEIGHT, currentContentWidget);
                }
            }
        });

        SAPHeader sapHeader = sapHeaderConstructionParameters.createComponent();
        LeaderboardPanel leaderboardPanel = leaderboardParameters.createComponent();
        leaderboardPanel.getContentWidget().getElement().getStyle().setFontWeight(FontWeight.BOLD);

        components.add(sapHeader);
        components.add(leaderboardPanel);
        
        dockPanel = new DockLayoutPanel(Unit.PX);
        dockPanel.addNorth(sapHeader, SAP_HEADER_HEIGHT);
        
        OldLeaderboard oldLeaderboard = new OldLeaderboard(leaderboardPanel);
        leaderboardPanel.addLeaderboardUpdateListener(oldLeaderboard);
        
        currentContentWidget = oldLeaderboard.getContentWidget();
        
        if(settings.isLeaderboardAutoZoom()) {
            autoZoomContentWidget(SAP_HEADER_HEIGHT, currentContentWidget);
        } else {
            Double zoom = settings.getLeaderboardZoomFactor();
            zoomContentWidget(SAP_HEADER_HEIGHT, currentContentWidget, zoom);
        }

        dockPanel.add(oldLeaderboard);
        
        initWidget(dockPanel);
    }

    @Override
    public String getPerspectiveName() {
        return StringMessages.INSTANCE.leaderboard() + " Viewer";
    }

    @Override
    public String getLocalizedShortName() {
        return getPerspectiveName();
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
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<LeaderboardPerspectiveSettings> getSettingsDialogComponent() {
        return new LeaderboardPerspectiveSettingsDialogComponent(settings, StringMessages.INSTANCE);
    }

    @Override
    public LeaderboardPerspectiveSettings getSettings() {
        return settings;
    }

    @Override
    public void updateSettings(LeaderboardPerspectiveSettings newSettings) {
        this.settings = newSettings;
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
}
