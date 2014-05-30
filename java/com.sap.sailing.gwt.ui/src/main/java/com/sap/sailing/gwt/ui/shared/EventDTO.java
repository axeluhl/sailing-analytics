package com.sap.sailing.gwt.ui.shared;

import java.net.URL;
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
    private List<URL> imageURLs = new ArrayList<>();
    private List<URL> videoURLs = new ArrayList<>();

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
    
    public void addImageURL(URL imageURL) {
        imageURLs.add(imageURL);
    }
    
    public void addVideoURL(URL videoURL) {
        videoURLs.add(videoURL);
    }

    public Iterable<URL> getImageURLs() {
        return imageURLs;
    }

    public Iterable<URL> getVideoURLs() {
        return videoURLs;
    }
}
