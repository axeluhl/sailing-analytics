package com.sap.sailing.gwt.home.shared.partials.anniversary;

import java.util.Collections;

import com.google.gwt.i18n.client.NumberFormat;
import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.domain.common.dto.EventType;
import com.sap.sailing.gwt.home.communication.anniversary.AnniversariesDTO;
import com.sap.sailing.gwt.home.communication.anniversary.AnniversaryDTO;
import com.sap.sailing.gwt.home.shared.partials.anniversary.AnniversariesView.AnniversaryAnnouncement;
import com.sap.sailing.gwt.home.shared.partials.anniversary.AnniversariesView.AnniversaryCountdown;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;
import com.sap.sailing.gwt.settings.client.EntryPointWithSettingsLinkFactory;
import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sailing.gwt.settings.client.raceboard.RaceboardContextDefinition;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapLifecycle;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

/**
 * Presenter class managing a {@link AnniversariesView} by adding entries based on a provided {@link AnniversariesDTO}.
 */
public class AnniversariesPresenter implements RefreshableWidget<AnniversariesDTO> {

    private static final String ANNOUNCEMENT_ICON = AnniversaryResources.INSTANCE.bottle().getSafeUri().asString();
    private static final StringMessages I18N = StringMessages.INSTANCE;
    private static final NumberFormat COUNT_FORMAT = NumberFormat.getFormat("#,###");

    private AnniversariesView view;

    /**
     * Creates a new {@link AnniversariesPresenter} instance managing the provided {@link AnniversariesView}.
     * 
     * @param view
     *            the {@link AnniversariesView} to manage
     */
    public AnniversariesPresenter(AnniversariesView view) {
        this.view = view;
    }

    @Override
    public void setData(AnniversariesDTO data) {
        this.view.clearAnniversaries();
        this.view.asWidget().setVisible(!data.isEmpty());
        data.getValues().forEach(this::addAnniversary);
    }

    private void addAnniversary(AnniversaryDTO anniversary) {
        final int target = anniversary.getTarget();
        if (anniversary.isAnnouncement()) {
            final AnniversaryAnnouncement item = view.addAnnouncement();
            item.setIconUrl(ANNOUNCEMENT_ICON);
            item.setCount(COUNT_FORMAT.format(target));
            item.setUnit(I18N.anniversaryUnitText(target));
            item.setTeaser(I18N.anniversaryAnnouncementTeaser(target));
            item.setDescription(I18N.anniversaryAnnouncementDescription(getRaceDisplayName(anniversary)));
            item.setLinkUrl(this.getRaceBoardLink(anniversary));
        } else {
            final int countdown = anniversary.getCountdown();
            final int currentRaceCount = anniversary.getCurrentRaceCount();
            final AnniversaryCountdown item = view.addCountdown();
            item.setCount(COUNT_FORMAT.format(currentRaceCount));
            item.setUnit(I18N.anniversaryUnitText(currentRaceCount));
            if (anniversary.getType() == AnniversaryType.QUARTER) {
                item.setTeaser(I18N.anniversaryMajorCountdownTeaser(countdown, target));
                item.setDescription(I18N.anniversaryMajorCountdownDescription(target));
            } else if (anniversary.getType() == AnniversaryType.REPEATED_DIGIT) {
                item.setTeaser(I18N.anniversaryRepdigitCountdownTeaser(countdown, target));
                item.setDescription(I18N.anniversaryRepdigitCountdownDescription(target));
            }
            // TODO: Separate the legal notice content for "Quarter" and "Rep digit" anniversaries
            item.setLegalNotice(new AnniversaryLegalNoticeBubbleContent(target));
        }
    }

    private String getRaceDisplayName(AnniversaryDTO anniversary) {
        final String eventName = anniversary.getEventName();
        final String leaderboardDisplayName = anniversary.getLeaderboardDisplayName() != null
                ? anniversary.getLeaderboardDisplayName() : anniversary.getLeaderboardName();
        final String leaderboardAndRaceName = leaderboardDisplayName + " - " + anniversary.getRaceName();
        if (anniversary.getEventType() == EventType.MULTI_REGATTA) {
            return eventName == null ? leaderboardAndRaceName : eventName + " - " + leaderboardAndRaceName;
        } else {
            return eventName == null ? leaderboardAndRaceName : eventName + " - " + anniversary.getRaceName();
        }
    }

    private String getRaceBoardLink(AnniversaryDTO anniversary) {
        return EntryPointWithSettingsLinkFactory.createRaceBoardLink(anniversary.getRemoteUrl(),
                new RaceboardContextDefinition(anniversary.getRegattaName(), anniversary.getRaceName(),
                        anniversary.getLeaderboardName(), null, anniversary.getEventID(), null),
                new PerspectiveCompositeSettings<>(new RaceBoardPerspectiveOwnSettings(), Collections
                        .singletonMap(RaceMapLifecycle.ID, RaceMapSettings.getDefaultWithShowMapControls(true))));
    }

}
