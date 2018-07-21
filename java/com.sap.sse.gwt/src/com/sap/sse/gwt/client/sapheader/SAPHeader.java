package com.sap.sse.gwt.client.sapheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Generic header widget that is designed with a SAP logo and SAP color scheme.
 * 
 * The header has the following elements:
 * <ul>
 *   <li>SAP Logo with an application title (like "Sailing Analytics")</li>
 *   <li>Page title</li>
 *   <li>Optional Subtitle</li>
 *   <li>Optional widget to show on the right side of the header</li>
 * </ul>
 */
public class SAPHeader extends Composite {
    
    private static final String LOGO_URL = "https://www.sap.com/sponsorships";
    
    private static SAPHeaderUiBinder uiBinder = GWT.create(SAPHeaderUiBinder.class);

    interface SAPHeaderUiBinder extends UiBinder<Widget, SAPHeader> {
    }

    @UiField
    AnchorElement applicationNameAnchor;
    @UiField
    DivElement pageTitleUi;
    @UiField
    DivElement titleUi;
    @UiField
    DivElement subTitleUi;
    @UiField
    SimplePanel rightSideUi;
    @UiField
    AnchorElement logoAnchor;

    public SAPHeader(String applicationName, String applicationBaseUrl) {
        SAPHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        applicationNameAnchor.setInnerText(applicationName != null ? applicationName : "&nbsp;");
        logoAnchor.setHref(LOGO_URL);
        String sapSailingUrl = applicationBaseUrl + "?locale=" + LocaleInfo.getCurrentLocale().getLocaleName();
        applicationNameAnchor.setHref(sapSailingUrl);
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
