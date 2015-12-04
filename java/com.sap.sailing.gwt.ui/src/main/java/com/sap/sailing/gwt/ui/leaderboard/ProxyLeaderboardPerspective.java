package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.autoplay.client.place.start.ProxyLeaderboardComponent;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.perspective.AbstractPerspectiveWithoutSettings;

/**
 * A proxy perspective containing a proxy component for the LeaderboardPanel
 * @author Frank
 *
 */
public class ProxyLeaderboardPerspective extends AbstractPerspectiveWithoutSettings {
    public ProxyLeaderboardPerspective(AbstractLeaderboardDTO leaderboard, LeaderboardSettings defaultLeaderboardSettings) {
        super();
        components.add(new ProxyLeaderboardComponent(defaultLeaderboardSettings, leaderboard, StringMessages.INSTANCE));
    }

    @Override
    public String getPerspectiveName() {
        return StringMessages.INSTANCE.leaderboard() + " Viewer";
    }
}
