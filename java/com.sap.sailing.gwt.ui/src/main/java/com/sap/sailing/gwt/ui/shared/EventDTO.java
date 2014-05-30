package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.NamedDTO;

public class EventDTO extends NamedDTO implements IsSerializable {
    private static final long serialVersionUID = -7100030301376959817L;
    public List<RegattaDTO> regattas;
    public VenueDTO venue;
    public Date startDate;
    public Date endDate;
    public boolean isPublic;
    public UUID id;

    private List<LeaderboardGroupDTO> leaderboardGroups = new ArrayList<>();
    private List<String> imageURLs = new ArrayList<>();
    private List<String> videoURLs = new ArrayList<>();

    private Date currentServerTime;
    
    public EventDTO() {
        initCurrentServerTime();
    }

    public EventDTO(String name) {
        super(name);
        initCurrentServerTime();
        regattas = new ArrayList<RegattaDTO>();
    }

    private void initCurrentServerTime() {
        currentServerTime = new Date();
    }

    public Date getCurrentServerTime() {
        return currentServerTime;
    }
    
    public void addLeaderboardGroup(LeaderboardGroupDTO leaderboardGroup) {
        leaderboardGroups.add(leaderboardGroup);
    }
    
    public Iterable<LeaderboardGroupDTO> getLeaderboardGroups() {
        return leaderboardGroups;
    }
    
    public void addImageURL(String imageURL) {
        imageURLs.add(imageURL);
    }
    
    public void addVideoURL(String videoURL) {
        videoURLs.add(videoURL);
    }

    public Iterable<String> getImageURLs() {
        return imageURLs;
    }

    public Iterable<String> getVideoURLs() {
        return videoURLs;
    }
}
