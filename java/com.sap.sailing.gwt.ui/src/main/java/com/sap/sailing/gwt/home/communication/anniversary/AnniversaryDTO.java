package com.sap.sailing.gwt.home.communication.anniversary;

import java.util.UUID;

import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sse.gwt.dispatch.shared.commands.DTO;

/**
 * {@link DTO} object representing an anniversary entry (count-down or announcement) to be shown on the start page.
 */
public class AnniversaryDTO implements DTO {

    private int target;
    private Integer countDown;
    private AnniversaryType type;
    private UUID eventID;
    private String leaderBoardName;
    private String remoteUrl;
    private String regattaName;
    private String raceName;

    /**
     * Gwt Serialisation only constructor
     */
    protected AnniversaryDTO() {
    }

    AnniversaryDTO(int target, Integer countDown, AnniversaryType type) {
        this.target = target;
        this.countDown = countDown;
        this.type = type;
    }

    AnniversaryDTO(int target, AnniversaryType type, UUID eventID, String leaderBoardName, String remoteUrl,
            String raceName, String regattaName) {
        this(target, null, type);
        this.eventID = eventID;
        this.leaderBoardName = leaderBoardName;
        this.remoteUrl = remoteUrl;
        this.raceName = raceName;
        this.regattaName = regattaName;
    }

    public int getTarget() {
        return target;
    }

    public Integer getCountDown() {
        return countDown;
    }

    public AnniversaryType getType() {
        return type;
    }

    public UUID getEventID() {
        return eventID;
    }

    public String getLeaderBoardName() {
        return leaderBoardName;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public String getRegattaName() {
        return regattaName;
    }

    public String getRaceName() {
        return raceName;
    }

    public boolean isAnnouncement() {
        return countDown == null && eventID != null && leaderBoardName != null && regattaName != null
                && raceName != null;
    }

    @Override
    public String toString() {
        return "AnniversaryInformation [target=" + target + ", countDown=" + countDown + ", type=" + type + ", eventID="
                + eventID + ", leaderBoardName=" + leaderBoardName + ", remoteUrl=" + remoteUrl + ", regattaName="
                + regattaName + ", raceName=" + raceName + "]";
    }
}