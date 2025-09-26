package com.sap.sse.branding.shared;

import java.util.Optional;

public interface BrandingConfiguration {
    /**
     * The ID by which to find or set this configuration in {@code BrandingConfigurationService#setActiveBrandingConfigurationById(String)}
     */
    String getId();

    String getBrandTitle(Optional<String> locale);

    String getDefaultBrandingLogoURL(Optional<String> locale);

    String getGreyTransparentLogoURL(Optional<String> locale);
    
    String getSolutionsInSailingImageURL();
    
    String getSoutionsInSailingTrimmedImageURL();
    
    String getSailingRaceManagerAppTrimmedImageURL();
    
    String getSailInSightAppImageURL();
    
    String getSailingRaceManagerAppImageURL();
    
    String getSailingSimulatorImageURL();
    
    String getSailingSimulatorTrimmedImageURL();

    String getBuoyPingerAppImageURL();

    String getSailingAnalyticsImageURL();

    String getSailingAnalyticsReadMoreText(Optional<String> locale);

    String getSailingAnalyticsSailing(Optional<String> locale);
    
    String getFooterCopyright();
    
    String getFooterLegalLink();
    
    String getFooterPrivacyLink();
    
    String getFooterJobsLink();
    
    String getFooterSupportLink();
    
    String getFooterWhatsNewLink();
    
    String getSportsOn(Optional<String> locale);
    
    String getFollowSports(Optional<String> locale);
    
    String getFacebookLink();
    
    String getxLink();
    
    String getInstagramLink();
    
    String getWelcomeToSailingAnalytics(Optional<String> locale);
    
    String getWelcomeToSailingAnalyticsBody(Optional<String> locale);
    
    String getEventBaseURL(Optional<String> locale);
    
    String getSolutions1Headline(Optional<String> locale);
    
    String getSolutions2Headline(Optional<String> locale);
    
    String getSolutions3Headline(Optional<String> locale);
    
    String getSolutions4Headline(Optional<String> locale);
    
    String getSolutions5Headline(Optional<String> locale);
    
    String getSolutions6Headline(Optional<String> locale);
    
    String getSolutions1Title(Optional<String> locale);
    
    String getContentSolutions11(Optional<String> locale);
    
    String getContentSolutions12(Optional<String> locale);
    
    String getContentSolutions13(Optional<String> locale);
    
    String getContentSolutions14(Optional<String> locale);
    
    String getContentSolutions15(Optional<String> locale);

    String getContentSolutions17(Optional<String> locale);
    
    String getContentSolutions18(Optional<String> locale);
    
    String getContentSolutions19(Optional<String> locale);
    
    String getContentSolutions110(Optional<String> locale);
    
    String getContentSolutions111(Optional<String> locale);
    
    String getContentSolutions112(Optional<String> locale);
    
    String getContentSolutions113(Optional<String> locale);
    
    String getSolutions2Title(Optional<String> locale);
    
    String getContentSolutions21(Optional<String> locale);
    
    String getContentSolutions22(Optional<String> locale);
    
    String getSolutions3Title(Optional<String> locale);
    
    String getContentSolutions3(Optional<String> locale);
    
    String getSolutions3ReadMore(Optional<String> locale);
    
    String getSolutions4Title(Optional<String> locale);
    
    String getContentSolutions4(Optional<String> locale);
    
    String getSolutions4ReadMore(Optional<String> locale);
    
    String getSolutions5Title(Optional<String> locale);
    
    String getContentSolutions5(Optional<String> locale);
    
    String getSolutions5ReadMore(Optional<String> locale);
    
    String getSolutions6Title(Optional<String> locale);
    
    String getContentSolutions6(Optional<String> locale);
    
    String getSolutions6ReadMore(Optional<String> locale);
    
    String getSolutions3PlayStoreURL();
    
    String getSolutions4AppStoreURL();
    
    String getSolutions4PlayStoreURL();
    
    String getSolutions5PlayStoreURL();
    
    String getSolution2ReadMoreLink();
    
    String getSolutions3ReadMoreLink();
    
    String getSolutions4ReadMoreLink();
    
    String getSolutions5ReadMoreLink();
    
    String getSolutions6ReadMoreLink();
    
    String getMoreLoginInformationNotificationsURL();
    
    String getMoreLoginInformationSettingsURL();
    
    String getMoreLoginInformationSailorProfilesURL();
    
    String getMoreLoginInformationSimulatorURL();
    
}
