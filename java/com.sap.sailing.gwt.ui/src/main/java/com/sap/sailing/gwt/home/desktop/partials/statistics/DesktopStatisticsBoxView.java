package com.sap.sailing.gwt.home.desktop.partials.statistics;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.partials.statistics.StatisticsBoxView;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class DesktopStatisticsBoxView extends Composite implements StatisticsBoxView {

    private static StatisticsBoxUiBinder uiBinder = GWT.create(StatisticsBoxUiBinder.class);

    interface StatisticsBoxUiBinder extends UiBinder<Widget, DesktopStatisticsBoxView> {
    }
    
    @UiField UListElement itemContainerUi;
    @UiField DivElement titleUi;
    
    public DesktopStatisticsBoxView() {
        this(false, StringMessages.INSTANCE.statistics());
    }
    
    public DesktopStatisticsBoxView(boolean embedded, String title) {
        StatisticsBoxResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        titleUi.setInnerText(title);
        if (embedded) {
            addStyleName(StatisticsBoxResources.INSTANCE.css().box_embedded());
        }
        setVisible(false);
    }

    @Override
    public void clear() {
        setVisible(true);
        itemContainerUi.removeAllChildren();
    }
    
    @Override
    public void addItem(String iconUrl, String name, Object payload) {
        itemContainerUi.appendChild(new StatisticsBoxItem(iconUrl, name, payload).getElement());
    }

}
