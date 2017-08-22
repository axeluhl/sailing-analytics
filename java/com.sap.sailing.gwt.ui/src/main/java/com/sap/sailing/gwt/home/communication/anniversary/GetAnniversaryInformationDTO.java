package com.sap.sailing.gwt.home.communication.anniversary;

import java.util.List;
import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sse.gwt.dispatch.shared.commands.DTO;
import com.sap.sse.gwt.dispatch.shared.commands.Result;

/**
 * This class is used to transmit the current anniversary state to the frontend. It is used to highlight anniversaries
 * that will happen soon, or that did happen a few days ago prominently
 */
public class GetAnniversaryInformationDTO implements DTO, Result {
    static class AnniversaryInformation implements IsSerializable {
        private int racenumber;
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
        public AnniversaryInformation() {
        }

        public AnniversaryInformation(int racenumber, Integer countDown, AnniversaryType type, UUID eventID,
                String leaderBoardName, String remoteUrl, String raceName, String regattaName) {
            super();
            this.racenumber = racenumber;
            this.countDown = countDown;
            this.type = type;
            this.eventID = eventID;
            this.leaderBoardName = leaderBoardName;
            this.remoteUrl = remoteUrl;
            this.raceName = raceName;
            this.regattaName = regattaName;
        }

        public int getRacenumber() {
            return racenumber;
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

        @Override
        public String toString() {
            return "AnniversaryInformation [racenumber=" + racenumber + ", countDown=" + countDown + ", type=" + type
                    + ", eventID=" + eventID + ", leaderBoardName=" + leaderBoardName + ", remoteUrl=" + remoteUrl
                    + ", regattaName=" + regattaName + ", raceName=" + raceName + "]";
        }
    }

    private List<AnniversaryInformation> anniversaryInformation;

    /**
     * Gwt Serialisation only constructor
     */
    public GetAnniversaryInformationDTO() {
    }

    public GetAnniversaryInformationDTO(List<AnniversaryInformation> anniversaryInformation) {
        this.anniversaryInformation = anniversaryInformation;
    }

    public List<AnniversaryInformation> getAnniversaryInformation() {
        return anniversaryInformation;
    }

    @Override
    public String toString() {
        return "GetAnniversaryInformationDTO [anniversaryInformation=" + anniversaryInformation + "]";
    }

}
