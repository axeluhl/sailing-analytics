package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide0;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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

public class PreLeaderBoardWithImageViewImpl extends ResizeComposite implements PreLeaderboardWithImageView {
    private static IdleLeaderBoardWithFlagsViewImplUiBinder uiBinder = GWT.create(IdleLeaderBoardWithFlagsViewImplUiBinder.class);

    interface IdleLeaderBoardWithFlagsViewImplUiBinder extends UiBinder<Widget, PreLeaderBoardWithImageViewImpl> {
    }

    @UiField
    ResizableFlowPanel leaderBoardHolder;

    @UiField
    ResizableFlowPanel infoHolder;

    @UiField
    Image image;
    @UiField
    Label subline1;
    @UiField
    Label subline2;
    @UiField
    Label subline3;

    private Timer resizer;
    private ImageProvider provider;

    public interface ImageProvider {

        String getImageUrl(CompetitorDTO marked);

    }

    public PreLeaderBoardWithImageViewImpl(ImageProvider provider) {
        initWidget(uiBinder.createAndBindUi(this));
        this.provider = provider;
    }

    @Override
    public void startingWith(Slide1Presenter p, AcceptsOneWidget panel) {
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
    public void onCompetitorSelect(CompetitorDTO marked) {
        image.setUrl(provider.getImageUrl(marked));
        if (marked.getBoat() != null && marked.getBoat().getName() != null) {
            subline1.setText(marked.getBoat().getName());
        } else {
            subline1.setText("");
        }

        if (marked.getName() != null) {
            subline2.setText(marked.getName());
        } else {
            subline2.setText("");
        }

        subline3.setText("");
    }

    @Override
    public void scrollLeaderBoardToTop() {
        leaderBoardHolder.getElement().setScrollTop(0);
    }

    @Override
    public void onStop() {
        resizer.cancel();
    }

}
