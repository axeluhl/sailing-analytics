package com.sap.sailing.domain.yellowbrickadapter.impl;

import com.sap.sailing.domain.yellowbrickadapter.YellowBrickConfiguration;
import com.sap.sse.common.impl.NamedImpl;

public class YellowBrickConfigurationImpl extends NamedImpl implements YellowBrickConfiguration {
    private static final long serialVersionUID = -2858994298835323825L;
    private final String raceUrl;
    private final String username;
    private final String password;
    private final String creatorName;

    public YellowBrickConfigurationImpl(String name, String raceUrl, String username, String password, String creatorName) {
        super(name);
        this.raceUrl = raceUrl;
        this.username = username;
        this.password = password;
        this.creatorName = creatorName;
    }

    @Override
    public String getRaceUrl() {
        return raceUrl;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getCreatorName() {
        return creatorName;
    }
}
