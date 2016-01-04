package com.sap.sailing.gwt.home.mobile.partials.statisticsBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.shared.partials.statistics.AbstractStatisticsBox;

public class StatisticsBox extends AbstractStatisticsBox {
    private static StatisticsBoxUiBinder uiBinder = GWT.create(StatisticsBoxUiBinder.class);
    
    interface StatisticsBoxUiBinder extends UiBinder<Widget, StatisticsBox> {
    }

    @UiField MobileSection itemContainerUi;
    
    public StatisticsBox() {
        this(true);
    }

    public StatisticsBox(boolean showRegattaInformation) {
        super(showRegattaInformation);
        StatisticsBoxResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    protected void clear() {
        itemContainerUi.clearContent();
    }
    
    public void addItem(String iconUrl, String name, Object payload) {
        itemContainerUi.addContent(new StatisticsBoxItem(iconUrl, name, payload));
    }
}
