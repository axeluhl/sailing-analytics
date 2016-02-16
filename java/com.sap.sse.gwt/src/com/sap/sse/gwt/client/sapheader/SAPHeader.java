package com.sap.sse.gwt.client.sapheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class SAPHeader extends Composite {
    private static SAPHeaderUiBinder uiBinder = GWT.create(SAPHeaderUiBinder.class);

    interface SAPHeaderUiBinder extends UiBinder<Widget, SAPHeader> {
    }

    @UiField
    DivElement applicationNameUi;
    @UiField
    DivElement pageTitleUi;
    @UiField
    DivElement titleUi;
    @UiField
    DivElement subTitleUi;
    @UiField
    SimplePanel rightSideUi;

    public SAPHeader(String applicationName) {
        SAPHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        applicationNameUi.setInnerText(applicationName != null ? applicationName : "&nbsp;");
    }

    public void setHeaderTitle(String title) {
        titleUi.setInnerText(title);
    }

    public void setHeaderSubTitle(String subtitle) {
        if (subtitle == null || subtitle.isEmpty()) {
            subTitleUi.getStyle().setDisplay(Display.NONE);
            titleUi.getStyle().setMarginTop(14, Unit.PX);
        } else {
            subTitleUi.setInnerText(subtitle);
            subTitleUi.getStyle().clearDisplay();
            titleUi.getStyle().clearMarginTop();
        }
    }

    public void addWidgetToRightSide(Widget widget) {
        rightSideUi.add(widget);
    }
}
