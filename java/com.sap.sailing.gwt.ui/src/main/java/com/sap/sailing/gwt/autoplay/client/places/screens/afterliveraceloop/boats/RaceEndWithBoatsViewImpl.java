package com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.boats;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard.PreLiveRaceLeaderboardWithImageViewImpl.ImageProvider;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.SingleRaceLeaderboardPanel;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
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
    Label statisticValue1;
    @UiField
    Label statisticProperty1;
    @UiField
    Label statisticValue2;
    @UiField
    Label statisticProperty2;
    @UiField
    Label statisticValue3;
    @UiField
    Label statisticProperty3;

    private NumberFormat compactFormat = NumberFormat.getFormat("#.0");

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
    public void setLeaderBoard(SingleRaceLeaderboardPanel leaderboardPanel) {
        leaderBoardHolder.add(leaderboardPanel);
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
        image.getElement().getStyle().setWidth(slightlyLarger ? 100 : 90, Unit.PCT);
        image.getElement().getStyle().setProperty("height", "90%");
        image.getElement().getStyle().setProperty("margin", "auto");
        image.getElement().getStyle().setProperty("backgroundPosition", "center bottom");
        image.getElement().getStyle().setProperty("backgroundSize", "contain");
        image.getElement().getStyle().setProperty("backgroundRepeat", "no-repeat");
    }

    @Override
    public void setStatistic(int competitorCount, Distance distance, Duration duration) {
        statisticProperty1.setText(StringMessages.INSTANCE.competitors());
        statisticValue1.setText(String.valueOf(competitorCount));

        statisticProperty2.setText(StringMessages.INSTANCE.distance());
        if(distance == null){
            statisticValue2
            .setText(StringMessages.INSTANCE.noDataFound());
        }else{
            statisticValue2
            .setText(compactFormat.format(distance.getSeaMiles()) + " " + StringMessages.INSTANCE.seaMiles());
        }

        statisticProperty3.setText(StringMessages.INSTANCE.durationPlain());
        statisticValue3.setText(compactFormat.format(duration.asMinutes()) + " " + StringMessages.INSTANCE.minutes());
    }

}
