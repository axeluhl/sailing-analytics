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

    private static final String ANNOUNCEMENT_ICON = "images/mobile/icon-bottle-white.svg";

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
            final String teaser = StringMessages.INSTANCE.anniversaryAnnouncementTeaser(target);
            final String raceDisplayName = anniversary.getRaceName() + " - " + anniversary.getLeaderBoardName();
            final String description = StringMessages.INSTANCE.anniversaryAnnouncementDescription(raceDisplayName);
            this.view.addAnnouncement(ANNOUNCEMENT_ICON, target, teaser, description, getRaceBoardLink(anniversary));
        } else {
            final int countDown = anniversary.getCountDown();
            if (anniversary.getType() == AnniversaryType.QUARTER) {
                final String teaser = StringMessages.INSTANCE.anniversaryMajorCountdownTeaser(countDown, target);
                final String description = StringMessages.INSTANCE.anniversaryMajorCountdownDescription(target);
                this.view.addCountdown(countDown, teaser, description);
            } else if (anniversary.getType() == AnniversaryType.REPEATED_DIGIT) {
                final String teaser = StringMessages.INSTANCE.anniversaryRepdigitCountdownTeaser(countDown, target);
                final String description = StringMessages.INSTANCE.anniversaryRepdigitCountdownDescription(target);
                this.view.addCountdown(anniversary.getCountDown(), teaser, description);
            }
        }
    }

    private String getRaceBoardLink(AnniversaryDTO anniversary) {
        return EntryPointWithSettingsLinkFactory.createRaceBoardLink(anniversary.getRemoteUrl(),
                new RaceboardContextDefinition(anniversary.getRegattaName(), anniversary.getRaceName(),
                        anniversary.getLeaderBoardName(), null, anniversary.getEventID(), null),
                new PerspectiveCompositeSettings<>(new RaceBoardPerspectiveOwnSettings(), Collections
                        .singletonMap(RaceMapLifecycle.ID, RaceMapSettings.getDefaultWithShowMapControls(true))));
    }

}
