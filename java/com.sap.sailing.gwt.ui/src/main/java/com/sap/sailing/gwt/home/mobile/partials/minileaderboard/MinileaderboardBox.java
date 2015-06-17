package com.sap.sailing.gwt.home.mobile.partials.minileaderboard;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.ui.shared.dispatch.ListResult;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetMobileLeaderbordAction.SimplifiedLeaderboardItemDTO;

public class MinileaderboardBox extends Composite implements RefreshableWidget<ListResult<SimplifiedLeaderboardItemDTO>> {
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
    public void setData(final ListResult<SimplifiedLeaderboardItemDTO> data, long nextUpdate, int updateNo) {
        setData(data.getValues());
    }

    public void setData(final List<SimplifiedLeaderboardItemDTO> data) {

        itemContainerUi.clearContent();
        for (SimplifiedLeaderboardItemDTO item : data) {
            itemContainerUi.addContent(new MinileaderboardBoxItem(item));
        }
    }
}
