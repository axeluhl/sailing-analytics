package com.sap.sailing.gwt.home.shared.partials.anniversary;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.partials.anniversary.AnniversariesView.AnniversaryAnnouncement;
import com.sap.sailing.gwt.home.shared.partials.bubble.Bubble;
import com.sap.sailing.gwt.home.shared.partials.bubble.Bubble.Direction;

public class AbstractAnniversaryItem extends Widget implements AnniversaryAnnouncement {

    @UiField
    public Style style;
    @UiField
    public Element iconUi, countUi, unitUi, teaserUi, descriptionUi, legalNoticeUi;
    @UiField
    public AnchorElement linkUi;

    protected void initLayout(boolean isAnnouncement) {
        if (isAnnouncement) {
            this.addStyleName(style.announcement());
            this.legalNoticeUi.removeFromParent();
        } else {
            this.addStyleName(style.countdown());
            this.iconUi.removeFromParent();
            this.linkUi.removeFromParent();
        }
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
    public void setLegalNotice(IsWidget content) {
        final LegalNoticePresenter presenter = new LegalNoticePresenter(content);
        presenter.registerTarget(legalNoticeUi);
    }

    @Override
    public void setIconUrl(String iconUrl) {
        this.iconUi.getStyle().setBackgroundImage("url('" + iconUrl + "')");
    }

    @Override
    public void setLinkUrl(String linkUrl) {
        this.linkUi.setHref(linkUrl);
    }

    private class LegalNoticePresenter implements EventListener {

        private final Bubble popup;

        private LegalNoticePresenter(IsWidget content) {
            this.popup = new Bubble(content);
        }

        private void registerTarget(Element target) {
            popup.addAutoHidePartner(target);
            Event.sinkEvents(target, Event.ONCLICK | Event.ONMOUSEOVER);
            Event.setEventListener(target, this);
        }

        @Override
        public void onBrowserEvent(Event event) {
            final int typeInt = event.getTypeInt();
            if ((typeInt == Event.ONCLICK || typeInt == Event.ONMOUSEOVER) && !popup.isAttached()) {
                popup.show(descriptionUi, getElement(), Direction.BOTTOM);
                event.preventDefault();
                event.stopPropagation();
            }
        }

    }

    public interface Style extends CssResource {

        String countdown();

        String announcement();
    }

}
