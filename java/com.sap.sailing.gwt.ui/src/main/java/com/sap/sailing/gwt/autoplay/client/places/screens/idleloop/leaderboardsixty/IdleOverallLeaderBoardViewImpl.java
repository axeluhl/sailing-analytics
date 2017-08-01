package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboardsixty;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayMainViewImpl;
import com.sap.sailing.gwt.autoplay.client.utils.LeaderBoardScaleHelper;
import com.sap.sailing.gwt.ui.leaderboard.MultiRaceLeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.SortedCellTable;
import com.sap.sse.gwt.client.panels.ResizableFlowPanel;

public class IdleOverallLeaderBoardViewImpl extends ResizeComposite implements IdleOverallLeaderBoardView {
    protected static final int TOOLBAR_SIZE = 80;

    private static LifeRaceWithRacemapViewImplUiBinder uiBinder = GWT.create(LifeRaceWithRacemapViewImplUiBinder.class);

    @UiField
    ResizableFlowPanel leaderBoardHolder;

    private Timer resizer;

    interface LifeRaceWithRacemapViewImplUiBinder extends UiBinder<Widget, IdleOverallLeaderBoardViewImpl> {
    }

    public IdleOverallLeaderBoardViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void onStop() {
        resizer.cancel();
    }

    @Override
    public void startingWith(Slide7Presenter p, AcceptsOneWidget panel, MultiRaceLeaderboardPanel leaderboardPanel) {
        panel.setWidget(this);
        leaderBoardHolder.add(leaderboardPanel);
        resizer = new Timer() {

            @Override
            public void run() {
                SortedCellTable<LeaderboardRowDTO> tbl = leaderboardPanel.getLeaderboardTable();
                LeaderBoardScaleHelper.scaleContentWidget(AutoPlayMainViewImpl.SAP_HEADER_IN_PX + TOOLBAR_SIZE, tbl);
            }
        };
        resizer.scheduleRepeating(100);
    }
}
