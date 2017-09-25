package com.sap.sailing.gwt.home.shared.partials.anniversary;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

class AnniversaryLegalNoticeBubbleContent extends Widget {

    private static AnniversaryLegalNoticeUiBinder uiBinder = GWT.create(AnniversaryLegalNoticeUiBinder.class);

    interface AnniversaryLegalNoticeUiBinder extends UiBinder<Element, AnniversaryLegalNoticeBubbleContent> {
    }

    @UiField
    AnniversaryLegalMessages i18n;
    @UiField
    Element legalNoticeHeadline, legalNoticeSectionContest, legalNoticeSectionPrizes;

    AnniversaryLegalNoticeBubbleContent(int anniversary) {
        setElement(uiBinder.createAndBindUi(this));
        legalNoticeHeadline.setInnerText(i18n.anniversaryLegalNoticeHeadline(anniversary));
        legalNoticeSectionContest.setInnerText(i18n.anniversaryLegalNoticeSectionContestParagraph(anniversary));
        legalNoticeSectionPrizes.setInnerText(i18n.anniversaryLegalNoticeSectionPrizesParagraph(anniversary));
    }

}
