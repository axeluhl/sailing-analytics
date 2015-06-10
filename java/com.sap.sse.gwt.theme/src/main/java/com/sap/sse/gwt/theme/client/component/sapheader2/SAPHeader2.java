package com.sap.sse.gwt.theme.client.component.sapheader2;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class SAPHeader2 extends Composite {
    private static SAPHeaderUiBinder uiBinder = GWT.create(SAPHeaderUiBinder.class);

    interface SAPHeaderUiBinder extends UiBinder<Widget, SAPHeader2> {
    }

    @UiField
    DivElement applicationNameUi;
    @UiField
    SimplePanel pageTitleUi;
    
    @UiField
    SimplePanel rightSideUi;

    public SAPHeader2(String applicationName, Widget pageTitle, boolean startInAutoScreenMode) {
        SAPHeaderResources.INSTANCE.css().ensureInjected();

        initWidget(uiBinder.createAndBindUi(this));

        applicationNameUi.setInnerText(applicationName != null ? applicationName : "&nbsp;");
        pageTitleUi.setWidget(pageTitle);


    }

    public void addWidgetToRightSide(Widget widget) {
        rightSideUi.add(widget);
    }
}
