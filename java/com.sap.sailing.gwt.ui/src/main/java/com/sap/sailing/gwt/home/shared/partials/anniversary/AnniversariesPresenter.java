package com.sap.sailing.gwt.home.shared.partials.anniversary;

import java.util.Collections;

import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.gwt.home.communication.anniversary.AnniversariesDTO;
import com.sap.sailing.gwt.home.communication.anniversary.AnniversaryDTO;
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

    private static final String COUNTDOWN_MAJOR_ICON = "images/mobile/icon_trackedCount.svg";
    private static final String COUNTDOWN_REPDIGIT_ICON = "images/mobile/icon_trackedCount.svg";
    private static final String ANNOUNCEMENT_ICON = "images/mobile/icon_trackedCount.svg";

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
        final String iconUrl, teaser, description;
        if (anniversary.isAnnouncement()) {
            iconUrl = ANNOUNCEMENT_ICON;
            teaser = StringMessages.INSTANCE.anniversaryAnnouncementTeaser(target);
            description = this.getAnnouncementDescription(anniversary);
        } else if (anniversary.getType() == AnniversaryType.QUARTER) {
            iconUrl = COUNTDOWN_MAJOR_ICON;
            teaser = StringMessages.INSTANCE.anniversaryMajorCountdownTeaser(anniversary.getCountDown(), target);
            description = StringMessages.INSTANCE.anniversaryMajorCountdownDescription(target);
        } else if (anniversary.getType() == AnniversaryType.REPEATED_DIGIT) {
            iconUrl = COUNTDOWN_REPDIGIT_ICON;
            teaser = StringMessages.INSTANCE.anniversaryRepdigitCountdownTeaser(anniversary.getCountDown(), target);
            description = StringMessages.INSTANCE.anniversaryRepdigitCountdownDescription(target);
        } else {
            return;
        }
        this.view.addAnniversary(iconUrl, teaser, description);
    }

    private String getAnnouncementDescription(AnniversaryDTO anniversary) {
        final String raceDisplayName = anniversary.getRaceName() + " - " + anniversary.getLeaderBoardName();
        final String raceBoardUrl = EntryPointWithSettingsLinkFactory.createRaceBoardLink(anniversary.getRemoteUrl(),
                new RaceboardContextDefinition(anniversary.getRegattaName(), anniversary.getRaceName(),
                        anniversary.getLeaderBoardName(), null, anniversary.getEventID(), null),
                new PerspectiveCompositeSettings<>(new RaceBoardPerspectiveOwnSettings(), Collections
                        .singletonMap(RaceMapLifecycle.ID, RaceMapSettings.getDefaultWithShowMapControls(true))));
        return StringMessages.INSTANCE.anniversaryAnnouncementDescription(raceDisplayName, raceBoardUrl);
    }

}
