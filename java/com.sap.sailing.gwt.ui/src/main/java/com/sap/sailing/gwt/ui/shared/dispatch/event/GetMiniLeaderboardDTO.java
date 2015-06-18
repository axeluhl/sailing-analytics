package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.ArrayList;
import java.util.Date;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public class GetMiniLeaderboardDTO implements DTO {
    private ArrayList<MiniLeaderboardItemDTO> items = new ArrayList<MiniLeaderboardItemDTO>();
    private String leaderboardDetailsURL;
    private String scoreCorrectionText;
    private Date lastScoreUpdate;
    private boolean live;

    public GetMiniLeaderboardDTO() {
    }

    public ArrayList<MiniLeaderboardItemDTO> getItems() {
        return items;
    }

    public void addItem(MiniLeaderboardItemDTO item) {
        items.add(item);
    }
    
    public void setLeaderboardDetailsURL(String leaderboardDetailsURL) {
        this.leaderboardDetailsURL = leaderboardDetailsURL;
    }

    public String getLeaderboardDetailsURL() {
        return leaderboardDetailsURL;
    }

    public String getScoreCorrectionText() {
        return scoreCorrectionText;
    }

    public void setScoreCorrectionText(String scoreCorrectionText) {
        this.scoreCorrectionText = scoreCorrectionText;
    }

    public Date getLastScoreUpdate() {
        return lastScoreUpdate;
    }

    public void setLastScoreUpdate(Date lastScoreUpdate) {
        this.lastScoreUpdate = lastScoreUpdate;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }
}
