package com.sap.sailing.gwt.home.desktop.partials.statistics;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.partials.statistics.AbstractStatisticsBox;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventStatisticsDTO;

public class StatisticsBox extends AbstractStatisticsBox {

    private static StatisticsBoxUiBinder uiBinder = GWT.create(StatisticsBoxUiBinder.class);

    interface StatisticsBoxUiBinder extends UiBinder<Widget, StatisticsBox> {
    }
    
    @UiField UListElement itemContainerUi;

    public StatisticsBox() {
        this(true);
    }
    
    public StatisticsBox(boolean showRegattaInformation) {
        super(showRegattaInformation);
        StatisticsBoxResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        setVisible(false);
    }
    
    @Override
    public void setData(EventStatisticsDTO statistics) {
        super.setData(statistics);
        setVisible(true);
    }

    @Override
    protected void clear() {
        itemContainerUi.removeAllChildren();
    }
    
    @Override
    protected void addItem(String iconUrl, String name, Object payload) {
        itemContainerUi.appendChild(new StatisticsBoxItem(iconUrl, name, payload).getElement());
    }

}
