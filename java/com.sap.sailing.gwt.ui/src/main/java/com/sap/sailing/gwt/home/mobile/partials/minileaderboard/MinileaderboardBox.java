package com.sap.sailing.gwt.home.mobile.partials.minileaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.MiniLeaderboardItemDTO;

public class MinileaderboardBox extends Composite implements RefreshableWidget<GetMiniLeaderboardDTO> {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, MinileaderboardBox> {
    }

    @UiField
    MobileSection itemContainerUi;
    @UiField
    SectionHeaderContent headerUi;

    public MinileaderboardBox() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setData(final GetMiniLeaderboardDTO data, long nextUpdate, int updateNo) {
        setData(data);
        headerUi.setInfoText(StringMessages.INSTANCE.details());
        headerUi.setClickAction(new Command() {
            @Override
            public void execute() {
                Window.open(data.getLeaderboardDetailsURL(), "Leaderboard", "");
            }
        });
    }

    public void setData(final GetMiniLeaderboardDTO data) {
        itemContainerUi.clearContent();
        
        if(data.getItems().isEmpty()) {
            itemContainerUi.addContent(getNoResultsInfoWidget());
            return;
        }
        
        if(data.getScoreCorrectionText() != null || data.getLastScoreUpdate() != null) {
            itemContainerUi.addContent(getScoreInformation(data));
        }
        
        for (MiniLeaderboardItemDTO item : data.getItems()) {
            itemContainerUi.addContent(new MinileaderboardBoxItem(item));
        }
    }
    
    private Widget getNoResultsInfoWidget() {
        Label label = new Label(StringMessages.INSTANCE.noResults());
        label.getElement().getStyle().setPadding(1, Unit.EM);
        label.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        label.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
        return label;
    }
    
    private Widget getScoreInformation(GetMiniLeaderboardDTO data) {
        FlowPanel scoreInformation = new FlowPanel();
        if (data.getScoreCorrectionText() != null) {
            scoreInformation.add(new Label(data.getScoreCorrectionText()));
        }
        if (data.getLastScoreUpdate() != null) {
            String lastUpdate = DateAndTimeFormatterUtil.longDateFormatter.render(data.getLastScoreUpdate()) + ", "
                    + DateAndTimeFormatterUtil.longTimeFormatter.render(data.getLastScoreUpdate());
            scoreInformation.add(new Label(StringMessages.INSTANCE.lastScoreUpdate() + ": " + lastUpdate));
        }
        scoreInformation.getElement().getStyle().setBackgroundColor("#f2f2f2");
        scoreInformation.getElement().getStyle().setProperty("borderTop", "1px solid #ccc");
        scoreInformation.getElement().getStyle().setFontSize(0.866666666666667, Unit.EM);
        scoreInformation.getElement().getStyle().setPadding(1, Unit.EM);
        if (data.isLive()) {
            scoreInformation.getElement().getStyle().setColor("#ff0000");
        }
        return scoreInformation;
    }
}
