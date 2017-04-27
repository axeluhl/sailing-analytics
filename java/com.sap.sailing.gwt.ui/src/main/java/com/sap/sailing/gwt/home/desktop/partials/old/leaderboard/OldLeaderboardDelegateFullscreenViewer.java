package com.sap.sailing.gwt.home.desktop.partials.old.leaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.desktop.partials.old.AbstractLeaderboardFullscreenViewer;
import com.sap.sailing.gwt.home.desktop.partials.old.leaderboard.OldLeaderboard.OldLeaderboardDelegate;

public class OldLeaderboardDelegateFullscreenViewer extends AbstractLeaderboardFullscreenViewer
        implements OldLeaderboardDelegate {

    interface OldLeaderboardDelegateFullscreenViewerUiBinder
            extends UiBinder<Widget, OldLeaderboardDelegateFullscreenViewer.ContentLayout> {
    }

    private static OldLeaderboardDelegateFullscreenViewerUiBinder uiBinder = GWT
            .create(OldLeaderboardDelegateFullscreenViewerUiBinder.class);

    private final Image showLiveRacesControl = new Image("images/home/launch-loupe.svg");
    private final Label hasLiveRace = new Label();
    private final Widget headerWidget;
    private final ContentLayout contentLayout = new ContentLayout();
    private final Widget content = uiBinder.createAndBindUi(this.contentLayout);

    class ContentLayout {
        @UiField SimplePanel liveRacesContainerUi, leaderboardContainerUi;
        @UiField FlowPanel subHeaderUi, subHeaderContentUi, subHeaderToolbarUi;
    }

    public OldLeaderboardDelegateFullscreenViewer() {
        setHeaderWidget(headerWidget = createPanel(lastScoringComment, hasLiveRace, scoringScheme));
        addToolbarAction(showLiveRacesControl);
    }

    @Override
    protected void onShow() {
        super.onShow();
        getContentWidget().getElement().getFirstChildElement().getStyle().clearMarginTop();
    }

    public void setLiveRacesPanel(Widget liveRaces) {
        contentLayout.liveRacesContainerUi.setWidget(liveRaces);
    }
    
    public Image getShowLiveRacesControl() {
        return showLiveRacesControl;
    }
    
    public void setShowLiveRaces(boolean show) {
        if (show) {
            contentLayout.subHeaderContentUi.add(headerWidget);
            contentLayout.subHeaderToolbarUi.add(busyIndicator);
            contentLayout.subHeaderToolbarUi.add(lastScoringUpdatePanel);
            contentLayout.subHeaderToolbarUi.add(autoRefreshControl);
            contentLayout.subHeaderToolbarUi.add(settingsControl);
        } else {
            setHeaderWidget(headerWidget);
            addToolbarBusyIndicator(busyIndicator);
            addToolbarInfo(lastScoringUpdatePanel);
            addToolbarAction(autoRefreshControl);
            addToolbarAction(settingsControl);
            addToolbarAction(showLiveRacesControl);
        }
        contentLayout.liveRacesContainerUi.setVisible(show);
        contentLayout.subHeaderUi.setVisible(show);
    }

    @Override
    public void setLeaderboardPanel(Widget leaderboardPanel) {
        contentLayout.leaderboardContainerUi.setWidget(leaderboardPanel);
        showContent(content);
    }

    @Override
    public Element getHasLiveRaceElement() {
        return hasLiveRace.getElement();
    }

}