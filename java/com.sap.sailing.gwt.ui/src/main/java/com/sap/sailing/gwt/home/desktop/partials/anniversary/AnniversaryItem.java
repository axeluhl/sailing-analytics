package com.sap.sailing.gwt.home.desktop.partials.anniversary;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.partials.anniversary.AnniversariesView.AnniversaryAnnouncement;
import com.sap.sailing.gwt.ui.client.StringMessages;

class AnniversaryItem extends Widget implements AnniversaryAnnouncement {

    interface AnniversaryItemUiBinder extends UiBinder<Element, AnniversaryItem> {
    }

    private static AnniversaryItemUiBinder uiBinder = GWT.create(AnniversaryItemUiBinder.class);

    @UiField
    StringMessages i18n;
    @UiField
    Style style;
    @UiField
    DivElement iconUi, countUi, unitUi, teaserUi, descriptionUi;
    @UiField
    AnchorElement linkUi;

    AnniversaryItem(boolean isAnnouncement) {
        setElement(uiBinder.createAndBindUi(this));
        if (isAnnouncement) {
            this.addStyleName(style.announcement());
        } else {
            this.iconUi.removeFromParent();
            this.linkUi.removeFromParent();
        }
    }

    @Override
    public void setIconUrl(String iconUrl) {
        this.iconUi.getStyle().setBackgroundImage("url('" + iconUrl + "')");
    }

    @Override
    public void setCount(String count) {
        this.countUi.setInnerText(count);
    }

    @Override
    public void setUnit(String unit) {
        this.unitUi.setInnerText(unit);
    }

    @Override
    public void setTeaser(String teaser) {
        this.teaserUi.setInnerText(teaser);
    }

    @Override
    public void setDescription(String desciption) {
        this.descriptionUi.setInnerText(desciption);
    }

    @Override
    public void setLinkUrl(String linkUrl) {
        this.linkUi.setHref(linkUrl);
    }

    interface Style extends CssResource {

        String announcement();
    }

}
