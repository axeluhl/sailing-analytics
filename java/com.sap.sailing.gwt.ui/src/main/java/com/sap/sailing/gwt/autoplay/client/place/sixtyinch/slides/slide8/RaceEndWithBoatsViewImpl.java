package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide8;

import javax.annotation.Resource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayMainViewSixtyInchImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SixtyInchLeaderBoard;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.LeaderBoardScaleHelper;
import com.sap.sse.gwt.client.panels.ResizableFlowPanel;

public class RaceEndWithBoatsViewImpl extends ResizeComposite implements RaceEndWithBoatsView {
    private static RaceEndWithBoatsViewImplUiBinder uiBinder = GWT.create(RaceEndWithBoatsViewImplUiBinder.class);

    interface RaceEndWithBoatsViewImplUiBinder extends UiBinder<Widget, RaceEndWithBoatsViewImpl> {
    }

    public interface RaceEndRessources extends CellTable.Resources {
        @Resource
        public ImageResource noTeamImagePlaceHolder();
    }

    private static final RaceEndRessources res = GWT.create(RaceEndRessources.class);

    @UiField
    ResizableFlowPanel leaderBoardHolder;

    @UiField
    ResizableFlowPanel infoHolder;

    @UiField
    Image image1;
    @UiField
    Image image2;
    @UiField
    Image image3;
    @UiField
    Label subline1;
    @UiField
    Label subline2;
    @UiField
    Label subline3;
    @UiField
    Label bottomText;

    private Timer resizer;

    public RaceEndWithBoatsViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void startingWith(NextRaceWithBoatsPresenter p, AcceptsOneWidget panel) {
        panel.setWidget(this);
    }

    @Override
    public void setLeaderBoard(SixtyInchLeaderBoard leaderboardPanel) {
        leaderBoardHolder.add(leaderboardPanel);
        resizer = new Timer() {

            @Override
            public void run() {
                LeaderBoardScaleHelper.scaleContentWidget(AutoPlayMainViewSixtyInchImpl.SAP_HEADER_IN_PX,
                        leaderboardPanel);
            }
        };
        resizer.scheduleRepeating(100);
    }

    @Override
    public void onStop() {
        resizer.cancel();
    }

    @Override
    public void setFirst(CompetitorDTO c) {
        subline1.setText("1. " + c.getName());
        if (c.getImageURL() != null) {
            image1.setUrl(c.getImageURL());
        } else {
            image1.setUrl(res.noTeamImagePlaceHolder().getSafeUri());
        }
    }

    @Override
    public void setSecond(CompetitorDTO c) {
        subline2.setText("2. " + c.getName());
        if (c.getImageURL() != null) {
            image2.setUrl(c.getImageURL());
        } else {
            image2.setUrl(res.noTeamImagePlaceHolder().getSafeUri());
        }
    }

    @Override
    public void setThird(CompetitorDTO c) {
        subline3.setText("3. " + c.getName());
        if (c.getImageURL() != null) {
            image3.setUrl(c.getImageURL());
        } else {
            image3.setUrl(res.noTeamImagePlaceHolder().getSafeUri());
        }
    }

}
