package com.sap.sailing.gwt.home.shared.partials.anniversary;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.Messages;

@DefaultLocale("en")
public interface AnniversaryLegalMessages extends Messages{
    public static final AnniversaryLegalMessages INSTANCE = GWT.create(AnniversaryLegalMessages.class);
    
    String anniversaryLegalNoticeHeadline(int anniversary);
    String anniversaryLegalNoticeSectionOverallCompetitionRulesAndPrizesHeadline();
    String anniversaryLegalNoticeSectionOrganizationAndParticipationParagraphTitle();
    String anniversaryLegalNoticeSectionOrganizationAndParticipationParagraph1();
    String anniversaryLegalNoticeSectionOrganizationAndParticipationParagraph2();
    String anniversaryLegalNoticeSectionOrganizationAndParticipationParagraph3();
    String anniversaryLegalNoticeSectionRequirementsHeadline();
    String anniversaryLegalNoticeSectionRequirementsListItem1();
    String anniversaryLegalNoticeSectionRequirementsListItem2();
    String anniversaryLegalNoticeSectionRequirementsListItem3();
    String anniversaryLegalNoticeSectionContestParagraphTitle();
    String anniversaryLegalNoticeSectionContestParagraph(int anniversary);
    String anniversaryLegalNoticeSectionCommunicationParagraphTitle();
    String anniversaryLegalNoticeSectionCommunicationParagraph();
    String anniversaryLegalNoticeSectionCharitablyPurposeParagraphTitle();
    String anniversaryLegalNoticeSectionCharitablyPurposeParagraph();
    String anniversaryLegalNoticeSectionPrizesParagraphTitle();
    String anniversaryLegalNoticeSectionPrizesParagraph(int anniversary);
    String anniversaryLegalNoticeSectionPersonalDataAndRightsToImagesParagraphTitle();
    String anniversaryLegalNoticeSectionPersonalDataAndRightsToImagesParagraph1();
    String anniversaryLegalNoticeSectionPersonalDataAndRightsToImagesParagraph2();
    String anniversaryLegalNoticeSectionDisclaimerSectionParagraphTitle();
    String anniversaryLegalNoticeSectionDisclaimerSectionParagraph();
    String anniversaryLegalNoticeSectionOtherParagraphTitle();
    String anniversaryLegalNoticeSectionOtherParagraph1();
    String anniversaryLegalNoticeSectionOtherParagraph2();

}
