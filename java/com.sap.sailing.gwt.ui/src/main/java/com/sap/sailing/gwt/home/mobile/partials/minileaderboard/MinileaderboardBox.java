package com.sap.sailing.gwt.home.mobile.partials.minileaderboard;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetMiniLeaderbordDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.MiniLeaderboardItemDTO;

public class MinileaderboardBox extends Composite implements RefreshableWidget<GetMiniLeaderbordDTO> {
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
    public void setData(final GetMiniLeaderbordDTO data, long nextUpdate, int updateNo) {
        setData(data.getItems());
        headerUi.setInfoText(StringMessages.INSTANCE.details());
        headerUi.setClickAction(new Command() {
            @Override
            public void execute() {
                Window.open(data.getLeaderboardDetailsURL(), "Leaderboard", "");
            }
        });
    }

    public void setData(final List<MiniLeaderboardItemDTO> data) {
        itemContainerUi.clearContent();
        
        if(data.isEmpty()) {
            itemContainerUi.addContent(getNoResultsInfoWidget());
            return;
        }
        
        for (MiniLeaderboardItemDTO item : data) {
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
}
