package com.sap.sailing.gwt.home.shared.partials.anniversary;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.partials.anniversary.AnniversariesView.AnniversaryAnnouncement;

public class AbstractAnniversaryItem extends Widget implements AnniversaryAnnouncement {

    @UiField
    public Style style;
    @UiField
    public DivElement iconUi, countUi, unitUi, teaserUi, descriptionUi;
    @UiField
    public AnchorElement linkUi;

    protected void initLayout(boolean isAnnouncement) {
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

    public interface Style extends CssResource {

        String announcement();
    }

}
