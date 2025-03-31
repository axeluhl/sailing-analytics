package com.sap.sailing.gwt.home.mobile.partials.statisticsBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.home.shared.partials.statistics.StatisticsBoxView;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class MobileStatisticsBoxView extends Composite implements StatisticsBoxView {
    private static StatisticsBoxUiBinder uiBinder = GWT.create(StatisticsBoxUiBinder.class);
    
    interface StatisticsBoxUiBinder extends UiBinder<MobileSection, MobileStatisticsBoxView> {
    }

    @UiField MobileSection itemContainerUi;
    @UiField SectionHeaderContent headerUi;
    
    public MobileStatisticsBoxView() {
        this(StringMessages.INSTANCE.statistics());
    }
    
    public MobileStatisticsBoxView(String title) {
        StatisticsBoxResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        headerUi.setSectionTitle(title);
    }

    @Override
    public void clear() {
        itemContainerUi.clearContent();
    }
    
    public void addItem(String iconUrl, String name, Object payload) {
        itemContainerUi.addContent(new StatisticsBoxItem(iconUrl, name, payload));
    }
}
