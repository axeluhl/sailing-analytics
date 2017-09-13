package com.sap.sailing.gwt.home.shared.partials.anniversary;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.partials.anniversary.AnniversariesView.AnniversaryAnnouncement;
import com.sap.sailing.gwt.home.shared.partials.bubble.Bubble;
import com.sap.sailing.gwt.home.shared.partials.bubble.Bubble.DefaultPresenter;
import com.sap.sailing.gwt.home.shared.partials.bubble.Bubble.Direction;

public class AbstractAnniversaryItem extends Widget implements AnniversaryAnnouncement {

    @UiField
    public Style style;
    @UiField
    public Element iconUi, countUi, unitUi, teaserUi, descriptionUi, disclaimerUi;
    @UiField
    public AnchorElement linkUi;

    protected void initLayout(boolean isAnnouncement) {
        if (isAnnouncement) {
            this.addStyleName(style.announcement());
            this.disclaimerUi.removeFromParent();
        } else {
            this.iconUi.removeFromParent();
            this.linkUi.removeFromParent();
        }
        // FIXME: Adjust Disclaimer content according to legal notices!
        final Bubble.DefaultPresenter bubblePresenter = new DefaultPresenter(
                new HTML("Lorem ipsum Lorem ipsum<br>Lorem ipsum Lorem ipsum Lorem ipsum Lorem ipsum<br>Lorem ipsum "
                        + "Lorem ipsum<br>Lorem ipsum Lorem ipsum Lorem ipsum<br>Lorem ipsum Lorem ipsum Lorem ipsum"),
                descriptionUi, descriptionUi, Direction.BOTTOM);
        bubblePresenter.registerTarget(disclaimerUi);
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
