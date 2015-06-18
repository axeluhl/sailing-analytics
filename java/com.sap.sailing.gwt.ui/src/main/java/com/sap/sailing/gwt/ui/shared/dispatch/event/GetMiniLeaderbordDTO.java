package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.ArrayList;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public class GetMiniLeaderbordDTO implements DTO {
    private ArrayList<MiniLeaderboardItemDTO> items = new ArrayList<MiniLeaderboardItemDTO>();
    private String leaderboardDetailsURL;

    @SuppressWarnings("unused")
    private GetMiniLeaderbordDTO() {
    }

    public GetMiniLeaderbordDTO(String leaderboardDetailsURL) {
        this.leaderboardDetailsURL = leaderboardDetailsURL;
    }

    public ArrayList<MiniLeaderboardItemDTO> getItems() {
        return items;
    }

    public void addItem(MiniLeaderboardItemDTO item) {
        items.add(item);
    }

    public String getLeaderboardDetailsURL() {
        return leaderboardDetailsURL;
    }
}
