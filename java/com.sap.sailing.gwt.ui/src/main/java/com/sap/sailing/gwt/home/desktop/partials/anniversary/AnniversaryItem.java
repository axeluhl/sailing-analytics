package com.sap.sailing.gwt.home.desktop.partials.anniversary;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;

class AnniversaryItem extends Widget {

    private static UpcomingAnniversaryUiBinder uiBinder = GWT.create(UpcomingAnniversaryUiBinder.class);

    interface UpcomingAnniversaryUiBinder extends UiBinder<Element, AnniversaryItem> {
    }

    private static NumberFormat numberFormat = NumberFormat.getFormat("#,###");

    @UiField
    StringMessages i18n;
    @UiField
    Style style;
    @UiField
    DivElement iconUi, countUi, unitUi, teaserUi, descriptionUi;
    @UiField
    AnchorElement linkUi;

    private AnniversaryItem(String teaser, String description) {
        setElement(uiBinder.createAndBindUi(this));
        this.teaserUi.setInnerText(teaser);
        this.descriptionUi.setInnerHTML(description);
    }

    AnniversaryItem(String iconUrl, int target, String teaser, String description, String linkUrl) {
        this(teaser, description);
        this.iconUi.getStyle().setBackgroundImage("url('" + iconUrl + "')");
        this.countUi.setInnerText(numberFormat.format(target));
        this.unitUi.setInnerText(i18n.anniversaryUnitTextRaces());
        this.linkUi.setHref(linkUrl);
        this.addStyleName(style.announcement());
    }

    AnniversaryItem(int countdown, String teaser, String description) {
        this(teaser, description);
        this.iconUi.removeFromParent();
        this.countUi.setInnerText(numberFormat.format(countdown));
        this.unitUi.setInnerText(countdown == 1 ? i18n.anniversaryUnitTextRace() : i18n.anniversaryUnitTextRaces());
        this.linkUi.removeFromParent();
    }

    interface Style extends CssResource {

        String announcement();
    }

}
