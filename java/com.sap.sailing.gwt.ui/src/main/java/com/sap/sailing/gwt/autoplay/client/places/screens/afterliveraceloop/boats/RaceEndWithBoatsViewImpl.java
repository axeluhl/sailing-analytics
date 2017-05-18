package com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.boats;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayMainViewImpl;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard.PreLeaderBoardWithImageViewImpl.ImageProvider;
import com.sap.sailing.gwt.autoplay.client.shared.SixtyInchLeaderBoard;
import com.sap.sailing.gwt.autoplay.client.utils.LeaderBoardScaleHelper;
import com.sap.sse.gwt.client.panels.ResizableFlowPanel;

public class RaceEndWithBoatsViewImpl extends ResizeComposite implements RaceEndWithBoatsView {
    private static RaceEndWithBoatsViewImplUiBinder uiBinder = GWT.create(RaceEndWithBoatsViewImplUiBinder.class);

    interface RaceEndWithBoatsViewImplUiBinder extends UiBinder<Widget, RaceEndWithBoatsViewImpl> {
    }

    @UiField
    ResizableFlowPanel leaderBoardHolder;

    @UiField
    ResizableFlowPanel infoHolder;

    @UiField
    FlowPanel image1;
    @UiField
    FlowPanel image2;
    @UiField
    FlowPanel image3;
    @UiField
    Label subline1;
    @UiField
    Label subline2;
    @UiField
    Label subline3;
    @UiField
    ResizableFlowPanel statistics;

    private Timer resizer;

    private ImageProvider provider;

    public RaceEndWithBoatsViewImpl(ImageProvider provider) {
        initWidget(uiBinder.createAndBindUi(this));
        this.provider = provider;
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
                LeaderBoardScaleHelper.scaleContentWidget(AutoPlayMainViewImpl.SAP_HEADER_IN_PX, leaderboardPanel);
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
        setImage(image1, provider.getImageUrl(c), true);
    }

    @Override
    public void setSecond(CompetitorDTO c) {
        subline2.setText("2. " + c.getName());
        setImage(image2, provider.getImageUrl(c), false);
    }

    @Override
    public void setThird(CompetitorDTO c) {
        subline3.setText("3. " + c.getName());
        setImage(image3, provider.getImageUrl(c), false);
    }

    private void setImage(FlowPanel image, String imageUrl, boolean slightlyLarger) {
        image.getElement().getStyle().setBackgroundImage("url(" + imageUrl + ")");
        image.getElement().getStyle().setHeight(90, Unit.PCT);
        image.getElement().getStyle().setWidth(slightlyLarger ? 100 : 90, Unit.PCT);
        image.getElement().getStyle().setProperty("backgroundPosition", "center bottom");
        image.getElement().getStyle().setProperty("backgroundSize", "contain");
        image.getElement().getStyle().setProperty("backgroundRepeat", "no-repeat");
    }

}
